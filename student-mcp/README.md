# student-mcp

TypeScript MCP server (stdio) exposing the StudentAPI REST endpoints as tools:
`list_students`, `get_student`, `add_student`, `update_student`, `delete_student`,
`search_by_name`, `search_by_major`, `search_by_email`, `search_older_than`.
Non-2xx responses are returned with `isError: true` plus the HTTP status and body.

## Run

```bash
npm install
npm run build
npm start          # or: npm run dev
npm test
```

Config: `STUDENT_API_BASE_URL` (default `http://localhost:8080/api/v1`). Requires Node 20+.

## Claude Desktop / Cursor config

`claude_desktop_config.json` or `.cursor/mcp.json`:

```json
{
  "mcpServers": {
    "student-api": {
      "command": "node",
      "args": ["/absolute/path/to/student-mcp/dist/index.js"],
      "env": { "STUDENT_API_BASE_URL": "http://localhost:8080/api/v1" }
    }
  }
}
```
