package igrp.resource;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Marcel Iekiny
 * Sep 16, 2017
 */
@XmlRootElement
public class GenericResource implements Serializable{ // A generic Rest/Restful response 

	private boolean success; 
	private Object data; // can be an object|array 
	
	public GenericResource() {}
	
	public boolean isSuccess() {
		return success;
	}
	
	public void setSuccess(boolean success) {
		this.success = success;
	}
	
	public Object getData() {
		return data;
	}
	
	public void setData(Object data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "GenericResource [success=" + success + ", data=" + data + "]";
	}
	
}
