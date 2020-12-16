/**
 * ---
 - name: "Check BOM dependencies"
 block:
 - name: "Make backup of the pom file"
 copy:
 src: "{{ local_sd_gh_project_path }}/pom.xml"
 dest: "{{ local_sd_gh_project_path }}/pom.xml.bak"

 - name: "Replace dependency management in pom.xml"
 replace:
 path: "{{ local_sd_gh_project_path }}/pom.xml"
 regexp: "{{ item }}"
 replace: '\1\2'
 loop:
 - '(\s+)<dependencyManagement>(\s+.*)?$'
 - '(\s+)</dependencyManagement>(\s+.*)?$'

 - name: "Run maven dependency tree"
 shell: "mvn dependency:tree"
 args:
 chdir: "{{ local_sd_gh_project_path }}"
 register: dependency_tree

 - name: "Save dependency info"
 copy:
 content: "{{ dependency_tree.stdout }}"
 dest: "{{ local_release_folder }}/{{ snowdrop_version_full }}/upstream-dependency-tree.txt"

 always:
 - name: "Leave pom.xml as was"
 copy:
 src: "{{ local_sd_gh_project_path }}/pom.xml.bak"
 dest: "{{ local_sd_gh_project_path }}/pom.xml"

 - name: "Delete backup"
 file:
 path: "{{ local_sd_gh_project_path }}/pom.xml.bak"
 state: absent

 - name: "Get git status for folder"
 shell: "git status"
 args:
 chdir: "{{ local_sd_gh_project_path }}"
 register: git_status_res

 - name: "Fail if there are local changes"
 fail:
 msg: "There are changes to the local repository ({{ local_sd_gh_project_path }}), cannot move on..."
 when: "not ('nothing to commit, working tree clean' in git_status_res.stdout)"

 ...

 **/

mavenJob('check-bom-dependencies') {
    description 'Job checking the BOM dependencies'
    label('maven3')

    logRotator {
        numToKeep 3
    }

    parameters {
        globalVariableParam('reportFileName', 'dependency-tree.txt', 'Report file name containing the putput of the dependency tree')
        stringParam('TAG', '2.3.4.Final',
                'The release version for the artifact. If you leave this empty, ' +
                        'the current SNAPSHOT version will be used with the ' +
                        '"-SNAPSHOT" suffix removed (example: if the current version ' +
                        'is "1.0-SNAPSHOT", the release version will be "1.0").')
    }

    scm {
        git {
            remote {
                url 'https://github.com/snowdrop/spring-boot-bom'
                branch '$TAG'
            }
        }
    }

    preBuildSteps {
            systemGroovyCommand '''\
              import jenkins.model.Jenkins
              import static groovy.io.FileType.*
              import hudson.EnvVars
              
              def jenkins = Jenkins.getInstanceOrNull()
              EnvVars envVars = build.getEnvironment(listener);
              
              def workspace = envVars.get('WORKSPACE')
              println "WKS: $workspace"
              
              println "Backup the pom.xml file"
              def src = new File("$workspace/pom.xml")
              def dst = new File("$workspace/pom.xml.bak")
              
              def report = new File("$workspace/dependency-tree.txt")
              
              println "Remove <dependencyManagement> tag from the pom.xml to allow to control if the GAVs exist"
              def pomModified = src.text
                      .replace('<dependencyManagement>', '')
                      .replace('</dependencyManagement>', '')
              src.text = pomModified
              println src.txt
              '''.stripIndent()
    }

    rootPOM 'pom.xml'
    goals '-B dependency:tree > $reportFileName'
}