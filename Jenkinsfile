def utils

pipeline {
    agent any
    environment {
        JRE = 'cgr.dev/chainguard/jre:latest'
        GIT_RSA_ID = "git_rsa_priv" // the RSA private key added its ".pub" to GitHub

        REPO_NAME = 'java-maven-app' // don't forget to create this repo at ECR before starting to push
        REGISTRY = 'docker.io/alkol'
        REPO_CRED_ID = 'dockerhub-pat-rw'
        KUBECONFIG_SECRET_FILE_ID = 'k8s-config-kvm'

        //// AWS related variables
        // AWS_JENKINS_ACC_KEY_ID is a Username/Password type of Jenkins credential for CLI access works
        // While creating the credential chose "Treat username as secret" for masking the KEY_ID at loggging.
        // After creating access key for the IAM user, create the Jenkins credential this way; 
        // Username=AWS_ACCESS_KEY_ID, Password=AWS_SECRET_ACCESS_KEY
        // The pipeline will figure out the AWS account id with AWS STS, then will create ECR registry and repo urls

        AWS_JENKINS_ACC_KEY_ID = "aws-jenkins-access-key"
        EKS_CLUSTER_NAME = "mykube"
        AWS_REGION = "us-east-1"
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

        stage('Compile Aapplication Code') {
            steps {
                script {
                    utils.buildCode()
                }
            }
        }

        stage('Container build') {
            steps{
                script{
                    echo "Creating OCI Containerfile for $APP_VER"
                    utils.createContainerfile()
                    utils.buildImageToECR()
                }
            }
        }

        stage("Deploy to EKS") {
            steps {
                script{
                    utils.deployToEKS()
                }
            }
        }

        // stage("Version bump") {
        //     steps {
        //         script{
        //             utils.versionUpdate()
        //         }
        //     }
        // }

        // stage("Git update for version bump") {
        //     steps {
        //         script {
        //             utils.gitPushNewVersion()
        //         }
        //     }
        // }

    }
}
