package igrp.service;

import java.util.StringTokenizer;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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
		
		DAOHelper.getInstance().getSession().beginTransaction();
		
		if(!OAuth2Helper.isValidToken(token, Scope.SESSION_READ + "")) 
			return Response.status(OAuth2Error.INVALID_GRANT.getStatus()).entity(new GenericResourceBuilder(false, new Error("" + OAuth2Error.INVALID_GRANT.getStatus(), OAuth2Error.INVALID_GRANT.getDescription())).build()).build(); 
		
		StringTokenizer t = new StringTokenizer(id, ":");
		String pk = null;
		String sessionId = null;
		try {
			pk = t.nextToken();
			sessionId = t.nextToken();
		}catch(Exception e) {}
		
		if(pk == null || pk.isEmpty() || sessionId == null || sessionId.isEmpty())
			return Response.status(500).entity(new GenericResourceBuilder(false, new Error("Exception_Occured", "The resource was not found.")).build()).build();
				
		igrp.resource.Session session = null; 
		try {
			session = (igrp.resource.Session) DAOHelper.getInstance().getSession().createQuery("select t from Session t where t.id = :_sPk and t.sessionId = :_sId")
					 .setParameter("_sId", sessionId).setParameter("_sPk", Integer.parseInt(pk))
					 .setMaxResults(1)
					 .getSingleResult();
		}catch(Exception e) {
			e.printStackTrace();
			return Response.status(500).entity(new GenericResourceBuilder(false, new Error("Exception_Occured", "The resource was not found.")).build()).build();
		}
		
		if(session == null) 
			return Response.status(500).entity(new GenericResourceBuilder(false, new Error("Exception_Occured", "The resource was not found.")).build()).build();
		
		//request.getScheme() + "://" + request.getLocalAddr() + ":" + request.getServerPort() + "/IGRP/webapps?r=igrp/home/index"; 
		session.setHomeUrl(session.getUrl() + "?r=igrp/home/index");
		session.setLoginUrl(session.getUrl() + "?r=igrp/login/logout"); 
		
		return Response.status(200).entity(new GenericResourceBuilder(true, session).build()).build();
	}

}
