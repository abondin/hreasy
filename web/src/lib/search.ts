seimport Fuse from "fuse.js";

const EN_LAYOUT = "`qwertyuiop[]asdfghjkl;'zxcvbnm,./";
const RU_LAYOUT = "ёйцукенгшщзхъфывапролджэячсмитьбю.";
const fuzzyOptions = {
  threshold: 0.3,
  ignoreLocation: true,
  minMatchCharLength: 4,
} as const;

export function normalizeSearchInput(value: unknown): string {
  return typeof value === "string" ? value : "";
}

export function matchesSearch(
  query: string,
  ...values: Array<string | null | undefined>
): boolean {
  const terms = normalize(query).split(/\s+/).filter(Boolean);
  if (terms.length === 0) {
    return true;
  }

  const haystack = normalize(values.filter(Boolean).join(" "));
  const words = haystack.split(/\s+/);
  return terms.every((term) => queryCandidates(term).some((candidate) =>
    haystack.includes(candidate)
    || (candidate.length >= 4
      && words.some((word) => Fuse.match(candidate, word, fuzzyOptions).isMatch)),
  ));
}

function normalize(value: string): string {
  return value.normalize("NFKC").toLocaleLowerCase("ru-RU").trim();
}

function queryCandidates(term: string): string[] {
  return [...new Set([
    term,
    convertLayout(term, EN_LAYOUT, RU_LAYOUT),
    convertLayout(term, RU_LAYOUT, EN_LAYOUT),
  ])];
}

function convertLayout(value: string, from: string, to: string): string {
  return [...value].map((character) => {
    const index = from.indexOf(character);
    return index < 0 ? character : to[index];
  }).join("");
}
