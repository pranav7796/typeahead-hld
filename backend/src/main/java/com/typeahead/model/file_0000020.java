package com.typeahead.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/** Search count per query. PostgreSQL is the source of truth for these. */
@Entity
@Table(name = "query_frequency")
public class QueryFrequency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "query_text", nullable = false, unique = true)
    private String queryText;

    @Column(name = "total_count", nullable = false)
    private long totalCount;

    @Column(name = "recent_count", nullable = false)
    private long recentCount;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected QueryFrequency() {
    }

    public Long getId() {
        return id;
    }

    public String getQueryText() {
        return queryText;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public long getRecentCount() {
        return recentCount;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
