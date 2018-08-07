/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.checkmarx.grid;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.plugins.view.dashboard.DashboardPortlet;
import java.util.List;
import org.jenkinsci.plugins.checkmarx.bean.CheckMarxMetricsResultsSummary;
import org.jenkinsci.plugins.checkmarx.portlet.CheckMarxLoadData;
import org.kohsuke.stapler.DataBoundConstructor;


public class CheckMarxMetricsGrid extends DashboardPortlet {
    public List<Job> dashboardJobs = getDashboard().getJobs();
     /**
   * Constructor with grid name as parameter. DataBoundConstructor
   * annotation helps the Stapler class to find which constructor that
   * should be used when automatically copying values from a web form
   * to a class.
   *
   * @param name
   *          grid name
   */
    @DataBoundConstructor
    public CheckMarxMetricsGrid(String name) {
        super(name);
    }
    
    
    
  /**
   * This method will be called by portlet.jelly to load data and
   * create the grid.
   *
   * @param jobs
   *          a Collection of Job objects
   * @return CheckMarxMetricsResultsSummary a coverage result summary
   */
  public CheckMarxMetricsResultsSummary getCheckMarxMetricsResultsSummary(List<Job> jobs) {
    
      return CheckMarxLoadData.getResultSummary(jobs);
  }   
  
  
   /**
   * Descriptor that will be shown on Dashboard Portlets view.
   */
  @Extension(optional = true)
  public static class CheckMarxMetricsGridDescriptor extends Descriptor<DashboardPortlet> {

    @Override
    public String getDisplayName() {
      return "CheckMarx Metrics";
    }
  }
}
