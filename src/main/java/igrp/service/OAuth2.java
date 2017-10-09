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
	
	/**
	 This method handle all oauth2 get request
	 For response-type: "code" | "token"
	 * */
	@GET
	@Path("/authorization")
   // @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public void authorizationCodeOrImplicit(@QueryParam("request") String request){
		
	}
	
	/**
	 The following methods handle all oauth2 post request ...
	 For grant-type: "authorization-code" | "password" | "refresh_token" | "client_credentials"
	 For two consumes media-type: "application/x-www-form-urlencoded" & "application/json"
	 * */	
	@POST
	@Path("/token")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON) // Produce always json response
	public void generateToken(@FormParam("request") String request) {
		
	}
	
	@POST
	@Path("/token")
	@Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON) // Produce always json response
	public void generateToken() {
		
	}
	
}
