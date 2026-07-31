def hello(){
    echo "hello"
}

def getAppVersion(){
    echo 'Getting current version from pom.xml'
    return sh(script:'mvn help:evaluate -Dexpression=project.version -q -DforceStdout', returnStdout: true).trim().split('-')[0]
}

def buildCode(){
    echo 'Building application code to JAR'
    sh 'mvn package'
}

def createContainerfile(){
    def JAR_FILE = sh(script: 'ls target/*.jar', returnStdout: true).trim()

    sh """
cat << EOF > Containerfile
FROM $JRE
LABEL org.opencontainers.image.commit="$GIT_COMMIT"
WORKDIR /app
COPY $JAR_FILE .
CMD -jar $JAR_FILE
EOF
"""
}

return this