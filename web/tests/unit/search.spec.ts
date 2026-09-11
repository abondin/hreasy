import { describe, expect, it } from "vitest";
import { createSearchSettings, matchesSearch } from "@/lib/search";

describe("matchesSearch", () => {
  it("matches name words in any order and ignores case", () => {
    expect(matchesSearch("Иван петров", ["Петров Иван Александрович"])).toBe(true);
  });

  it("matches text typed in the wrong keyboard layout", () => {
    expect(matchesSearch("зшющту", ["pi.one"])).toBe(true);
    expect(matchesSearch("Fktrcq", ["Алексей"])).toBe(true);
  });

  it("requires every term and keeps fuzzy matching strict", () => {
    expect(matchesSearch("Иван Сидоров", ["Петров Иван"])).toBe(false);
    expect(matchesSearch("Ana", ["Ann"])).toBe(false);
  });

  it("can disable each optional matching behavior", () => {
    expect(matchesSearch("иван петров", ["Петров Иван"], {
      ...createSearchSettings(),
      unorderedTerms: false,
    })).toBe(false);
    expect(matchesSearch("зшющту", ["pi.one"], {
      ...createSearchSettings(),
      keyboardLayout: false,
    })).toBe(false);
    expect(matchesSearch("алексй", ["Алексей"], {
      ...createSearchSettings(),
      fuzzy: false,
    })).toBe(false);
  });
});
