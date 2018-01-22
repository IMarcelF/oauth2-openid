package igrp.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import igrp.helper.DAOHelper;
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
	public Response getUser(@PathParam(value = "id") String id) {
		igrp.resource.User user = null;
		GenericResource genericResource = new GenericResource();
		try {
			//user = DAOHelper.getInstance().getEntityManager().find(igrp.resource.User.class, id);
			user = (igrp.resource.User) DAOHelper.getInstance().getEntityManager().createQuery("select t from User t where t.user_name = :_u or t.email = :_m")
			 .setParameter("_u", id).setParameter("_m", id)
			 .setMaxResults(1)
			 .getSingleResult();
			
		}catch(Exception e) {
			e.printStackTrace();
			Error error = new Error("Exception_Occured", "The resource not found.");
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
