import { Suggestion } from "@/types/Suggestion";

/** Response from GET /api/suggestions. */
export interface SuggestionResponse {
  prefix: string;
  suggestions: Suggestion[];
}

/** Response from POST /api/search. */
export interface SearchSubmissionResponse {
  accepted: boolean;
}
