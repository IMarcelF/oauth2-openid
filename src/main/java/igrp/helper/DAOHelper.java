package igrp.helper;
/**
 * @author Marcel Iekiny
 * Oct 4, 2017
 */
public class DAOHelper {// Singleton class
	
	private static DAOHelper dao;
	
	private DAOHelper() {
		
	}
	
	public DAOHelper getInstance() {
		if(DAOHelper.dao == null)
			DAOHelper.dao = new DAOHelper();
		return DAOHelper.dao;
	}	
}
