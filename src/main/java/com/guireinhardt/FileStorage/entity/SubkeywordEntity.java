package com.guireinhardt.FileStorage.entity;

import jakarta.persistence.*;

@Entity
public class SubkeywordEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String palavra;

    @ManyToOne
    @JoinColumn(name = "keyword_id")
    private KeywordEntity keyword;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPalavra() {
        return palavra;
    }

    public void setPalavra(String palavra) {
        this.palavra = palavra;
    }

    public KeywordEntity getKeyword() {
        return keyword;
    }

    public void setKeyword(KeywordEntity keyword) {
        this.keyword = keyword;
    }
}
