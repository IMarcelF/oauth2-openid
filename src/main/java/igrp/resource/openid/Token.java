package igrp.resource.openid;
/**
 * Iekiny Marcel
 * Feb 8, 2018
 */
public class Token extends igrp.resource.oauth2.Token{
	
	private String id_token;
	
	public Token() {}
	
	public Token(igrp.resource.oauth2.Token t) {
		this.setAccess_token(t.getAccess_token());
		this.setRefresh_token(t.getRefresh_token());
		this.setExpires_in(t.getExpires_in());
		this.setToken_type(t.getToken_type());
	}
	
	public String getId_token() {
		return id_token;
	}
	
	public void setId_token(String id_token) {
		this.id_token = id_token;
	}
	
}
