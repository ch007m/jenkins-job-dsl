import jenkins.model.Jenkins
import static groovy.io.FileType.*
import hudson.EnvVars

def jenkins = Jenkins.getInstanceOrNull()
EnvVars envVars = build.getEnvironment(listener);

def workspace = envVars.get('WORKSPACE')
println "WKS: $workspace"