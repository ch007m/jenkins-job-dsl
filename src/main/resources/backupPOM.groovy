import hudson.EnvVars
import hudson.model.AbstractBuild
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

def workspace
def AbstractBuild build

/*
 * Print the workspace path
 */
if (build != null) {
    EnvVars envVars = build.getEnvironment(listener);
    workspace = envVars.get('WORKSPACE')
} else {
    workspace = args[0]
}

println "WKS: $workspace"

/*
 * Backup the pom.xml file
 */
println "Backup pom.xml file"
def pomfile = new File("$workspace/pom.xml")
def pomBackupFile = new File("$workspace/pom.xml.bk")
Files.copy(pomfile.toPath(), pomBackupFile.toPath(), StandardCopyOption.REPLACE_EXISTING)

/*
 * List the files within the job workspace
 */
println "List files within the Workspace of the job"
"ls -la $workspace".execute().text

/*
 * Remove the <dependencyManagement>, </dependencyManagement> tags
 */
pomFileText = Files.readString(Paths.get(pomfile.toURI()));
pomFileText.replaceFirst("<dependencyManagement>","")
pomFileText.replaceFirst("</dependencyManagement>","")
//println("Pom modified : " + pomFileText)

pomfile.write(pomFileText)
println "Look if the pom file dont include the tags <dependencyManagement>, </dependencyManagement>"
"cat $workspace/pom.xml".execute().text