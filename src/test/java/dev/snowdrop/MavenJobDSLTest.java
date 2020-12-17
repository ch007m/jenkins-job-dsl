package dev.snowdrop;

import hudson.maven.MavenModuleSet;
import hudson.maven.MavenModuleSetBuild;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import javaposse.jobdsl.plugin.ExecuteDslScripts;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MavenJobDSLTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void useMavenDSLGroovyFileAsJob() throws Exception {
        FreeStyleProject job = j.createFreeStyleProject();

        // Setup the ExecuteDslScripts with the target = mavenJob.groovy
        ExecuteDslScripts e = new ExecuteDslScripts();
        e.setTargets("mavenJob.groovy");
        e.setUseScriptText(true);
        job.getBuildersList().add(e);

        // Copy to the workspace the groovy file to be used
        j.getInstance().getWorkspaceFor(job).child("mavenJob.groovy").copyFrom(getClass().getResourceAsStream("/mavenJob.groovy"));

        // Execute the seed job to create the mavenJob
        FreeStyleBuild b = job.scheduleBuild2(0).get();

        assertEquals(1, b.number);
        assertEquals("#1",b.getDisplayName());
        if (b.getResult().toString() != "SUCCESS") {
            ArrayList LogResult = (ArrayList) b.getLog(100);
            LogResult.forEach((s) -> System.out.println(s));
        }

        // Check if the FreeStyleProject build reported that it generated the job: say-hello-world
        ArrayList LogResult = (ArrayList) b.getLog(100);
        //LogResult.forEach((s) -> System.out.println(s));
        assertTrue(b.getLog(100).stream().anyMatch(str -> str.contains("GeneratedJob{name='mvn-spring-boot-rest-http'")));

        // Get the FreeStyleProject containing the Job DSL steps to be executed - Say Hello World
        MavenModuleSet mavenJob = (hudson.maven.MavenModuleSet) j.jenkins.getItem("mvn-spring-boot-rest-http");
        MavenModuleSetBuild b2 = mavenJob.scheduleBuild2(0).get();

        assertEquals(1, b2.number);
        assertEquals("#1",b2.getDisplayName());
        assertEquals("SUCCESS",b2.getResult().toString());

        // Check if the FreeStyleProject build reported that it generated the job: say-hello-world
        assertTrue(b2.getLog(100).stream().anyMatch(str -> str.contains("Say Hello World !")));

        /* Added for debugging and logging purposes ;)
           LogResult = (ArrayList) b2.getLog(100);
           LogResult.forEach((s) -> System.out.println(s));
           System.out.println("//");
         */
    }
}
