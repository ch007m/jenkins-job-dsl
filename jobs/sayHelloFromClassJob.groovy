freeStyleJob('hello-from-class-dsl-helloFreeStyleJob') {
    displayName('hello-from-class-dsl-helloFreeStyleJob')
    description('Job DSL which say hello using a Grrovy class.')

    parameters {
        stringParam('user', '...', 'Your user name')
    }

    scm {
        git {
            remote {
                url 'https://gitlab.cee.redhat.com/snowdrop/jenkins-jobs-dsl.git'
                branch 'master'
            }
        }
    }

    steps {
        groovyCommand(readFileFromWorkspace('jobs/sayHelloFromClass.groovy')) {
            groovyInstallation('groovy-3.0.7')
            prop('user', '$user')
        }
    }
}