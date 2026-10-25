#!/usr/bin/env node
import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import { z } from "zod";
import { callApi } from "./api.js";

const server = new McpServer({ name: "student-mcp", version: "0.1.0" });
const enc = encodeURIComponent;
const id = z.number().int().describe("Student id");
const student = {
  name: z.string().describe("Student name"),
  age: z.number().int().describe("Age"),
  gender: z.string().describe("Gender"),
  email: z.string().describe("Email (not validated client-side so API validation can be tested)"),
  major: z.string().describe("Major"),
};

server.tool("list_students", "List all students (GET /students)", {}, () => callApi("GET", "/students"));
server.tool("get_student", "Get a student by id (GET /students/{id})", { id }, ({ id }) => callApi("GET", `/students/${id}`));
server.tool("add_student", "Create a student (POST /students)", student, (s) => callApi("POST", "/students", s));
server.tool("update_student", "Update a student (PUT /students/{id})",
  { id, ...Object.fromEntries(Object.entries(student).map(([k, v]) => [k, v.optional()])) } as any,
  ({ id, ...rest }: any) => callApi("PUT", `/students/${id}`, rest));
server.tool("delete_student", "Delete a student (DELETE /students/{id})", { id }, ({ id }) => callApi("DELETE", `/students/${id}`));
server.tool("search_by_name", "Find students by exact name", { name: z.string() }, ({ name }) => callApi("GET", `/students/name/${enc(name)}`));
server.tool("search_by_major", "Find students by exact major", { major: z.string() }, ({ major }) => callApi("GET", `/students/major/${enc(major)}`));
server.tool("search_by_email", "Find students by email", { email: z.string() }, ({ email }) => callApi("GET", `/students/email/${enc(email)}`));
server.tool("search_older_than", "Find students older than age (API enforces min 15)", { age: z.number().int() }, ({ age }) => callApi("GET", `/students/older-than/${age}`));

await server.connect(new StdioServerTransport());
