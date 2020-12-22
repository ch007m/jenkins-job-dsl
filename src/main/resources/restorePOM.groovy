import hudson.EnvVars;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

def workspace;
def env = System.getenv();

/*
 * Find the workspace env var locally or when running under jenkins
 */
try {
    EnvVars envVars = build.getEnvironment(listener);
    workspace = envVars.get('WORKSPACE')
} catch(groovy.lang.MissingPropertyException e) {
    println ("Build is null");
    workspace = env['MAVEN_PROJECT'];
}

/*
 * Restore the pom.xml file
 */
println "Restore pom.xml file"
File pomfile = new File("$workspace/pom.xml");
File pomBackupFile = new File("$workspace/pom.xml.bk");
Files.copy(pomBackupFile.toPath(), pomfile.toPath(), StandardCopyOption.REPLACE_EXISTING);