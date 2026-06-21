package com.typeahead.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/** A raw search submission, persisted in batches as an audit log. */
@Entity
@Table(name = "search_events")
public class SearchEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "query_text", nullable = false)
    private String queryText;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected SearchEvent() {
    }

    public SearchEvent(String queryText, Instant createdAt) {
        this.queryText = queryText;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getQueryText() {
        return queryText;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
