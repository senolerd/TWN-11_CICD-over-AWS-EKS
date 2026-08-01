def utils

pipeline {
    agent any
    environment {
        JRE = 'cgr.dev/chainguard/jre:latest'
        REPO = 'docker.io/alkol/java-maven-app'
        REPO_CRED_ID = 'dockerhub-pat-rw'
        KUBECONFIG_SECRET_FILE_ID = 'k8s-config-kvm'
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

        // stage('Compile Aapplication Code') {
        //     steps {
        //         script {
        //             utils.buildCode()
        //         }
        //     }
        // }

        // stage('Container build') {
        //     steps{
        //         script{
        //             echo "Creating OCI Containerfile for $APP_VER"
        //             utils.createContainerfile()
        //             utils.buildImage()
        //         }
        //     }
        // }

        // stage("Deploy to KVM") {
        //     steps {
        //         script{
        //             utils.deployToKVM()
        //         }
        //     }
        // }

        // stage("Version bump") {
        //     steps {
        //         script{
        //             utils.versionUpdate()
        //         }
        //     }
        // }

        stage("Git update for version bump") {
            steps {
                script {
                    utils.gitPushNewVersion()
                }
            }
        }

    }
}
