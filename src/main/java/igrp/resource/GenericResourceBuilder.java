package igrp.resource;

import java.io.Serializable;

/**
 * Iekiny Marcel
 * Jan 23, 2018
 */
public final class GenericResourceBuilder implements Serializable{ // Not inherit ... 
	
	private GenericResource gr;
	
	private boolean status;
	private Object data;
	
	public GenericResourceBuilder() {
		this.gr = new GenericResource();
	}
	
	public GenericResourceBuilder(boolean status, Object data) {
		this.gr = new GenericResource();
		this.status  = status;
		this.data = data;
	}
	
	public GenericResourceBuilder success(boolean status) {
		this.status = status;
		return this;
	}
	
	public GenericResourceBuilder wrapData(Object data) {
		this.data = data;
		return this;
	}
	
	public GenericResource build() {
		this.gr.setSuccess(status);
		this.gr.setData(data);
		return this.gr;
	}
	
}
