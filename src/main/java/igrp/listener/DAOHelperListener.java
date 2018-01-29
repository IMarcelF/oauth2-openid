package igrp.listener;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;

import igrp.helper.DAOHelper;
/**
 * Iekiny Marcel
 * Jan 26, 2018
 */
@WebListener
public class DAOHelperListener implements ServletRequestListener{
	
	 public void requestDestroyed(ServletRequestEvent arg0)  { 
		 System.out.println("Entrado ...");
    	 DAOHelper.getInstance().closeCurrentSession();
    }

    public void requestInitialized(ServletRequestEvent arg0)  {}    

}