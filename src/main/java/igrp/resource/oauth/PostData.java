package igrp.resource.oauth;

import java.io.Serializable;

/**
 * @author Marcel Iekiny
 * Oct 9, 2017
 */
// This class encapsulate all oauth2 (grant-types) post request data
public class PostData implements Serializable{// For serialize and deserialize purpose
	
	private String grant_type;
	private String code;
	private String redirect_uri;
	private String scope;
	private String username;
	private String password;
	private String refresh_token;
	
	public PostData() {}
	
	public PostData(String grant_type, String code, String redirect_uri, String scope, String username, String password,
			String refresh_token) {
		super();
		this.grant_type = grant_type;
		this.code = code;
		this.redirect_uri = redirect_uri;
		this.scope = scope;
		this.username = username;
		this.password = password;
		this.refresh_token = refresh_token;
	}
	
	public String getGrant_type() {
		return grant_type;
	}
	public void setGrant_type(String grant_type) {
		this.grant_type = grant_type;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getRedirect_uri() {
		return redirect_uri;
	}
	public void setRedirect_uri(String redirect_uri) {
		this.redirect_uri = redirect_uri;
	}
	public String getScope() {
		return scope;
	}
	public void setScope(String scope) {
		this.scope = scope;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getRefresh_token() {
		return refresh_token;
	}
	public void setRefresh_token(String refresh_token) {
		this.refresh_token = refresh_token;
	}

	@Override
	public String toString() {
		return "PostData [grant_type=" + grant_type + ", code=" + code + ", redirect_uri=" + redirect_uri + ", scope="
				+ scope + ", username=" + username + ", password=" + password + ", refresh_token=" + refresh_token
				+ "]";
	}
	
}
