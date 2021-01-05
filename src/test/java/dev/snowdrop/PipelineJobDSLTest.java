package dev.snowdrop;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.WithPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.stream.Collectors;


public class PipelineJobDSLTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    private String pipelineScript;

    @Before
    public void fetchPipelineGroovy() {
        pipelineScript = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/pipelinegitMaven.groovy")))
                .lines().collect(Collectors.joining("\n"));
    }

    @WithPlugin({"durable-task-1.35.hpi"})
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