"use client";

import { useState, useEffect, useRef } from "react";
import { useSuggestions } from "@/hooks/useSuggestions";
import { SuggestionDropdown } from "@/components/SuggestionDropdown";
import { LoadingSpinner } from "@/components/LoadingSpinner";
import { submitSearch } from "@/services/searchApi";
import { Suggestion } from "@/types/Suggestion";

// Custom Icons
const SearchIcon = () => (
  <svg className="search-icon" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
    <circle cx="11" cy="11" r="8"></circle>
    <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
  </svg>
);

const ClearIcon = () => (
  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
    <line x1="18" y1="6" x2="6" y2="18"></line>
    <line x1="6" y1="6" x2="18" y2="18"></line>
  </svg>
);

const CheckIcon = () => (
  <svg className="submitted-icon" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
    <polyline points="20 6 9 17 4 12"></polyline>
  </svg>
);

export function SearchBar() {
  const [query, setQuery] = useState("");
  const [submitted, setSubmitted] = useState<string | null>(null);
  const [isFocused, setIsFocused] = useState(false);
  const [history, setHistory] = useState<string[]>([]);
  const [activeIndex, setActiveIndex] = useState(-1);
  const inputRef = useRef<HTMLInputElement>(null);
  const containerRef = useRef<HTMLDivElement>(null);
  const { suggestions, loading } = useSuggestions(query);

  // Load search history from localStorage on client side mount
  useEffect(() => {
    const saved = localStorage.getItem("search_history");
    if (saved) {
      try {
        setHistory(JSON.parse(saved));
      } catch {
        setHistory([]);
      }
    }
  }, []);

  // Handle clicking outside to dismiss dropdown/blur
  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
        setIsFocused(false);
        setActiveIndex(-1);
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, []);

  // Save term to search history
  function saveToHistory(term: string) {
    const trimmed = term.trim();
    if (!trimmed) return;
    
    // Filter existing, append to front, slice to max 5 items
    const updated = [trimmed, ...history.filter((item) => item.toLowerCase() !== trimmed.toLowerCase())].slice(0, 5);
    setHistory(updated);
    localStorage.setItem("search_history", JSON.stringify(updated));
  }

  function clearHistory() {
    setHistory([]);
    localStorage.removeItem("search_history");
    setActiveIndex(-1);
    inputRef.current?.focus();
  }

  async function runSearch(value: string) {
    const trimmed = value.trim();
    if (!trimmed) {
      return;
    }
    setQuery(trimmed);
    setSubmitted(trimmed);
    saveToHistory(trimmed);
    setIsFocused(false);
    setActiveIndex(-1);
    
    // De-focus input
    inputRef.current?.blur();
    
    try {
      await submitSearch(trimmed);
    } catch {
      // Failed submission handled gracefully
    }
  }

  const isQueryEmpty = !query.trim();
  const showHistory = isFocused && isQueryEmpty && history.length > 0;
  const showSuggestions = isFocused && !isQueryEmpty && suggestions.length > 0;

  // Active items mapping for navigation
  const activeItems: Suggestion[] = showHistory
    ? history.map((text) => ({ text, count: 0 }))
    : showSuggestions
    ? suggestions
    : [];

  // Reset active index when items change
  useEffect(() => {
    setActiveIndex(-1);
  }, [query, isFocused]);

  function onKeyDown(e: React.KeyboardEvent<HTMLInputElement>) {
    if (!isFocused || activeItems.length === 0) return;

    if (e.key === "ArrowDown") {
      e.preventDefault();
      setActiveIndex((prev) => (prev + 1 >= activeItems.length ? 0 : prev + 1));
    } else if (e.key === "ArrowUp") {
      e.preventDefault();
      setActiveIndex((prev) => (prev - 1 < 0 ? activeItems.length - 1 : prev - 1));
    } else if (e.key === "Enter") {
      e.preventDefault();
      if (activeIndex >= 0 && activeIndex < activeItems.length) {
        void runSearch(activeItems[activeIndex].text);
      } else {
        void runSearch(query);
      }
    } else if (e.key === "Escape") {
      e.preventDefault();
      setIsFocused(false);
      setActiveIndex(-1);
      inputRef.current?.blur();
    }
  }

  function onSelect(suggestion: Suggestion) {
    void runSearch(suggestion.text);
  }

  function onClearInput() {
    setQuery("");
    setSubmitted(null);
    setActiveIndex(-1);
    inputRef.current?.focus();
  }

  return (
    <div style={{ width: "100%" }}>
      <div 
        ref={containerRef}
        className={`search-card ${isFocused ? "focused" : ""}`}
      >
        <form
          className="search-input-row"
          onSubmit={(event) => {
            event.preventDefault();
            void runSearch(query);
          }}
        >
          <SearchIcon />
          <input
            ref={inputRef}
            className="search-input"
            type="text"
            value={query}
            placeholder="Search queries..."
            autoFocus
            onFocus={() => setIsFocused(true)}
            onKeyDown={onKeyDown}
            onChange={(event) => {
              setQuery(event.target.value);
              setSubmitted(null);
            }}
          />
          {loading && <LoadingSpinner />}
          {query && (
            <button 
              type="button" 
              className="clear-btn" 
              onClick={onClearInput}
              title="Clear input"
            >
              <ClearIcon />
            </button>
          )}
        </form>

        {(showHistory || showSuggestions) && (
          <div className="dropdown-container">
            <div className="dropdown-header">
              <span>{showHistory ? "Recent Searches" : "Suggestions"}</span>
              {showHistory && (
                <button 
                  type="button" 
                  className="clear-history-btn" 
                  onClick={clearHistory}
                >
                  Clear History
                </button>
              )}
            </div>
            <SuggestionDropdown 
              suggestions={activeItems} 
              onSelect={onSelect} 
              activeIndex={activeIndex}
              query={query}
              isHistory={showHistory}
            />
          </div>
        )}
      </div>

      {submitted && (
        <div className="submitted-card">
          <CheckIcon />
          <div className="submitted-info">
            <div className="submitted-label">Database Status</div>
            <div className="submitted-query">Queued click metrics for &ldquo;{submitted}&rdquo;</div>
          </div>
        </div>
      )}
    </div>
  );
}
