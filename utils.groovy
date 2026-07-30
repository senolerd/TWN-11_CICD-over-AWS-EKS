def hello(){
    echo "hello"
}

def getAppVersion(){
    echo "Getting current version from pom.xml"
    return sh(script:"mvn help:evaluate -Dexpression=project.version -q -DforceStdout", returnStdout: true).trim().split("-")[0]
}

return this