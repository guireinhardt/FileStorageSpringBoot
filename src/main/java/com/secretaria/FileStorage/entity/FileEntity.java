package com.secretaria.FileStorage.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "files", indexes = {
        @Index(name = "idx_files_folder", columnList = "folder_id"),
        @Index(name = "idx_files_visibility", columnList = "visibility"),
        @Index(name = "idx_files_created_at", columnList = "createdAt")
})
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "folder_id", nullable = false)
    private FolderEntity folder;

    @Column(nullable = false, length = 512)
    private String originalName;

    @Column(nullable = false, length = 200)
    private String contentType;

    @Column(nullable = false)
    private long size;

    @Column(nullable = false, length = 1024)
    private String storageKey;
    // ex: "uploads/02.FINALIZADOS/uuid.jpg" (local)
    // depois: "finalizados/uuid.jpg" (GCS)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FileVisibility visibility = FileVisibility.RESTRICTED;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(length = 150)
    private String createdBy; // username/email/id (opcional)

    public FileEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public FolderEntity getFolder() { return folder; }
    public void setFolder(FolderEntity folder) { this.folder = folder; }

    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }

    public String getStorageKey() { return storageKey; }
    public void setStorageKey(String storageKey) { this.storageKey = storageKey; }

    public FileVisibility getVisibility() { return visibility; }
    public void setVisibility(FileVisibility visibility) { this.visibility = visibility; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}