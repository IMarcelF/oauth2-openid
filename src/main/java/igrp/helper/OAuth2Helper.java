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
import igrp.resource.OAuthorizationCode;
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
	public static Object doGet(String client_id, String response_type, String scope, String redirect_uri, String authorize) {
		/** Go to https://tools.ietf.org/html/rfc6749#section-3.3 for more details 
		 *  Anyway, you can use scope as list of string separate by comma too
		 */
		scope = scope.replaceAll("(\\s|%20)", ",");
		/***/
		String url = OAuth2Helper.idpUrl + "&oauth=1";
		if(response_type == null || response_type.isEmpty()) {
			return Response.status(400).build();
		}
		switch(response_type) {
		
			case "code": 
				if(authorize != null && !authorize.isEmpty())
					return authorizationCodeGrant(client_id, scope, redirect_uri);
				
			case "token":
				
				if(authorize != null && !authorize.isEmpty())
					return implicitGrant(client_id, scope, redirect_uri);
				
				url += (response_type != null && !response_type.isEmpty() ? "&response_type=" + response_type : "");
				url += (client_id != null && !client_id.isEmpty() ? "&client_id=" + client_id : "");
			try {
				url += (redirect_uri != null && !redirect_uri.isEmpty() ? "&redirect_uri=" + URLEncoder.encode(redirect_uri, "utf-8") : "");
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
				url += (scope != null && !scope.isEmpty() ? "&scope=" + scope : "");
			try {
				if(url.contains("&response_type=") && url.contains("&client_id=") && url.contains("&scope=") && url.contains("&redirect_uri=")) 
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
		/** Go to https://tools.ietf.org/html/rfc6749#section-3.3 for more details 
		 *  Anyway, you can use scope as list of string separate by comma too
		 */
		data.setScope(data.getScope().replaceAll("(\\s|%20)", ","));
		/***/
		Object result = null;
		if(data.getGrant_type() != null)
			switch(data.getGrant_type()) {
			
				case "authorization_code"://code, client_id, client_secret, redirect_uri
					result = swapCodeByToken(data.getCode(), data.getClient_id(), data.getClient_secret(), data.getRedirect_uri());
				break;
				
				case "password": 
					result = resourceOwnerPasswordGrant(data.getUsername(), data.getPassword(), data.getClient_id(),data.getClient_secret(), data.getScope());
				break;
				
				case "refresh_token":
					result = refreshToken(data.getRefresh_token(), data.getScope(), data.getClient_id(),data.getClient_secret());
				break;
				
				case "client_credentials":
					result = clientCredentialsGrant(data.getClient_id(), data.getClient_secret(), data.getScope());
				break;
				
				default: result = new Error(OAuth2Error.UNSUPPORTED_GRANT_TYPE.name(), OAuth2Error.UNSUPPORTED_GRANT_TYPE.getDescription()); break;
			}
		else
			result = new Error(OAuth2Error.UNSUPPORTED_GRANT_TYPE.name(), OAuth2Error.UNSUPPORTED_GRANT_TYPE.getDescription());
		return result;
	}
	
	// OAuth2 response_type=code
	public static Response authorizationCodeGrant(String client_id, String scope, String redirect_uri) {
		String username = "demo"; // eliminar
		String url = "";
		String queryString = "";
		boolean die = false;
		User user = null;
		OAuthClient client = null;
		
		try {
			client = (OAuthClient) DAOHelper.getInstance().getEntityManager().createQuery("select t from OAuthClient t where t.client_id = :_c").setParameter("_c", client_id).getSingleResult();
		}catch(Exception e) {
			die = true;
			queryString = "?error=" + OAuth2Error.INVALID_CLIENT;
			e.printStackTrace();
		}
		url += (client == null || (redirect_uri != null && !redirect_uri.isEmpty()) ? redirect_uri : client.getRedirect_uri());
		
		if(!die && client != null && !validateScope(scope, client)) {
			queryString = "?error=" + OAuth2Error.INVALID_SCOPE;
			die = true;
		}
		
		if(!die) {
			try {
				user = (User) DAOHelper.getInstance().getEntityManager().createQuery("select t from User t where t.user_name = :_u").setParameter("_u", username).getSingleResult();
			}catch(Exception e) {
				die = true;
				e.printStackTrace(); // For log
				queryString = "?error=" + OAuth2Error.INVALID_USER;
			}
		}
		
		if(!die) {
			OAuthorizationCode code = new OAuthorizationCode();
			code.setUser(user);
			code.setAuthClient(client);
			code.setAuthorization_code(generateAuthorizationCode());
			if(redirect_uri != null && !redirect_uri.isEmpty())
				code.setRedirect_uri(redirect_uri);
			code.setExpires(generateCodeExpires());
			code.setScope(scope);
			
			DAOHelper.getInstance().getEntityManager().getTransaction().begin();
			DAOHelper.getInstance().getEntityManager().persist(code);
			DAOHelper.getInstance().getEntityManager().getTransaction().commit();
			
			queryString = "?code=" + code.getAuthorization_code();
		}
		
		try {
			url += queryString;
			return Response.temporaryRedirect(new URI(url)).build();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
	return Response.status(400).build();
	}
	
	// OAuth2 response_type=token & grant_type=implicit
	public static Object implicitGrant(String client_id, String scope, String redirect_uri) {
		String username = "demo"; // Eliminar
		String url = "";
		String queryString = "";
		boolean die = false;
		User user = null;
		OAuthClient client = null;
		
		try {
			client = (OAuthClient) DAOHelper.getInstance().getEntityManager().createQuery("select t from OAuthClient t where t.client_id = :_c").setParameter("_c", client_id).getSingleResult();
		}catch(Exception e) {
			die = true;
			queryString = "#error=" + OAuth2Error.INVALID_CLIENT;
			e.printStackTrace();
		}
		url += (client == null || (redirect_uri != null && !redirect_uri.isEmpty()) ? redirect_uri : client.getRedirect_uri());
		
		if(!die && client != null && !validateScope(scope, client)) {
			queryString = "#error=" + OAuth2Error.INVALID_SCOPE;
			die = true;
		}
		
		if(!die) {
			try {
				user = (User) DAOHelper.getInstance().getEntityManager().createQuery("select t from User t where t.user_name = :_u").setParameter("_u", username).getSingleResult();
			}catch(Exception e) {
				die = true;
				e.printStackTrace(); // For log
				queryString = "#error=" + OAuth2Error.INVALID_USER;
			}
		}
		
		if(!die) {
			/** Reuse the existing token if it is alive ... **/
			OAuthAccessToken token = null;
			boolean generateNewToken = false;
			try {
				token = (OAuthAccessToken) DAOHelper.getInstance().getEntityManager().createQuery("select t from OAuthAccessToken t where t.scope = :_s and t.authClient.client_id = :_c and t.user.id = :_u ORDER BY t.id desc").
						setParameter("_c", client.getClient_id()).
						setParameter("_s", scope).
						setParameter("_u", user.getId()).
						setMaxResults(1).
						getSingleResult();
			
				if(token.getExpires().compareTo(System.currentTimeMillis() + "") <= 0)
					generateNewToken = true;
					
			}catch(Exception e) {
				e.printStackTrace();
				generateNewToken = true;
				// Go ahead ...
			}
			
			if(generateNewToken) {
				/** Otherwise generate new token **/
				token = new OAuthAccessToken(); 
				token.setAccess_token(generateAccessToken());
				token.setExpires(generateTokenExpires());
				token.setAuthClient(client);
				token.setUser(user);
				token.setScope(scope);
				
				DAOHelper.getInstance().getEntityManager().getTransaction().begin();
				DAOHelper.getInstance().getEntityManager().persist(token);
				DAOHelper.getInstance().getEntityManager().getTransaction().commit();
			}
			
			queryString = "#token=" + token.getAccess_token();
		}
		
		try {
			url += queryString;
			return Response.temporaryRedirect(new URI(url)).build();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return Response.status(400).build();
	}
	
	// OAuth2 grant_type=authorization_code 
	public static Object swapCodeByToken(String code, String client_id, String client_secret, String redirect_uri) {
		OAuthorizationCode authorizationCode = null;
		try {
			authorizationCode = (OAuthorizationCode) DAOHelper.getInstance().getEntityManager().createQuery("select t from OAuthorizationCode t where t.authorization_code = :_c").setParameter("_c", code).getSingleResult();
		}catch(Exception e) {
			e.printStackTrace(); 
			return new Error(OAuth2Error.INVALID_AUTHORIZATION_CODE.name(), OAuth2Error.INVALID_AUTHORIZATION_CODE.getDescription());
		}
		if(authorizationCode.getExpires().compareTo(System.currentTimeMillis() + "") <= 0)
			return new Error(OAuth2Error.INVALID_AUTHORIZATION_CODE.name(), OAuth2Error.INVALID_AUTHORIZATION_CODE.getDescription());
		
		if(authorizationCode.getRedirect_uri() != null && !authorizationCode.getRedirect_uri().equals(redirect_uri))
			return new Error(OAuth2Error.INVALID_REDIRECT_URI.name(), OAuth2Error.INVALID_REDIRECT_URI.getDescription());
	
		OAuthClient client = null;
		try {
			client = (OAuthClient) DAOHelper.getInstance().getEntityManager().createQuery("select t from OAuthClient t where t.client_id = :_c").setParameter("_c", client_id).getSingleResult();
		}catch(Exception e) {
			return new Error(OAuth2Error.INVALID_CLIENT.name(), OAuth2Error.INVALID_CLIENT.getDescription());
		}
		if(!client.getClient_secret().equals(client_secret))
			return new Error(OAuth2Error.INVALID_CLIENT_SECRET.name(), OAuth2Error.INVALID_CLIENT_SECRET.getDescription());
		
		if(!authorizationCode.getAuthClient().getClient_id().equals(client.getClient_id()))
			return new Error(OAuth2Error.INVALID_CLIENT.name(), OAuth2Error.INVALID_CLIENT.getDescription());
		
		OAuthAccessToken accessToken = null;
		OAuthRefreshToken refreshToken = null;
		try {
			accessToken = (OAuthAccessToken) DAOHelper.getInstance().getEntityManager().createQuery("select t from OAuthAccessToken t where t.scope = :_s and t.authClient.client_id = :_c and t.user.id = :_u ORDER BY t.id desc").
					setParameter("_c", authorizationCode.getAuthClient().getClient_id()).
					setParameter("_s", authorizationCode.getScope()).
					setParameter("_u", authorizationCode.getUser().getId()).
					setMaxResults(1).
					getSingleResult();
			
			refreshToken = (OAuthRefreshToken) DAOHelper.getInstance().getEntityManager().createQuery("select t from OAuthRefreshToken t where t.scope = :_s and t.authClient.client_id = :_c and t.user.id = :_u ORDER BY t.id desc").
					setParameter("_c", authorizationCode.getAuthClient().getClient_id()).
					setParameter("_s", authorizationCode.getScope()).
					setParameter("_u", authorizationCode.getUser().getId()).
					setMaxResults(1).
					getSingleResult();
			
			if(accessToken.getExpires().compareTo(System.currentTimeMillis() + "") > 0 && refreshToken.getExpires().compareTo(System.currentTimeMillis() + "") > 0)
				return generateOAuth2Token(accessToken, refreshToken);
			
		}catch(Exception e) {
			e.printStackTrace();
			// Go ahead ...
		}
		
		accessToken = new OAuthAccessToken(); 
		accessToken.setAccess_token(generateAccessToken());
		accessToken.setExpires(generateTokenExpires());
		accessToken.setAuthClient(client);
		accessToken.setUser(authorizationCode.getUser());
		accessToken.setScope(authorizationCode.getScope());
		
		refreshToken = new OAuthRefreshToken();
		refreshToken.setRefresh_token(generateRefreshToken());
		refreshToken.setAuthClient(client);
		refreshToken.setUser(authorizationCode.getUser());
		refreshToken.setExpires(generatetRefreshTokenExpires());
		refreshToken.setScope(authorizationCode.getScope());
		
		DAOHelper.getInstance().getEntityManager().getTransaction().begin();
		DAOHelper.getInstance().getEntityManager().persist(accessToken);
		DAOHelper.getInstance().getEntityManager().persist(refreshToken);
		DAOHelper.getInstance().getEntityManager().getTransaction().commit();
		
		return generateOAuth2Token(accessToken, refreshToken);
	}
	
	// OAuth2 grant_type=password
	public static Object resourceOwnerPasswordGrant(String username, String password, String client_id, String client_secret, String scope) { 
		User user = null;
		try {
			user = (User) DAOHelper.getInstance().getEntityManager().createQuery("select t from User t where t.user_name = :_u").setParameter("_u", username).getSingleResult();
		}catch(Exception e) {
			e.printStackTrace(); // For log
			return new Error(OAuth2Error.INVALID_USER.name(), OAuth2Error.INVALID_USER.getDescription());
		}
		if(!user.getPass_hash().equals(password))
			return new Error(OAuth2Error.INVALID_USER_CREDENTIALS.name(), OAuth2Error.INVALID_USER_CREDENTIALS.getDescription());
		
		OAuthClient client = null;
		try {
			client = (OAuthClient) DAOHelper.getInstance().getEntityManager().createQuery("select t from OAuthClient t where t.client_id = :_c").setParameter("_c", client_id).getSingleResult();
		}catch(Exception e) {
			e.printStackTrace();
			return new Error(OAuth2Error.INVALID_CLIENT.name(), OAuth2Error.INVALID_CLIENT.getDescription());
		}
		if(!client.getClient_secret().equals(client_secret))
			return new Error(OAuth2Error.INVALID_CLIENT_SECRET.name(), OAuth2Error.INVALID_CLIENT_SECRET.getDescription());
		
		if(!validateScope(scope, client))
			return new Error(OAuth2Error.INVALID_SCOPE.name(), OAuth2Error.INVALID_SCOPE.getDescription());
		
		OAuthAccessToken accessToken = null;
		OAuthRefreshToken refreshToken = null;
		try { // reuse existing token 
			accessToken = (OAuthAccessToken) DAOHelper.getInstance().getEntityManager().createQuery("select t from OAuthAccessToken t where t.scope = :_s and t.authClient.client_id = :_c and t.user.id = :_u ORDER BY t.id desc").
					setParameter("_c",client.getClient_id()).
					setParameter("_s", (scope == null || scope.isEmpty() ? client.getScope() : scope)).
					setParameter("_u", user.getId()).
					setMaxResults(1).
					getSingleResult();
			
			refreshToken = (OAuthRefreshToken) DAOHelper.getInstance().getEntityManager().createQuery("select t from OAuthRefreshToken t where t.scope = :_s and t.authClient.client_id = :_c and t.user.id = :_u ORDER BY t.id desc").
					setParameter("_c", client.getClient_id()).
					setParameter("_s", accessToken.getScope()).
					setParameter("_u", user.getId()).
					setMaxResults(1).
					getSingleResult();
			
			if(accessToken.getExpires().compareTo(System.currentTimeMillis() + "") > 0 && refreshToken.getExpires().compareTo(System.currentTimeMillis() + "") > 0)
				return generateOAuth2Token(accessToken, refreshToken);
			
		}catch(Exception e) {
			e.printStackTrace();
			// Go ahead ...
		}
		
		accessToken = new OAuthAccessToken();
		accessToken.setAccess_token(generateAccessToken());
		accessToken.setExpires(generateTokenExpires());
		accessToken.setAuthClient(client);
		accessToken.setUser(user);
		accessToken.setScope((scope == null || scope.isEmpty() ? client.getScope() : scope));
		
		refreshToken = new OAuthRefreshToken();
		refreshToken.setRefresh_token(generateRefreshToken());
		refreshToken.setAuthClient(client);
		refreshToken.setUser(user);
		refreshToken.setExpires(generatetRefreshTokenExpires());
		refreshToken.setScope(accessToken.getScope());
		
		DAOHelper.getInstance().getEntityManager().getTransaction().begin();
		DAOHelper.getInstance().getEntityManager().persist(accessToken);
		DAOHelper.getInstance().getEntityManager().persist(refreshToken);
		DAOHelper.getInstance().getEntityManager().getTransaction().commit();
		
		return generateOAuth2Token(accessToken, refreshToken);
	}
	
	// OAuth2 grant_type=client_credentials
	public static Object clientCredentialsGrant(String client_id, String client_secret, String scope) {
		OAuthClient client = null;
		try {
			client = (OAuthClient) DAOHelper.getInstance().getEntityManager().createQuery("select t from OAuthClient t where t.client_id = :_c").setParameter("_c", client_id).getSingleResult();
		}catch(Exception e) {
			e.printStackTrace();
			return new Error(OAuth2Error.INVALID_CLIENT.name(), OAuth2Error.INVALID_CLIENT.getDescription());
		}
		if(!client.getClient_secret().equals(client_secret))
			return new Error(OAuth2Error.INVALID_CLIENT_SECRET.name(), OAuth2Error.INVALID_CLIENT_SECRET.getDescription());
		
		if(!validateScope(scope, client))
			return new Error(OAuth2Error.INVALID_SCOPE.name(), OAuth2Error.INVALID_SCOPE.getDescription());
		
		OAuthAccessToken accessToken = null;
		/** Reuse existing token if it is alive ... **/
		try {
			accessToken = (OAuthAccessToken) DAOHelper.getInstance().getEntityManager().createQuery("select t from OAuthAccessToken t where t.scope = :_s and t.authClient.client_id = :_c and t.user.id = :_u ORDER BY t.id desc").
					setParameter("_c",client.getClient_id()).
					setParameter("_s", (scope == null || scope.isEmpty() ? client.getScope() : scope)).
					setParameter("_u",client.getUser().getId()).
					setMaxResults(1).
					getSingleResult();
			
			if(accessToken.getExpires().compareTo(System.currentTimeMillis() + "") > 0)
				return generateOAuth2Token(accessToken);
			
		}catch(Exception e) {
			e.printStackTrace();
			// Go ahead ...
		}
		/** Otherwise generate a new token **/
		accessToken = new OAuthAccessToken();
		accessToken.setAccess_token(generateAccessToken());
		accessToken.setExpires(generateTokenExpires());
		accessToken.setAuthClient(client);
		accessToken.setUser(client.getUser());
		accessToken.setScope((scope == null || scope.isEmpty() ? client.getScope() : scope));
		
		DAOHelper.getInstance().getEntityManager().getTransaction().begin();
		DAOHelper.getInstance().getEntityManager().persist(accessToken);
		DAOHelper.getInstance().getEntityManager().getTransaction().commit();
		
		return generateOAuth2Token(accessToken);
	}
	
	// OAuth2 grant_type=refresh_token
	public static Object refreshToken(String refresh_token, String scope, String client_id, String client_secret) {
		Object result = null;
		
		OAuthRefreshToken refreshToken = null;
		try {
			refreshToken = (OAuthRefreshToken) DAOHelper.getInstance().getEntityManager().createQuery("select t from OAuthRefreshToken t where t.refresh_token = :_r").setParameter("_r", refresh_token).getSingleResult();
		}catch(Exception e) {
			e.printStackTrace();
			return new Error(OAuth2Error.INVALID_REFRESH_TOKEN.name(), OAuth2Error.INVALID_REFRESH_TOKEN.getDescription());
		}
		if(refreshToken.getExpires().compareTo("" + System.currentTimeMillis()) <= 0)
			return new Error(OAuth2Error.TOKEN_EXPIRED.name(), OAuth2Error.TOKEN_EXPIRED.getDescription());
		
		OAuthAccessToken accessToken = null;
		try {
			accessToken = (OAuthAccessToken) DAOHelper.getInstance().getEntityManager().createQuery("select t from OAuthAccessToken t where t.authClient.client_id = :_c and t.user.id = :_u and t.scope = :_s ORDER BY t.id desc").
					setMaxResults(1).
					setParameter("_c", refreshToken.getAuthClient().getClient_id()).
					setParameter("_u", refreshToken.getUser().getId()).
					setParameter("_s", refreshToken.getScope()).getSingleResult();
		}catch(Exception e) {
			e.printStackTrace();
		}
		if(!validateScope(scope, accessToken))
			return new Error(OAuth2Error.INVALID_SCOPE.name(), OAuth2Error.INVALID_SCOPE.getDescription());
		
		OAuthClient client = null;
		try {
			client = (OAuthClient) DAOHelper.getInstance().getEntityManager().createQuery("select t from OAuthClient t where t.client_id = :_c").setParameter("_c", client_id).getSingleResult();
		}catch(Exception e) {
			e.printStackTrace();
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
		
		return generateOAuth2Token(accessToken_, refreshToken_);
	}
	
	/** Aux. algorithm **/
	private static String generateAccessToken() {
		return java.util.UUID.randomUUID().toString().replaceAll("-", "");
	}
	
	private static String generateRefreshToken() {
		return java.util.UUID.randomUUID().toString().replaceAll("-", "");
	}
	
	private static String generateAuthorizationCode() { // for now ... equals to previous methods
		return java.util.UUID.randomUUID().toString().replaceAll("-", "");
	}
	
	private static String generateTokenExpires() {
		return "" + (System.currentTimeMillis() + 1000*3600); // 1h
	}
	
	private static String generateCodeExpires() {
		return "" + (System.currentTimeMillis() + 1000*60*3); // 3min
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
	
	private static Token generateOAuth2Token(OAuthAccessToken accessToken, OAuthRefreshToken refreshToken) {
		Token token = new Token();
		token.setAccess_token(accessToken.getAccess_token());
		token.setExpires_in(Integer.parseInt(getExpiresIn(accessToken)));
		token.setRefresh_token(refreshToken.getRefresh_token());
		token.setToken_type(DEFAULT_TOKEN_TYPE);
	return token;
	}
	// overload
	private static Token generateOAuth2Token(OAuthAccessToken accessToken) {
		Token token = new Token();
		token.setAccess_token(accessToken.getAccess_token());
		token.setExpires_in(Integer.parseInt(getExpiresIn(accessToken)));
		token.setToken_type(DEFAULT_TOKEN_TYPE);
	return token;
	}
}
