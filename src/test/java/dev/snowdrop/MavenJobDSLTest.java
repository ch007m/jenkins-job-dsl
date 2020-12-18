package dev.snowdrop;

import hudson.tasks.Maven;
import org.junit.Rule;
import org.jvnet.hudson.test.*;
import hudson.maven.MavenModuleSet;
import hudson.maven.MavenModuleSetBuild;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import javaposse.jobdsl.plugin.ExecuteDslScripts;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MavenJobDSLTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void useMavenDSLGroovyFileAsJob() throws Exception {
        // Add maven to the Jenkins Global Configuration Tools as it is needed by the mavenJob
        Maven.MavenInstallation mvn = ToolInstallations.configureDefaultMaven("apache-maven-3.6.3", Maven.MavenInstallation.MAVEN_30);
        Maven.MavenInstallation m3 = new Maven.MavenInstallation("apache-maven-3.6.3", mvn.getHome(), JenkinsRule.NO_PROPERTIES);
        j.getInstance().getDescriptorByType(Maven.DescriptorImpl.class).setInstallations(m3);

        FreeStyleProject job = j.createFreeStyleProject("maven-seed-job");

        // Setup the ExecuteDslScripts to load the content of the DSL groovy script = mavenJob.groovy
        ExecuteDslScripts e = new ExecuteDslScripts();
        //e.setTargets("mavenJob.groovy");
        String dslScript = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/mavenJob.groovy")))
                .lines().collect(Collectors.joining("\n"));
        e.setScriptText(dslScript);
        e.setUseScriptText(true);
        job.getBuildersList().add(e);

        // Execute the seed job to create the mavenJob
        FreeStyleBuild b = job.scheduleBuild2(0).get();

        assertEquals(1, b.number);
        assertEquals("#1",b.getDisplayName());
        if (b.getResult().toString() != "SUCCESS") {
            ArrayList LogResult = (ArrayList) b.getLog(200);
            LogResult.forEach((s) -> System.out.println(s));
        }

        // Check if the FreeStyleProject build reported that it generated the job: say-hello-world
        ArrayList LogResult = (ArrayList) b.getLog(100);
        //LogResult.forEach((s) -> System.out.println(s));
        assertTrue(b.getLog(100).stream().anyMatch(str -> str.contains("GeneratedJob{name='mvn-spring-boot-rest-http'")));

        // Get the FreeStyleProject containing the Job DSL steps to be executed - "mvn-spring-boot-rest-http"
        MavenModuleSet mavenJob = (hudson.maven.MavenModuleSet) j.jenkins.getItem("mvn-spring-boot-rest-http");
        MavenModuleSetBuild b2 = mavenJob.scheduleBuild2(0).get();

        assertEquals(1, b2.number);
        assertEquals("#1",b2.getDisplayName());
        if (b2.getResult().toString() != "SUCCESS") {
            LogResult = (ArrayList) b2.getLog(200);
            LogResult.forEach((s) -> System.out.println(s));
        }
        // TODO : Add code to fix : ERROR: A Maven installation needs to be available for this project to be built.Either your server has no Maven installations
    }
}
