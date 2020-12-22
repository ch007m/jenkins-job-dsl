import hudson.EnvVars

import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors

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
 * Remove the <dependencyManagement>, </dependencyManagement> tags from the pom.xml file
 */
println "Remove from the pom.xml file <dependencyManagement>, </dependencyManagement> tags and save the result within pom.xml"
File pomfile = new File("$workspace/pom.xml");
String pomFileText = Files.lines(Paths.get(pomfile.toURI()))
        .collect(Collectors.joining(System.lineSeparator()));
pomFileText = pomFileText.replaceFirst("<dependencyManagement>","");
pomFileText = pomFileText.replaceFirst("</dependencyManagement>","");
pomfile.write(pomFileText);