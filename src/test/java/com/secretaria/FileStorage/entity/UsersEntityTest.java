package com.secretaria.FileStorage.entity;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class UsersEntityTest {
    @Test
    public void testGetAuthoritiesAdmin() {
        UsersEntity adminUser = new UsersEntity();
        adminUser.setRole(UsersRole.ADMIN);

        Collection<? extends GrantedAuthority> authorities = adminUser.getAuthorities();

        assertEquals(1, authorities.size());
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    public void testGetAuthoritiesUsers() {
        UsersEntity normalUser = new UsersEntity();
        normalUser.setRole(UsersRole.USERS);

        Collection<? extends GrantedAuthority> authorities = normalUser.getAuthorities();

        assertEquals(1, authorities.size());
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_USERS")));
    }

    @Test
    public void testGetAuthoritiesPublico() {
        UsersEntity publicUser = new UsersEntity();
        publicUser.setRole(UsersRole.PUBLICO);

        Collection<? extends GrantedAuthority> authorities = publicUser.getAuthorities();

        assertEquals(1, authorities.size());
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_PUBLICO")));
    }
}
