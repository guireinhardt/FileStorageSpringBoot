package com.guireinhardt.FileStorage.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "folders",
        uniqueConstraints = @UniqueConstraint(name = "uk_folders_name", columnNames = {"name"})
)
public class FolderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name; // ex: "02.FINALIZADOS", "EVENTOS", "CAMPANHAS_2026"

    @Column(nullable = false)
    private boolean isPublic = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private FolderEntity parent; // opcional: suporta árvore

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public FolderEntity() {}

    public FolderEntity(String name, boolean isPublic, FolderEntity parent) {
        this.name = name;
        this.isPublic = isPublic;
        this.parent = parent;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean aPublic) { isPublic = aPublic; }

    public FolderEntity getParent() { return parent; }
    public void setParent(FolderEntity parent) { this.parent = parent; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}