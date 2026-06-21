package com.typeahead.repository;

import com.typeahead.model.QueryFrequency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QueryFrequencyRepository extends JpaRepository<QueryFrequency, Long> {

    /** Top suggestions for a prefix, most popular first (LIKE 'prefix%' ORDER BY total_count DESC LIMIT 10). */
    List<QueryFrequency> findTop10ByQueryTextStartingWithOrderByTotalCountDesc(String prefix);

    /** Increment counts for a query, inserting the row if it does not exist yet. */
    @Modifying
    @Query(value = """
            INSERT INTO query_frequency (query_text, total_count, recent_count, updated_at)
            VALUES (:queryText, :count, :count, now())
            ON CONFLICT (query_text) DO UPDATE
                SET total_count = query_frequency.total_count + :count,
                    recent_count = query_frequency.recent_count + :count,
                    updated_at = now()
            """, nativeQuery = true)
    void incrementSearchCount(@Param("queryText") String queryText, @Param("count") long count);
}
