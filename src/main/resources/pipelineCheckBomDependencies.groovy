@Library('snowdrop-lib@0.2') _

import org.jenkinsci.plugins.workflow.libs.Library
def AGENT_LABEL = null

node('master') {
    stage('Determine platform and set agent'){
        if (fileExists('/var/run/secrets/kubernetes.io')) {
            AGENT_LABEL = "maven"
        } else {
            AGENT_LABEL = "any"
        }
    }
}

pipeline {
    agent { label "${AGENT_LABEL}" }

    environment {
        PIPELINE_LOG_LEVEL = 'INFO'
    }

    options {
        ansiColor('xterm')
    }

    parameters {
        string(name: 'baseDir', defaultValue: 'src', description: 'Directory to extract the git cloned project')
    }

    stages {
        stage('Parameters') {
            steps {
                echo params.baseDir
                echo "--------"
                echo "BaseDir parameter: ${params.baseDir}"
            }
        }

        stage('Checkout') {
            steps {
                gitCheckout(
                        repo: "https://github.com/snowdrop/spring-boot-bom.git",
                        branch: "2.3.6.Alpha2",
                        baseDir: params.baseDir)
            }
        }

        stage('Clean up pom.xml file and backup it') {
            steps {
                renamePomFile(ext: '.bk', baseDir: params.baseDir)
                removeDependencyManagementTag(baseDir: params.baseDir)
            }
        }

        stage('Check dependencies tree') {
            steps {
                mavenBuild(dependencyTree: true, compile: false, baseDir: params.baseDir)
            }
        }

        stage('Check if we have git diff') {
            steps {
                restorePomFile(ext: '.bk', remove: true, baseDir: params.baseDir)

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

    post {
        always {
            archiveArtifacts artifacts: 'report.txt', fingerprint: true
        }
    }
}