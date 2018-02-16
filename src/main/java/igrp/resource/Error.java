package igrp.resource;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Marcel Iekiny
 * Sep 16, 2017
 */
@XmlRootElement
public class Error implements Serializable{
	
	private String name;
	private String message;
	
	public Error() {}
	
	public Error(String name, String message) {
		this.name = name;
		this.message = message;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "Error [name=" + name + ", message=" + message + "]";
	}
}
