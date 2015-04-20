/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linkedtv.dashboard.managed;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;

/**
 * 
 * @author jan
 */
@ManagedBean(name="progressBean")
@ViewScoped
public class ProgressBarView implements Serializable {

	private static final long serialVersionUID = 178676567869L;
	private String error;
	private String success;
	
    final static Logger logger = Logger.getLogger(ProgressBarView.class); 	
	
	private Integer progress;

	public void buildNutch() {
		String s = null;
		error = null;
		success = null;
		try {
			Process p = Runtime
					.getRuntime()
					.exec("sudo -u hadoop /opt/pokus/nutch/java_exec.sh >> /opt/pokus/exec_log.log");

			BufferedReader stdInput = new BufferedReader(new InputStreamReader(
					p.getInputStream()));

			BufferedReader stdError = new BufferedReader(new InputStreamReader(
					p.getErrorStream()));

			// read the output from the command
			System.out.println("Here is the standard output of the command:\n");
			while ((s = stdInput.readLine()) != null) {
				System.out.println(s);
				if (s.contains("PART_deset"))  {
					setProgress(10);
		            try {
		                Thread.sleep(1000);
		                setProgress(15);
		            } catch (InterruptedException e) {
		            }
				} else if (s.contains("PART_tricet"))  {
					setProgress(30);
		            try {
		                Thread.sleep(1000);
		                setProgress(45);
		            } catch (InterruptedException e) {
		            }
				} else if (s.contains("PART_sedesat"))  {
					setProgress(60);
		            try {
		                Thread.sleep(1000);
		                setProgress(65);
		            } catch (InterruptedException e) {
		            }
				} else if (s.contains("PART_petasedmdesat"))  {
					setProgress(75);
		            try {
		                Thread.sleep(1000);
		                setProgress(83);
		            } catch (InterruptedException e) {
		            }
				} else if (s.contains("PART_devade"))  {
					setProgress(90);
		            try {
		                Thread.sleep(1000);
		                setProgress(95);
		            } catch (InterruptedException e) {
		            }
				} else if (s.contains("PART_sto"))  {
					setProgress(100);
		            try {
		                Thread.sleep(200);
		            } catch (InterruptedException e) {
		            }
				}
			}

			// read any errors from the attempted command
			logger.debug("Here is the standard error of the command (if any):\n");
			while ((s = stdError.readLine()) != null) {
				logger.debug(s);
				error = s;
			}

		} catch (IOException e) {
			logger.error("Exception occured while building NUTCH with progress bar.");
			error = e.getMessage();
		}
	}
	
	  public Integer getProgress() {  
		  if (progress == null) {
			  progress = 0;
		  }
		  
		  if(progress > 100)
              progress = 100;
		  
		    return progress;  
		  }  

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getSuccess() {
		return success;
	}

	public void setSuccess(String success) {
		this.success = success;
	}
	
    public void setProgress(Integer progress) {
        this.progress = progress;
    }
     
    public void onComplete() {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Process Completed"));
    }	
    
    public void cancel() {  
        progress = null;  
    }     
}