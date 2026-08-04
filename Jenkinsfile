def utils

pipeline {
    agent any
    environment {
        JRE = 'cgr.dev/chainguard/jre:latest'
        GIT_RSA_ID = "git_rsa_priv" // the RSA private key added its ".pub" to GitHub

        //// AWS related variables
        // AWS_JENKINS_ACC_KEY_ID is a Username/Password type of Jenkins credential for CLI access works.
        // While creating the credential chose "Treat username as secret" for masking the KEY_ID at loggging.
        // After creating access key for the IAM user, create the Jenkins credential this way; 
        // Username=AWS_ACCESS_KEY_ID, Password=AWS_SECRET_ACCESS_KEY
        // The pipeline will figure out the AWS account id with AWS STS, then pulls kubeconfig file for Helm chart, 
        // create ECR registry and repo urls
        // ** Notice ***: 
        //      The user, AWS IAM access key going to be used, has to have some permission. Policy sample is given on README.md. 
        //      The user also should be added to "EKS Access Entries" as the type of "Standart" if a deployment user is
        //      other than who created the cluster.

        AWS_JENKINS_ACC_KEY_ID = "aws-jenkins-access-key"
        EKS_CLUSTER_NAME = "" // cluster should be created and defined here before the pipeline is run
        REPO_NAME = 'java-maven-app'
        APP_NAMESPACE = "java-maven"
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

        stage("Version bump") {
            steps {
                script{
                    utils.versionUpdate()
                }
            }
        }

        stage("Git update for version bump") {
            steps {
                script {
                    utils.gitPushNewVersion()
                }
            }
        }

    }
}
