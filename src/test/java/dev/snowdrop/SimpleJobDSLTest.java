package dev.snowdrop;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import javaposse.jobdsl.plugin.ExecuteDslScripts;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SimpleJobDSLTest {

    @Rule public JenkinsRule j = new JenkinsRule();
    private ArrayList<String> LogResult;

    @Test
    public void useSimpleDSLGroovyFileAsJob() throws Exception {
        FreeStyleProject job = j.createFreeStyleProject("simple-seed-job");

        // Setup the ExecuteDslScripts to load the content of the DSL groovy script = mavenJob.groovy
        ExecuteDslScripts e = new ExecuteDslScripts();
        String dslScript = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/simpleJob.groovy")))
                .lines().collect(Collectors.joining("\n"));
        e.setScriptText(dslScript);
        e.setUseScriptText(true);
        job.getBuildersList().add(e);

        // Execute the seed job to create the mavenJob
        FreeStyleBuild b = job.scheduleBuild2(0).get();

        assertEquals(1, b.number);
        assertEquals("#1", b.getDisplayName());
        if (b.getResult().toString() != "SUCCESS") {
            LogResult = (ArrayList<String>) b.getLog(100);
            LogResult.forEach((s) -> System.out.println(s));
        }

        // Check if the FreeStyleProject build reported that it generated the job: say-hello-world
        LogResult = (ArrayList<String>) b.getLog(100);
        LogResult.forEach((s) -> System.out.println(s));

        assertTrue(b.getLog(100).stream().anyMatch(str -> str.contains("GeneratedJob{name='hello-world'")));

        FreeStyleProject freeStyleProjectGeneratedJob = (FreeStyleProject) j.jenkins.getItem("hello-world");
        FreeStyleBuild b2 = freeStyleProjectGeneratedJob.scheduleBuild2(0).get();

        assertEquals(1, b2.number);
        assertEquals("#1",b2.getDisplayName());
        assertEquals("SUCCESS",b2.getResult().toString());

        // Check if the FreeStyleProject build reported that it generated the job: say-hello-world
        assertTrue(b2.getLog(100).stream().anyMatch(str -> str.contains("Say Hello World !")));
    }
}
