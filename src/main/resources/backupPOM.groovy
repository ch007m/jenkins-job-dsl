import jenkins.model.Jenkins
import hudson.EnvVars

def jenkins = Jenkins.getInstanceOrNull()
EnvVars envVars = build.getEnvironment(listener);

def workspace = envVars.get('WORKSPACE')
println "WKS: $workspace"
println "Backup pom.xml file"
"cp $workspace/pom.xml $workspace/pom.xml.bk".execute().text

println "List files within the Workspace of the job"
"ls -la $workspace".execute().text