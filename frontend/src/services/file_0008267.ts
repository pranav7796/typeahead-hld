import { SuggestionResponse } from "@/types/ApiResponse";

/** Fetches suggestions for a prefix from the backend (proxied via Next rewrites). */
export async function fetchSuggestions(prefix: string): Promise<SuggestionResponse> {
  const response = await fetch(`/api/suggestions?prefix=${encodeURIComponent(prefix)}`);
  if (!response.ok) {
    throw new Error(`Suggestion request failed: ${response.status}`);
  }
  return response.json();
}
