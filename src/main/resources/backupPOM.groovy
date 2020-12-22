import hudson.EnvVars;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Collectors;

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

println "WKS: $workspace";

/*
 * Backup the pom.xml file
 */
println "Backup pom.xml file"
File pomfile = new File("$workspace/pom.xml");
File pomBackupFile = new File("$workspace/pom.xml.bk");
Files.copy(pomfile.toPath(), pomBackupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

/*
 * Remove the <dependencyManagement>, </dependencyManagement> tags
 */
// Code is only working with jdk11
// !!! String pomFileText = Files.readString(Paths.get(pomfile.toURI()));

println "remove from the pom.xml file <dependencyManagement>, </dependencyManagement> tags and save the result within pom.xml"
String pomFileText = Files.lines(Paths.get(pomfile.toURI()))
        .collect(Collectors.joining(System.lineSeparator()));
pomFileText = pomFileText.replaceFirst("<dependencyManagement>","");
pomFileText = pomFileText.replaceFirst("</dependencyManagement>","");
pomfile.write(pomFileText);
