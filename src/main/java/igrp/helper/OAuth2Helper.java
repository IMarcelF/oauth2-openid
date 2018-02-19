package igrp.helper;

import java.net.URISyntaxException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.security.Key;
import java.util.Base64;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.ws.rs.core.Response;

import org.hibernate.Transaction;

import igrp.oauth2.util.OAuth2Error;
import igrp.oauth2.util.Scope;
import igrp.resource.User;
import igrp.resource.oauth2.Error;
import igrp.resource.oauth2.OAuthAccessToken;
import igrp.resource.oauth2.OAuthClient;
import igrp.resource.oauth2.OAuthRefreshToken;
import igrp.resource.oauth2.OAuthorizationCode;
import igrp.resource.oauth2.PostData;
import igrp.resource.oauth2.Token;
import igrp.resource.openid.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;
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
	public static Object doGet(String client_id, String response_type, String scope, String redirect_uri, String authorize, String url, String userId) {
		/** Go to https://tools.ietf.org/html/rfc6749#section-3.3 for more details 
		 *  Anyway, you can use scope as list of string separate by comma too 
		 */
		scope = scope.replaceAll("(\\s|%20)", ",");
		/***/
		if(url == null || url.isEmpty())
			url = new String(OAuth2Helper.idpUrl) + "&oauth=1";
		else
			url += "/IGRP/webapps?r=igrp/login/login" + "&oauth=1";
		
		if(response_type == null || response_type.isEmpty()) {
			return Response.status(400).build();
		}
		
		try {
			userId = new String(Base64.getDecoder().decode(userId));
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		switch(response_type) {
		
			case "code": 
				if(authorize != null && !authorize.isEmpty())
					return authorizationCodeGrant(client_id, scope, redirect_uri, userId);
			
			case "id_token":
				if(authorize != null && !authorize.isEmpty())
					return implicitGrant(client_id, scope, redirect_uri, userId, 1);
			case "id_token token":	
				if(authorize != null && !authorize.isEmpty())
					return implicitGrant(client_id, scope, redirect_uri, userId, 2);
			case "token":
				
				if(authorize != null && !authorize.isEmpty())
					return implicitGrant(client_id, scope, redirect_uri, userId, 0);
				
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
		try {
			data.setScope(data.getScope().replaceAll("(\\s|%20)", ","));
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
		/***/
		Object result = null;
		if(data.getGrant_type() != null)
			switch(data.getGrant_type()) {
			
				case "authorization_code":
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
	public static Response authorizationCodeGrant(String client_id, String scope, String redirect_uri, String username) {
		Transaction t = DAOHelper.getInstance().getSession().beginTransaction();
		//String username = "demo"; // eliminar
		String url = "";
		String queryString = "";
		boolean die = false;
		User user = null;
		OAuthClient client = null;
		
		try {
			client = (OAuthClient) DAOHelper.getInstance().getSession().createQuery("select t from OAuthClient t where t.client_id = :_c").setParameter("_c", client_id).getSingleResult();
		}catch(Exception e) {
			die = true;
			queryString = "?error=" + OAuth2Error.INVALID_CLIENT;
			e.printStackTrace();
		}
		url += (client == null || (redirect_uri != null && !redirect_uri.trim().isEmpty()) ? redirect_uri : client.getRedirect_uri().trim());
		
		if(!die && client != null && !validateScope(scope, client)) {
			queryString = "?error=" + OAuth2Error.INVALID_SCOPE;
			die = true;
		}
		
		if(!die) {
			try {
				user = (User) DAOHelper.getInstance().getSession().createQuery("select t from User t where t.user_name = :_u").setParameter("_u", username).getSingleResult();
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
			
			DAOHelper.getInstance().getSession().persist(code);
			t.commit();
			
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
	
	// OAuth2 OpenId-Connect grant_type=implicit 
	// openIdFlag == 0 -> OAuth2 response_type=token & grant_type=implicit 
	// openIdFlag == 1 -> OAuth2 response_type=id_token%20token & grant_type=implicit 
	// openIdFlag == 2 -> OAuth2 response_type=id_token & grant_type=implicit 
	public static Object implicitGrant(String client_id, String scope, String redirect_uri, String userId, int openIdFlag) {
		Transaction t = DAOHelper.getInstance().getSession().beginTransaction();
		//String username = "demo"; // Eliminar 
		String username = userId;
		String url = "";
		String queryString = "";
		boolean die = false;
		User user = null;
		OAuthClient client = null;
		
		try {
			client = (OAuthClient) DAOHelper.getInstance().getSession().createQuery("select t from OAuthClient t where t.client_id = :_c").setParameter("_c", client_id).getSingleResult();
		}catch(Exception e) {
			die = true;
			queryString = "#error=" + OAuth2Error.INVALID_CLIENT;
			e.printStackTrace();
		}
		url += (client == null || (redirect_uri != null && !redirect_uri.isEmpty()) ? redirect_uri : client.getRedirect_uri());
		
		if((!die && client != null && !validateScope(scope, client)) 
				|| (openIdFlag != 0 && !scope.contains(Scope.OPENID.getValue())) // "openid" scope is required in this case ... 
				) {
			queryString = "#error=" + OAuth2Error.INVALID_SCOPE;
			die = true;
		}
		
		if(!die) {
			try {
				user = (User) DAOHelper.getInstance().getSession().createQuery("select t from User t where t.user_name = :_u").setParameter("_u", username).getSingleResult();
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
				token = (OAuthAccessToken) DAOHelper.getInstance().getSession().createQuery("select t from OAuthAccessToken t where t.scope = :_s and t.authClient.client_id = :_c and t.user.id = :_u ORDER BY t.id desc").
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
				
				DAOHelper.getInstance().getSession().persist(token);
				t.commit();
			}
			
			if (openIdFlag != 0) {
				Jwt jwt = new Jwt();
				jwt.setSub(token.getUser().getUser_name());
				String id_token = generateJwt(scope, jwt);
				
				if(openIdFlag == 1) { // Authentication and Authorization purpose 
					synchronized (DEFAULT_TOKEN_TYPE) {
						queryString = "#token=" + token.getAccess_token() + "&token_type=" + DEFAULT_TOKEN_TYPE.toLowerCase() + "&id_token=" + id_token;
					}
				}else { // openIdFlag == 2 -> Response with just id_token ... just for authentication 
					queryString = "#id_token=" + id_token;
				}
			}else // Just OAuth 2.0 
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
		Transaction t = DAOHelper.getInstance().getSession().beginTransaction();
		OAuthorizationCode authorizationCode = null;
		try {
			authorizationCode = (OAuthorizationCode) DAOHelper.getInstance().getSession().createQuery("select t from OAuthorizationCode t where t.authorization_code = :_c").setParameter("_c", code).getSingleResult();
		}catch(Exception e) {
			e.printStackTrace(); 
			return new Error(OAuth2Error.INVALID_AUTHORIZATION_CODE.name(), OAuth2Error.INVALID_AUTHORIZATION_CODE.getDescription());
		}
		if(authorizationCode.getExpires().compareTo(System.currentTimeMillis() + "") <= 0)
			return new Error(OAuth2Error.INVALID_AUTHORIZATION_CODE.name(), OAuth2Error.INVALID_AUTHORIZATION_CODE.getDescription());
		
		if(authorizationCode.getRedirect_uri() != null && !authorizationCode.getRedirect_uri().trim().isEmpty() && redirect_uri != null && !redirect_uri.trim().isEmpty() && !authorizationCode.getRedirect_uri().equals(redirect_uri))
			return new Error(OAuth2Error.INVALID_REDIRECT_URI.name(), OAuth2Error.INVALID_REDIRECT_URI.getDescription());
	
		OAuthClient client = null;
		try {
			client = (OAuthClient) DAOHelper.getInstance().getSession().createQuery("select t from OAuthClient t where t.client_id = :_c").setParameter("_c", client_id).getSingleResult();
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
			accessToken = (OAuthAccessToken) DAOHelper.getInstance().getSession().createQuery("select t from OAuthAccessToken t where t.scope = :_s and t.authClient.client_id = :_c and t.user.id = :_u ORDER BY t.id desc").
					setParameter("_c", authorizationCode.getAuthClient().getClient_id()).
					setParameter("_s", authorizationCode.getScope()).
					setParameter("_u", authorizationCode.getUser().getId()).
					setMaxResults(1).
					getSingleResult();
			
			refreshToken = (OAuthRefreshToken) DAOHelper.getInstance().getSession().createQuery("select t from OAuthRefreshToken t where t.scope = :_s and t.authClient.client_id = :_c and t.user.id = :_u ORDER BY t.id desc").
					setParameter("_c", authorizationCode.getAuthClient().getClient_id()).
					setParameter("_s", authorizationCode.getScope()).
					setParameter("_u", authorizationCode.getUser().getId()).
					setMaxResults(1).
					getSingleResult();
			
			if(accessToken.getExpires().compareTo(System.currentTimeMillis() + "") > 0 && refreshToken.getExpires().compareTo(System.currentTimeMillis() + "") > 0) {
				
				/** OpenId implementation begin **/
				if(isOpenIdDefaultScopes(authorizationCode.getScope())) 
					return generateOpenIdToken(accessToken, refreshToken, authorizationCode.getScope());
				/** OpenId implementation end **/
				
				return generateOAuth2Token(accessToken, refreshToken);
			}
			
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
		
		
		DAOHelper.getInstance().getSession().persist(accessToken);
		DAOHelper.getInstance().getSession().persist(refreshToken);
		t.commit();
		
		/** OpenId implementation begin **/
		if(isOpenIdDefaultScopes(authorizationCode.getScope())) 
			return generateOpenIdToken(accessToken, refreshToken, authorizationCode.getScope());
		/** OpenId implementation end **/
		
		return generateOAuth2Token(accessToken, refreshToken);
	}
	
	// OAuth2 grant_type=password
	public static Object resourceOwnerPasswordGrant(String username, String password, String client_id, String client_secret, String scope) { 
		Transaction t = DAOHelper.getInstance().getSession().beginTransaction();
		User user = null;
		try {
			user = (User) DAOHelper.getInstance().getSession().createQuery("select t from User t where t.user_name = :_u").setParameter("_u", username).getSingleResult();
		}catch(Exception e) {
			e.printStackTrace(); // For log
			return new Error(OAuth2Error.INVALID_USER.name(), OAuth2Error.INVALID_USER.getDescription());
		}
		if(!user.getPass_hash().equals(password))
			return new Error(OAuth2Error.INVALID_USER_CREDENTIALS.name(), OAuth2Error.INVALID_USER_CREDENTIALS.getDescription());
		
		OAuthClient client = null;
		try {
			client = (OAuthClient) DAOHelper.getInstance().getSession().createQuery("select t from OAuthClient t where t.client_id = :_c").setParameter("_c", client_id).getSingleResult();
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
			accessToken = (OAuthAccessToken) DAOHelper.getInstance().getSession().createQuery("select t from OAuthAccessToken t where t.scope = :_s and t.authClient.client_id = :_c and t.user.id = :_u ORDER BY t.id desc").
					setParameter("_c",client.getClient_id()).
					setParameter("_s", (scope == null || scope.isEmpty() ? client.getScope() : scope)).
					setParameter("_u", user.getId()).
					setMaxResults(1).
					getSingleResult();
			
			refreshToken = (OAuthRefreshToken) DAOHelper.getInstance().getSession().createQuery("select t from OAuthRefreshToken t where t.scope = :_s and t.authClient.client_id = :_c and t.user.id = :_u ORDER BY t.id desc").
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
		
		
		DAOHelper.getInstance().getSession().persist(accessToken);
		DAOHelper.getInstance().getSession().persist(refreshToken);
		t.commit();
		
		return generateOAuth2Token(accessToken, refreshToken);
	}
	
	// OAuth2 grant_type=client_credentials
	public static Object clientCredentialsGrant(String client_id, String client_secret, String scope) {
		Transaction t = DAOHelper.getInstance().getSession().beginTransaction();
		OAuthClient client = null;
		try {
			client = (OAuthClient) DAOHelper.getInstance().getSession().createQuery("select t from OAuthClient t where t.client_id = :_c").setParameter("_c", client_id).getSingleResult();
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
			accessToken = (OAuthAccessToken) DAOHelper.getInstance().getSession().createQuery("select t from OAuthAccessToken t where t.scope = :_s and t.authClient.client_id = :_c and t.user.id = :_u ORDER BY t.id desc").
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
		
		DAOHelper.getInstance().getSession().persist(accessToken);
		t.commit();
		
		return generateOAuth2Token(accessToken);
	}
	
	// OAuth2 grant_type=refresh_token
	public static Object refreshToken(String refresh_token, String scope, String client_id, String client_secret) {
		Transaction t = DAOHelper.getInstance().getSession().beginTransaction();
		OAuthRefreshToken refreshToken = null;
		try {
			refreshToken = (OAuthRefreshToken) DAOHelper.getInstance().getSession().createQuery("select t from OAuthRefreshToken t where t.refresh_token = :_r").setParameter("_r", refresh_token).getSingleResult();
		}catch(Exception e) {
			e.printStackTrace();
			return new Error(OAuth2Error.INVALID_REFRESH_TOKEN.name(), OAuth2Error.INVALID_REFRESH_TOKEN.getDescription());
		}
		if(refreshToken.getExpires().compareTo("" + System.currentTimeMillis()) <= 0)
			return new Error(OAuth2Error.TOKEN_EXPIRED.name(), OAuth2Error.TOKEN_EXPIRED.getDescription());
		
		OAuthAccessToken accessToken = null;
		try {
			accessToken = (OAuthAccessToken) DAOHelper.getInstance().getSession().createQuery("select t from OAuthAccessToken t where t.authClient.client_id = :_c and t.user.id = :_u and t.scope = :_s ORDER BY t.id desc").
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
			client = (OAuthClient) DAOHelper.getInstance().getSession().createQuery("select t from OAuthClient t where t.client_id = :_c").setParameter("_c", client_id).getSingleResult();
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
		
		DAOHelper.getInstance().getSession().persist(accessToken_);
		DAOHelper.getInstance().getSession().persist(refreshToken_);
		t.commit();
		
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
		synchronized (DEFAULT_TOKEN_TYPE) {
			token.setToken_type(DEFAULT_TOKEN_TYPE);
		}
	return token;
	}
	
	private static igrp.resource.openid.Token generateOpenIdToken(OAuthAccessToken accessToken, OAuthRefreshToken refreshToken, String openIdScopes){
		igrp.resource.openid.Token t = new igrp.resource.openid.Token(generateOAuth2Token(accessToken, refreshToken));
		Jwt jwt = new Jwt();
		jwt.setSub(accessToken.getUser().getUser_name());
		t.setId_token(generateJwt(openIdScopes, jwt));
		return t;
	}
	
	// overload
	private static Token generateOAuth2Token(OAuthAccessToken accessToken) {
		Token token = new Token();
		token.setAccess_token(accessToken.getAccess_token());
		token.setExpires_in(Integer.parseInt(getExpiresIn(accessToken)));
		synchronized (DEFAULT_TOKEN_TYPE) {
			token.setToken_type(DEFAULT_TOKEN_TYPE);
		}
	return token;
	}
	
	private static String generateJwt(String openIdScopes, Jwt j) {
		String jwt = "PUT_JWT_HERE";
		// We need a signing key, so we'll create one just for this example. Usually
    	// the key would be read from your application configuration instead.
    	Key key = MacProvider.generateKey();
    	// https://github.com/jwtk/jjwt
    	//System.out.println(new String(key.getFormat()));
    	jwt = Jwts.builder()
    	  .setSubject(j.getSub())
    	  .signWith(SignatureAlgorithm.HS512, key)
    	  .compact();
		
		return jwt;
	}
	
	private static boolean isOpenIdDefaultScopes(String scopes) {
		return scopes.contains(Scope.OPENID.getValue()) || scopes.contains(Scope.EMAIL.getValue()) || scopes.contains(Scope.PROFILE.getValue()) || scopes.contains(Scope.ADDRESS.getValue()) 
				|| scopes.contains(Scope.PHONE.getValue()) || scopes.contains(Scope.OFFLINE_ACCESS.getValue());
	}
	
	public static boolean isValidToken(String token, String scope /* A single scope */) {
		return isValidToken(token, scope, null);
	}
	
	public static boolean isValidToken(String token, String scope /* A single scope */, OAuthAccessToken returnToken) {
		if(token == null || token.isEmpty()) return false;
		synchronized (DEFAULT_TOKEN_TYPE) {
			token = token.replaceFirst(DEFAULT_TOKEN_TYPE + " ", ""); 
		}
		OAuthAccessToken accessToken = null;
		try {
			CriteriaBuilder criteriaBuilder = DAOHelper.getInstance().getSession().getCriteriaBuilder();
			CriteriaQuery<Object> criteriaQuery = criteriaBuilder.createQuery();
			Root<OAuthAccessToken> u = criteriaQuery.from(OAuthAccessToken.class);
			criteriaQuery.select(u).where(
							criteriaBuilder.equal(u.get("access_token"), criteriaBuilder.parameter(String.class, "_t"))
					);
			criteriaQuery.orderBy(criteriaBuilder.desc(u.get("id")));
			accessToken = (OAuthAccessToken)DAOHelper.getInstance().getSession().
					createQuery(criteriaQuery).
					setParameter("_t", token).
					setMaxResults(1).
					getSingleResult();
			if(returnToken != null) {
				returnToken.setAccess_token(accessToken.getAccess_token());
				returnToken.setUser(accessToken.getUser());
				returnToken.setId(accessToken.getId());
				returnToken.setScope(accessToken.getScope());
				returnToken.setAuthClient(accessToken.getAuthClient());
				returnToken.setExpires(accessToken.getExpires());
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		return accessToken != null && accessToken.getExpires().compareTo(System.currentTimeMillis() + "") > 0 && validateScope(scope, accessToken);
	}
	
}
