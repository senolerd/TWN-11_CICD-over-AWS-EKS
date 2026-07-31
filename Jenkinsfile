def utils

pipeline {
    agent any
    environment {
        JRE = 'cgr.dev/chainguard/jre:latest'
        REPO = 'docker.io/alkol/java-maven-app'
        REPO_CRED_ID = 'dockerhub-pat-rw'
    }
    tools { 
        maven 'Maven'
    }
    stages {

        stage('init') {
            steps {
                script {
                    utils = load 'utils.groovy'
                    env.APP_VER = utils.getAppVersion()
                }
            }
        }

        stage('Compile Aapplication Code'){
            steps{
                script{
                    utils.buildCode()
                    sh 'env'
                }
            }
        }

        stage('Container build') {
            steps{
                script{
                    echo "Creating OCI Containerfile for $APP_VER"
                    utils.createContainerfile()
                    utils.buildImage()
                }
            }
        }

        stage("Version bump") {
            steps {
                script{
                    versionUpdate()
                }
            }
        }


    }
}
