package igrp.service;

import java.util.StringTokenizer;

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
import igrp.oauth2.error.Scope;
import igrp.resource.Error;
import igrp.resource.GenericResourceBuilder;

/**
 * Iekiny Marcel
 * Jan 23, 2018
 */
@Path("/session")
public class Session {
	
	@GET
	@Path("{id}")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response getSession(@PathParam(value = "id") String id, @HeaderParam(value = "Authorization") String token) {
		
		//if(!OAuth2Helper.isValidToken(token, Scope.SESSION_READ + "")) 
		//	return Response.status(OAuth2Error.INVALID_GRANT.getStatus()).entity(new GenericResourceBuilder(false, new Error("" + OAuth2Error.INVALID_GRANT.getStatus(), OAuth2Error.INVALID_GRANT.getDescription())).build()).build(); 
		
		StringTokenizer t = new StringTokenizer(id, ":");
		String userId = null;
		String sessionId = null;
		
		try {
			userId = t.nextToken();
			sessionId = t.nextToken();
		}catch(Exception e) {}
		
		
		if(userId == null || userId.isEmpty() || sessionId == null || sessionId.isEmpty())
			return Response.status(500).entity(new GenericResourceBuilder(false, new Error("Exception_Occured", "The resource não foi encontrado.")).build()).build();
				
		igrp.resource.Session session = null;
		try {
			session = (igrp.resource.Session) DAOHelper.getInstance().getEntityManager().createQuery("select t from Session t where t.sessionId = :_s or t.user.id = :_u order by startTime desc")
			 .setParameter("_s", sessionId).setParameter("_u", Integer.parseInt(userId))
			 .setMaxResults(1)
			 .getSingleResult();
		}catch(Exception e) {
			e.printStackTrace();
			return Response.status(500).entity(new GenericResourceBuilder(false, new Error("Exception_Occured", "The resource não foi encontrado.")).build()).build();
		}
		
		if(session == null) 
			return Response.status(500).entity(new GenericResourceBuilder(false, new Error("Exception_Occured", "The resource não foi encontrado.")).build()).build();
		
		return Response.status(200).entity(new GenericResourceBuilder(true, session).build()).build();
	}

}
