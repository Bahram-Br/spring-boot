export const BASE_URL = (process.env.STUDENT_API_BASE_URL ?? "http://localhost:8080/api/v1").replace(/\/+$/, "");

export type ToolResult = { content: { type: "text"; text: string }[]; isError?: boolean };

export async function callApi(method: string, path: string, body?: unknown, fetchImpl: typeof fetch = fetch): Promise<ToolResult> {
  let res: Response;
  try {
    res = await fetchImpl(`${BASE_URL}${path}`, {
      method,
      headers: body !== undefined ? { "Content-Type": "application/json" } : undefined,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
  } catch (e) {
    return { isError: true, content: [{ type: "text", text: `Request failed: ${method} ${path}: ${(e as Error).message}` }] };
  }
  const raw = await res.text();
  let pretty = raw;
  try { pretty = JSON.stringify(JSON.parse(raw), null, 2); } catch { /* keep raw text */ }
  if (!res.ok) {
    return { isError: true, content: [{ type: "text", text: `HTTP ${res.status} ${res.statusText} for ${method} ${path}\n${pretty}` }] };
  }
  return { content: [{ type: "text", text: pretty || `HTTP ${res.status} (empty body)` }] };
}
