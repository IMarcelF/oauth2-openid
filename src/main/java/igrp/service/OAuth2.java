package igrp.service;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.Produces;
import javax.ws.rs.FormParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import igrp.resource.Employee;

@Path("/oauth2")
public class OAuth2 {

	private static final Logger logger = Logger.getLogger(OAuth2.class);

	@GET
	@Path("/getSomething")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Employee[] getSomething(@QueryParam("request") String request ,
			 @DefaultValue("1") @QueryParam("version") int version) {
		
		if (logger.isDebugEnabled()) {
			logger.debug("Start getSomething");
			logger.debug("data: '" + request + "'");
			logger.debug("version: '" + version + "'");
		}
	
		String response = null;

        try{			
            switch(version){
	            case 1:
	                if(logger.isDebugEnabled()) logger.debug("in version 1");

	                response = "Response from Jersey Restful Webservice : " + request;
                    break;
                default: throw new Exception("Unsupported version: " + version);
            }
        }
        catch(Exception e){
        	response = e.getMessage().toString();
        }
        
        if(logger.isDebugEnabled()){
            logger.debug("result: '"+response+"'");
            logger.debug("End getSomething");
        }
        Employee employee = new Employee();
        employee.setCod(10);
        employee.setName("IMF");
        
        Employee employee2 = new Employee();
        employee2.setCod(11);
        employee2.setName("IMF Fernandes");
        
        Employee list[] = new Employee[] {employee, employee2};
        
        return list;	
	}
	
}
