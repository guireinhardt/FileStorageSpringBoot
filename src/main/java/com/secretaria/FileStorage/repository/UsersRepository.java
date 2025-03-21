package com.secretaria.FileStorage.repository;

import com.secretaria.FileStorage.entity.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

public interface UsersRepository extends JpaRepository<UsersEntity,Long> {
    UserDetails findByUsername(String username);

    
}
