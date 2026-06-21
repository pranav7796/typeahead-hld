package com.typeahead.repository;

import com.typeahead.model.SearchEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchEventRepository extends JpaRepository<SearchEvent, Long> {
}
