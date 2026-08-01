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

void buildImage() {
    // Creates image, logins to registry, pushes the image, then logouts from registry.
    env.IMG_REGISTRY = env.REPO.split('/')[0]

    echo "Building image for version $APP_VER"
    sh """
        podman build -t $REPO:$APP_VER-$BUILD_NUMBER .
        podman image prune -f
    """

    withCredentials([usernamePassword(credentialsId: env.REPO_CRED_ID, passwordVariable: 'PW', usernameVariable: 'USER')]) {
        echo "Logging in to $env.IMG_REGISTRY"
        sh 'podman login -u $USER -p $PW $IMG_REGISTRY'
    }

    echo "Pushing image to $REPO"
    sh "podman push $REPO:$APP_VER-$BUILD_NUMBER"
    sh 'podman logout $IMG_REGISTRY'
}

void deployToKVM() {
    sh "sed -i '/appVersion/c\\appVersion: $APP_VER-$BUILD_NUMBER' helm-chart/Chart.yaml"

    withCredentials([file(credentialsId: env.KUBECONFIG_SECRET_FILE_ID, variable: 'KUBECONFIG')]) {
        // Updating application with Helm Chart to local K8s cluster runs on KVM
        echo "Trying to update with HELM"
        sh "helm upgrade --install java-maven helm-chart -n java-maven --create-namespace --set image=$REPO:$APP_VER-$BUILD_NUMBER"
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

    withCredentials([usernamePassword(credentialsId: 'githubpat', passwordVariable: 'PAT', usernameVariable: 'USER')]) {

        env.remoteOriginUrl = sh(script:'git config get remote.origin.url', returnStdout: true).trim().replace('//','//$USER:$PAT@' )
        echo env.remoteOriginUrl
        



    }


}

return this
