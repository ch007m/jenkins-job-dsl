package dev.snowdrop;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.ArrayList;

public class PipelineJobDSLTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    private String script = "pipeline {\n" +
            "    agent any\n" +
            "\n" +
            "     stages {\n" +
            "        stage('Build') {\n" +
            "            steps {\n" +
            "                echo 'Building..'\n" +
            "            }\n" +
            "        }\n" +
            "        stage('Test') {\n" +
            "            steps {\n" +
            "                echo 'Testing..'\n" +
            "            }\n" +
            "        }\n" +
            "        stage('Deploy') {\n" +
            "            steps {\n" +
            "                echo 'Deploying....'\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}\n";

    @Test
    public void useSimpleDSLGroovyFileAsJob() throws Exception {
        CpsFlowDefinition flow = new CpsFlowDefinition(script,false);
        WorkflowJob workflowJob = j.createProject(WorkflowJob.class,"pipeline-job");
        workflowJob.setDefinition(flow);
        WorkflowRun result = workflowJob.scheduleBuild2(0).get();

        if (result.toString() != "SUCCESS") {
            ArrayList LogResult = (ArrayList) result.getLog(200);
            LogResult.forEach((s) -> System.out.println(s));
        }
        j.assertLogContains("Started", result);
    }
}
