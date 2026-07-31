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
    sh """
cat < EOF > Containerfile
FROM cgr.dev/chainguard/maven:latest
WORKDIR /app
COPY target/*.jar .
CMD -jar *.jar
EOF

"""
}

return this