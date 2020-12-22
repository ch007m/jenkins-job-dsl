package dev.snowdrop;

import hudson.maven.MavenModuleSet;
import hudson.maven.MavenModuleSetBuild;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.plugins.groovy.Groovy;
import hudson.plugins.groovy.GroovyInstallation;
import hudson.tasks.Maven;
import javaposse.jobdsl.plugin.ExecuteDslScripts;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.ToolInstallations;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MavenCheckBOMJobDSLTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void useMavenDSLGroovyFileAsJob() throws Exception {
        String seedJobName = "maven-seed-job";
        // Add maven to the Jenkins Global Configuration Tools as it is needed by the mavenJob
        Maven.MavenInstallation mvn = ToolInstallations.configureDefaultMaven("apache-maven-3.6.3", Maven.MavenInstallation.MAVEN_30);
        Maven.MavenInstallation m3 = new Maven.MavenInstallation("apache-maven-3.6.3", mvn.getHome(), JenkinsRule.NO_PROPERTIES);
        j.getInstance().getDescriptorByType(Maven.DescriptorImpl.class).setInstallations(m3);

        // Add Groovy to the Jenkins Global Configuration Tools as it is needed by the mavenJob
        // systemGroovyCommand - child
        // groovyCommand - master
        GroovyInstallation groovy = new GroovyInstallation("groovy3","",null);
        j.getInstance().getDescriptorByType(Groovy.DescriptorImpl.class).setInstallations(groovy);

        FreeStyleProject job = j.createFreeStyleProject(seedJobName);

        // Create under the temp jenkins directory, the wokspace of the job
        File root = j.jenkins.root;
        File wksJobDir = new File(root.getAbsolutePath() + "/workspace/" + seedJobName);
        wksJobDir.mkdir();
        // Create destination file
        File groovyWksFile = new File(wksJobDir.getAbsolutePath() + "/backupPOM.groovy");

        // Copy the needed groovy file from local to the job workspace
        InputStream is = getClass().getResourceAsStream("/backupPOM.groovy");
        Path dest = Paths.get(groovyWksFile.getAbsolutePath());
        Files.copy(is, dest, StandardCopyOption.REPLACE_EXISTING);

        // Setup the ExecuteDslScripts to load the content of the DSL groovy script = mavenJob.groovy
        ExecuteDslScripts e = new ExecuteDslScripts();
        String dslScript = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/checkBomDependenciesJob.groovy")))
                .lines().collect(Collectors.joining("\n"));
        e.setScriptText(dslScript);
        e.setUseScriptText(true);
        job.getBuildersList().add(e);

        // Execute the seed job to create the mavenJob
        FreeStyleBuild b = job.scheduleBuild2(0).get();

        assertEquals(1, b.number);
        assertEquals("#1", b.getDisplayName());
        if (b.getResult().toString() != "SUCCESS") {
            ArrayList LogResult = (ArrayList) b.getLog(200);
            LogResult.forEach((s) -> System.out.println(s));
        }

        // Check if the FreeStyleProject build reported that it generated the job: say-hello-world
        ArrayList LogResult = (ArrayList) b.getLog(100);
        //LogResult.forEach((s) -> System.out.println(s));
        assertTrue(b.getLog(100).stream().anyMatch(str -> str.contains("GeneratedJob{name='check-bom-dependencies'")));

        // Get the FreeStyleProject containing the Job DSL steps to be executed - "check-bom-dependencies"
        MavenModuleSet mavenJob = (MavenModuleSet) j.jenkins.getItem("check-bom-dependencies");
        MavenModuleSetBuild b2 = mavenJob.scheduleBuild2(0).get();

        assertEquals(1, b2.number);
        assertEquals("#1", b2.getDisplayName());
        if (b2.getResult().toString() != "SUCCESS") {
            LogResult = (ArrayList) b2.getLog(200);
            LogResult.forEach((s) -> System.out.println(s));
        } else {
            System.out.println("Job build succeeded !");
            LogResult = (ArrayList) b2.getLog(200);
            LogResult.forEach((s) -> System.out.println(s));
        }
    }
}
