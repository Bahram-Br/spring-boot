---
name: maven-wrapper-windows-diagnosis
description: Diagnose mvnw/mvnw.cmd startup failures on Windows (e.g. "fail to move MAVEN_HOME", "Cannot start maven from wrapper") by inspecting the wrapper script and the ~/.m2/wrapper/dists cache. Use when a Maven wrapper run fails before Maven ever starts.
source: auto-skill
extracted_at: '2026-09-23T00:38:09.858Z'
---

# Diagnosing Maven wrapper startup failures on Windows

## When to use
`.\mvnw.cmd <goal>` fails before Maven ever runs, with symptoms like:
- `fail to move MAVEN_HOME`
- `Cannot start maven from wrapper`
- Wrapper exits code 1 with no Java/Maven/compile output at all

## Key insight
A wrapper startup failure is a **build-tooling problem, not a code/compile problem**. `mvn` never launched, so don't start debugging the project source — debug the wrapper and its cache first.

## Procedure (verified working sequence)
1. **Run the exact command** the user ran and capture the full output + exit code. In `-q` mode the only output may be the wrapper error itself.
2. **Locate the failing step in the wrapper script**: grep `mvnw.cmd` for the error message (e.g. `fail to move`). In wrapper 3.3.x the message is emitted at the end of the PowerShell install block: after download → `Expand-Archive` → `Rename-Item` → `Move-Item`, when the final `Test-Path $MAVEN_HOME` check fails. Conclusion: download and extraction succeeded; the **move into the wrapper cache failed**.
3. **Validate `.mvn/wrapper/maven-wrapper.properties`** — file exists and `distributionUrl` is well-formed → config is not the problem.
4. **Inspect the wrapper dists cache** at `C:\Users\<user>\.m2\wrapper\dists\`:
   - A **freshly created EMPTY folder** named after the distribution (e.g. `apache-maven-3.9.16`, timestamp = run time) = the move failed; the wrapper created the parent but the `<sha256-hash>` subfolder never landed.
   - An **older-format folder** (e.g. `apache-maven-3.9.16-bin\<hash>\apache-maven-3.9.16\`) from a previous run usually contains a **complete, working Maven** — wrapper 3.3.x changed the cache layout, so it won't reuse old-format caches even though they are valid.
5. **Confirm the cached Maven is usable** by checking `<old-cache>\apache-maven-3.9.16\bin\` contains `mvn.cmd` etc.

## Most likely root cause
Windows Defender / antivirus momentarily locking freshly extracted files during `Move-Item`, or a transient permission/filesystem hiccup. Typically run-specific, not a permanent break.

## Workarounds (offer in this order, do not execute without asking)
1. **Just retry the wrapper** — the move often succeeds once the AV lock clears. If a partial empty cache folder exists, it may need deleting first.
2. **Run the already-cached Maven directly** (read-only for the project — only writes build output to `target/`):
   `"C:\Users\<user>\.m2\wrapper\dists\apache-maven-3.9.16-bin\<hash>\apache-maven-3.9.16\bin\mvn.cmd" -q <goal>`
   Use this to verify whether the *code* itself compiles when the user wants to know what's wrong.
3. Install Maven globally and set `MAVEN_HOME` / use `mvn` directly.

## Notes
- If the user says "do not modify anything": deleting cache folders or changing config requires confirmation; running a compile via the cached Maven is acceptable (writes only to `target/`) but confirm first.
- Report the diagnosis as: tooling failure (wrapper) → evidence found in cache → likely cause → options. Don't blame the project code.
