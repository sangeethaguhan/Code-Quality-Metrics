/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.metrics.gating;

import com.checkmarx.jenkins.CxScanResult;
import hudson.Extension;
import hudson.model.Hudson;
import hudson.model.Job;
import hudson.model.RootAction;
import hudson.model.Run;
import hudson.plugins.jacoco.JacocoBuildAction;
import hudson.plugins.logparser.LogParserAction;
import hudson.plugins.view.dashboard.test.TestResult;
import hudson.plugins.view.dashboard.test.TestUtil;
import hudson.util.FormFieldValidator;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.servlet.ServletException;
import jenkins.model.Jenkins;
import org.apache.log4j.Logger;
import org.jenkinsci.plugins.metrics.bean.QualityMetricsResultsSummary;
import org.jenkinsci.plugins.metrics.portlet.QualityMetricsLoadData;
import static org.jenkinsci.plugins.metrics.portlet.QualityMetricsLoadData.readJsonFromUrl;
import org.jenkinsci.plugins.sonarmetrics.Messages;
import org.json.JSONException;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.interceptor.RequirePOST;


@ExportedBean
@Extension
public class QualityGate implements RootAction, StaplerProxy {
    
    private static Logger topLogger = Logger.getLogger("All Jenkins Logs");
    private static String gitProjectPath = "";
    public static String emailRecepients = "saldin.samuel@247-inc.com";
    public QualityGate() {
        super();
        //listRessources = IdAllocator.getListRessources();
    }

    public Object getTarget() {
        //Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);
        return this;
    }

    @Deprecated
    public void load() {
        getList();
    }


     /**
     * This method will be called by portlet.jelly to load data and create the
     * grid.
     *
     * @param jobs a Collection of Job objects
     * @return QualityMetricsResultsSummary a coverage result summary
     */
    public QualityMetricsResultsSummary getQualityMetricsResultsSummary() throws IOException {
        Jenkins jenkins=Jenkins.getInstance();
        List<Job> jobs=jenkins.getAllItems(Job.class);
        Iterator<Job> iter = jobs.iterator();
        while (iter.hasNext()) {
            Job job = iter.next();
            if (!job.getDisplayName().contains("_commit")){
                iter.remove();
            }
          }
        return getResultSummary(jobs);
    }
    
    /**
     * Summarize the last coverage results of all jobs. If a job doesn't include
     * any coverage, add zero.
     *
     * @param jobs a final Collection of Job objects
     * @return QualityMetricsResultsSummary the result summary
     */
    public static QualityMetricsResultsSummary getResultSummary(List<Job> listJobs) throws IOException {

        String sonarBaseURL = Messages.SonarQubeServerUrl() + "/api";
        QualityMetricsResultsSummary summary = new QualityMetricsResultsSummary();

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
                        summary.addMetricsResult(getQualityMetrics(job, run, sonarBaseURL, projectName));
                    } else {
                        Log("SonarQube is not configured for this job: " + job.getDisplayName());
                    }
                    props = "";
                } catch (FileNotFoundException e) {
                  topLogger.error("Got unexpected FileNotFoundException", e);
                } catch (RuntimeException e) {
                  topLogger.error("Got unexpected RuntimeException", e);
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
       
        //SonarQube 
        
        Log("SonarBaseURL : " + sonarBaseURL + "Project Name : " + projectName);
        String metrics = Messages.SonarQubeMetricsList();
       // String orgName = job.getDisplayName().substring(0,job.getDisplayName().indexOf("_"));
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

            //Checkmarx 
            
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

            //Unit Test Results 
            
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
            
            //Warnings Plugin

            try {
                    metricsMap.put("findBugsWarnings", "-");
                    metricsMap.put("checkStyleWarnings", "-");
                    metricsMap.put("pmdWarnings", "-");
                    metricsMap.put("compilerWarnings", "-");
                    metricsMap.put("totalWarnings", "0");
                    Log("Warnings metrics are successfully collected for " + job.getDisplayName());
            } catch (RuntimeException  e) {
                metricsMap.put("findBugsWarnings", "-");
                    metricsMap.put("checkStyleWarnings", "-");
                    metricsMap.put("pmdWarnings", "-");
                    metricsMap.put("compilerWarnings", "-");
                    metricsMap.put("totalWarnings", "0");
                Log("Warnings plugin is not configured for this job : " + job.getDisplayName(),e);
            }

            
            //Parsed console metric
            
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
            
            //JaCoCo code coverage

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
            
            //Gerrit
            
            try {
                metricsMap.put("newCodeReviews", "0");
                metricsMap.put("draftCodeReviews", "0");
                metricsMap.put("submittedCodeReviews", "0");
                metricsMap.put("mergedCodeReviews", "0");
                metricsMap.put("abandonedCodeReviews", "0");
            
//                Log("GIT Project : " + gitProjectPath);
//                GerritRestApiFactory gerritRestApiFactory = new GerritRestApiFactory();
//                GerritAuthData.Basic authData = new GerritAuthData.Basic(Messages.GerritServerUrl(), Messages.GerritUserName(), Messages.GerritPassword());
//                GerritApi gerritApi = gerritRestApiFactory.create(authData);
//                List<ChangeInfo> changes = gerritApi.changes().query(gitProjectPath).get();
//                Log("Count " + changes.size());
//               // List<ChangeInfo> projectChanges = new ArrayList<ChangeInfo>();
//                List<ChangeInfo> newStatusForProject = new ArrayList<ChangeInfo>();
//                List<ChangeInfo> mergedStatusForProject = new ArrayList<ChangeInfo>();
//                List<ChangeInfo> draftStatusForProject = new ArrayList<ChangeInfo>();
//                List<ChangeInfo> submittedStatusForProject = new ArrayList<ChangeInfo>();
//                List<ChangeInfo> abandonedStatusForProject = new ArrayList<ChangeInfo>();
//                List<ChangeInfo> otherStatusForProject = new ArrayList<ChangeInfo>();
//                List<ChangeInfo> sameReviewers = new ArrayList<ChangeInfo>();
//                List<ChangeInfo> noReviewers = new ArrayList<ChangeInfo>();
//
//                for (ChangeInfo change : changes) {
//                    if (change.reviewers != null) {
//                        Log("Reviewers " + change.reviewers.size());
//                        Log("Is Owner the reviewer ? " + change.reviewers.containsKey(change.owner.name));
//                        if (change.reviewers.size() == 1 && change.reviewers.containsKey(change.owner.name)) {
//                            sameReviewers.add(change);
//                        }
//                    } else {
//                        noReviewers.add(change);
//                    }
//                    switch (change.status.toString()) {
//                        case "NEW":
//                            newStatusForProject.add(change);
//                            break;
//                        case "MERGED":
//                            mergedStatusForProject.add(change);
//                            break;
//                        case "DRAFT":
//                            draftStatusForProject.add(change);
//                            break;
//                        case "SUBMITTED":
//                            submittedStatusForProject.add(change);
//                            break;
//                        case "ABANDONED":
//                            abandonedStatusForProject.add(change);
//                            break;
//                        default:
//                            otherStatusForProject.add(change);
//                            break;
//                    }
//                }
//                metricsMap.put("newCodeReviews", String.valueOf(newStatusForProject.size()));
//                metricsMap.put("draftCodeReviews", String.valueOf(draftStatusForProject.size()));
//                metricsMap.put("submittedCodeReviews", String.valueOf(submittedStatusForProject.size()));
//                metricsMap.put("mergedCodeReviews", String.valueOf(mergedStatusForProject.size()));
//                metricsMap.put("abandonedCodeReviews", String.valueOf(abandonedStatusForProject.size()));
            } catch (RuntimeException  e) {
                metricsMap.put("newCodeReviews", "0");
                metricsMap.put("draftCodeReviews", "0");
                metricsMap.put("submittedCodeReviews", "0");
                metricsMap.put("mergedCodeReviews", "0");
                metricsMap.put("abandonedCodeReviews", "0");
                Log("This project is not enabled in Gerrit: " + job.getDisplayName(),e);

//            } catch (RestApiException ex) {
//                metricsMap.put("newCodeReviews", "0");
//                metricsMap.put("draftCodeReviews", "0");
//                metricsMap.put("submittedCodeReviews", "0");
//                metricsMap.put("mergedCodeReviews", "0");
//                metricsMap.put("abandonedCodeReviews", "0");
//                java.util.logging.Logger.getLogger(QualityMetricsLoadData.class.getName()).log(Level.SEVERE, null, ex);
            }

            String[] metricsStr = metrics.split(",");
            for (String metric : metricsStr) {
                if (!metricsMap.containsKey(metric)) {
                    metricsMap.put(metric, "0.0");
                }
            }

            Log("Before Returning the data : " + job.getDisplayName());
             String orgName = job.getDisplayName().substring(0,job.getDisplayName().indexOf("_"));
             
            return new QualityMetricsResultsSummary(job, orgName,job.getDisplayName(), getProjectOwner(orgName),job.getLastSuccessfulBuild().getTimestampString(), run.getNumber(), Messages.SonarQubeServerUrl(), projectName, gitProjectPath, Messages.GerritServerUrl(), Hudson.getInstance().getUrl(),
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

    }

    
    // List of declared resources over all Jobs (Oleksandr Kulychok: leave as is for now)
    @Restricted(NoExternalUse.class) // Exported for view
    // TODO why does this update stuff?
    public List<Job> getList() {
        return Jenkins.getInstance().getItems(Job.class);
       
    }
    
    public static String getProjectOwner(String orgName){
        switch (orgName.trim()) {
            case "big-data":
                return Messages.big_data_owner();
            case "enterprise-mgmt":
                return Messages.enterprise_mgmt_owner();
            case "Assist-PE":
                return Messages.assist_pe_owner();
            case "oe-solutions":
                return Messages.oe_solutions_owner();
            case "platform":
                return Messages.platform_owner();
            case "java-commons":
                return Messages.java_commons_owner();
            case "advancedprototypes":
                 return Messages.advancedprototype_owner();
            case "ui":
                 return Messages.ui_owner();
            default:
                return "Sangeetha.Guhan";
        }
        
    }


    @Restricted(NoExternalUse.class) // Exported for view
    public List<Job> getAllocatedResources() {
        
        return Jenkins.getInstance().getItems(Job.class);


    }


    public class AllocatedResource {
        public String resourceId;
        public String runUrl;  // url to 'build', but lets use jenkins core naming in java code
        public String runId;
    }


    //Called when we click on "release " link
    @RequirePOST
    @Restricted(NoExternalUse.class) // Exported for view
    public void doFreeResource(StaplerRequest res, StaplerResponse rsp,
                               @QueryParameter("resourceId") String resourceId,
                               @QueryParameter("runId") String runId
    ) throws IOException, InterruptedException {


    }

    /**
     *
     * @param req
     * @param rsp
     * @param accessId
     * @throws IOException
     * @throws ServletException
     */
    public void doTestConnection(StaplerRequest req, StaplerResponse rsp,
    @QueryParameter("accessId") final String accessId) throws IOException, ServletException {
    new FormFieldValidator(req,rsp,true) {
        protected void check() throws IOException, ServletException {
            try {
                emailRecepients = accessId;
                Emailer.SendQualityGateReport();
                
                
                ok("Success");
            } catch (Exception e) {
                error("Client error : "+e.getMessage());
            }
        }
    }.process();
}

    
    
    public String getIconFileName() {
        return "/plugin/Exclusion/icons/exclusion.png";
    }

    public String getDisplayName() {
        return "Quality Gate";
    }

    public String getUrlName() {
        return "/qualitygate";
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
}
