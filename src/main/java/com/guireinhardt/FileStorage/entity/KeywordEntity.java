package com.guireinhardt.FileStorage.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class KeywordEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String palavra;

    @OneToMany(mappedBy = "keyword", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubkeywordEntity> subkeywords;

    // Getters e setters


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

    public List<SubkeywordEntity> getSubkeywords() {
        return subkeywords;
    }

    public void setSubkeywords(List<SubkeywordEntity> subkeywords) {
        this.subkeywords = subkeywords;
    }
}




