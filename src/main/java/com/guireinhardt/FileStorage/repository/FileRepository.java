package com.guireinhardt.FileStorage.repository;

import com.guireinhardt.FileStorage.entity.FileEntity;
import com.guireinhardt.FileStorage.entity.FileVisibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FileRepository extends JpaRepository<FileEntity, UUID> {
    Page<FileEntity> findByFolderId(UUID folderId, Pageable pageable);

    Page<FileEntity> findByFolderIdAndVisibility(UUID folderId, FileVisibility visibility, Pageable pageable);
}