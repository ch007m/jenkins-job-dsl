// Build a Maven project using the standard image and Scripted syntax.
// Rather than inline YAML, you could use: yaml: readTrusted('jenkins-pod.yaml')
// Or, to avoid YAML: containers: [containerTemplate(name: 'maven', image: 'maven:3.6.3-jdk-8', command: 'sleep', args: 'infinity')]
podTemplate(
        cloud: 'openshift',
        serviceAccount: 'jenkins',
        containers: [
          containerTemplate(
                  name: 'maven',
                  image: 'maven:3.6.3-jdk-8',
                  ttyEnabled: true,
                  command: 'cat')
        ],
        volumes: [
          persistentVolumeClaim(
                  mountPath: '/root/.m2/repository',
                  claimName: 'maven-repo',
                  readOnly: false)
        ]) {
    node(POD_LABEL) {
        git url: 'https://github.com/jglick/simple-maven-project-with-tests.git'
        container('maven') {
            sh 'mvn -B -ntp -Dmaven.test.failure.ignore verify'
        }
        junit '**/target/surefire-reports/TEST-*.xml'
        archiveArtifacts '**/target/*.jar'
    }
}
