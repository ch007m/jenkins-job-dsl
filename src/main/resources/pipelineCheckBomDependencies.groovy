@Library('snowdrop-lib@0.1') _

// Global variables can be only set using the @Field pattern
import groovy.transform.Field
@Field def variable

pipeline {
    agent any

    environment {
        PIPELINE_LOG_LEVEL = 'INFO'
    }

    options {
        ansiColor('xterm')
    }

    stages {
        stage('Checkout') {
            steps {
                gitCheckout(repo: "https://github.com/snowdrop/spring-boot-bom.git", branch: "2.3.6.Alpha2")
            }
        }

        stage('Clean up pom.xml file and backup it') {
            steps {
                renamePomFile(ext: '.bk')
                removeDependencyManagementTag()
            }
        }

        stage('Check dependencies tree') {
            steps {
                mavenBuild(dependencyTree: true, compile: false)
            }
        }

        stage('Check if we have git diff') {
            steps {
                restorePomFile(ext: '.bk', remove: true)

                script {
                    def status = gitStatus()
                    // TODO: Define as parameter the message as the message returned is different between the git tool installed !!
                    // TODO: For older version (e.g: 1.8.3.1), it is "nothing to commit, working directory clean"
                    // TODO: For recent version (e.g: 2.7.0),  it is "nothing to commit, working tree clean"
                    if (status.contains("nothing to commit, working tree clean")) {
                        log(level: 'WARN', text: "No Git difference :-)")
                    } else {
                        log(level: 'ERROR', text: "Git difference observed '${status}'. Consult the report.txt file under the workspace")
                        sh "exit 1"
                    }
                }
            }
        }
    }
}