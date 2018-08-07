/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.sonarmetrics.bean;

import hudson.model.Job;
import java.util.ArrayList;
import java.util.List;


public class SonarQubeMetricsResultsSummary {
    
    
  /**
   * The related job.
   */
  private Job<?,?> job;

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
  private int complexityScore;

  private List<SonarQubeMetricsResultsSummary> metricsResults = new ArrayList<SonarQubeMetricsResultsSummary>();

  /**
   * Default Constructor.
   */
  public SonarQubeMetricsResultsSummary() {
  }

  /**
   * Constructor with parameters.
   *
   * @param job
   *          the related Job
   * @param linesOfCode
   *          Lines of Code
   * @param noOfFiles
   *          Number of Files
   * @param noOfFunctions
   *          Number of Functions
   */
  public SonarQubeMetricsResultsSummary(Job<?,?> job, long linesOfCode, int noOfFiles, int noOfFunctions,
    int bugs, int vulnerabilities, int codesmells, int complexity, int duplicatedLinesDensity, int coverage) {
    this.job = job;
    this.linesOfCode = linesOfCode;
    this.noOfFiles = noOfFiles;
    this.noOfFunctions = noOfFunctions;
    this.bugs = bugs;
    this.vulnerabilities = vulnerabilities;
    this.codesmells = codesmells;
    this.complexity = complexity;
    this.duplicatedLinesDensity = duplicatedLinesDensity;
    this.coverage = coverage;
  }

  /**
   * Get list of SonarQubeMetricsResults objects.
   *
   * @return List a List of SonarQubeMetricsResults objects
   */
  public List<SonarQubeMetricsResultsSummary> getSonarQubeMetricsResults() {
    return this.getMetricsResults();
  }

  /**
   * @return Job a job
   */
  public Job<?,?> getJob() {
    return job;
  }

  public int getBugs() {
    return bugs;
  }

  public int getVulnerabilities() {
    return vulnerabilities;
  }

  public int getComplexityScore() {
    return complexityScore;
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
   * @param job
   *          the job to set
   */
  public void setJob(Job<?,?> job) {
    this.job = job;
  }

  public void setBugs(int bugs) {
    this.bugs = bugs;
  }

  public void setVulnerabilities(int vulnerabilities) {
    this.vulnerabilities = vulnerabilities;
  }


  /**
   * @param linesOfCode
   *          the linesOfCode to set
   */
  public void setLinesOfCode(long linesOfCode) {
    this.linesOfCode = linesOfCode;
  }

  /**
   * @param noOfFiles
   *          the noOfFiles to set
   */
  public void setNoOfFiles(int noOfFiles) {
    this.noOfFiles = noOfFiles;
  }

  /**
   * @param noOfFunctions
   *          the noOfFunctions to set
   */
  public void setNoOfFunctions(int noOfFunctions) {
    this.noOfFunctions = noOfFunctions;
  }


 /**
   * @param metricsResults
   *          the list of metrics results to set
   */
  public List<SonarQubeMetricsResultsSummary> getMetricsResults() {
    //return this.getMetricsResults();
      return this.metricsResults;
  }
  
  /**
   * @param metricsResults
   *          the list of metrics results to set
   */
  public void setMetricsResults(List<SonarQubeMetricsResultsSummary> metricsResults) {
    this.metricsResults = metricsResults;
  }
 
   /**
   * Add a metrics result.
   *
   * @param metricsResults
   *          a coverage result
   * @return SonarQubeMetricsResultsSummary summary of the SonarQube Metrics result
   */
  public SonarQubeMetricsResultsSummary addMetricsResult(SonarQubeMetricsResultsSummary metResults) {

    this.setLinesOfCode(this.getLinesOfCode()+ metResults.getLinesOfCode());
    this.setNoOfFiles(this.getNoOfFiles()+ metResults.getNoOfFiles());
    this.setNoOfFunctions(this.getNoOfFunctions()+ metResults.getNoOfFunctions());
    
    this.setBugs(this.getBugs()+ metResults.getBugs());
    this.setVulnerabilities(this.getVulnerabilities()+ metResults.getVulnerabilities());
    this.setCodesmells(this.getCodesmells()+ metResults.getCodesmells());
    this.setComplexity(this.getComplexity()+ metResults.getComplexity());
    this.setDuplicatedLinesDensity(this.getDuplicatedLinesDensity()+ metResults.getDuplicatedLinesDensity());
    this.setCoverage(this.getCoverage()+ metResults.getCoverage());
    
    getMetricsResults().add(metResults);

    return this;
  }
  
}
