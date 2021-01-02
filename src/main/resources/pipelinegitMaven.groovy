pipeline {
    agent any
    stages {
        stage('Clone sources') {
            steps {
                checkout scm: [$class: 'GitSCM',
                               branches: [[name: 'refs/tags/2.3.4.Final']],
                               userRemoteConfigs: [[url: 'https://github.com/snowdrop/spring-boot-bom']]]
            }
        }
        stage('Build') {
            steps {
                echo 'Building spring Boot BOM'
                sh 'mvn -B dependency:tree'
            }
        }
        stage('Example Test') {
            steps {
                echo 'Hello, JDK'
                sh 'java -version'
            }
        }
    }
}