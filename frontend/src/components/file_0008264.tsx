import { useEffect, useRef } from "react";
import { Suggestion } from "@/types/Suggestion";

interface SuggestionDropdownProps {
  suggestions: Suggestion[];
  onSelect: (suggestion: Suggestion) => void;
  activeIndex: number;
  query: string;
  isHistory?: boolean;
}

// Icons for dropdown items
const ClockIcon = () => (
  <svg className="dropdown-item-icon" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <circle cx="12" cy="12" r="10"></circle>
    <polyline points="12 6 12 12 16 14"></polyline>
  </svg>
);

const SuggestIcon = () => (
  <svg className="dropdown-item-icon" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <circle cx="11" cy="11" r="8"></circle>
    <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
  </svg>
);

export function SuggestionDropdown({ 
  suggestions, 
  onSelect, 
  activeIndex, 
  query, 
  isHistory = false 
}: SuggestionDropdownProps) {
  const itemRefs = useRef<(HTMLButtonElement | null)[]>([]);

  // Reset/resize refs array if suggestions list changes length
  useEffect(() => {
    itemRefs.current = itemRefs.current.slice(0, suggestions.length);
  }, [suggestions]);

  // Keep the active item visible in the scrollable container
  useEffect(() => {
    if (activeIndex >= 0 && itemRefs.current[activeIndex]) {
      itemRefs.current[activeIndex]?.scrollIntoView({
        behavior: "smooth",
        block: "nearest",
      });
    }
  }, [activeIndex]);

  if (suggestions.length === 0) {
    return null;
  }

  function renderHighlightedText(text: string, search: string) {
    const trimmedSearch = search.trim();
    if (!trimmedSearch) {
      return <span className="dropdown-text">{text}</span>;
    }
    const idx = text.toLowerCase().indexOf(trimmedSearch.toLowerCase());
    if (idx === -1) {
      return <span className="dropdown-text">{text}</span>;
    }
    const before = text.substring(0, idx);
    const match = text.substring(idx, idx + trimmedSearch.length);
    const after = text.substring(idx + trimmedSearch.length);

    return (
      <span className="dropdown-text">
        {before}
        <span className="highlight-match">{match}</span>
        {after}
      </span>
    );
  }

  return (
    <ul className="dropdown">
      {suggestions.map((suggestion, index) => (
        <li key={suggestion.text}>
          <button
            ref={(el) => {
              itemRefs.current[index] = el;
            }}
            type="button"
            className={`dropdown-item ${activeIndex === index ? "active" : ""}`}
            onClick={() => onSelect(suggestion)}
          >
            <div className="dropdown-item-content">
              {isHistory ? <ClockIcon /> : <SuggestIcon />}
              {renderHighlightedText(suggestion.text, query)}
            </div>
            {!isHistory && suggestion.count > 0 && (
              <span className="dropdown-count">{suggestion.count.toLocaleString()}</span>
            )}
          </button>
        </li>
      ))}
    </ul>
  );
}
