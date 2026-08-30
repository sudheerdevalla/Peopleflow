package com.hr.hrapp.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Set;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinTable;
import jakarta.persistence.JoinColumn;
   

@Entity
public class User {
	
	 @ManyToMany(fetch = jakarta.persistence.FetchType.LAZY)
	    @JoinTable(
	        name = "user_roles",
	        joinColumns = @JoinColumn(name = "user_id"),
	        inverseJoinColumns = @JoinColumn(name = "role_id")
	    )
	    private Set<Role> roles;
	    public Set<Role> getRoles() {
	        return roles;
	    }

	    public void setRoles(Set<Role> roles) {
	        this.roles = roles;
	    }

	

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true, nullable = false)
    private String username;
    private String password;
    private String role; // ADMIN or USER
    private boolean forcePasswordChange = false;

    private boolean mfaEnabled = false;
    private String totpSecret;

    // Getters and Setters
    public int getId() {
    	return id;
    	}

    	public void setId(int id) {
    	this.id = id;
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

    	public String getRole() {
    	return role;
    	}

    	public void setRole(String role) {
    	this.role = role;
    	}
        public boolean isForcePasswordChange() {
    return forcePasswordChange;
}

public void setForcePasswordChange(boolean forcePasswordChange) {
    this.forcePasswordChange = forcePasswordChange;
}


    public boolean isMfaEnabled() {
        return mfaEnabled;
    }

    public void setMfaEnabled(boolean mfaEnabled) {
        this.mfaEnabled = mfaEnabled;
    }

    public String getTotpSecret() {
        return totpSecret;
    }

    public void setTotpSecret(String totpSecret) {
        this.totpSecret = totpSecret;
    }
}
