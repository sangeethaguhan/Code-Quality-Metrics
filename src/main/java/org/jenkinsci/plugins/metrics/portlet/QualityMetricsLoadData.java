/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.metrics.portlet;

import com.checkmarx.jenkins.CxScanResult;
import com.google.gerrit.extensions.api.GerritApi;
import com.google.gerrit.extensions.common.ChangeInfo;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.urswolfer.gerrit.client.rest.GerritAuthData;
import com.urswolfer.gerrit.client.rest.GerritRestApiFactory;
import hudson.model.Hudson;
import hudson.model.Job;
import hudson.model.Run;
import hudson.plugins.jacoco.JacocoBuildAction;
import hudson.plugins.logparser.LogParserAction;
import hudson.plugins.view.dashboard.test.TestResult;
import hudson.plugins.view.dashboard.test.TestUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.jenkinsci.plugins.metrics.bean.QualityMetricsResultsSummary;
import static org.jenkinsci.plugins.metrics.gating.QualityGate.getProjectOwner;
import org.jenkinsci.plugins.metrics.grid.QualityMetricsGrid;
import org.jenkinsci.plugins.sonarmetrics.Messages;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class QualityMetricsLoadData {

    private static Logger topLogger = Logger.getLogger("All Jenkins Logs");
    private static String gitProjectPath = "";

    /**
     * Summarize the last coverage results of all jobs. If a job doesn't include
     * any coverage, add zero.
     *
     * @param jobs a final Collection of Job objects
     * @return QualityMetricsResultsSummary the result summary
     */
    public static QualityMetricsResultsSummary getResultSummary(List<Job> listJobs) throws IOException {

        String sonarBaseURL = Messages.SonarQubeServerUrl() + "/api";
        QualityMetricsGrid s = new QualityMetricsGrid("TestBoard");
        listJobs = s.dashboardJobs;
        QualityMetricsResultsSummary summary = new QualityMetricsResultsSummary();
        GerritRestApiFactory gerritRestApiFactory = new GerritRestApiFactory();
        GerritAuthData.Basic authData = new GerritAuthData.Basic(Messages.GerritServerUrl(), Messages.GerritUserName(), Messages.GerritPassword());
        GerritApi gerritApi = gerritRestApiFactory.create(authData);
        for (Job<?, ?> job : listJobs) {
            Log("Job Name : " + job.getDisplayName());
            Run<?, ?> run = job.getLastSuccessfulBuild();
            String projectName = "";
            if (run != null) {

                try {
                    String props = job.getConfigFile().asString();
                    if (props.contains("UserRemoteConfig")) {
                        String tempString = props.substring(props.indexOf("<string>projectPath</string>") + ("<string>projectPath</string>").length(), props.length());//.replaceAll("</url>","").trim();
                        gitProjectPath = tempString.substring(0, tempString.indexOf("</entry>")).replaceAll("<string>", "").replaceAll("</string>", "").trim();
                    } else {
                        gitProjectPath = "platform/pdsp-wrapper";
                    }
                    if (props.contains("sonarProjectKey")) {
                        String tempString = props.substring(props.indexOf("<string>sonarProjectKey</string>") + ("<string>sonarProjectKey</string>").length(), props.length());
                        projectName = tempString.substring(0, tempString.indexOf("</entry>")).replaceAll("<string>", "").replaceAll("</string>", "").trim();
                        summary.addMetricsResult(getQualityMetrics(job, run, sonarBaseURL, projectName, gerritApi));
                    } else {
                        Log("SonarQube is not configured for this job: " + job.getDisplayName());
                    }
                    props = "";
                } catch (RuntimeException e) {
                  topLogger.error("Got unexpected exception", e);
                }
            }
        }
        return summary;
    }

    /**
     * Gets the Quality Metrics for each project.
     *
     * @param job the Job object
     * @param sonarBaseURL the SonarQube base URL to get the metrics.
     * @param projectName the SonarQube project id to get the metrics
     * @return QualityMetricsResultsSummary the result summary for the sonar
     * project
     */
    public static QualityMetricsResultsSummary getQualityMetrics(Job job, Run run, String sonarBaseURL, String projectName) throws IOException {
        Log("SonarBaseURL : " + sonarBaseURL + "Project Name : " + projectName);
        String metrics = Messages.SonarQubeMetricsList();
        QualityMetricsGrid s = new QualityMetricsGrid(Messages.SonarQubeTempPortletName());
      
        Map<String, String> metricsMap = new HashMap<String, String>();
        //try {
            String url = sonarBaseURL + "/resources?resource=" + projectName + "&metrics=" + metrics;
            Log("URL : " + url);
            try {
                metricsMap = readJsonFromUrl(url, metricsMap);
            } catch (RuntimeException e) {
                Log("SonarQube is not configured for this job : " + job.getDisplayName(),e);
            } catch (JSONException ex) {
                java.util.logging.Logger.getLogger(QualityMetricsLoadData.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                CxScanResult res = run.getAction(CxScanResult.class);
                if (res != null) {
                    metricsMap.put("highCheckMarx", String.valueOf(res.getHighCount()));
                    metricsMap.put("mediumCheckMarx", String.valueOf(res.getMediumCount()));
                    metricsMap.put("lowCheckMarx", String.valueOf(res.getLowCount()));
                } else {
                    metricsMap.put("highCheckMarx", "0.0");
                    metricsMap.put("mediumCheckMarx", "0.0");
                    metricsMap.put("lowCheckMarx", "0.0");
                }
                Log("CheckMarx metrics are successfully collected for " + job.getDisplayName() + " High : " + metricsMap.get("highCheckMarx"));
            } catch (RuntimeException e) {
                metricsMap.put("highCheckMarx", "0.0");
                    metricsMap.put("mediumCheckMarx", "0.0");
                    metricsMap.put("lowCheckMarx", "0.0");
                Log("CheckMarx is not configured for this job : " + job.getDisplayName(),e);
            }

            try {
                TestResult testResults = TestUtil.getTestResult(run);
                if (testResults != null) {
                    metricsMap.put("unitTestsCount", String.valueOf(testResults.getTests()));
                    metricsMap.put("unitTestsSuccess", String.valueOf(testResults.getSuccess()));
                    metricsMap.put("unitTestsSuccessPct", String.valueOf(testResults.getSuccessPct()));
                    metricsMap.put("unitTestsFailed", String.valueOf(testResults.getFailed()));
                    metricsMap.put("unitTestsFailedPct", String.valueOf(testResults.getFailedPct()));
                    metricsMap.put("unitTestsSkipped", String.valueOf(testResults.getSkipped()));
                    metricsMap.put("unitTestsSkippedPct", String.valueOf(testResults.getSkippedPct()));
                } else {
                    metricsMap.put("unitTestsCount", "0.0");
                    metricsMap.put("unitTestsSuccess", "0.0");
                    metricsMap.put("unitTestsSuccessPct", "0.0");
                    metricsMap.put("unitTestsFailed", "0.0");
                    metricsMap.put("unitTestsFailedPct", "0.0");
                    metricsMap.put("unitTestsSkipped", "0.0");
                    metricsMap.put("unitTestsSkippedPct", "0.0");
                }
                Log("Unit Test metrics are successfully collected for " + job.getDisplayName() + " unitTestsCount : " + metricsMap.get("unitTestsCount"));
            } catch (RuntimeException  e) {
                metricsMap.put("unitTestsCount", "0.0");
                    metricsMap.put("unitTestsSuccess", "0.0");
                    metricsMap.put("unitTestsSuccessPct", "0.0");
                    metricsMap.put("unitTestsFailed", "0.0");
                    metricsMap.put("unitTestsFailedPct", "0.0");
                    metricsMap.put("unitTestsSkipped", "0.0");
                    metricsMap.put("unitTestsSkippedPct", "0.0");
                Log("Unit Test Results are not available for this job : " + job.getDisplayName(),e);
            }

            try {
                if (s.warningsAggregator != null) {
                    metricsMap.put("findBugsWarnings", s.warningsAggregator.getFindBugs(job));
                    metricsMap.put("checkStyleWarnings", s.warningsAggregator.getCheckStyle(job));
                    metricsMap.put("pmdWarnings", s.warningsAggregator.getPmd(job));
                    metricsMap.put("compilerWarnings", s.warningsAggregator.getCompilerWarnings(job));
                    metricsMap.put("totalWarnings", s.warningsAggregator.getTotal(job));
                } else {
                    metricsMap.put("findBugsWarnings", "-");
                    metricsMap.put("checkStyleWarnings", "-");
                    metricsMap.put("pmdWarnings", "-");
                    metricsMap.put("compilerWarnings", "-");
                    metricsMap.put("totalWarnings", "0");
                }
                Log("Warnings metrics are successfully collected for " + job.getDisplayName());
            } catch (RuntimeException  e) {
                metricsMap.put("findBugsWarnings", "-");
                    metricsMap.put("checkStyleWarnings", "-");
                    metricsMap.put("pmdWarnings", "-");
                    metricsMap.put("compilerWarnings", "-");
                    metricsMap.put("totalWarnings", "0");
                Log("Warnings plugin is not configured for this job : " + job.getDisplayName(),e);
            }

            try {
                LogParserAction logParser = job.getLastBuild().getAction(LogParserAction.class);
                if (logParser != null) {
                    metricsMap.put("totalParsedErrors", String.valueOf(logParser.getResult().getTotalErrors()));
                    metricsMap.put("totalParsedWarnings", String.valueOf(logParser.getResult().getTotalWarnings()));
                    metricsMap.put("totalParsedInfos", String.valueOf(logParser.getResult().getTotalInfos()));
                    metricsMap.put("parsedErrorLinkFile", logParser.getResult().getErrorLinksFile());
                    metricsMap.put("parsedWarningLinkFile", logParser.getResult().getWarningLinksFile());
                    metricsMap.put("parsedInfoLinkFile", logParser.getResult().getInfoLinksFile());
                } else {
                    metricsMap.put("totalParsedErrors", "0");
                    metricsMap.put("totalParsedWarnings", "0");
                    metricsMap.put("totalParsedInfos", "0");
                    metricsMap.put("parsedErrorLinkFile", "");
                    metricsMap.put("parsedWarningLinkFile", "");
                    metricsMap.put("parsedInfoLinkFile", "");
                }
                Log("Parsed Console metrics are successfully collected for " + job.getDisplayName());
            } catch (RuntimeException  e) {
                metricsMap.put("totalParsedErrors", "0");
                    metricsMap.put("totalParsedWarnings", "0");
                    metricsMap.put("totalParsedInfos", "0");
                    metricsMap.put("parsedErrorLinkFile", "");
                    metricsMap.put("parsedWarningLinkFile", "");
                    metricsMap.put("parsedInfoLinkFile", "");
                Log("Parsed Console Output is not configured for this job : " + job.getDisplayName(),e);
            }

            try {
                JacocoBuildAction jacocoAction = job.getLastSuccessfulBuild().getAction(JacocoBuildAction.class);
                if (jacocoAction != null) {
                    if (null != jacocoAction.getClassCoverage()) {
                        metricsMap.put("classCoverage", String.valueOf(jacocoAction.getClassCoverage().getPercentageFloat()));
                    }
                    if (null != jacocoAction.getLineCoverage()) {
                        metricsMap.put("lineCoverage", String.valueOf(jacocoAction.getLineCoverage().getPercentageFloat()));
                    }
                    if (null != jacocoAction.getMethodCoverage()) {
                        metricsMap.put("methodCoverage", String.valueOf(jacocoAction.getMethodCoverage().getPercentageFloat()));
                    }
                    if (null != jacocoAction.getBranchCoverage()) {
                        metricsMap.put("branchCoverage", String.valueOf(jacocoAction.getBranchCoverage().getPercentageFloat()));
                    }
                    if (null != jacocoAction.getInstructionCoverage()) {
                        metricsMap.put("instructionCoverage", String.valueOf(jacocoAction.getInstructionCoverage().getPercentageFloat()));
                    }
                    if (null != jacocoAction.getComplexityScore()) {
                        metricsMap.put("complexityScore", String.valueOf(jacocoAction.getComplexityScore().getPercentageFloat()));
                    }
                } else {
                    metricsMap.put("lineCoverage", "0");
                    metricsMap.put("methodCoverage", "0");
                    metricsMap.put("classCoverage", "0");
                    metricsMap.put("branchCoverage", "0");
                    metricsMap.put("instructionCoverage", "0");
                    metricsMap.put("complexityScore", "0");
                }
                Log("JaCoCo Code Coverage Metrics are successfully collected for " + job.getDisplayName());
            } catch (RuntimeException  e) {
                metricsMap.put("lineCoverage", "0");
                    metricsMap.put("methodCoverage", "0");
                    metricsMap.put("classCoverage", "0");
                    metricsMap.put("branchCoverage", "0");
                    metricsMap.put("instructionCoverage", "0");
                    metricsMap.put("complexityScore", "0");
                    Log("JaCoCo Code Coverage Metrics are not configured for this job: " + job.getDisplayName(),e);
            }

            try {
                Log("GIT Project : " + gitProjectPath);
                GerritRestApiFactory gerritRestApiFactory = new GerritRestApiFactory();
                GerritAuthData.Basic authData = new GerritAuthData.Basic(Messages.GerritServerUrl(), Messages.GerritUserName(), Messages.GerritPassword());
                GerritApi gerritApi = gerritRestApiFactory.create(authData);
                List<ChangeInfo> changes = gerritApi.changes().query(gitProjectPath).get();
                Log("Count " + changes.size());
               // List<ChangeInfo> projectChanges = new ArrayList<ChangeInfo>();
                List<ChangeInfo> newStatusForProject = new ArrayList<ChangeInfo>();
                List<ChangeInfo> mergedStatusForProject = new ArrayList<ChangeInfo>();
                List<ChangeInfo> draftStatusForProject = new ArrayList<ChangeInfo>();
                List<ChangeInfo> submittedStatusForProject = new ArrayList<ChangeInfo>();
                List<ChangeInfo> abandonedStatusForProject = new ArrayList<ChangeInfo>();
                List<ChangeInfo> otherStatusForProject = new ArrayList<ChangeInfo>();
                List<ChangeInfo> sameReviewers = new ArrayList<ChangeInfo>();
                List<ChangeInfo> noReviewers = new ArrayList<ChangeInfo>();

                for (ChangeInfo change : changes) {
                    if (change.reviewers != null) {
                        Log("Reviewers " + change.reviewers.size());
                        Log("Is Owner the reviewer ? " + change.reviewers.containsKey(change.owner.name));
                        if (change.reviewers.size() == 1 && change.reviewers.containsKey(change.owner.name)) {
                            sameReviewers.add(change);
                        }
                    } else {
                        noReviewers.add(change);
                    }
                    switch (change.status.toString()) {
                        case "NEW":
                            newStatusForProject.add(change);
                            break;
                        case "MERGED":
                            mergedStatusForProject.add(change);
                            break;
                        case "DRAFT":
                            draftStatusForProject.add(change);
                            break;
                        case "SUBMITTED":
                            submittedStatusForProject.add(change);
                            break;
                        case "ABANDONED":
                            abandonedStatusForProject.add(change);
                            break;
                        default:
                            otherStatusForProject.add(change);
                            break;
                    }
                }
                metricsMap.put("newCodeReviews", String.valueOf(newStatusForProject.size()));
                metricsMap.put("draftCodeReviews", String.valueOf(draftStatusForProject.size()));
                metricsMap.put("submittedCodeReviews", String.valueOf(submittedStatusForProject.size()));
                metricsMap.put("mergedCodeReviews", String.valueOf(mergedStatusForProject.size()));
                metricsMap.put("abandonedCodeReviews", String.valueOf(abandonedStatusForProject.size()));
            } catch (RuntimeException  e) {
                metricsMap.put("newCodeReviews", "0");
                metricsMap.put("draftCodeReviews", "0");
                metricsMap.put("submittedCodeReviews", "0");
                metricsMap.put("mergedCodeReviews", "0");
                metricsMap.put("abandonedCodeReviews", "0");
                Log("This project is not enabled in Gerrit: " + job.getDisplayName(),e);

            } catch (Exception ex) {
                metricsMap.put("newCodeReviews", "0");
                metricsMap.put("draftCodeReviews", "0");
                metricsMap.put("submittedCodeReviews", "0");
                metricsMap.put("mergedCodeReviews", "0");
                metricsMap.put("abandonedCodeReviews", "0");
                java.util.logging.Logger.getLogger(QualityMetricsLoadData.class.getName()).log(Level.SEVERE, null, ex);
            }

            String[] metricsStr = metrics.split(",");
            for (String metric : metricsStr) {
                if (!metricsMap.containsKey(metric)) {
                    metricsMap.put(metric, "0.0");
                }
            }

            Log("Before Returning the data : " + job.getDisplayName());
            String orgName = job.getDisplayName().substring(0,job.getDisplayName().indexOf("_"));
            return new QualityMetricsResultsSummary(job,orgName,job.getDisplayName(), getProjectOwner(orgName),job.getLastSuccessfulBuild().getTimestampString(), run.getNumber(), Messages.SonarQubeServerUrl(), projectName, gitProjectPath, Messages.GerritServerUrl(), Hudson.getInstance().getUrl(),
                    Float.valueOf(metricsMap.get("ncloc")).longValue(),
                    Float.valueOf(metricsMap.get("files")).intValue(),
                    Float.valueOf(metricsMap.get("functions")).intValue(),
                    Float.valueOf(metricsMap.get("blocker_violations")).intValue(),
                    Float.valueOf(metricsMap.get("critical_violations")).intValue(),
                    Float.valueOf(metricsMap.get("bugs")).intValue(),
                    Float.valueOf(metricsMap.get("vulnerabilities")).intValue(),
                    Float.valueOf(metricsMap.get("code_smells")).intValue(),
                    Float.valueOf(metricsMap.get("complexity")).intValue(),
                    Float.valueOf(metricsMap.get("duplicated_lines_density")).intValue(),
                    Float.valueOf(metricsMap.get("coverage")).intValue(),
                    Float.valueOf(metricsMap.get("highCheckMarx")).intValue(),
                    Float.valueOf(metricsMap.get("mediumCheckMarx")).intValue(),
                    Float.valueOf(metricsMap.get("lowCheckMarx")).intValue(),
                    Float.valueOf(metricsMap.get("unitTestsCount")).intValue(),
                    Float.valueOf(metricsMap.get("unitTestsSuccess")).intValue(),
                    Double.valueOf(metricsMap.get("unitTestsSuccessPct")).doubleValue() * 100,
                    Float.valueOf(metricsMap.get("unitTestsFailed")).intValue(),
                    Double.valueOf(metricsMap.get("unitTestsFailedPct")).doubleValue() * 100,
                    Float.valueOf(metricsMap.get("unitTestsSkipped")).intValue(),
                    Double.valueOf(metricsMap.get("unitTestsSkippedPct")).doubleValue() * 100,
                    metricsMap.get("findBugsWarnings"),
                    metricsMap.get("checkStyleWarnings"),
                    metricsMap.get("pmdWarnings"),
                    metricsMap.get("compilerWarnings"),
                    Integer.parseInt(metricsMap.get("totalWarnings")),
                    Float.valueOf(metricsMap.get("totalParsedErrors")).intValue(),
                    Float.valueOf(metricsMap.get("totalParsedWarnings")).intValue(),
                    Float.valueOf(metricsMap.get("totalParsedInfos")).intValue(),
                    metricsMap.get("parsedErrorLinkFile"),
                    metricsMap.get("parsedWarningLinkFile"),
                    metricsMap.get("parsedInfoLinkFile"),
                    Float.valueOf(metricsMap.get("lineCoverage")).floatValue(),
                    Float.valueOf(metricsMap.get("methodCoverage")).floatValue(),
                    Float.valueOf(metricsMap.get("classCoverage")).floatValue(),
                    Float.valueOf(metricsMap.get("branchCoverage")).floatValue(),
                    Float.valueOf(metricsMap.get("instructionCoverage")).floatValue(),
                    Float.valueOf(metricsMap.get("complexityScore")).floatValue(),
                    Integer.parseInt(metricsMap.get("newCodeReviews")),
                    Integer.parseInt(metricsMap.get("draftCodeReviews")),
                    Integer.parseInt(metricsMap.get("submittedCodeReviews")),
                    Integer.parseInt(metricsMap.get("mergedCodeReviews")),
                    Integer.parseInt(metricsMap.get("abandonedCodeReviews"))
            );
//        } catch (RuntimeException  e) {
//            Log("Error: " + e.getMessage(),e);
//            return new QualityMetricsResultsSummary(job, job.getDisplayName(),
//                    0,
//                    "",
//                    "",
//                    "",
//                    "",
//                    "",
//                    0,
//                    0,
//                    0,
//                    0,
//                    0,
//                    0,
//                    0,
//                    0,
//                    0,
//                    0,
//                    0,
//                    0,
//                    0,
//                    0,
//                    0,
//                    0,
//                    0,
//                    0,
//                    0,
//                    "-",
//                    "-",
//                    "-",
//                    "-",
//                    0,
//                    0,
//                    0,
//                    0,
//                    "",
//                    "",
//                    "",
//                    0,
//                    0,
//                    0,
//                    0,
//                    0,
//                    0,
//                    0,
//                    0,
//                    0,
//                    0,
//                    0
//            );
//        }
    }

    
       /**
     * Gets the Quality Metrics for each project.
     *
     * @param job the Job object
     * @param sonarBaseURL the SonarQube base URL to get the metrics.
     * @param projectName the SonarQube project id to get the metrics
     * @return QualityMetricsResultsSummary the result summary for the sonar
     * project
     */
    public static QualityMetricsResultsSummary getQualityMetrics(Job job, Run run, String sonarBaseURL, String projectName, GerritApi gerritApi) throws IOException {
        Log("SonarBaseURL : " + sonarBaseURL + "Project Name : " + projectName);
        String metrics = Messages.SonarQubeMetricsList();
        QualityMetricsGrid s = new QualityMetricsGrid(Messages.SonarQubeTempPortletName());
        
        Map<String, String> metricsMap = new HashMap<String, String>();
        //try {
            String url = sonarBaseURL + "/resources?resource=" + projectName + "&metrics=" + metrics;
            Log("URL : " + url);
            try {
                metricsMap = readJsonFromUrl(url, metricsMap);
            } catch (RuntimeException e) {
                Log("SonarQube is not configured for this job : " + job.getDisplayName(),e);
            } catch (JSONException ex) {
                java.util.logging.Logger.getLogger(QualityMetricsLoadData.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                CxScanResult res = run.getAction(CxScanResult.class);
                if (res != null) {
                    metricsMap.put("highCheckMarx", String.valueOf(res.getHighCount()));
                    metricsMap.put("mediumCheckMarx", String.valueOf(res.getMediumCount()));
                    metricsMap.put("lowCheckMarx", String.valueOf(res.getLowCount()));
                } else {
                    metricsMap.put("highCheckMarx", "0.0");
                    metricsMap.put("mediumCheckMarx", "0.0");
                    metricsMap.put("lowCheckMarx", "0.0");
                }
                Log("CheckMarx metrics are successfully collected for " + job.getDisplayName() + " High : " + metricsMap.get("highCheckMarx"));
            } catch (RuntimeException e) {
                metricsMap.put("highCheckMarx", "0.0");
                    metricsMap.put("mediumCheckMarx", "0.0");
                    metricsMap.put("lowCheckMarx", "0.0");
                Log("CheckMarx is not configured for this job : " + job.getDisplayName(),e);
            }

            try {
                TestResult testResults = TestUtil.getTestResult(run);
                if (testResults != null) {
                    metricsMap.put("unitTestsCount", String.valueOf(testResults.getTests()));
                    metricsMap.put("unitTestsSuccess", String.valueOf(testResults.getSuccess()));
                    metricsMap.put("unitTestsSuccessPct", String.valueOf(testResults.getSuccessPct()));
                    metricsMap.put("unitTestsFailed", String.valueOf(testResults.getFailed()));
                    metricsMap.put("unitTestsFailedPct", String.valueOf(testResults.getFailedPct()));
                    metricsMap.put("unitTestsSkipped", String.valueOf(testResults.getSkipped()));
                    metricsMap.put("unitTestsSkippedPct", String.valueOf(testResults.getSkippedPct()));
                } else {
                    metricsMap.put("unitTestsCount", "0.0");
                    metricsMap.put("unitTestsSuccess", "0.0");
                    metricsMap.put("unitTestsSuccessPct", "0.0");
                    metricsMap.put("unitTestsFailed", "0.0");
                    metricsMap.put("unitTestsFailedPct", "0.0");
                    metricsMap.put("unitTestsSkipped", "0.0");
                    metricsMap.put("unitTestsSkippedPct", "0.0");
                }
                Log("Unit Test metrics are successfully collected for " + job.getDisplayName() + " unitTestsCount : " + metricsMap.get("unitTestsCount"));
            } catch (RuntimeException  e) {
                metricsMap.put("unitTestsCount", "0.0");
                    metricsMap.put("unitTestsSuccess", "0.0");
                    metricsMap.put("unitTestsSuccessPct", "0.0");
                    metricsMap.put("unitTestsFailed", "0.0");
                    metricsMap.put("unitTestsFailedPct", "0.0");
                    metricsMap.put("unitTestsSkipped", "0.0");
                    metricsMap.put("unitTestsSkippedPct", "0.0");
                Log("Unit Test Results are not available for this job : " + job.getDisplayName(),e);
            }

            try {
                if (s.warningsAggregator != null) {
                    metricsMap.put("findBugsWarnings", s.warningsAggregator.getFindBugs(job));
                    metricsMap.put("checkStyleWarnings", s.warningsAggregator.getCheckStyle(job));
                    metricsMap.put("pmdWarnings", s.warningsAggregator.getPmd(job));
                    metricsMap.put("compilerWarnings", s.warningsAggregator.getCompilerWarnings(job));
                    metricsMap.put("totalWarnings", s.warningsAggregator.getTotal(job));
                } else {
                    metricsMap.put("findBugsWarnings", "-");
                    metricsMap.put("checkStyleWarnings", "-");
                    metricsMap.put("pmdWarnings", "-");
                    metricsMap.put("compilerWarnings", "-");
                    metricsMap.put("totalWarnings", "0");
                }
                Log("Warnings metrics are successfully collected for " + job.getDisplayName());
            } catch (RuntimeException  e) {
                metricsMap.put("findBugsWarnings", "-");
                    metricsMap.put("checkStyleWarnings", "-");
                    metricsMap.put("pmdWarnings", "-");
                    metricsMap.put("compilerWarnings", "-");
                    metricsMap.put("totalWarnings", "0");
                Log("Warnings plugin is not configured for this job : " + job.getDisplayName(),e);
            }

            try {
                LogParserAction logParser = job.getLastBuild().getAction(LogParserAction.class);
                if (logParser != null) {
                    metricsMap.put("totalParsedErrors", String.valueOf(logParser.getResult().getTotalErrors()));
                    metricsMap.put("totalParsedWarnings", String.valueOf(logParser.getResult().getTotalWarnings()));
                    metricsMap.put("totalParsedInfos", String.valueOf(logParser.getResult().getTotalInfos()));
                    metricsMap.put("parsedErrorLinkFile", logParser.getResult().getErrorLinksFile());
                    metricsMap.put("parsedWarningLinkFile", logParser.getResult().getWarningLinksFile());
                    metricsMap.put("parsedInfoLinkFile", logParser.getResult().getInfoLinksFile());
                } else {
                    metricsMap.put("totalParsedErrors", "0");
                    metricsMap.put("totalParsedWarnings", "0");
                    metricsMap.put("totalParsedInfos", "0");
                    metricsMap.put("parsedErrorLinkFile", "");
                    metricsMap.put("parsedWarningLinkFile", "");
                    metricsMap.put("parsedInfoLinkFile", "");
                }
                Log("Parsed Console metrics are successfully collected for " + job.getDisplayName());
            } catch (RuntimeException  e) {
                metricsMap.put("totalParsedErrors", "0");
                    metricsMap.put("totalParsedWarnings", "0");
                    metricsMap.put("totalParsedInfos", "0");
                    metricsMap.put("parsedErrorLinkFile", "");
                    metricsMap.put("parsedWarningLinkFile", "");
                    metricsMap.put("parsedInfoLinkFile", "");
                Log("Parsed Console Output is not configured for this job : " + job.getDisplayName(),e);
            }

            try {
                JacocoBuildAction jacocoAction = job.getLastSuccessfulBuild().getAction(JacocoBuildAction.class);
                if (jacocoAction != null) {
                    if (null != jacocoAction.getClassCoverage()) {
                        metricsMap.put("classCoverage", String.valueOf(jacocoAction.getClassCoverage().getPercentageFloat()));
                    }
                    if (null != jacocoAction.getLineCoverage()) {
                        metricsMap.put("lineCoverage", String.valueOf(jacocoAction.getLineCoverage().getPercentageFloat()));
                    }
                    if (null != jacocoAction.getMethodCoverage()) {
                        metricsMap.put("methodCoverage", String.valueOf(jacocoAction.getMethodCoverage().getPercentageFloat()));
                    }
                    if (null != jacocoAction.getBranchCoverage()) {
                        metricsMap.put("branchCoverage", String.valueOf(jacocoAction.getBranchCoverage().getPercentageFloat()));
                    }
                    if (null != jacocoAction.getInstructionCoverage()) {
                        metricsMap.put("instructionCoverage", String.valueOf(jacocoAction.getInstructionCoverage().getPercentageFloat()));
                    }
                    if (null != jacocoAction.getComplexityScore()) {
                        metricsMap.put("complexityScore", String.valueOf(jacocoAction.getComplexityScore().getPercentageFloat()));
                    }
                } else {
                    metricsMap.put("lineCoverage", "0");
                    metricsMap.put("methodCoverage", "0");
                    metricsMap.put("classCoverage", "0");
                    metricsMap.put("branchCoverage", "0");
                    metricsMap.put("instructionCoverage", "0");
                    metricsMap.put("complexityScore", "0");
                }
                Log("JaCoCo Code Coverage Metrics are successfully collected for " + job.getDisplayName());
            } catch (RuntimeException  e) {
                metricsMap.put("lineCoverage", "0");
                    metricsMap.put("methodCoverage", "0");
                    metricsMap.put("classCoverage", "0");
                    metricsMap.put("branchCoverage", "0");
                    metricsMap.put("instructionCoverage", "0");
                    metricsMap.put("complexityScore", "0");
                    Log("JaCoCo Code Coverage Metrics are not configured for this job: " + job.getDisplayName(),e);
            }

            try {
                Log("GIT Project : " + gitProjectPath);

                List<ChangeInfo> changes = gerritApi.changes().query(gitProjectPath).get();
                Log("Count " + changes.size());
               // List<ChangeInfo> projectChanges = new ArrayList<ChangeInfo>();
                List<ChangeInfo> newStatusForProject = new ArrayList<ChangeInfo>();
                List<ChangeInfo> mergedStatusForProject = new ArrayList<ChangeInfo>();
                List<ChangeInfo> draftStatusForProject = new ArrayList<ChangeInfo>();
                List<ChangeInfo> submittedStatusForProject = new ArrayList<ChangeInfo>();
                List<ChangeInfo> abandonedStatusForProject = new ArrayList<ChangeInfo>();
                List<ChangeInfo> otherStatusForProject = new ArrayList<ChangeInfo>();
                List<ChangeInfo> sameReviewers = new ArrayList<ChangeInfo>();
                List<ChangeInfo> noReviewers = new ArrayList<ChangeInfo>();

                for (ChangeInfo change : changes) {
                    if (change.reviewers != null) {
                        Log("Reviewers " + change.reviewers.size());
                        Log("Is Owner the reviewer ? " + change.reviewers.containsKey(change.owner.name));
                        if (change.reviewers.size() == 1 && change.reviewers.containsKey(change.owner.name)) {
                            sameReviewers.add(change);
                        }
                    } else {
                        noReviewers.add(change);
                    }
                    switch (change.status.toString()) {
                        case "NEW":
                            newStatusForProject.add(change);
                            break;
                        case "MERGED":
                            mergedStatusForProject.add(change);
                            break;
                        case "DRAFT":
                            draftStatusForProject.add(change);
                            break;
                        case "SUBMITTED":
                            submittedStatusForProject.add(change);
                            break;
                        case "ABANDONED":
                            abandonedStatusForProject.add(change);
                            break;
                        default:
                            otherStatusForProject.add(change);
                            break;
                    }
                }
                metricsMap.put("newCodeReviews", String.valueOf(newStatusForProject.size()));
                metricsMap.put("draftCodeReviews", String.valueOf(draftStatusForProject.size()));
                metricsMap.put("submittedCodeReviews", String.valueOf(submittedStatusForProject.size()));
                metricsMap.put("mergedCodeReviews", String.valueOf(mergedStatusForProject.size()));
                metricsMap.put("abandonedCodeReviews", String.valueOf(abandonedStatusForProject.size()));
            } catch (RuntimeException  e) {
                metricsMap.put("newCodeReviews", "0");
                metricsMap.put("draftCodeReviews", "0");
                metricsMap.put("submittedCodeReviews", "0");
                metricsMap.put("mergedCodeReviews", "0");
                metricsMap.put("abandonedCodeReviews", "0");
                Log("This project is not enabled in Gerrit: " + job.getDisplayName(),e);

            } catch (RestApiException ex) {
                metricsMap.put("newCodeReviews", "0");
                metricsMap.put("draftCodeReviews", "0");
                metricsMap.put("submittedCodeReviews", "0");
                metricsMap.put("mergedCodeReviews", "0");
                metricsMap.put("abandonedCodeReviews", "0");
                java.util.logging.Logger.getLogger(QualityMetricsLoadData.class.getName()).log(Level.SEVERE, null, ex);
            }

            String[] metricsStr = metrics.split(",");
            for (String metric : metricsStr) {
                if (!metricsMap.containsKey(metric)) {
                    metricsMap.put(metric, "0.0");
                }
            }

            Log("Before Returning the data : " + job.getDisplayName());
            String orgName = job.getDisplayName().substring(0,job.getDisplayName().indexOf("_"));
            return new QualityMetricsResultsSummary(job,orgName,job.getDisplayName(), getProjectOwner(orgName),job.getLastSuccessfulBuild().getTimestampString(), run.getNumber(), Messages.SonarQubeServerUrl(), projectName, gitProjectPath, Messages.GerritServerUrl(), Hudson.getInstance().getUrl(),
                    Float.valueOf(metricsMap.get("ncloc")).longValue(),
                    Float.valueOf(metricsMap.get("files")).intValue(),
                    Float.valueOf(metricsMap.get("functions")).intValue(),
                    Float.valueOf(metricsMap.get("blocker_violations")).intValue(),
                    Float.valueOf(metricsMap.get("critical_violations")).intValue(),
                    Float.valueOf(metricsMap.get("bugs")).intValue(),
                    Float.valueOf(metricsMap.get("vulnerabilities")).intValue(),
                    Float.valueOf(metricsMap.get("code_smells")).intValue(),
                    Float.valueOf(metricsMap.get("complexity")).intValue(),
                    Float.valueOf(metricsMap.get("duplicated_lines_density")).intValue(),
                    Float.valueOf(metricsMap.get("coverage")).intValue(),
                    Float.valueOf(metricsMap.get("highCheckMarx")).intValue(),
                    Float.valueOf(metricsMap.get("mediumCheckMarx")).intValue(),
                    Float.valueOf(metricsMap.get("lowCheckMarx")).intValue(),
                    Float.valueOf(metricsMap.get("unitTestsCount")).intValue(),
                    Float.valueOf(metricsMap.get("unitTestsSuccess")).intValue(),
                    Double.valueOf(metricsMap.get("unitTestsSuccessPct")).doubleValue() * 100,
                    Float.valueOf(metricsMap.get("unitTestsFailed")).intValue(),
                    Double.valueOf(metricsMap.get("unitTestsFailedPct")).doubleValue() * 100,
                    Float.valueOf(metricsMap.get("unitTestsSkipped")).intValue(),
                    Double.valueOf(metricsMap.get("unitTestsSkippedPct")).doubleValue() * 100,
                    metricsMap.get("findBugsWarnings"),
                    metricsMap.get("checkStyleWarnings"),
                    metricsMap.get("pmdWarnings"),
                    metricsMap.get("compilerWarnings"),
                    Integer.parseInt(metricsMap.get("totalWarnings")),
                    Float.valueOf(metricsMap.get("totalParsedErrors")).intValue(),
                    Float.valueOf(metricsMap.get("totalParsedWarnings")).intValue(),
                    Float.valueOf(metricsMap.get("totalParsedInfos")).intValue(),
                    metricsMap.get("parsedErrorLinkFile"),
                    metricsMap.get("parsedWarningLinkFile"),
                    metricsMap.get("parsedInfoLinkFile"),
                    Float.valueOf(metricsMap.get("lineCoverage")).floatValue(),
                    Float.valueOf(metricsMap.get("methodCoverage")).floatValue(),
                    Float.valueOf(metricsMap.get("classCoverage")).floatValue(),
                    Float.valueOf(metricsMap.get("branchCoverage")).floatValue(),
                    Float.valueOf(metricsMap.get("instructionCoverage")).floatValue(),
                    Float.valueOf(metricsMap.get("complexityScore")).floatValue(),
                    Integer.parseInt(metricsMap.get("newCodeReviews")),
                    Integer.parseInt(metricsMap.get("draftCodeReviews")),
                    Integer.parseInt(metricsMap.get("submittedCodeReviews")),
                    Integer.parseInt(metricsMap.get("mergedCodeReviews")),
                    Integer.parseInt(metricsMap.get("abandonedCodeReviews"))
            );
//        } catch (RuntimeException  e) {
//            Log("Error: " + e.getMessage(),e);
//            return new QualityMetricsResultsSummary(job, job.getDisplayName(),
//                    0,
//                    "",
//                    "",
//                    "",
//                    "",
//                    "",
//                    0,
//                    0,
//                    0,
//                    0,
//                    0,
//                    0,
//                    0,
//                    0,
//                    0,
//                    0,
//                    0,
//                    0,
//                    0,
//                    0,
//                    0,
//                    0,
//                    0,
//                    0,
//                    0,
//                    "-",
//                    "-",
//                    "-",
//                    "-",
//                    0,
//                    0,
//                    0,
//                    0,
//                    "",
//                    "",
//                    "",
//                    0,
//                    0,
//                    0,
//                    0,
//                    0,
//                    0,
//                    0,
//                    0,
//                    0,
//                    0,
//                    0
//            );
//        }
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
        Log("Before InputStream " + url);
        InputStream is = new URL(url).openStream();
        Log("InputStream " + is.available());
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            Log("Result Lenghth : " + jsonText.length());
            JSONArray rootArray = new JSONArray(jsonText);
            Log("rootArray count " + rootArray.length());
            for (int i = 0; i < rootArray.length(); i++) {
                JSONObject jOb = (JSONObject) rootArray.getJSONObject(i);
                Log("JSON Object " + jOb.toString());
                try {
                    JSONArray jArray = jOb.getJSONArray("msr");
                    for (int j = 0; j < jArray.length(); j++) {
                        JSONObject jObj = jArray.getJSONObject(j);
                        metricsMap.put(jObj.get("key").toString(), jObj.get("val").toString());
                    }
                } catch (JSONException e) {
                    Log("SonarQube is not configured for this job");
                }
            }
            return metricsMap;
        } finally {
            is.close();
        }
    }
private static void Log(String msg){
        if (Messages.LogFlag().equalsIgnoreCase("true")) {
            topLogger.info(msg);
        }
    }
private static void Log(String msg,Exception e){
        if (Messages.LogFlag().equalsIgnoreCase("true")) {
            topLogger.info(msg,e);
        }
    }

       /**
     * Summarize the last coverage results of all jobs. If a job doesn't include
     * any coverage, add zero.
     *
     * @param jobs a final Collection of Job objects
     * @return QualityMetricsResultsSummary the result summary
     */
    public static QualityMetricsResultsSummary getResultSummaryForAllJobs(List<Job> listJobs) throws IOException {
        String sonarBaseURL = Messages.SonarQubeServerUrl();
        QualityMetricsResultsSummary summary = new QualityMetricsResultsSummary();
        
        QualityMetricsResultsSummary.metricsResultsForAllJobs.clear();
        
        for (Job<?, ?> job : listJobs) {
            Log("AllJob Name : " + job.getDisplayName());
            
                    
            Run<?, ?> run = job.getLastSuccessfulBuild();
            String projectName = "";
            if (run != null) {
                
                try {
                    String props = job.getConfigFile().asString();
                    //Log("props contains- UserRemoteConfig:"+props.contains("UserRemoteConfig"));
                    if (props.contains("UserRemoteConfig")) {
                        String tempString = props.substring(props.indexOf("<string>projectPath</string>") + ("<string>projectPath</string>").length(), props.length());//.replaceAll("</url>","").trim();
                        if (tempString.contains("</entry>")) {
                            gitProjectPath = tempString.substring(0, tempString.indexOf("</entry>")).replaceAll("<string>", "").replaceAll("</string>", "").trim();
                        }
                        
                    } else {
                        gitProjectPath = "platform/pdsp-wrapper";
                    }
                    //Log("gitProjectPath : "+gitProjectPath);
                    
                    //Log("props contains sonarProjectKey: "+props.contains("sonarProjectKey"));
                    if (props.contains("sonarProjectKey")) {
                        String tempString = props.substring(props.indexOf("<string>sonarProjectKey</string>") + ("<string>sonarProjectKey</string>").length(), props.length());
                        projectName = tempString.substring(0, tempString.indexOf("</entry>")).replaceAll("<string>", "").replaceAll("</string>", "").trim();
                        Log("addMetricsResultForAllJobs Job Name : "+job.getName());
                        if (job.getName().contains("_sanity")) {
                            summary.addMetricsResultForAllJobs(getQualityMetrics(job, run, sonarBaseURL, projectName));
                        }
                        
                    } else {
                        topLogger.info("SonarQube is not configured for this job: " + job.getDisplayName());
                    }
                    //Log("projectName "+projectName);
                    props = "";
                } catch (RuntimeException e) {
                  topLogger.error("Got unexpected exception", e);
                } catch (Exception e) {
                  topLogger.error("Got unexpected exception", e);
                }
            }
        }
        return summary;
    }
}
