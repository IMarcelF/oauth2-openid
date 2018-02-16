package igrp.service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
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
import igrp.resource.GenericResourceBuilder;
import igrp.resource.oauth2.OAuthAccessToken;
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
		
		DAOHelper.getInstance().getSession().beginTransaction();
		
		if(!OAuth2Helper.isValidToken(token, Scope.USER_READ + "")) 
			return Response.status(OAuth2Error.INVALID_GRANT.getStatus()).entity(new GenericResourceBuilder(false, new Error("" + OAuth2Error.INVALID_GRANT.getStatus(), OAuth2Error.INVALID_GRANT.getDescription())).build()).build(); 
		
		igrp.resource.User user = null;
		try {
			CriteriaBuilder criteriaBuilder = DAOHelper.getInstance().getSession().getCriteriaBuilder();
			CriteriaQuery<Object> criteriaQuery = criteriaBuilder.createQuery();
			Root<igrp.resource.User> u = criteriaQuery.from(igrp.resource.User.class);
			criteriaQuery.select(u).where(
					criteriaBuilder.or(
							criteriaBuilder.equal(u.get("user_name"), criteriaBuilder.parameter(String.class, "_u")), 
							criteriaBuilder.equal(u.get("email"), criteriaBuilder.parameter(String.class, "_m")))
					);
			user = (igrp.resource.User)DAOHelper.getInstance().getSession().
					createQuery(criteriaQuery).
					setParameter("_u", id).setParameter("_m", id).
					setMaxResults(1).
					getSingleResult();
			
		}catch(Exception e) {
			e.printStackTrace();
			return Response.status(500).entity(new GenericResourceBuilder(false, new Error("Exception_Occured", "The resource não foi encontrado.")).build()).build();
		}
		if(user == null) 
			return Response.status(404).entity(new GenericResourceBuilder(false, new Error("404", "Resource not found.")).build()).build();
		
		return Response.status(200).entity(new GenericResourceBuilder(true, user).build()).build();
	}
	
	@GET
	@Path("/token")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response getUser(@HeaderParam(value = "Authorization") String token) {
		
		DAOHelper.getInstance().getSession().beginTransaction();
		
		OAuthAccessToken accessToken = new OAuthAccessToken();
		
		if(!OAuth2Helper.isValidToken(token, Scope.USER_READ + "", accessToken)) 
			return Response.status(OAuth2Error.INVALID_GRANT.getStatus()).entity(new GenericResourceBuilder(false, new Error("" + OAuth2Error.INVALID_GRANT.getStatus(), OAuth2Error.INVALID_GRANT.getDescription())).build()).build(); 
		
		//System.out.println(accessToken);
		
		igrp.resource.User user = null;
		try {
			user = accessToken.getUser();
		}catch(Exception e) {
			
		}
		
		if(user == null) 
			return Response.status(404).entity(new GenericResourceBuilder(false, new Error("404", "Resource not found.")).build()).build();
		
		return Response.status(200).entity(new GenericResourceBuilder(true, user).build()).build();
	}
	
	@GET
	@Path("/token/kill")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response killCurrentToken(@HeaderParam(value = "Authorization") String token) {
		
		DAOHelper.getInstance().getSession().beginTransaction();
		
		OAuthAccessToken accessToken = new OAuthAccessToken();
		
		if(!OAuth2Helper.isValidToken(token, Scope.USER_READ + "", accessToken)) 
			return Response.status(OAuth2Error.INVALID_GRANT.getStatus()).entity(new GenericResourceBuilder(false, new Error("" + OAuth2Error.INVALID_GRANT.getStatus(), OAuth2Error.INVALID_GRANT.getDescription())).build()).build(); 
		
		accessToken.setExpires("-1");
		DAOHelper.getInstance().getSession().flush();
		DAOHelper.getInstance().getSession().clear();
		DAOHelper.getInstance().getSession().update(accessToken);
		DAOHelper.getInstance().getSession().getTransaction().commit();
		
		return Response.status(200).entity(new GenericResourceBuilder(true, accessToken).build()).build();
	}
	
}
