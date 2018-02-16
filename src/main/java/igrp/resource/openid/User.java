package igrp.resource.openid;
/**
 * Iekiny Marcel
 * Feb 8, 2018
 */
public class User extends igrp.resource.User{
	
	// for scope=email (optional) 
	private boolean email_verified;
	
	// for scope=profile (optional) 
	private String family_name;
	private String given_name;
	private String middle_name;
	private String nickname;
	private String preferred_username;
	private String profile;
	private String picture;
	private String website;
	private String gender;
	private String birthdate;
	private String zoneinfo;
	private String locale;
	
	private String address; // for scope=address (optional) 
	
	private boolean phone_number_verified; // for scope=phone (optional) 
	
	// ... and others ... 
	
}
