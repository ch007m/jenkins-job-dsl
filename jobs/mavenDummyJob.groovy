// See: https://www.happycoders.eu/devops/jenkins-tutorial-create-jobs-with-job-dsl/
mavenJob('mvn-dummy') {
    description 'A dummy Maven Job'
    label('maven3')

    parameters {
        gitParameter {
            name 'SELECTED_TAG'
            description 'The Git tag to checkout'
            type 'PT_TAG'
            defaultValue '2.3.4-2'
            branch ''
            branchFilter 'origin/(.*)'
            quickFilterEnabled false
            selectedValue 'DEFAULT'
            sortMode 'DESCENDING_SMART'
            tagFilter '*'
            useRepository '.*rest-http-example.git'
            listSize '10'
        }
    }

    scm {
        git {
            remote {
                url 'https://github.com/snowdrop/rest-http-example.git'
                branch('$SELECTED_TAG')
            }
        }
    }
    rootPOM 'pom.xml'
    goals 'clean install'
}