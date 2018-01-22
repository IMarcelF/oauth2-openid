package igrp.service;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import igrp.helper.DAOHelper;
import igrp.helper.OAuth2Helper;
import igrp.oauth2.error.OAuth2Error;
import igrp.resource.GenericResource;
import igrp.resource.Error;

/**
 * Marcel Iekiny
 * Sep 16, 2017
 */

@Path("/user")
public class User {

	@GET
	@Path("{id}")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response getUser(@PathParam(value = "id") String id, @HeaderParam(value = "Authorization") String token) {
		
		GenericResource genericResource = new GenericResource();
		
		// To valid the token (Begin) 
		boolean die = false;
		try {
			token = token.split(" ")[1];
				die = !OAuth2Helper.isValidToken(token, "login");
		}catch(Exception e) {
			die = true;
		}
		if(die) {
			Error error = new Error("" + OAuth2Error.INVALID_GRANT.getStatus(), OAuth2Error.INVALID_GRANT.getDescription());
			genericResource.setSuccess(false);
			genericResource.setData(error);
			return Response.status(OAuth2Error.INVALID_GRANT.getStatus()).entity(genericResource).build();
		}
		// To valid the token (End) 
		
		igrp.resource.User user = null;
		
		try {
			//user = DAOHelper.getInstance().getEntityManager().find(igrp.resource.User.class, id);
			user = (igrp.resource.User) DAOHelper.getInstance().getEntityManager().createQuery("select t from User t where t.user_name = :_u or t.email = :_m")
			 .setParameter("_u", id).setParameter("_m", id)
			 .setMaxResults(1)
			 .getSingleResult();
			
		}catch(Exception e) {
			e.printStackTrace();
			Error error = new Error("Exception_Occured", "The resource não foi encontrado.");
			genericResource.setSuccess(false);
			genericResource.setData(error);
			return Response.status(500).entity(genericResource).build();
		}
		if(user == null) {
			Error error = new Error("404", "Resource not found.");
			genericResource.setSuccess(false);
			genericResource.setData(error);
			return Response.status(404).entity(genericResource).build();
		}
		genericResource.setSuccess(true);
		genericResource.setData(user);
		return Response.status(200).entity(genericResource).build();
	}
	
}
