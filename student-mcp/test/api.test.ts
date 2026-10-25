import { describe, it, expect, vi } from "vitest";
import { callApi } from "../src/api.js";

describe("callApi", () => {
  it("maps non-2xx to isError with status and body", async () => {
    const f = vi.fn().mockResolvedValue(new Response('{"error":"boom"}', { status: 500, statusText: "Internal Server Error" }));
    const r = await callApi("POST", "/students", { a: 1 }, f as any);
    expect(r.isError).toBe(true);
    expect(r.content[0].text).toContain("HTTP 500");
    expect(r.content[0].text).toContain("boom");
  });
  it("returns JSON text on success", async () => {
    const f = vi.fn().mockResolvedValue(new Response('[{"id":1}]', { status: 200 }));
    const r = await callApi("GET", "/students", undefined, f as any);
    expect(r.isError).toBeUndefined();
    expect(JSON.parse(r.content[0].text)).toEqual([{ id: 1 }]);
  });
  it("maps network failure to isError", async () => {
    const f = vi.fn().mockRejectedValue(new Error("ECONNREFUSED"));
    const r = await callApi("GET", "/students", undefined, f as any);
    expect(r.isError).toBe(true);
  });
});
