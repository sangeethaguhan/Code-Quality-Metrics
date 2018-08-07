/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.checkmarx.portlet;

import hudson.model.Job;
import hudson.model.Run;
import java.util.List;
import org.apache.log4j.Logger;
import org.jenkinsci.plugins.checkmarx.bean.CheckMarxMetricsResultsSummary;
import org.jenkinsci.plugins.checkmarx.grid.CheckMarxMetricsGrid;


public class CheckMarxLoadData {
    private static Logger topLogger = Logger.getLogger("All Jenkins Logs");
    
      /**
     * Summarize the last coverage results of all jobs. If a job doesn't include
     * any coverage, add zero.
     *
     * @param jobs a final Collection of Job objects
     * @return CheckMarxMetricsResultsSummary the result summary
     */
    public static CheckMarxMetricsResultsSummary getResultSummary(List<Job> listJobs) {
        CheckMarxMetricsGrid s = new CheckMarxMetricsGrid("TestBoard");
        listJobs = s.dashboardJobs;
        CheckMarxMetricsResultsSummary summary = new CheckMarxMetricsResultsSummary();
        for (Job<?, ?> job : listJobs) {
            topLogger.info("Job Name : " + job.getDisplayName());
            try {
                if (getCheckMarxMetrics(job) != null) {
                    summary.addMetricsResult(getCheckMarxMetrics(job));
                }
            } catch(RuntimeException e) {
                topLogger.error("Got unexpected exception", e);
            }
        }
        return summary;
    }

    /**
     * Gets the CheckMarx Metrics for each project.
     *
     * @param job the Job object
     * @return CheckMarxMetricsResultsSummary the result summary for CheckMarx
     * project
     */
    private static CheckMarxMetricsResultsSummary getCheckMarxMetrics(Job job) {
        
       // Run<?, ?> run = job.getLastSuccessfulBuild();
        try {
         return (new CheckMarxMetricsResultsSummary(job,
                    0,0,0));
        } catch (RuntimeException  e) {
            topLogger.error("CheckMarx is not configured for this job : "+job.getDisplayName(),e);
        }
        return (new CheckMarxMetricsResultsSummary(job,0,0,0));
    }
    
}
