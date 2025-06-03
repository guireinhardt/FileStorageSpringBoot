package com.secretaria.FileStorage.service;

import com.secretaria.FileStorage.entity.UsersEntity;
import com.secretaria.FileStorage.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService implements UserDetailsService {
    @Autowired
    UsersRepository repository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDetails user = repository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("Usuário não encontrado: " + username);
        }
        return user; // Como user já é UserDetails (ou contém os authorities)
    }


}
