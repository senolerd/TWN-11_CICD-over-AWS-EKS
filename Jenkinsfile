def utils

pipeline {
    agent any
    environment {
        JRE= 'cgr.dev/chainguard/jre:latest'
        REPO = 'docker.io/alkol/java-maven-app'
    }
    tools {
        maven 'Maven'
    }
    stages {

        stage('init') {
            steps {
                script {
                    utils = load 'utils.groovy'
                    env.APP_VER = echo utils.getAppVersion()

                }
            }
        }

        stage("Compile Aapplication Code"){
            steps{
                script{
                    utils.buildCode()
                    sh 'env'
                }
            }
        }

        stage("Container build") {
            steps{
                script{
                    echo "Creating OCI Containerfile"
                    utils.createContainerfile()
                    utils.buildImage()
                }
            }
        }




    }
}

// BUILD_ID