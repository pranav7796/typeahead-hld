import { SearchSubmissionResponse } from "@/types/ApiResponse";

/** Submits a search query so it feeds back into popularity counts. */
export async function submitSearch(query: string): Promise<SearchSubmissionResponse> {
  const response = await fetch("/api/search", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ query }),
  });
  if (!response.ok) {
    throw new Error(`Search submission failed: ${response.status}`);
  }
  return response.json();
}
