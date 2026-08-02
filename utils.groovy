String getAppVersion() {
    echo 'Getting current version from pom.xml'
    return sh(script:'mvn help:evaluate -Dexpression=project.version -q -DforceStdout', returnStdout: true).trim().split('-')[0]
}

void buildCode() {
    echo 'Building application code to JAR'
    sh 'mvn clean package'
}

void createContainerfile() {
    env.JAR_FILE = sh(script: 'ls target/*.jar', returnStdout: true).trim().split('/')[1]

    sh """
    cat << EOF > Containerfile
FROM $JRE
LABEL org.opencontainers.image.commit="$GIT_COMMIT"
EXPOSE 8080
WORKDIR /app
COPY target/$JAR_FILE .
CMD ["-jar", "$JAR_FILE"]
EOF
    """
}

void buildImageToDocker() {
    // Creates image, logins to registry, pushes the image, then logouts from registry.
    // env.IMG_REGISTRY = env.REPO.split('/')[0]

    echo "Building image for version $APP_VER"
    sh """
        podman build -t $REGISTRY/$IMAGE_NAME:$APP_VER-$BUILD_NUMBER .
        podman image prune -f
    """

    withCredentials([usernamePassword(credentialsId: env.REPO_CRED_ID, passwordVariable: 'PW', usernameVariable: 'USR')]) {
        echo "Logging in to ${env.REGISTRY}"
        sh 'podman login -u $USR -p $PW $REGISTRY'
    }

    echo "Pushing image to $REGISTRY"
    sh "podman push $REGISTRY/$IMAGE_NAME:$APP_VER-$BUILD_NUMBER"
    sh 'podman logout $REGISTRY'
}

void deployToKVM() {
    sh "sed -i '/appVersion/c\\appVersion: $APP_VER-$BUILD_NUMBER' helm-chart/Chart.yaml"

    withCredentials([file(credentialsId: env.KUBECONFIG_SECRET_FILE_ID, variable: 'KUBECONFIG')]) {
        // Updating application with Helm Chart to local K8s cluster runs on KVM
        echo "Update app with Helm chart"
        sh "helm upgrade --install java-maven helm-chart -n java-maven --create-namespace --set image=$REGISTRY/$IMAGE_NAME:$APP_VER-$BUILD_NUMBER"
    }
}





void versionUpdate() {
    // If evertyhing went well so far it is time to new version.
    echo 'Updating application version'
    sh '''mvn build-helper:parse-version versions:set -DnewVersion='${parsedVersion.majorVersion}.${parsedVersion.nextMinorVersion}.0-SNAPSHOT' -q versions:commit'''
}

void gitPushNewVersion() {
    sh '''
        git add pom.xml
        git commit -m "[ci] Version bump"
    '''

    gitRemoteUserAndProject = sh(script:'git config get remote.origin.url', returnStdout: true).trim().split('/')[-2..-1]
    env.GIT_USER = gitRemoteUserAndProject[0]
    env.GIT_PROJECT = gitRemoteUserAndProject[1]

    sshagent([GIT_RSA_ID]) {
        sh 'git push git@github.com:$GIT_USER/$GIT_PROJECT HEAD:$GIT_BRANCH'
    }
}

return this
