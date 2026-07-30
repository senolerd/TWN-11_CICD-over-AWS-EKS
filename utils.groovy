def hello(){
    echo "hello"
}

def getAppVersion(){
    return sh(script:'mvn help:evaluate -Dexpression=project.version -q -DforceStdout', returnStdout: true)
}

return this