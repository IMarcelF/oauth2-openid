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
import igrp.oauth2.util.OAuth2Error;
import igrp.oauth2.util.Scope;
import igrp.resource.GenericResource;
import igrp.resource.GenericResourceBuilder;
import igrp.resource.Error;
import javax.persistence.EntityManager;

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
		
		if(!OAuth2Helper.isValidToken(token, Scope.USER_READ + "")) 
			return Response.status(OAuth2Error.INVALID_GRANT.getStatus()).entity(new GenericResourceBuilder(false, new Error("" + OAuth2Error.INVALID_GRANT.getStatus(), OAuth2Error.INVALID_GRANT.getDescription())).build()).build(); 
		
		igrp.resource.User user = null;
                EntityManager em = DAOHelper.getInstance().getSession();
		try {
			user = (igrp.resource.User) em.createQuery("select t from User t where t.user_name = :_u or t.email = :_m")
			 .setParameter("_u", id).setParameter("_m", id)
			 .setMaxResults(1)
			 .getSingleResult();
		}catch(Exception e) {
			e.printStackTrace();
			return Response.status(500).entity(new GenericResourceBuilder(false, new Error("Exception_Occured", "The resource não foi encontrado.")).build()).build();
		}finally{
                    if(em != null){
                        em.close();
                    }
                }
		if(user == null) 
			return Response.status(404).entity(new GenericResourceBuilder(false, new Error("404", "Resource not found.")).build()).build();
		
		return Response.status(200).entity(new GenericResourceBuilder(true, user).build()).build();
	}
	
}
