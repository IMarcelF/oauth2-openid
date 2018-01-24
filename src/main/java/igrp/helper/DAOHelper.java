package igrp.helper;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
/**
 * @author Marcel Iekiny
 * Oct 4, 2017
 */
public class DAOHelper {// Singleton class
	
	private static DAOHelper dao;
	
	private EntityManagerFactory emf; // Just this object ... is enough !!!
	
	private DAOHelper() {
		Configuration conf = new Configuration().configure("igrp-rest.cfg.xml");
		//this slould be safe because SessionFactory implements EntityManagerFactory
                this.emf = (EntityManagerFactory) conf.buildSessionFactory();
	}
	
	public EntityManager getEntityManager() {
		return this.emf.createEntityManager();
	}
	
	public static DAOHelper getInstance() {
		if(DAOHelper.dao == null)
			DAOHelper.dao = new DAOHelper();
		return DAOHelper.dao;
	}
	
	public void closeAllConnection() {
		this.emf.close();
	}
}
