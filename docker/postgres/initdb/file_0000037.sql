-- Schema for the TypeAhead system. Runs once when the Postgres data volume is
-- first initialized. PostgreSQL is the only source of truth; Redis is a cache.

CREATE TABLE IF NOT EXISTS query_frequency (
    id          BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    query_text  TEXT        NOT NULL UNIQUE,
    total_count BIGINT      NOT NULL DEFAULT 0,
    recent_count BIGINT     NOT NULL DEFAULT 0,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS search_events (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    query_text TEXT        NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Prefix search: text_pattern_ops lets "query_text LIKE 'pre%'" use the index.
CREATE INDEX IF NOT EXISTS idx_query_frequency_prefix
    ON query_frequency (query_text text_pattern_ops);

-- Supports ORDER BY total_count DESC for ranking suggestions.
CREATE INDEX IF NOT EXISTS idx_query_frequency_total_count
    ON query_frequency (total_count DESC);
