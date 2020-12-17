// See: https://www.happycoders.eu/devops/jenkins-tutorial-create-jobs-with-job-dsl/
mavenJob('mvn-spring-boot-rest-http') {
    description 'A Maven Job compiling the project Spring Boot Rest HTTP Example'

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
                // branch('$SELECTED_TAG')
                branch('2.3.4-2')
            }
        }
    }
    rootPOM 'pom.xml'
    goals 'clean install'
}