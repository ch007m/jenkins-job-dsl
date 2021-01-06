package dev.snowdrop.util;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.JenkinsTriggerHelper;
import com.offbytwo.jenkins.model.BuildWithDetails;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class RemoteJenkinsClient {
    private final static String jobName = "mvn-spring-boot-rest-http";
    private final static String serverURL = "http://localhost:8080";
    private final static String userName = "admin";
    private final static String password = "admin";
    private static JenkinsServer jenkinsCli;

    public static void main(String[] args) throws Exception {
        init();

        assertEquals(jobName,jenkinsCli.getJob(jobName).getName());
        assertEquals("A Maven Job compiling the project Spring Boot Rest HTTP Example",jenkinsCli.getJob(jobName).getDescription());

        JenkinsTriggerHelper h = new JenkinsTriggerHelper(jenkinsCli);

        Map<String, String> params = new HashMap<String, String>();
        params.put("SELECTED_TAG", "2.3.4_2");

        BuildWithDetails build = h.triggerJobAndWaitUntilFinished(jobName, params);
        assertEquals("SUCCESS",build.getResult().name());

    }

    public static void init() {
        try {
            jenkinsCli = new JenkinsServer(new URI(serverURL), userName, password);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
