package igrp.helper;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
/**
 * @author Marcel Iekiny
 * Oct 4, 2017
 */
public class DAOHelper {// Singleton class 
	
	private volatile static DAOHelper dao;
	
	private SessionFactory sf; // Just this object ... is enough !!! 
	
	private DAOHelper() {
		Configuration conf = new Configuration().configure("igrp-rest.cfg.xml");
        this.sf =  conf.buildSessionFactory();
	}
	
	public Session getSession() {
		return this.sf.openSession();
	}
	
	// Double-checked locking singleton 
	public static DAOHelper getInstance() {
        if (DAOHelper.dao == null) {                         
            synchronized (DAOHelper.class) {
                if (DAOHelper.dao == null) {       
                	DAOHelper.dao = new DAOHelper();
                }
            }
        }
        return DAOHelper.dao;
    }
	
	public void closeAllConnection() {
		this.sf.close();
	}
	
	public void closeCurrentSession() {
		if(this.sf.getCurrentSession().isOpen())
			this.sf.getCurrentSession().close();
	}
}
