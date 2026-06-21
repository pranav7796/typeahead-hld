import { useEffect, useState } from "react";
import { Suggestion } from "@/types/Suggestion";
import { fetchSuggestions } from "@/services/suggestionApi";

const DEBOUNCE_MS = 150;

/** Debounced suggestion lookup for the current prefix. */
export function useSuggestions(prefix: string): { suggestions: Suggestion[]; loading: boolean } {
  const [suggestions, setSuggestions] = useState<Suggestion[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const trimmed = prefix.trim();
    if (!trimmed) {
      setSuggestions([]);
      setLoading(false);
      return;
    }

    let cancelled = false;
    setLoading(true);
    const timer = setTimeout(async () => {
      try {
        const response = await fetchSuggestions(trimmed);
        if (!cancelled) {
          setSuggestions(response.suggestions);
        }
      } catch {
        if (!cancelled) {
          setSuggestions([]);
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }, DEBOUNCE_MS);

    return () => {
      cancelled = true;
      clearTimeout(timer);
    };
  }, [prefix]);

  return { suggestions, loading };
}
