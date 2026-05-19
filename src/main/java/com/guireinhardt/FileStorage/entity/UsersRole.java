package com.guireinhardt.FileStorage.entity;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public enum UsersRole  {
    ADMIN("admin"),
    USERS("users"),
    PUBLICO("publico");


    private String role;

    UsersRole(String role){
        this.role = role;
    }

    public String getRole(){
        return role;
    }
}

