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
	
	private EntityManager em; // Just this object ... is enough
	
	private DAOHelper() {
		Configuration conf = new Configuration().configure("igrp-rest.cfg.xml");
		SessionFactory sf =  conf.buildSessionFactory();
		Session session = sf.openSession();
		EntityManagerFactory emf = session.getEntityManagerFactory();
		this.em = emf.createEntityManager();
	}
	
	public EntityManager geEntityManager() {
		return this.em;
	}
	
	public DAOHelper getInstance() {
		if(DAOHelper.dao == null)
			DAOHelper.dao = new DAOHelper();
		return DAOHelper.dao;
	}
	
	public void closeAllConnection() {
		em.close();
		em.getEntityManagerFactory().close();
	}
}
