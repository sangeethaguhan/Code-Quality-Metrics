/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.metrics.bean;

import hudson.model.Job;
import java.util.ArrayList;
import java.util.List;


public class QualityMetricsResultsSummary {

    /**
     * The related job.
     */
    private Job<?, ?> job;
    private String jobUrl;
    private String jobName;
    private String projectOwner;
    private String lastSuccess;

    public String getLastSuccess() {
        return lastSuccess;
    }

    public void setLastSuccess(String lastSuccess) {
        this.lastSuccess = lastSuccess;
    }

    public String getProjectOwner() {
        return projectOwner;
    }

    public void setProjectOwner(String projectOwner) {
        this.projectOwner = projectOwner;
    }
    
    private String sonarQubeUrl;
    private String gerritUrl;
    private String gitUrl;
    private String sonarProjectKey;
    private int buildNumber;
    private String gitProjectPath;
    private String currentUrl;
    private String jenkinsJobName;
    private String gitOrgName;


    public static List<QualityMetricsResultsSummary> metricsResultsForAllJobs = new ArrayList<QualityMetricsResultsSummary>();
    
    /**
     * Lines Of Code.
     */
    private long linesOfCode;

    /**
     * Number of Files.
     */
    private int noOfFiles;

    /**
     * No of Functions.
     */
    private int noOfFunctions;

     /**
     * No of Blockers.
     */
    private int blocker;

     /**
     * No of Critical.
     */
    private int critical;

    /**
     * Total Issues.
     */
    private int bugs;
    /**
     * Blocker Issues.
     */
    private int vulnerabilities;

    /**
     * Critical Issues.
     */
    private int codesmells;

    /**
     * Major Issues.
     */
    private int complexity;

    /**
     * Minor Issues.
     */
    private int duplicatedLinesDensity;

    /**
     * Info Issues.
     */
    private int coverage;

    public int getCodesmells() {
        return codesmells;
    }

    public void setCodesmells(int codesmells) {
        this.codesmells = codesmells;
    }

    public int getComplexity() {
        return complexity;
    }

    public void setComplexity(int complexity) {
        this.complexity = complexity;
    }

    public int getDuplicatedLinesDensity() {
        return duplicatedLinesDensity;
    }

    public void setDuplicatedLinesDensity(int duplicatedLinesDensity) {
        this.duplicatedLinesDensity = duplicatedLinesDensity;
    }

    public int getCoverage() {
        return coverage;
    }

    public void setCoverage(int coverage) {
        this.coverage = coverage;
    }

    /**
     * Complexity score (not a percentage).
     */
    private float complexityScore;

    /**
     * High severity CheckMarx issues.
     */
    private long highCheckMarx;

    /**
     * Medium severity CheckMarx issues.
     */
    private int mediumCheckMarx;

    /**
     * Low severity CheckMarx issues.
     */
    private int lowCheckMarx;

    /**
     * Total Unit Tests.
     */
    private int unitTestsCount;

    /**
     * Total Unit Tests Success.
     */
    private int unitTestsSuccess;

    /**
     * Total Unit Tests Skipped.
     */
    private int unitTestsSkipped;

    /**
     * Total Unit Tests Failed.
     */
    private int unitTestsFailed;

    /**
     * Total Unit Tests Success Percentage.
     */
    private double unitTestsSuccessPct;

    /**
     * Total Unit Tests Skipped Percentage.
     */
    private double unitTestsSkippedPct;

    /**
     * Total Unit Tests Failed Percentage.
     */
    private double unitTestsFailedPct;

    /**
     * Total Warnings.
     */
    private int totalWarnings;

    /**
     * Total Compiler Warnings.
     */
    private String compilerWarnings;

    /**
     * Total FindBugs Warnings.
     */
    private String findBugsWarnings;

    /**
     * Total Compiler Warnings.
     */
    private String checkStyleWarnings;

    /**
     * Total PMD Warnings.
     */
    private String pmdWarnings;

    /**
     * Total Parsed Errors
     */
    private int totalParsedErrors = 0;
    
    
    /**
     * Total Parsed Warnings.
     */
    private int totalParsedWarnings = 0;

    /**
     * Total Parsed Info.
     */
    private int totalParsedInfos = 0;
    /**
     * Total Parsed Error Link File.
     */
    private String parsedErrorLinkFile;
    /**
     * Total Parsed Warning Link File.
     */
    private String parsedWarningLinkFile;
    /**
     * Total Parsed Info Link File.
     */
    private String parsedInfoLinkFile;

      /**
   * Line coverage percentage.
   */
  private float lineCoverage;

  /**
   * Method coverage percentage.
   */
  private float methodCoverage;

  /**
   * Class coverage percentage.
   */
  private float classCoverage;

  /**
   * Block coverage percentage.
   */
  private float instructionCoverage;

  /**
   * Block coverage percentage.
   */
  private float branchCoverage;
    
   /**
   * Gerrit Code Reviews with Status - NEW per project.
   */
  private float newCodeReviews;
  
  /**
   * Gerrit Code Reviews with Status - DRAFT per project.
   */
  private float draftCodeReviews;
  
  /**
   * Gerrit Code Reviews with Status - SUBMITTED per project.
   */
  private float submittedCodeReviews;
  
  /**
   * Gerrit Code Reviews with Status - MERGED per project.
   */
  private float mergedCodeReviews;
  
  /**
   * Gerrit Code Reviews with Status - ABANDONED per project.
   */
  private float abandonedCodeReviews;
  
    public long getHighCheckMarx() {
        return highCheckMarx;
    }

    public void setHighCheckMarx(long highCheckMarx) {
        this.highCheckMarx = highCheckMarx;
    }

    public int getMediumCheckMarx() {
        return mediumCheckMarx;
    }

    public void setMediumCheckMarx(int mediumCheckMarx) {
        this.mediumCheckMarx = mediumCheckMarx;
    }

    public int getLowCheckMarx() {
        return lowCheckMarx;
    }

    public void setLowCheckMarx(int lowCheckMarx) {
        this.lowCheckMarx = lowCheckMarx;
    }

    private List<QualityMetricsResultsSummary> metricsResults = new ArrayList<QualityMetricsResultsSummary>();

    /**
     * Default Constructor.
     */
    public QualityMetricsResultsSummary() {
    }

    /**
     * Constructor with parameters.
     *
     * @param job the related Job
     * @param jenkinsJobName Jenkins Job Name
     * @param projectOwner Project Owner
     * @param lastSuccess Last Success of the job
     * @param gitOrgName Git Org Name
     * @param buildNumber Jenkins Build Number
     * @param sonarQubeUrl SonarQube Server URL
     * @param sonarProjectKey SonarQube Project Key
     * @param gitProjectPath Git Project Path
     * @param gerritUrl Gerrit URL for the project
     * @param currentUrl Curent Jenkins URL
     * @param linesOfCode Lines of Code in the project
     * @param noOfFiles No of files in the project
     * @param noOfFunctions No of functions in the project
     * @param blocker
     * @param critical
     * @param bugs No of Bugs
     * @param vulnerabilities No of vulnerabilities
     * @param codesmells No of Code Smells
     * @param complexity Percentage of Code Complexity
     * @param duplicatedLinesDensity Percentage of Duplications
     * @param coverage Percentage of Code Coverage
     * @param highCheckMarx CheckMarx issues with High Severity
     * @param mediumCheckMarx CheckMarx issues with Medium Severity
     * @param lowCheckMarx CheckMarx issues with Low Severity
     * @param unitTestsCount Total Unit Tests
     * @param unitTestsSuccess No of tests passed
     * @param unitTestsSuccessPct Percentage of tests passed
     * @param unitTestsFailed No of tests failed
     * @param unitTestsFailedPct Percentage of tests failed
     * @param unitTestsSkipped No of tests Skipped
     * @param unitTestsSkippedPct Percentage of tests skipped
     * @param findBugsWarnings No of FindBugs Warnings
     * @param checkStyleWarnings No of CheckStyle Warnings
     * @param pmdWarnings No of PMD Warnings
     * @param compilerWarnings No of Compiler Warnings
     * @param totalWarnings Total Warnings
     * @param totalParsedErrors Total Parsed Errors displayed in console
     * @param totalParsedWarnings Total Parsed Warnings displayed in console
     * @param totalParsedInfos Total Parsed Infos displayed in console
     * @param parsedErrorLinkFile Link to the Parsed Errors
     * @param parsedWarningLinkFile Link to the Parsed Warnings
     * @param parsedInfoLinkFile Link to the Parsed Infos.
     * @param lineCoverage Percentage of Line Coverage
     * @param methodCoverage Percentage of Method Coverage
     * @param classCoverage Percentage of Class Coverage
     * @param branchCoverage Percentage of Branch Coverage
     * @param instructionCoverage Percentage of Instruction Coverage
     * @param complexityScore Percentage of Complexity Score
     * @param newCodeReviews No of New code reviews
     * @param draftCodeReviews No of Draft code reviews
     * @param submittedCodeReviews No of Submited code reviews
     * @param mergedCodeReviews No of Merged code reviews
     * @param abandonedCodeReviews No of Abandoned code reviews
     */
    public QualityMetricsResultsSummary(Job<?, ?> job, String gitOrgName,String jenkinsJobName, String projectOwner, String lastSuccess, int buildNumber, String sonarQubeUrl, String sonarProjectKey, String gitProjectPath, String gerritUrl, String currentUrl, 
            long linesOfCode, int noOfFiles, int noOfFunctions,
            int blocker, int critical, int bugs, int vulnerabilities, int codesmells, int complexity, int duplicatedLinesDensity, int coverage,
            int highCheckMarx, int mediumCheckMarx, int lowCheckMarx,
            int unitTestsCount, int unitTestsSuccess, double unitTestsSuccessPct, int unitTestsFailed,
            double unitTestsFailedPct, int unitTestsSkipped, double unitTestsSkippedPct,
            String findBugsWarnings, String checkStyleWarnings, String pmdWarnings, String compilerWarnings, int totalWarnings,
            int totalParsedErrors, int totalParsedWarnings, int totalParsedInfos, String parsedErrorLinkFile, String parsedWarningLinkFile, String parsedInfoLinkFile,
            float lineCoverage, float methodCoverage, float classCoverage, float branchCoverage, 
            float instructionCoverage, float complexityScore, int newCodeReviews, int draftCodeReviews, int submittedCodeReviews, int mergedCodeReviews, int abandonedCodeReviews) {

        this.job = job;
        this.gitOrgName = gitOrgName;
        this.jenkinsJobName = jenkinsJobName;
        this.projectOwner = projectOwner;
        this.lastSuccess = lastSuccess;
        
        this.buildNumber = buildNumber;
        this.sonarQubeUrl = sonarQubeUrl;
        this.sonarProjectKey = sonarProjectKey;
        this.gitProjectPath = gitProjectPath;
        this.gerritUrl = gerritUrl;
        this.currentUrl = currentUrl;
        
        this.linesOfCode = linesOfCode;
        this.noOfFiles = noOfFiles;
        this.noOfFunctions = noOfFunctions;
        this.blocker = blocker;
        this.critical = critical;
        this.bugs = bugs;
        this.vulnerabilities = vulnerabilities;
        this.codesmells = codesmells;
        this.complexity = complexity;
        this.duplicatedLinesDensity = duplicatedLinesDensity;
        this.coverage = coverage;

        this.highCheckMarx = highCheckMarx;
        this.mediumCheckMarx = mediumCheckMarx;
        this.lowCheckMarx = lowCheckMarx;

        this.unitTestsCount = unitTestsCount;
        this.unitTestsSuccess = unitTestsSuccess;
        this.unitTestsSuccessPct = unitTestsSuccessPct;
        this.unitTestsFailed = unitTestsFailed;
        this.unitTestsFailedPct = unitTestsFailedPct;
        this.unitTestsSkipped = unitTestsSkipped;
        this.unitTestsSkippedPct = unitTestsSkippedPct;

        this.findBugsWarnings = findBugsWarnings;
        this.checkStyleWarnings = checkStyleWarnings;
        this.pmdWarnings = pmdWarnings;
        this.compilerWarnings = compilerWarnings;
        this.totalWarnings = totalWarnings;

        this.totalParsedErrors = totalParsedErrors;
        this.totalParsedWarnings = totalParsedWarnings;
        this.totalParsedInfos = totalParsedInfos;
        this.parsedErrorLinkFile = parsedErrorLinkFile;
        this.parsedWarningLinkFile = parsedWarningLinkFile;
        this.parsedInfoLinkFile = parsedInfoLinkFile;
        
        this.lineCoverage = lineCoverage;
        this.methodCoverage = methodCoverage;
        this.classCoverage = classCoverage;
        this.branchCoverage = branchCoverage;
        this.instructionCoverage = instructionCoverage;
        this.complexityScore = complexityScore;
        
        this.newCodeReviews = newCodeReviews;
        this.draftCodeReviews = draftCodeReviews;
        this.submittedCodeReviews = submittedCodeReviews;
        this.mergedCodeReviews = mergedCodeReviews;
        this.abandonedCodeReviews = abandonedCodeReviews;
        
    }

    /**
     * Get list of QualityMetricsResultsSummary objects.
     *
     * @return List a List of QualityMetricsResults objects
     */
    public List<QualityMetricsResultsSummary> getQualityMetricsResults() {
        return this.getMetricsResults();
    }

    /**
     * @return Job a job
     */
    public Job<?, ?> getJob() {
        return job;
    }

    public int getBugs() {
        return bugs;
    }

    public int getVulnerabilities() {
        return vulnerabilities;
    }

    /**
     * @return the linesOfCode
     */
    public long getLinesOfCode() {
        return linesOfCode;
    }

    /**
     * @return the noOfFiles
     */
    public int getNoOfFiles() {
        return noOfFiles;
    }

    /**
     * @return the noOfFunctions
     */
    public int getNoOfFunctions() {
        return noOfFunctions;
    }

    /**
     * @param job the job to set
     */
    public void setJob(Job<?, ?> job) {
        this.job = job;
    }

    public void setBugs(int bugs) {
        this.bugs = bugs;
    }

    public void setVulnerabilities(int vulnerabilities) {
        this.vulnerabilities = vulnerabilities;
    }

    /**
     * @param linesOfCode the linesOfCode to set
     */
    public void setLinesOfCode(long linesOfCode) {
        this.linesOfCode = linesOfCode;
    }

    /**
     * @param noOfFiles the noOfFiles to set
     */
    public void setNoOfFiles(int noOfFiles) {
        this.noOfFiles = noOfFiles;
    }

    /**
     * @param noOfFunctions the noOfFunctions to set
     */
    public void setNoOfFunctions(int noOfFunctions) {
        this.noOfFunctions = noOfFunctions;
    }

    /**
     * @param metricsResults the list of metrics results to set
     */
    public List<QualityMetricsResultsSummary> getMetricsResults() {
        //return this.getMetricsResults();
        return this.metricsResults;
    }

    /**
     * @param metricsResults the list of metrics results to set
     */
    public void setMetricsResults(List<QualityMetricsResultsSummary> metricsResults) {
        this.metricsResults = metricsResults;
    }

    /**
     * Add a metrics result.
     *
     * @param metricsResults a coverage result
     * @return QualityMetricsResultsSummary summary of the Quality Metrics
     * result
     */
    public QualityMetricsResultsSummary addMetricsResult(QualityMetricsResultsSummary metResults) {

        this.setLinesOfCode(this.getLinesOfCode() + metResults.getLinesOfCode());
        this.setNoOfFiles(this.getNoOfFiles() + metResults.getNoOfFiles());
        this.setNoOfFunctions(this.getNoOfFunctions() + metResults.getNoOfFunctions());

        this.setBlocker(this.getBlocker() + metResults.getBlocker());
        this.setCritical(this.getCritical()+ metResults.getCritical());
        
        this.setBugs(this.getBugs() + metResults.getBugs());
        this.setVulnerabilities(this.getVulnerabilities() + metResults.getVulnerabilities());
        this.setCodesmells(this.getCodesmells() + metResults.getCodesmells());
        this.setComplexity(this.getComplexity() + metResults.getComplexity());
        this.setDuplicatedLinesDensity(this.getDuplicatedLinesDensity() + metResults.getDuplicatedLinesDensity());
        this.setCoverage(this.getCoverage() + metResults.getCoverage());

        this.setHighCheckMarx(this.getHighCheckMarx() + metResults.getHighCheckMarx());
        this.setMediumCheckMarx(this.getMediumCheckMarx() + metResults.getMediumCheckMarx());
        this.setLowCheckMarx(this.getLowCheckMarx() + metResults.getLowCheckMarx());

        this.setUnitTestsCount(this.getUnitTestsCount() + metResults.getUnitTestsCount());
        this.setUnitTestsSuccess(this.getUnitTestsSuccess() + metResults.getUnitTestsSuccess());
        this.setUnitTestsSuccessPct(this.getUnitTestsSuccessPct() + metResults.getUnitTestsSuccessPct());
        this.setUnitTestsFailed(this.getUnitTestsFailed() + metResults.getUnitTestsFailed());
        this.setUnitTestsFailedPct(this.getUnitTestsFailedPct() + metResults.getUnitTestsFailedPct());
        this.setUnitTestsSkipped(this.getUnitTestsSkipped() + metResults.getUnitTestsSkipped());
        this.setUnitTestsSkippedPct(this.getUnitTestsSkippedPct() + metResults.getUnitTestsSkippedPct());
       

        getMetricsResults().add(metResults);
//        boolean found = false;
//            for (QualityMetricsResultsSummary metricsResult : metricsResults) {
//                if (metricsResult.getJenkinsJobName().equalsIgnoreCase(metResults.getJenkinsJobName())) {
//                    found=true;
//                }
//            }
            boolean foundForAll = false;
            for (QualityMetricsResultsSummary metricsResult : metricsResultsForAllJobs) {
                if (metricsResult.getJenkinsJobName().equalsIgnoreCase(metResults.getJenkinsJobName())) {
                    foundForAll=true;
                }
            }
//            if (!found) {
//            getQualityMetricsResults().add(metResults);
//            }
            if (!foundForAll) {
            metricsResultsForAllJobs.add(metResults);
            }
        return this;
    }

    public int getUnitTestsCount() {
        return unitTestsCount;
    }

    public void setUnitTestsCount(int unitTestsCount) {
        this.unitTestsCount = unitTestsCount;
    }

    public int getUnitTestsSuccess() {
        return unitTestsSuccess;
    }

    public void setUnitTestsSuccess(int unitTestsSuccess) {
        this.unitTestsSuccess = unitTestsSuccess;
    }

    public int getUnitTestsSkipped() {
        return unitTestsSkipped;
    }

    public void setUnitTestsSkipped(int unitTestsSkipped) {
        this.unitTestsSkipped = unitTestsSkipped;
    }

    public int getUnitTestsFailed() {
        return unitTestsFailed;
    }

    public void setUnitTestsFailed(int unitTestsFailed) {
        this.unitTestsFailed = unitTestsFailed;
    }

    public double getUnitTestsSuccessPct() {
        return unitTestsSuccessPct;
    }

    public void setUnitTestsSuccessPct(double unitTestsSuccessPct) {
        this.unitTestsSuccessPct = unitTestsSuccessPct;
    }

    public double getUnitTestsSkippedPct() {
        return unitTestsSkippedPct;
    }

    public void setUnitTestsSkippedPct(double unitTestsSkippedPct) {
        this.unitTestsSkippedPct = unitTestsSkippedPct;
    }

    public double getUnitTestsFailedPct() {
        return unitTestsFailedPct;
    }

    public void setUnitTestsFailedPct(double unitTestsFailedPct) {
        this.unitTestsFailedPct = unitTestsFailedPct;
    }

    public int getTotalWarnings() {
        return totalWarnings;
    }

    public void setTotalWarnings(int totalWarnings) {
        this.totalWarnings = totalWarnings;
    }

    public String getCompilerWarnings() {
        return compilerWarnings;
    }

    public void setCompilerWarnings(String compilerWarnings) {
        this.compilerWarnings = compilerWarnings;
    }

    public String getFindBugsWarnings() {
        return findBugsWarnings;
    }

    public void setFindBugsWarnings(String findBugsWarnings) {
        this.findBugsWarnings = findBugsWarnings;
    }

    public String getCheckStyleWarnings() {
        return checkStyleWarnings;
    }

    public void setCheckStyleWarnings(String checkStyleWarnings) {
        this.checkStyleWarnings = checkStyleWarnings;
    }

    public String getPmdWarnings() {
        return pmdWarnings;
    }

    public void setPmdWarnings(String pmdWarnings) {
        this.pmdWarnings = pmdWarnings;
    }

    public int getTotalParsedErrors() {
        return totalParsedErrors;
    }
    
    public void setTotalParsedErrors(int totalParsedErrors) {
        this.totalParsedErrors = totalParsedErrors;
    }

    public int getTotalParsedWarnings() {
        return totalParsedWarnings;
    }

    public void setTotalParsedWarnings(int totalParsedWarnings) {
        this.totalParsedWarnings = totalParsedWarnings;
    }

    public int getTotalParsedInfos() {
        return totalParsedInfos;
    }

    public void setTotalParsedInfos(int totalParsedInfos) {
        this.totalParsedInfos = totalParsedInfos;
    }

    public String getParsedErrorLinkFile() {
        return parsedErrorLinkFile;
    }

    public void setParsedErrorLinkFile(String parsedErrorLinksFile) {
        this.parsedErrorLinkFile = parsedErrorLinksFile;
    }

    public String getParsedWarningLinkFile() {
        return parsedWarningLinkFile;
    }

    public void setParsedWarningLinkFile(String parsedWarningLinksFile) {
        this.parsedWarningLinkFile = parsedWarningLinksFile;
    }

    public String getParsedInfoLinkFile() {
        return parsedInfoLinkFile;
    }

    public void setParsedInfoLinkFile(String parsedInfoLinksFile) {
        this.parsedInfoLinkFile = parsedInfoLinksFile;
    }

    public float getComplexityScore() {
        return complexityScore;
    }

    public void setComplexityScore(float complexityScore) {
        this.complexityScore = complexityScore;
    }

    public float getLineCoverage() {
        return lineCoverage;
    }

    public void setLineCoverage(float lineCoverage) {
        this.lineCoverage = lineCoverage;
    }

    public float getMethodCoverage() {
        return methodCoverage;
    }

    public void setMethodCoverage(float methodCoverage) {
        this.methodCoverage = methodCoverage;
    }

    public float getClassCoverage() {
        return classCoverage;
    }

    public void setClassCoverage(float classCoverage) {
        this.classCoverage = classCoverage;
    }

    public float getInstructionCoverage() {
        return instructionCoverage;
    }

    public void setInstructionCoverage(float instructionCoverage) {
        this.instructionCoverage = instructionCoverage;
    }

    public float getBranchCoverage() {
        return branchCoverage;
    }

    public void setBranchCoverage(float branchCoverage) {
        this.branchCoverage = branchCoverage;
    }

    public float getNewCodeReviews() {
        return newCodeReviews;
    }

    public void setNewCodeReviews(float newCodeReviews) {
        this.newCodeReviews = newCodeReviews;
    }

    public float getDraftCodeReviews() {
        return draftCodeReviews;
    }

    public void setDraftCodeReviews(float draftCodeReviews) {
        this.draftCodeReviews = draftCodeReviews;
    }

    public float getSubmittedCodeReviews() {
        return submittedCodeReviews;
    }

    public void setSubmittedCodeReviews(float submittedCodeReviews) {
        this.submittedCodeReviews = submittedCodeReviews;
    }

    public float getMergedCodeReviews() {
        return mergedCodeReviews;
    }

    public void setMergedCodeReviews(float mergedCodeReviews) {
        this.mergedCodeReviews = mergedCodeReviews;
    }

    public float getAbandonedCodeReviews() {
        return abandonedCodeReviews;
    }

    public void setAbandonedCodeReviews(float abandonedCodeReviews) {
        this.abandonedCodeReviews = abandonedCodeReviews;
    }

    public String getSonarQubeUrl() {
        return sonarQubeUrl;
    }

    public void setSonarQubeUrl(String sonarQubeUrl) {
        this.sonarQubeUrl = sonarQubeUrl;
    }

    public String getGerritUrl() {
        return gerritUrl;
    }

    public void setGerritUrl(String gerritUrl) {
        this.gerritUrl = gerritUrl;
    }

    public String getGitUrl() {
        return gitUrl;
    }

    public void setGitUrl(String gitUrl) {
        this.gitUrl = gitUrl;
    }

    public String getSonarProjectKey() {
        return sonarProjectKey;
    }

    public void setSonarProjectKey(String sonarProjectKey) {
        this.sonarProjectKey = sonarProjectKey;
    }

    public int getBuildNumber() {
        return buildNumber;
    }

    public void setBuildNumber(int buildNumber) {
        this.buildNumber = buildNumber;
    }

    public String getGitProjectPath() {
        return gitProjectPath;
    }

    public void setGitProjectPath(String gitProjectPath) {
        this.gitProjectPath = gitProjectPath;
    }

    public String getCurrentUrl() {
        return currentUrl;
    }

    public void setCurrentUrl(String currentUrl) {
        this.currentUrl = currentUrl;
    }

    public String getJenkinsJobName() {
        return jenkinsJobName;
    }

    public void setJenkinsJobName(String jenkinsJobName) {
        this.jenkinsJobName = jenkinsJobName;
    }

    
    public String getGitOrgName() {
        return gitOrgName;
    }

    public void setGitOrgName(String gitOrgName) {
        this.gitOrgName = gitOrgName;
    }
    
    public int getBlocker() {
        return blocker;
    }

    public void setBlocker(int blocker) {
        this.blocker = blocker;
    }

    public int getCritical() {
        return critical;
    }

    public void setCritical(int critical) {
        this.critical = critical;
    }

    public String getJobUrl() {
        return job.getAbsoluteUrl();
    }

    public void setJobUrl(String jobUrl) {
        this.jobUrl = jobUrl;
    }

    public String getJobName() {
        return job.getDisplayName();
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    
           /**
     * Add a metrics result.
     *
     * @param metricsResults a coverage result
     * @return QualityMetricsResultsSummary summary of the Quality Metrics
     * result
     */
    public QualityMetricsResultsSummary addMetricsResultForAllJobs(QualityMetricsResultsSummary metResults) {
        
        this.setLinesOfCode(this.getLinesOfCode() + metResults.getLinesOfCode());
        this.setNoOfFiles(this.getNoOfFiles() + metResults.getNoOfFiles());
        this.setNoOfFunctions(this.getNoOfFunctions() + metResults.getNoOfFunctions());

        this.setBlocker(this.getBlocker() + metResults.getBlocker());
        this.setCritical(this.getCritical()+ metResults.getCritical());
        
        this.setBugs(this.getBugs() + metResults.getBugs());
        this.setVulnerabilities(this.getVulnerabilities() + metResults.getVulnerabilities());
        this.setCodesmells(this.getCodesmells() + metResults.getCodesmells());
        this.setComplexity(this.getComplexity() + metResults.getComplexity());
        this.setDuplicatedLinesDensity(this.getDuplicatedLinesDensity() + metResults.getDuplicatedLinesDensity());
        this.setCoverage(this.getCoverage() + metResults.getCoverage());
       

        this.setHighCheckMarx(this.getHighCheckMarx() + metResults.getHighCheckMarx());
        this.setMediumCheckMarx(this.getMediumCheckMarx() + metResults.getMediumCheckMarx());
        this.setLowCheckMarx(this.getLowCheckMarx() + metResults.getLowCheckMarx());

        this.setUnitTestsCount(this.getUnitTestsCount() + metResults.getUnitTestsCount());
        this.setUnitTestsSuccess(this.getUnitTestsSuccess() + metResults.getUnitTestsSuccess());
        this.setUnitTestsSuccessPct(this.getUnitTestsSuccessPct() + metResults.getUnitTestsSuccessPct());
        this.setUnitTestsFailed(this.getUnitTestsFailed() + metResults.getUnitTestsFailed());
        this.setUnitTestsFailedPct(this.getUnitTestsFailedPct() + metResults.getUnitTestsFailedPct());
        this.setUnitTestsSkipped(this.getUnitTestsSkipped() + metResults.getUnitTestsSkipped());
        this.setUnitTestsSkippedPct(this.getUnitTestsSkippedPct() + metResults.getUnitTestsSkippedPct());
            boolean foundForAll = false;
            for (QualityMetricsResultsSummary metricsResult : metricsResultsForAllJobs) {
                if (metricsResult.getJenkinsJobName().equalsIgnoreCase(metResults.getJenkinsJobName())) {
                    foundForAll=true;
                }
            }
            if (!foundForAll) {
            metricsResultsForAllJobs.add(metResults);
            }
        
        return this;
    }

   
}
