package dev.snowdrop;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.WithPlugin;

import java.util.ArrayList;


public class PipelineJobDSLTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    private String script = "pipeline {\n" +
            "    agent any\n" +
            "\n" +
            "     stages {\n" +
            "        stage('Prepare') {\n" +
            "            steps {\n" +
            "                echo 'Preparing ...'\n" +
            "                sh 'mvn --version'\n" +
            "            }\n" +
            "        }\n" +
            "        stage('Build') {\n" +
            "            steps {\n" +
            "                echo 'Building ...'\n" +
            "            }\n" +
            "        }\n" +
            "        stage('Deploy') {\n" +
            "            steps {\n" +
            "                echo 'Deploying ...'\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}\n";

    @WithPlugin({"durable-task-1.35.hpi"})
    @Test
    public void useSimpleDSLGroovyFileAsJob() throws Exception {
        CpsFlowDefinition flow = new CpsFlowDefinition(script,false);
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
