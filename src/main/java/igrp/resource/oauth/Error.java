package igrp.resource.oauth;

import java.io.Serializable;

/**
 * @author Marcel Iekiny
 * Oct 9, 2017
 */
public class Error implements Serializable{ // POJO
	
	private String error; // oauth2 standard error code
	private String error_description;
	
	public Error() {}
	
	public Error(String error, String error_description) {
		super();
		this.error = error;
		this.error_description = error_description;
	}
	
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	public String getError_description() {
		return error_description;
	}
	public void setError_description(String error_description) {
		this.error_description = error_description;
	}
	
	@Override
	public String toString() {
		return "Error [error=" + error + ", error_description=" + error_description + "]";
	}
}
