package dev.snowdrop;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.WithPlugin;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class PipelineJobDSLTest {

    @Rule public JenkinsRule j = new JenkinsRule();
    private String pipelineScript;
    private static Map<String, String> plugins = new HashMap<String, String>();

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @BeforeClass
    public static void installPlugins() throws Exception {
        String destPath = "target/classes/plugins/";
        Files.createDirectories(Paths.get(destPath));

        plugins.put(
                "durable-task.hpi", "http://repo.jenkins-ci.org/releases/org/jenkins-ci/plugins/durable-task/1.35/durable-task-1.35.hpi"
        );

        plugins.forEach((key,value) -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .GET()
                        .uri(URI.create(value))
                        .setHeader("User-Agent", "Java 11 HttpClient Bot") // add request header
                        .build();
                HttpResponse<Path> response = httpClient.send(request, HttpResponse.BodyHandlers.ofFile(Paths.get(destPath + key)));
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    @Before
    public void fetchPipelineGroovy() {
        pipelineScript = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/pipelinegitMaven.groovy")))
                .lines().collect(Collectors.joining("\n"));
    }

    @WithPlugin({"durable-task.hpi"})
    @Test
    public void useSimpleDSLGroovyFileAsJob() throws Exception {
        CpsFlowDefinition flow = new CpsFlowDefinition(pipelineScript,false);
        WorkflowJob workflowJob = j.createProject(WorkflowJob.class,"pipeline-job");
        workflowJob.setDefinition(flow);
        WorkflowRun result = workflowJob.scheduleBuild2(0).get();

        j.assertBuildStatusSuccess(result);
        j.assertLogContains("Started", result);

        if (result.toString() != "SUCCESS") {
            ArrayList LogResult = (ArrayList) result.getLog(200);
            LogResult.forEach((s) -> System.out.println(s));
        }
    }
}
