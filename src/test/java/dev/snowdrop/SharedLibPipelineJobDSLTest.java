package dev.snowdrop;

import hudson.model.Result;
import hudson.plugins.git.GitSCM;
import jenkins.plugins.git.GitSCMSource;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.libs.GlobalLibraries;
import org.jenkinsci.plugins.workflow.libs.LibraryConfiguration;
import org.jenkinsci.plugins.workflow.libs.SCMRetriever;
import org.jenkinsci.plugins.workflow.libs.SCMSourceRetriever;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class SharedLibPipelineJobDSLTest {

    @Rule public JenkinsRule j = new JenkinsRule();
    private String pipelineScript;
    private ArrayList<String> LogResult;
    private static String GIT_SHARED_LIB_REPO = "https://github.com/ch007m/jenkins-job-dsl.git";

    @Before
    public void fetchPipelineGroovy() {
        pipelineScript = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/pipelineSharedLib.groovy")))
                .lines().collect(Collectors.joining("\n"));
    }

    @Test
    public void useSimpleDSLGroovyFileAsJob() throws Exception {
        // Configure the Pipeline Global Shared Libraries
        LibraryConfiguration mytools = new LibraryConfiguration("mytools",
                new SCMSourceRetriever(new GitSCMSource(GIT_SHARED_LIB_REPO)));
        mytools.setDefaultVersion("main");
        mytools.setAllowVersionOverride(true);
        mytools.setIncludeInChangesets(true);
        GlobalLibraries.get().setLibraries(Arrays.asList(mytools));

        // Create a Job loading the Pipeline Groovy script
        CpsFlowDefinition flow = new CpsFlowDefinition(pipelineScript,false);
        WorkflowJob workflowJob = j.createProject(WorkflowJob.class,"pipeline-shared-lib-job");
        workflowJob.setDefinition(flow);
        WorkflowRun b = j.assertBuildStatus(Result.SUCCESS, workflowJob.scheduleBuild2(0));

        j.assertLogContains("Started", b);
        j.assertLogContains("Do a test", b);

        if (b.toString() != "SUCCESS") {
            LogResult = (ArrayList<String>) b.getLog(200);
            LogResult.forEach((s) -> System.out.println(s));
        }
    }
}
