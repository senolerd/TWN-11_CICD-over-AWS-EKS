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
    env.IMAGE_NAME = "${REGISTRY}/${REPO_NAME}:${APP_VER}-${BUILD_NUMBER}"
    
    echo "Building image for version $APP_VER"
    sh """
        podman build -t $IMAGE_NAME .
        podman image prune -f
    """

    withCredentials([usernamePassword(credentialsId: env.REPO_CRED_ID, passwordVariable: 'PW', usernameVariable: 'USR')]) {
        echo "Logging in to ${env.REGISTRY}"
        sh 'podman login -u $USR -p $PW $REGISTRY'
    }

    echo "Pushing image to $REGISTRY"
    sh "podman push $IMAGE_NAME"
    sh 'podman logout $REGISTRY'
}


void buildImageToECR() {

    withCredentials([usernamePassword(credentialsId: env.AWS_JENKINS_ACC_KEY_ID, passwordVariable: 'AWS_SECRET_ACCESS_KEY', usernameVariable: 'AWS_ACCESS_KEY_ID')]) {
        
        env.ECR_TOKEN = sh(script:'podman run --rm --name aws -e AWS_REGION -e AWS_ACCESS_KEY_ID -e AWS_SECRET_ACCESS_KEY  public.ecr.aws/aws-cli/aws-cli:2.36.6 ecr get-login-password', returnStdout: true).trim()
        env.AWS_ACCOUNT = sh(script:'podman run --rm --name aws -e AWS_REGION -e AWS_ACCESS_KEY_ID -e AWS_SECRET_ACCESS_KEY  public.ecr.aws/aws-cli/aws-cli:2.36.6 sts get-caller-identity --query "Account" --output text', returnStdout: true).trim()
        env.ECR_REGISTRY = "${env.AWS_ACCOUNT}.dkr.ecr.${env.AWS_REGION}.amazonaws.com"

        echo "Logging in to ECR"
        sh '''
            set +x
            podman login -u AWS -p $ECR_TOKEN ${AWS_ACCOUNT}.dkr.ecr.${AWS_REGION}.amazonaws.com 
            set -x
            echo "Checking login"
            podman login ${AWS_ACCOUNT}.dkr.ecr.${AWS_REGION}.amazonaws.com 
            '''
        
        env.IMAGE_NAME = "${AWS_ACCOUNT}.dkr.ecr.${AWS_REGION}.amazonaws.com/${REPO_NAME}:${APP_VER}-${BUILD_NUMBER}"
        
        
        echo "BUILDING"
        sh """
            podman build -t $IMAGE_NAME .
            podman image prune -f
        """

        echo "PUSHING"
        echo "Pushing image to ${env.ECR_REGISTRY}"
        sh "podman push $IMAGE_NAME"
        sh 'podman logout $ECR_REGISTRY'



    }

}





void deployToKVM() {
    sh "sed -i '/appVersion/c\\appVersion: $APP_VER-$BUILD_NUMBER' helm-chart/Chart.yaml"

    withCredentials([file(credentialsId: env.KUBECONFIG_SECRET_FILE_ID, variable: 'KUBECONFIG')]) {
        echo "Update app with Helm chart"
        sh "helm upgrade --install java-maven helm-chart -n java-maven --create-namespace --set image=$IMAGE_NAME"
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


