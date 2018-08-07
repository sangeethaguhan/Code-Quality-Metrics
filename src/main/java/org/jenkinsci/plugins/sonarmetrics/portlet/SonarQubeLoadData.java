/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.sonarmetrics.portlet;

import hudson.model.Job;
import hudson.model.Run;
import java.util.List;
import org.jenkinsci.plugins.sonarmetrics.bean.SonarQubeMetricsResultsSummary;
import java.util.HashMap;
import java.util.Map;
import org.jenkinsci.plugins.sonarmetrics.grid.SonarQubeMetricsGrid;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SonarQubeLoadData {

    private static Logger topLogger = Logger.getLogger("All Jenkins Logs");

    /**
     * Summarize the last coverage results of all jobs. If a job doesn't include
     * any coverage, add zero.
     *
     * @param jobs a final Collection of Job objects
     * @return SonarQubeMetricsResultsSummary the result summary
     */
    public static SonarQubeMetricsResultsSummary getResultSummary(List<Job> listJobs) throws IOException {

        String sonarBaseURL = "http://codescan01.pool.sv2.247-inc.net:4001/api";
        SonarQubeMetricsGrid s = new SonarQubeMetricsGrid("TestBoard");
        listJobs = s.dashboardJobs;
        SonarQubeMetricsResultsSummary summary = new SonarQubeMetricsResultsSummary();
        for (Job<?, ?> job : listJobs) {
            topLogger.info("Job Name : " + job.getDisplayName());
            Run<?, ?> run = job.getLastSuccessfulBuild();
            String projectName = "";
            if (run != null) {
                try {
                    String props = job.getConfigFile().asString();
                    if (props.contains("sonar.projectKey=")) {
                        projectName = props.substring(props.indexOf("sonar.projectKey=") + ("sonar.projectKey=").length(), props.indexOf("sonar.projectName=")).trim();
                        summary.addMetricsResult(getSonarMetrics(job, sonarBaseURL, projectName));
                    } else {
                        topLogger.info("SonarQube is not configured for this job: " + job.getDisplayName());
                    }
                    props = "";
                } catch(RuntimeException e) {
                  topLogger.error("Got unexpected exception", e);
                }
            }
        }
        return summary;
    }

    /**
     * Gets the SonarQube Metrics for each project.
     *
     * @param job the Job object
     * @param sonarBaseURL the SonarQube base URL to get the metrics.
     * @param projectName the SonarQube project id to get the metrics
     * @return SonarQubeMetricsResultsSummary the result summary for the sonar
     * project
     */
    private static SonarQubeMetricsResultsSummary getSonarMetrics(Job job, String sonarBaseURL, String projectName) {
        topLogger.info("SonarBaseURL : " + sonarBaseURL);
        topLogger.info("Project Name : " + projectName);
        String metrics = "ncloc,files,functions,bugs,vulnerabilities,code_smells,complexity,duplicated_lines_density,coverage";
        SonarQubeMetricsResultsSummary summary = new SonarQubeMetricsResultsSummary();

        Map<String, String> metricsMap = new HashMap<String, String>();
        try {
            String url = sonarBaseURL + "/resources?resource=" + projectName + "&metrics=" + metrics;
            topLogger.info("URL : " + url);
            metricsMap = readJsonFromUrl(url, metricsMap);
            String[] metricsStr = metrics.split(",");
            for (String metric : metricsStr) {
                if (!metricsMap.containsKey(metric)) {
                    metricsMap.put(metric, "0.0");
                }
            }
            return new SonarQubeMetricsResultsSummary(job,
                    Float.valueOf(metricsMap.get("ncloc")).longValue(),
                    Float.valueOf(metricsMap.get("files")).intValue(),
                    Float.valueOf(metricsMap.get("functions")).intValue(),
                    Float.valueOf(metricsMap.get("bugs")).intValue(),
                    Float.valueOf(metricsMap.get("vulnerabilities")).intValue(),
                    Float.valueOf(metricsMap.get("code_smells")).intValue(),
                    Float.valueOf(metricsMap.get("complexity")).intValue(),
                    Float.valueOf(metricsMap.get("duplicated_lines_density")).intValue(),
                    Float.valueOf(metricsMap.get("coverage")).intValue());

        } catch (Exception e) {
            topLogger.info(e.getMessage(),e);
        }
        return summary;
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static Map<String, String> readJsonFromUrl(String url, Map<String, String> metricsMap) throws IOException, JSONException {
        topLogger.info("Before InputStream "+url);
        InputStream is = new URL(url).openStream();
        topLogger.info("InputStream "+is.available());
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            topLogger.info("Result Lenghth : "+jsonText.length());
            JSONArray rootArray = new JSONArray(jsonText);
            topLogger.info("rootArray count "+rootArray.length());
            for (int i = 0; i < rootArray.length(); i++) {
                JSONObject jOb = (JSONObject) rootArray.getJSONObject(i);
                topLogger.info("JSON Object "+jOb.toString());
                try {
                    JSONArray jArray = jOb.getJSONArray("msr");
                    for (int j = 0; j < jArray.length(); j++) {
                        JSONObject jObj = jArray.getJSONObject(j);
                        metricsMap.put(jObj.get("key").toString(), jObj.get("val").toString());
                    }
                } catch (JSONException e) {
                  topLogger.info("JSON Exception",e);
                }

            }
            return metricsMap;
        } finally {
            is.close();
        }
    }
}
