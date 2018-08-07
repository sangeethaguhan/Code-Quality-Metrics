/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.metrics.gating;

import java.util.Date;
import com.hp.gagawa.java.Document;
import com.hp.gagawa.java.DocumentType;
import com.hp.gagawa.java.elements.A;
import com.hp.gagawa.java.elements.H1;
import com.hp.gagawa.java.elements.Table;
import com.hp.gagawa.java.elements.Td;
import com.hp.gagawa.java.elements.Text;
import com.hp.gagawa.java.elements.Title;
import com.hp.gagawa.java.elements.Tr;
import hudson.model.Hudson;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.TopLevelItem;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.metrics.bean.QualityMetricsResultsSummary;
import org.jenkinsci.plugins.metrics.grid.QualityMetricsGrid;
import org.jenkinsci.plugins.metrics.portlet.QualityMetricsLoadData;
import org.jenkinsci.plugins.sonarmetrics.Messages;

public class Emailer {
    Date now;
    private static String gitProjectPath = "";
    private static String emailSubject = "Code Quality Report";
    private static String emailCC = "saldin.samuel@247-inc.com";
    private static String emailTo = "saldin.samuel@247-inc.com,sangeetha.guhan@247-inc.com";
    private static String emailFrom = "ci247@247-inc.com";
     private static String emailHost = "smtp.home.tellme.com";
     private static String emailPort = "25";
     
     
    public static List<QualityMetricsResultsSummary> getQualityMetricsResultsForAllJobs() throws IOException {

        if (QualityMetricsResultsSummary.metricsResultsForAllJobs.size()==0) {
          Jenkins jenkins=Jenkins.getInstance();
          List<Job> jobs=jenkins.getItems(Job.class);
          QualityMetricsLoadData.getResultSummaryForAllJobs(jobs);
        }
        
        return QualityMetricsResultsSummary.metricsResultsForAllJobs;
    }
     
     public static void SendQualityGateReport(){
         try{
             
         emailTo = QualityGate.emailRecepients;
             
             
         List<QualityMetricsResultsSummary> summaryList = QualityMetricsResultsSummary.metricsResultsForAllJobs;
            Log(" ***** Summary List Size : "+summaryList.size());
            for (QualityMetricsResultsSummary qualityMetricsResultsSummary : summaryList) {
                Log("qualityMetricsResultsSummary "+qualityMetricsResultsSummary.getJenkinsJobName());
            }
            
            
            Set<String> metricsDetails = new LinkedHashSet<String>();
            
            for (QualityMetricsResultsSummary list : summaryList) {
                metricsDetails.add("Job"+"***"
                                    +"Lines of Code"+"***"
                                    +"No. of Files"+"***"
                                    +"No. of Functions"+"***"
                                    +"Total Tests"+"***"
                                    +"Passed"+"***"
                                    +"Failed"+"***"
                                    +"Skipped"+"***"
                                    +"% Passed"+"***"
                                    +"Line Coverage"+"***"
                                    +"Branch Coverage"+"***"
                                    +"Class Coverage"+"***"
                                    +"Method Coverage"+"***"
                                    +"Total Parsed Error");
                String jenkinsUrl = Hudson.getInstance().getRootUrl();
                String jobname = list.getJenkinsJobName();
                String organization = jobname.substring(0,jobname.indexOf("_",0));
                metricsDetails.add(list.getJenkinsJobName()+"::"+list.getJob().getAbsoluteUrl()+"***"
                                    +list.getLinesOfCode()+"::"+list.getSonarQubeUrl()+"/component_measures/metric/ncloc/list?id="+list.getSonarProjectKey()+"***"
                                    +list.getNoOfFiles()+"::"+list.getSonarQubeUrl()+"/component_measures/metric/files/list?id="+list.getSonarProjectKey()+"***"
                                    +list.getNoOfFunctions()+"::"+list.getSonarQubeUrl()+"/component_measures/metric/functions/list?id="+list.getSonarProjectKey()+"***"
                                    +list.getUnitTestsCount()+"::"+jenkinsUrl+"view/Organizations/job/"+organization+"/job/"+list.getJenkinsJobName()+"/lastCompletedBuild/testReport/"+"***"
                                    +list.getUnitTestsSuccess()+"::"+jenkinsUrl+"view/Organizations/job"+organization+"job/"+list.getJenkinsJobName()+"/lastCompletedBuild/testReport/"+"***"
                                    +list.getUnitTestsFailed()+"::"+jenkinsUrl+"view/Organizations/job"+organization+"job/"+list.getJenkinsJobName()+"/lastCompletedBuild/testReport/"+"***"
                                    +list.getUnitTestsSkipped()+"::"+jenkinsUrl+"view/Organizations/job"+organization+"job/"+list.getJenkinsJobName()+"/lastCompletedBuild/testReport/"+"***"
                                    +list.getUnitTestsSuccessPct()+"::"+jenkinsUrl+"view/Organizations/job"+organization+"job/"+list.getJenkinsJobName()+"/lastCompletedBuild/testReport/"+"***"
                                    +list.getLineCoverage()+"::"+jenkinsUrl+"view/Organizations/job"+organization+"job/"+list.getJenkinsJobName()+"/lastCompletedBuild/jacoco/"+"***"
                                    +list.getBranchCoverage()+"::"+jenkinsUrl+"view/Organizations/job"+organization+"job/"+list.getJenkinsJobName()+"/lastCompletedBuild/jacoco/"+"***"
                                    +list.getClassCoverage()+"::"+jenkinsUrl+"view/Organizations/job"+organization+"job/"+list.getJenkinsJobName()+"/lastCompletedBuild/jacoco/"+"***"
                                    +list.getMethodCoverage()+"::"+jenkinsUrl+"view/Organizations/job"+organization+"job/"+list.getJenkinsJobName()+"/lastCompletedBuild/jacoco/"+"***"
                                    +list.getTotalParsedErrors()+"::"+jenkinsUrl+"view/Organizations/job"+organization+"job/"+list.getJenkinsJobName()+"/lastCompletedBuild/parsed_console/");
                
            }
            
            for (String metricsDetail : metricsDetails) {
                //Log(metricsDetail);
            }
            
            Document document = new Document(DocumentType.XHTMLTransitional);
		document.head.appendChild(new Title().appendChild(new Text("Quality Gate v1.0 Report")));
		// document.body.setBgcolor("#95B9C7");

		H1 header = new H1();
		header.appendText("Quality Gate v1.0 Report" +"   ("+new SimpleDateFormat("MM/dd/yyyy").format(new Date())+")");
		header.setAlign("CENTER");
		header.setStyle("background-color:#95B9C7;color:darkblue;text-align:center");
		document.body.appendChild(header);

		//constructReportHeader();
		document.body.appendChild(new Text("<br>"));
		
		Table testResultsTable = new Table();
		testResultsTable.setAlign("center");
		testResultsTable.setStyle("border: 2px solid black; border-collapse: collapse;");
		testResultsTable.setWidth("750px");
                
                int cntUrgentCritical = 0;
		//Collections.reverse(urgentCriticalDefects);

		for (Iterator iterator = metricsDetails.iterator(); iterator.hasNext();) {
			String item = (String) iterator.next().toString();
			System.out.println(item);
			StringTokenizer stItem = new StringTokenizer(item,"***");
			Tr tr = new Tr();
			testResultsTable.appendChild(tr);
			tr.setStyle("border:1px solid black;");
			
			int c = 0;
			while (stItem.hasMoreTokens()) {
				String value = stItem.nextToken();
				System.out.println(value);
				Td td = new Td();
				tr.appendChild(td);
				if (cntUrgentCritical==0) {
					td.setStyle("border:1px solid black;border:1px solid #F1D0CE;width:100px;");	
					td.setBgcolor("#D1D0CE");
					td.setStyle("font-weight: bold;border:1px solid black;");
					td.setHeight("20px;");	
					td.setWidth("35%;");	
					//td.setAlign("center");	
				} else {
					if (c != 0 ) {
						td.setAlign("center");	
						td.setStyle("white-space: nowrap;");	
					}
					td.setStyle("border:1px solid black;border:1px solid #F1D0CE;");	
					

				}
				String green = "#E7FFB4",red = "#FFD6B4";
                               
				if (value.contains("::")) {
					String defectCount = value.substring(0, value.indexOf("::"));
					String defectUrl = value.substring(value.indexOf("::")+2,value.length());
					A link = new A(defectUrl,defectCount,defectCount);
					td.appendChild(link);	
                                        if (cntUrgentCritical != 0 && c==4) {
                                            if (Integer.parseInt(defectCount) == 0) {
                                                td.setBgcolor(red);
                                            } else {
                                                td.setBgcolor(green);
                                            }
                                        } 
				} else {
					td.appendChild(new Text(value));
                                        if (cntUrgentCritical != 0 && c==4) {
                                            if (Integer.parseInt(value) == 0) {
                                                td.setBgcolor(red);
                                            } else {
                                                td.setBgcolor(green);
                                            }
                                        } 
				}
				c++;	
			}
			cntUrgentCritical++;
		}
		document.body.appendChild(testResultsTable);
                
                File file = new File("/var/tellme/tmp/jenkins/"+"quality_gate_report.html");
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		bw.write(document.write());
		bw.close();
		String emailSubject = "Quality Gate v1.0 Report" +" ("+new SimpleDateFormat("MM/dd/yyyy").format(new Date())+")";
                Log("Email Subject : "+emailSubject);
                
		FileInputStream fis = new FileInputStream(file);
                byte[] data = new byte[(int)file.length()];
                fis.read(data);
                fis.close();
                String s = new String(data, "UTF-8");
                //Log("Email Content: "+s);
                sendTestResults(s);
        } catch (Exception ex) {
            //Logger.getLogger(ScheduledTask.class.getName()).log(Level.SEVERE, null, ex);
            Log(ex.getMessage());
        }
     }
    private static org.apache.log4j.Logger topLogger = org.apache.log4j.Logger.getLogger("All Jenkins Logs");
    private static void Log(String msg){
        if (Messages.LogFlag().equalsIgnoreCase("true")) {
            topLogger.info(msg);
        }
    }   
  public synchronized static void sendTestResults(String file) {
	      String subject = String.format("%s :: Automation Results.", emailSubject);
              //Log("Subject: "+subject);
	            sendTestReport(emailHost, emailFrom,
	                         "Saldin.Samuel@247-inc.com", emailCC,
	                         subject, file);
	   
	  }
  
    public static void sendTestReport(String host, final String from, String to, String cc,
	      String subject, String body) {
	    Properties properties = System.getProperties();
	    properties.setProperty("mail.smtp.starttls.enable", "true");
	    properties.setProperty("mail.smtp.auth", "true");
	    properties.setProperty("mail.smtp.host", emailHost);
//	    properties.setProperty("mail.smtp.socketFactory.port", emailPort);
//	    properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
//	    properties.setProperty("mail.smtp.socketFactory.fallback", "true");
	    properties.setProperty("mail.smtp.port", emailPort);
	    properties.setProperty("mail.smtp.timeout", "30000");

	    Log("host : "+host);
	    Log("From : "+from);
	    Log("to : "+to);
	    Log("cc : "+cc);
	    Log("subject : "+subject);
	    Log("body : "+body);
	    
	    // Get the default Session object.
	    //Session session = Session.getDefaultInstance(properties, null);
	    Session session = Session.getDefaultInstance(properties, new Authenticator() {
	    	protected PasswordAuthentication getPasswordAuthentication(){
	    		return new PasswordAuthentication(emailFrom, "123456@abc");
	    	}
		});
            Log("Authenticated for user : "+emailFrom);
            
	    try {
	      MimeMessage message = new MimeMessage(session);

	      message.setFrom(new InternetAddress(from));
              
	      for (String address : getAddress(to)) {
	        message.addRecipient(Message.RecipientType.TO, new InternetAddress(address));
	      }

	      for (String address : getAddress(cc)) {
	        message.addRecipient(Message.RecipientType.CC, new InternetAddress(address));
	      }

	      message.setSubject(subject);
	      message.setContent(body, "text/html" );
              Log("Before sending the message");
	      Transport.send(message);
	      Log("Sent message successfully....");
	   }catch (MessagingException mex) {
              Log("MessagingException : ");
              Log("MessagingException : "+mex.getMessage()+" : "+mex.getCause().getMessage());
               mex.printStackTrace();
	   }catch (Exception ex) {
              Log("Error while Messaging : "+ex.getMessage()+" : "+ex.getCause().getMessage());   
	   }
	  }
    
     public static List <String> getAddress(String address) {
    List <String> addressList = new ArrayList<String>();
    for (String addressSpace : address.split(" ")) {
      for (String addressSemiColon : addressSpace.split(";")) {
        for (String addressComma : addressSemiColon.split(",")) {
          if (addressComma.length() > 3) {
            if (!addressComma.contains("@")) {
              addressComma += "@247-inc.com";
            }
            addressList.add(addressComma);
          }
        }
      }
    }
    return addressList;
  }     
}
