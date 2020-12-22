import hudson.EnvVars;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Collectors;

def workspace;
def env = System.getenv();

/*
 * Print the workspace path
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
 * List the files within the job workspace
 */
println "List files within the Workspace of the job"
"ls -la $workspace".execute().text;

/*
 * Remove the <dependencyManagement>, </dependencyManagement> tags
 */
// Code is only working with jdk11
//String pomFileText = Files.readString(Paths.get(pomfile.toURI()));
String pomFileText = Files.lines(Paths.get(pomfile.toURI()))
        .collect(Collectors.joining(System.lineSeparator()));
pomFileText = pomFileText.replaceFirst("<dependencyManagement>","");
pomFileText = pomFileText.replaceFirst("</dependencyManagement>","");
//println("Pom modified : " + pomFileText)

pomfile.write(pomFileText);
println "Look if the pom file dont include the tags <dependencyManagement>, </dependencyManagement>"
"cat $workspace/pom.xml".execute().text;