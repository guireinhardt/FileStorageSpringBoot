package com.secretaria.FileStorage.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "search_log", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_keyword_id", columnList = "keyword_id"),
        @Index(name = "idx_occurred_at", columnList = "occurred_at")
})
public class SearchLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private UsersEntity user; // Usuário que fez a pesquisa

    @ManyToOne
    @JoinColumn(name = "keyword_id", referencedColumnName = "id")
    private KeywordEntity keyword; // Palavra-chave associada

    @ManyToOne
    @JoinColumn(name = "subkeyword_id", referencedColumnName = "id")
    private SubkeywordEntity subkeyword; // Subpalavra associada (se houver)

    @Column(name = "occurred_at", nullable = false)
    @CreationTimestamp
    private LocalDate occurredAt; // Data da pesquisa (data gerada automaticamente no momento da criação)

    @Column(nullable = false)
    private String query; // A consulta realizada (palavra-chave ou subpalavra)

    @Column(name = "search_count", nullable = false)
    private long searchCount = 0; // Contagem de buscas para aquela palavra-chave no dia

    // Getters e setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UsersEntity getUser() {
        return user;
    }

    public void setUser(UsersEntity user) {
        this.user = user;
    }

    public KeywordEntity getKeyword() {
        return keyword;
    }

    public void setKeyword(KeywordEntity keyword) {
        this.keyword = keyword;
    }

    public SubkeywordEntity getSubkeyword() {
        return subkeyword;
    }

    public void setSubkeyword(SubkeywordEntity subkeyword) {
        this.subkeyword = subkeyword;
    }

    public long getSearchCount() {
        return searchCount;
    }

    public void setSearchCount(long searchCount) {
        this.searchCount = searchCount;
    }

    public LocalDate getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDate occurredAt) {
        this.occurredAt = occurredAt;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}

