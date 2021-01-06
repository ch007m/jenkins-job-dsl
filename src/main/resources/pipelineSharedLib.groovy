@org.jenkinsci.plugins.workflow.libs.Library('mytools')

import org.mytools.Tools
Tools t = new Tools(this)

pipeline {
    agent any
    stages {
        stage('test') {
            steps {
                script {
                    t.myEcho('Do a test')
                }
            }
        }
    }
}