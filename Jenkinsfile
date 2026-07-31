def utils

pipeline {
    agent any
    environment {
        JRE= 'cgr.dev/chainguard/jre:latest'
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
                }
            }
        }




    }
}

// BUILD_ID