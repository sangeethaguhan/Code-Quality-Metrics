/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.sonarmetrics.grid;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.plugins.view.dashboard.DashboardPortlet;
import java.io.IOException;
import java.util.List;
import org.jenkinsci.plugins.sonarmetrics.portlet.SonarQubeLoadData;
import org.jenkinsci.plugins.sonarmetrics.bean.SonarQubeMetricsResultsSummary;
import org.jenkinsci.plugins.sonarmetrics.Messages;
import org.kohsuke.stapler.DataBoundConstructor;


public class SonarQubeMetricsGrid extends DashboardPortlet {
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
  public SonarQubeMetricsGrid(String name) {
        super(name);
  }

  /**
   * This method will be called by portlet.jelly to load data and
   * create the grid.
   *
   * @param jobs
   *          a Collection of Job objects
   * @return SonarQubeMetricsResultsSummary a coverage result summary
   */
  public SonarQubeMetricsResultsSummary getSonarQubeMetricsResultsSummary(List<Job> jobs) throws IOException {
    
      return SonarQubeLoadData.getResultSummary(jobs);
  }

  /**
   * Descriptor that will be shown on Dashboard Portlets view.
   */
  @Extension(optional = true)
  public static class SonarQubeMetricsGridDescriptor extends Descriptor<DashboardPortlet> {

    @Override
    public String getDisplayName() {
      return Messages.GridTitle();
    }
  }
}
