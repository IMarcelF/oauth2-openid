package igrp.resource;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;
/**
 * @author Marcel Iekiny
 * Aug 16, 2017
 */
@XmlRootElement
public class Employee implements Serializable{
	
	private int cod;
	private String name;
	
	public int getCod() {
		return cod;
	}
	public void setCod(int cod) {
		this.cod = cod;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
