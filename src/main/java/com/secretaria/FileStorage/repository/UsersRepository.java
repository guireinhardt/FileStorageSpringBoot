package com.secretaria.FileStorage.repository;

import com.secretaria.FileStorage.entity.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepository extends JpaRepository<UsersEntity,Long> {
    UserDetails findByUsername(String username);

    // Contar usuários ativos
    @Query("SELECT COUNT(u) FROM UsersEntity u WHERE u.enabled = true")
    long countActiveUsers();

    // Método para contar o total de usuários
    @Query("SELECT COUNT(u) FROM UsersEntity u")
    long count();
}
