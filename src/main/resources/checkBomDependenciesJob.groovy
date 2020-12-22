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
    description 'Maven Job checking the BOM dependencies of the Spring Boot project'

    logRotator {
        numToKeep 3
    }

    parameters {
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
            systemGroovyCommand(readFileFromWorkspace('backupPOM.groovy')) {
                // groovyInstallation('groovy-3.0.7')
              }
    }

    rootPOM 'pom.xml'
    goals '-B dependency:tree'
}