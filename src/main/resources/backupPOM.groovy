import jenkins.model.Jenkins
import hudson.EnvVars
import java.nio.file.Files
import java.nio.file.StandardCopyOption

def jenkins = Jenkins.getInstanceOrNull()
EnvVars envVars = build.getEnvironment(listener);

def workspace = envVars.get('WORKSPACE')
println "WKS: $workspace"
println "Backup pom.xml file"
//"cp $workspace/pom.xml $workspace/pom.xml.bk".execute().text

def file = new File("$workspace/pom.xml")
def newFile = new File("$workspace/pom.xml.bk")
Files.copy(file.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING)

println "List files within the Workspace of the job"
"ls -la $workspace".execute().text