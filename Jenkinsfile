def utils

pipeline {
    agent any
    tools {
        maven 'Maven'
    }
    stages {
        stage('init') {
            steps {
                script {
                    utils = load 'utils.groovy'
                }
            }
        }

        stage("test") {
            steps{
                script{
                    utils.hello()
                    echo utils.getAppVersion()
                }
            }
        }

    }
}

// Reading current version
// mvn help:evaluate -Dexpression=project.version -q -DforceStdout

// Updating to next version
// mvn build-helper:parse-version versions:set -DnewVersion='${parsedVersion.majorVersion}.${parsedVersion.nextMinorVersion}.0-SNAPSHOT' versions:commit