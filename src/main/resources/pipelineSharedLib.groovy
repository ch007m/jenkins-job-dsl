@org.jenkinsci.plugins.workflow.libs.Library('mytools')

import org.mytools.Tools
Tools t = new Tools(this)

import org.mytools.Messaging
Messaging m = new Messaging();

pipeline {
    agent any
    stages {
        stage('test') {
            steps {
                script {
                    t.myEcho('Do a test')
                    m.sayHelloTo('Charles')
                }
            }
        }
    }
}