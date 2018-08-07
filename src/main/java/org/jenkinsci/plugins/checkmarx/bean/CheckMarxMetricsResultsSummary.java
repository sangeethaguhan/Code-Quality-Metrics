/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.checkmarx.bean;

import hudson.model.Job;
import java.util.ArrayList;
import java.util.List;

public class CheckMarxMetricsResultsSummary {

    public CheckMarxMetricsResultsSummary(Job<?, ?> job, long high, int medium, int low) {
        this.job = job;
        this.high = high;
        this.medium = medium;
        this.low = low;
    }

    public CheckMarxMetricsResultsSummary() {
    }
    
       
  /**
   * The related job.
   */
  private Job<?,?> job;

  private int critical;
  
  private int blocker;
  
  /**
   * Lines Of Code.
   */
  private long high;

  /**
   * Number of Files.
   */
  private int medium;

  /**
   * No of Functions.
   */
  private int low;

    private List<CheckMarxMetricsResultsSummary> checkMarxMetricsResults = new ArrayList<CheckMarxMetricsResultsSummary>();

    public Job<?, ?> getJob() {
        return job;
    }

    public void setJob(Job<?, ?> job) {
        this.job = job;
    }

    public long getHigh() {
        return high;
    }

    public void setHigh(long high) {
        this.high = high;
    }

    public int getMedium() {
        return medium;
    }

    public void setMedium(int medium) {
        this.medium = medium;
    }

    public int getLow() {
        return low;
    }

    public void setLow(int low) {
        this.low = low;
    }

  /**
   * @param metricsResults
   *          the list of metrics results to set
   */
  public List<CheckMarxMetricsResultsSummary> getCheckMarxMetricsResults() {
      return this.checkMarxMetricsResults;
  }
  
    /**
   * @param metricsResults
   *          the list of metrics results to set
   */
  public void setCheckMarxMetricsResults(List<CheckMarxMetricsResultsSummary> checkMarxMetricsResults) {
    this.checkMarxMetricsResults = checkMarxMetricsResults;
  }
 
   /**
   * Add a metrics result.
   *
   * @param metricsResults
   *          a coverage result
   * @return CheckMarxMetricsResultsSummary summary of the CheckMarx Metrics result
   */
  public CheckMarxMetricsResultsSummary addMetricsResult(CheckMarxMetricsResultsSummary metResults) {

    this.setHigh(this.getHigh()+ metResults.getHigh());
    this.setMedium(this.getMedium()+ metResults.getMedium());
    this.setLow(this.getLow()+ metResults.getLow());
    getCheckMarxMetricsResults().add(metResults);

    return this;
  }

    public int getCritical() {
        return critical;
    }

    public void setCritical(int critical) {
        this.critical = critical;
    }

    public int getBlocker() {
        return blocker;
    }

    public void setBlocker(int blocker) {
        this.blocker = blocker;
    }
}
