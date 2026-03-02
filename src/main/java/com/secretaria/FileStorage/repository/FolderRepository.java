package com.secretaria.FileStorage.repository;

import com.secretaria.FileStorage.entity.FolderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FolderRepository extends JpaRepository<FolderEntity, UUID> {
    Optional<FolderEntity> findByName(String name);
}