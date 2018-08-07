/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.metrics.grid;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.plugins.analysis.collector.AnalysisDescriptor;
import hudson.plugins.analysis.collector.WarningsAggregator;
import hudson.plugins.view.dashboard.DashboardPortlet;
import java.io.IOException;
import java.util.List;
import org.jenkinsci.plugins.metrics.bean.QualityMetricsResultsSummary;
import org.jenkinsci.plugins.metrics.portlet.QualityMetricsLoadData;
import org.jenkinsci.plugins.sonarmetrics.Messages;
import org.kohsuke.stapler.DataBoundConstructor;


public class QualityMetricsGrid extends DashboardPortlet {

    public List<Job> dashboardJobs = getDashboard().getJobs();
    public WarningsAggregator warningsAggregator;

    /**
     * Constructor with grid name as parameter. DataBoundConstructor annotation
     * helps the Stapler class to find which constructor that should be used
     * when automatically copying values from a web form to a class.
     *
     * @param name grid name
     */
    @DataBoundConstructor
    public QualityMetricsGrid(String name) {
        super(name);
        warningsAggregator = new WarningsAggregator(
                AnalysisDescriptor.isCheckStyleInstalled(),
                AnalysisDescriptor.isDryInstalled(),
                AnalysisDescriptor.isFindBugsInstalled(),
                AnalysisDescriptor.isPmdInstalled(),
                AnalysisDescriptor.isOpenTasksInstalled(),
                AnalysisDescriptor.isWarningsInstalled());
    }

    /**
     * This method will be called by portlet.jelly to load data and create the
     * grid.
     *
     * @param jobs a Collection of Job objects
     * @return QualityMetricsResultsSummary a coverage result summary
     */
    public QualityMetricsResultsSummary getQualityMetricsResultsSummary(List<Job> jobs) throws IOException {

        return QualityMetricsLoadData.getResultSummary(jobs);
    }

    /**
     * Descriptor that will be shown on Dashboard Portlets view.
     */
    @Extension(optional = true)
    public static class QualityMetricsGridDescriptor extends Descriptor<DashboardPortlet> {

        @Override
        public String getDisplayName() {
            return Messages.QualityMetricsPortletTitle();
        }
    }
}
