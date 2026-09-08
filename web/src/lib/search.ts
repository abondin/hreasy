import Fuse from "fuse.js";

const EN_LAYOUT = "`qwertyuiop[]asdfghjkl;'zxcvbnm,./";
const RU_LAYOUT = "ёйцукенгшщзхъфывапролджэячсмитьбю.";
const fuzzyOptions = {
  threshold: 0.3,
  ignoreLocation: true,
  minMatchCharLength: 4,
} as const;

export interface SearchSettings {
  fuzzy: boolean;
  keyboardLayout: boolean;
  unorderedTerms: boolean;
}

export function createSearchSettings(): SearchSettings {
  return { fuzzy: true, keyboardLayout: true, unorderedTerms: true };
}

export function normalizeSearchInput(value: unknown): string {
  return typeof value === "string" ? value : "";
}

export function matchesSearch(
  query: string,
  values: Array<string | null | undefined>,
  settings: SearchSettings = createSearchSettings(),
): boolean {
  const normalizedQuery = normalize(query);
  const terms = settings.unorderedTerms
    ? normalizedQuery.split(/\s+/).filter(Boolean)
    : [normalizedQuery].filter(Boolean);
  if (terms.length === 0) {
    return true;
  }

  const haystack = normalize(values.filter(Boolean).join(" "));
  const words = haystack.split(/\s+/);
  const fuzzyTargets = settings.unorderedTerms ? words : [haystack];
  return terms.every((term) => queryCandidates(term, settings.keyboardLayout).some((candidate) =>
    haystack.includes(candidate)
    || (settings.fuzzy && candidate.length >= 4
      && fuzzyTargets.some((target) => Fuse.match(candidate, target, fuzzyOptions).isMatch)),
  ));
}

function normalize(value: string): string {
  return value.normalize("NFKC").toLocaleLowerCase("ru-RU").trim();
}

function queryCandidates(term: string, keyboardLayout: boolean): string[] {
  return keyboardLayout
    ? [...new Set([
      term,
      convertLayout(term, EN_LAYOUT, RU_LAYOUT),
      convertLayout(term, RU_LAYOUT, EN_LAYOUT),
    ])]
    : [term];
}

function convertLayout(value: string, from: string, to: string): string {
  return [...value].map((character) => {
    const index = from.indexOf(character);
    return index < 0 ? character : to[index];
  }).join("");
}
