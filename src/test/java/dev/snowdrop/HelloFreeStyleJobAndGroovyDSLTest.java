package dev.snowdrop;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import javaposse.jobdsl.plugin.ExecuteDslScripts;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HelloFreeStyleJobAndGroovyDSLTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void helloJobUsingDSLScript() throws Exception {
        String script = "job('say-hello-world') {\n" +
                "    steps {\n" +
                "        shell('echo Say Hello World !')\n" +
                "    }\n" +
                "}";

        FreeStyleProject p = j.createFreeStyleProject();
        ExecuteDslScripts e = new ExecuteDslScripts();
        e.setScriptText(script);
        p.getBuildersList().add(e);

        // Assert if build succeeded, if the Script executed includes the echo message
        FreeStyleBuild b = j.assertBuildStatusSuccess(p.scheduleBuild2(0).get());
        b.keepLog();

        assertEquals(1, b.number);
        assertEquals("#1",b.getDisplayName());
        assertEquals("SUCCESS",b.getResult().toString());

        // Check if the FreeStyleProject build reported that it generated the job: say-hello-world
        ArrayList LogResult = (ArrayList) b.getLog(100);
        //LogResult.forEach((s) -> System.out.println(s));
        assertTrue(b.getLog(100).stream().anyMatch(str -> str.contains("GeneratedJob{name='say-hello-world'}")));

        // Get the FreeStyleProject containing the Job DSL steps to be executed - Say Hello World
        FreeStyleProject freeStyleProjectGeneratedJob = (FreeStyleProject) j.jenkins.getItem("say-hello-world");
        FreeStyleBuild b2 =  freeStyleProjectGeneratedJob.scheduleBuild2(0).get();

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

    @Test
    public void helloJobUsingDSLFile() throws Exception {
        FreeStyleProject p = j.createFreeStyleProject();
        p.scheduleBuild2(0).get(); // run a build to create a workspace
        p.getSomeWorkspace().child("sayHelloWorldProject/dummy.groovy").copyFrom(getClass().getResourceAsStream("/dummy.groovy"));

        ExecuteDslScripts e = new ExecuteDslScripts();
        e.setTargets("sayHelloWorldProject/dummy.groovy");
        p.getBuildersList().add(e);

        // Assert if build succeeded, if the Script executed includes the echo message
        FreeStyleBuild b = j.assertBuildStatusSuccess(p.scheduleBuild2(0).get());
        b.keepLog();

        assertEquals(2, b.number);
        assertEquals("#2",b.getDisplayName());
        assertEquals("SUCCESS",b.getResult().toString());

        // Check if the FreeStyleProject build reported that it generated the job: say-hello-world
        ArrayList LogResult = (ArrayList) b.getLog(100);
        //LogResult.forEach((s) -> System.out.println(s));
        assertTrue(b.getLog(100).stream().anyMatch(str -> str.contains("GeneratedJob{name='say-hello-world'}")));

        // Get the FreeStyleProject containing the Job DSL steps to be executed - Say Hello World
        FreeStyleProject freeStyleProjectGeneratedJob = (FreeStyleProject) j.jenkins.getItem("say-hello-world");
        FreeStyleBuild b2 =  freeStyleProjectGeneratedJob.scheduleBuild2(0).get();

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