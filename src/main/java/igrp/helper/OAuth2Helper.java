package igrp.helper;

import java.net.URISyntaxException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

import javax.ws.rs.core.Response;

import igrp.oauth2.error.OAuth2Error;
import igrp.resource.oauth.PostData;
import igrp.resource.oauth.Token;
import igrp.resource.OAuthAccessToken;
import igrp.resource.OAuthClient;
import igrp.resource.OAuthRefreshToken;
import igrp.resource.User;
import igrp.resource.oauth.Error;
/**
 * @author Marcel Iekiny
 * Oct 4, 2017
 */
public final class OAuth2Helper { // Not inherit ...
	
	private OAuth2Helper() {} // Not instantiate ...
	
	/** A set of public static fields ... **/
	public static String idpUrl = "http://localhost:8080/IGRP/webapps?r=igrp/login/login"; // In this case use IGRP-Framework login page
	
	private static String DEFAULT_TOKEN_TYPE = "Bearer";
	
	/** A set of public static methods ... **/
	// Proccess all oauth2 GET request 
	public static Object doGet(String client_id, String response_type, String scope, String redirect_uri) {
		String url = OAuth2Helper.idpUrl + "&oauth=1";
		if(response_type == null || response_type.isEmpty()) {
			return Response.status(400).build();
		}
		switch(response_type) {
			case "code": 
			case "token": 
				url += (response_type != null && !response_type.isEmpty() ? "&response_type=" + response_type : "");
				url += (client_id != null && !client_id.isEmpty() ? "&client_id=" + client_id : "");
			try {
				url += (redirect_uri != null && !redirect_uri.isEmpty() ? "&redirect_uri=" + URLEncoder.encode(redirect_uri, "utf-8") : "");
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
				url += (scope != null && !scope.isEmpty() ? "&scope=" + scope : "");
			try {
				return Response.temporaryRedirect(new URI(url)).build();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			default:// Do nothing yet ...
		}
		return Response.status(400).build();
	}
	
	// Proccess all oauth2 POST request
	public static Object doPost(PostData data) {
		Object result = null;
		if(data.getGrant_type() != null)
			switch(data.getGrant_type()) {
			
				case "authorization_code":break;
				
				case "password": 
					result = resourceOwnerPasswordGrant(data.getUsername(), data.getPassword(), data.getClient_id(),data.getClient_secret(), data.getScope());
				break;
				
				case "refresh_token":
					result = refreshToken(data.getRefresh_token(), data.getScope(), data.getClient_id(),data.getClient_secret());
				break;
				
				case "client_credentials": // Not set yet
				default: result = new Error(OAuth2Error.UNSUPPORTED_GRANT_TYPE.name(), OAuth2Error.UNSUPPORTED_GRANT_TYPE.getDescription()); break;
			}
		else
			result = new Error(OAuth2Error.UNSUPPORTED_GRANT_TYPE.name(), OAuth2Error.UNSUPPORTED_GRANT_TYPE.getDescription());
		return result;
	}
	
	// OAuth2 response_type=code
	public static void authorizationCodeGrant(String client_id, String scope, String redirect_uri) {
		
	}
	
	// OAuth2 grant_type=authorization_code
	public static Object swapCodeByToken(String code, String client_id, String client_secret, String redirect_uri) {
		Object result = null;
		return result;
	}
	
	// OAuth2 response_type=token & grant_type=implicit
	public static void implicitGrant(String client_id, String scope, String redirect_uri) {
		
	}
	
	// OAuth2 grant_type=password
	public static Object resourceOwnerPasswordGrant(String username, String password, String client_id, String client_secret, String scope) { 
		User user = null;
		try {
			user = (User) DAOHelper.getInstance().getEntityManager().createQuery("select t from User t where t.user_name = :_u").setParameter("_u", username).getSingleResult();
		}catch(Exception e) {
			return new Error(OAuth2Error.INTERNAL_ERROR.name(), OAuth2Error.INTERNAL_ERROR.getDescription());
		}
		if(!user.getPass_hash().equals(password))
			return new Error(OAuth2Error.INTERNAL_ERROR.name(), OAuth2Error.INTERNAL_ERROR.getDescription());
				
		OAuthClient client = null;
		try {
			client = (OAuthClient) DAOHelper.getInstance().getEntityManager().createQuery("select t from OAuthClient t where t.client_id = :_c").setParameter("_c", client_id).getSingleResult();
		}catch(Exception e) {
			return new Error(OAuth2Error.INVALID_CLIENT.name(), OAuth2Error.INVALID_CLIENT.getDescription());
		}
		if(!client.getClient_secret().equals(client_secret))
			return new Error(OAuth2Error.INVALID_CLIENT_SECRET.name(), OAuth2Error.INVALID_CLIENT_SECRET.getDescription());
		
		if(!validateScope(scope, client))
			return new Error(OAuth2Error.INTERNAL_ERROR.name(), OAuth2Error.INTERNAL_ERROR.getDescription());
		
		OAuthAccessToken accessToken = new OAuthAccessToken();
		accessToken.setAccess_token(generateAccessToken());
		accessToken.setExpires(generateTokenExpires());
		accessToken.setAuthClient(client);
		accessToken.setUser(user);
		accessToken.setScope((scope == null || scope.isEmpty() ? client.getScope() : scope));
		
		OAuthRefreshToken refreshToken = new OAuthRefreshToken();
		refreshToken.setRefresh_token(generateRefreshToken());
		refreshToken.setAuthClient(client);
		refreshToken.setUser(user);
		refreshToken.setExpires(generatetRefreshTokenExpires());
		refreshToken.setScope(accessToken.getScope());
		
		DAOHelper.getInstance().getEntityManager().getTransaction().begin();
		DAOHelper.getInstance().getEntityManager().persist(accessToken);
		DAOHelper.getInstance().getEntityManager().persist(refreshToken);
		DAOHelper.getInstance().getEntityManager().getTransaction().commit();
		
		Token token = new Token();
		token.setAccess_token(accessToken.getAccess_token());
		token.setExpires_in(Integer.parseInt(getExpiresIn(accessToken)));
		token.setRefresh_token(refreshToken.getRefresh_token());
		token.setToken_type(DEFAULT_TOKEN_TYPE);
		
		return token;
	}
	
	// OAuth2 grant_type=client_credentials
	public static Object clientCredentialsGrant(String client_id, String client_secret) {
		Object result = null;
		return result;
	}
	
	// OAuth2 grant_type=refresh_token
	public static Object refreshToken(String refresh_token, String scope, String client_id, String client_secret) {
		Object result = null;
		
		OAuthRefreshToken refreshToken = null;
		try {
			refreshToken = (OAuthRefreshToken) DAOHelper.getInstance().getEntityManager().createQuery("select t from OAuthRefreshToken t where t.refresh_token = :_r").setParameter("_r", refresh_token).getSingleResult();
		}catch(Exception e) {
			return new Error(OAuth2Error.INVALID_REFRESH_TOKEN.name(), OAuth2Error.INVALID_REFRESH_TOKEN.getDescription());
		}
		if(refreshToken.getExpires().compareTo("" + System.currentTimeMillis()) <= 0)
			return new Error(OAuth2Error.TOKEN_EXPIRED.name(), OAuth2Error.TOKEN_EXPIRED.getDescription());
		
		OAuthAccessToken accessToken = null;
		try {
			accessToken = (OAuthAccessToken) DAOHelper.getInstance().getEntityManager().createQuery("select t from OAuthAccessToken t where t.client_id = :_c and t.user_id = :_u and t.scope = :_s ORDER BY t.id desc").
					setParameter("_c", refreshToken.getAuthClient().getClient_id()).
					setParameter("_u", refreshToken.getUser().getId()).
					setParameter("_s", refreshToken.getScope()).getSingleResult();
		}catch(Exception e) {
			e.printStackTrace();
		}
		if(!validateScope(scope, accessToken))
			return new Error(OAuth2Error.INTERNAL_ERROR.name(), OAuth2Error.INTERNAL_ERROR.getDescription());
		
		OAuthClient client = null;
		try {
			client = (OAuthClient) DAOHelper.getInstance().getEntityManager().createQuery("select t from OAuthClient t where t.client_id = :_c").setParameter("_c", client_id).getSingleResult();
		}catch(Exception e) {
			return new Error(OAuth2Error.INVALID_CLIENT.name(), OAuth2Error.INVALID_CLIENT.getDescription());
		}
		if(!client.getClient_secret().equals(client_secret))
			return new Error(OAuth2Error.INVALID_CLIENT_SECRET.name(), OAuth2Error.INVALID_CLIENT_SECRET.getDescription());
		
		if(!refreshToken.getAuthClient().getClient_id().equals(client.getClient_id()))
			return new Error(OAuth2Error.INVALID_CLIENT.name(), OAuth2Error.INVALID_CLIENT.getDescription());
		
		OAuthAccessToken accessToken_ = new OAuthAccessToken();
		accessToken_.setAccess_token(generateAccessToken());
		accessToken_.setExpires(generateTokenExpires());
		accessToken_.setAuthClient(accessToken.getAuthClient());
		accessToken_.setUser(accessToken.getUser());
		accessToken_.setScope((scope == null || scope.isEmpty() ?  accessToken.getScope() : scope));
		
		OAuthRefreshToken refreshToken_ = new OAuthRefreshToken();
		refreshToken_.setRefresh_token(generateRefreshToken());
		refreshToken_.setAuthClient(refreshToken.getAuthClient());
		refreshToken_.setUser(refreshToken.getUser());
		refreshToken_.setExpires(generatetRefreshTokenExpires());
		refreshToken_.setScope(accessToken_.getScope());
		
		DAOHelper.getInstance().getEntityManager().getTransaction().begin();
		DAOHelper.getInstance().getEntityManager().persist(accessToken_);
		DAOHelper.getInstance().getEntityManager().persist(refreshToken_);
		DAOHelper.getInstance().getEntityManager().getTransaction().commit();
		
		Token token = new Token();
		token.setAccess_token(accessToken_.getAccess_token());
		token.setExpires_in(Integer.parseInt(getExpiresIn(accessToken_)));
		token.setRefresh_token(refreshToken_.getRefresh_token());
		token.setToken_type(DEFAULT_TOKEN_TYPE);
		
		return token;
	}
	
	/** Aux. algorithm **/
	private static String generateAccessToken() {
		return java.util.UUID.randomUUID().toString().replaceAll("-", "");
	}
	
	private static String generateRefreshToken() {
		return java.util.UUID.randomUUID().toString().replaceAll("-", "");
	}
	
	private static String generateTokenExpires() {
		return "" + (System.currentTimeMillis() + 1000*3600); // 1h
	}
	
	private static String generatetRefreshTokenExpires() {
		return "" + (System.currentTimeMillis() + 1000*3600*24*7); // a week
	}
	
	private static String getExpiresIn(OAuthAccessToken accessToken) {
		return "" + ((Long.parseLong(accessToken.getExpires()) - System.currentTimeMillis())/1000);
	}
	
	private static boolean validateScope(String scopes, OAuthClient client) { // Ex.: scope1,scope2,...,scopeN
		if(scopes == null || scopes.isEmpty()) 
			return true; // scopes is optional in this case
		String []aux = scopes.split(",");
		for(String obj : aux)
			if(!client.getScope().contains(obj))
				return false;
		return true;
	}
	// overload
	private static boolean validateScope(String scopes, OAuthAccessToken accessToken) { // Ex.: scope1,scope2,...,scopeN
		if(scopes == null || scopes.isEmpty()) 
			return true; // scopes is optional in this case
		String []aux = scopes.split(",");
		for(String obj : aux)
			if(!accessToken.getScope().contains(obj))
				return false;
		return true;
	}
}
