package igrp.service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import igrp.resource.User;

/**
 * @author Marcel Iekiny
 * Oct 6, 2017
 */
public class Test {
	
	public static void main(String []args) {
		Configuration conf = new Configuration().configure("igrp-rest.cfg.xml");
		SessionFactory sf =  conf.buildSessionFactory();
		Session session = sf.openSession();
		EntityManagerFactory emf = session.getEntityManagerFactory();
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		
		User user = (User) em.createQuery("select t from User").getSingleResult();
		
		System.out.println(user);
		
		em.close();
		emf.close();
	}
	
}
