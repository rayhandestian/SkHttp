# Changelog

All notable changes to this maintained fork of SkHttp are documented here. This fork's line begins at 1.6.0; releases 1.0 through 1.5 predate it and came from the original project.

## 2.0.0

Migrates the addon onto Skript's modern `org.skriptlang` registration API (`SyntaxRegistry` / `EventValueRegistry`), replacing the `Skript.registerX`, `EventValues`, `Getter` and `ExpressionType` APIs that are deprecated for removal. This future-proofs SkHttp against their eventual removal (they still work in Skript 2.16). Behavior is unchanged from 1.6.1.

- **Requires Skript 2.15+** and disables itself with a clear message on older Skript. For Skript 2.10 to 2.14, use the 1.6.x line (maintained on the `1.6.x` branch).
- No functional changes to any syntax. Also clears the remaining deprecated-for-removal API uses (`loadCode`, `Timespan.getMilliSeconds`) and fixes the `response version`/`response url` pattern typos and a lang-file typo picked up along the way.

## 1.6.1

Bug-fix release following a full-codebase audit of the modules the 1.6.0 review did not cover (Discord webhooks, file download, the client and websocket builders, type parsers, and the remaining expressions). 21 distinct defects were fixed.

- HTTP client builder: parses without an `executor:` subsection; `follow redirects` is respected instead of always using `normal`; `priority` and `version` no longer crash or drop each other; executor code runs on the main thread.
- WebSockets: request builder no longer crashes or leaks headers between scripts; `last websocket` is actually set; `send text/binary message` sends a complete message (use `partial` for a fragment); invalid urls warn instead of crashing.
- Expressions: `header ... by key`, `version of`, and `previous response of` no longer crash on absent/empty values.
- `new http server` no longer picks an invalid port above 65535. `https server` syntax removed (it silently served plaintext).
- `httpresponse` type pattern no longer collides with `httprequest`. `/skhttp reload` no longer overwrites the edited config. File download closes its streams. Discord embed/footer/author/field stringifiers no longer crash on unset fields. Header interpolation quotes replacements.

Behavior changes: `send text message` is complete by default (use `send partial text message` for fragments); `https server` no longer parses (use `http server`).

## 1.6.0

First release of the maintained fork. Modernizes the build and fixes bugs across every module.

- Build: Gradle 9.6.1, Java 21 bytecode, compiles against Skript 2.15.4, CI actions pinned to commit SHAs.
- HTTP server: fixes the broken pipe / "headers already sent" console spam on client disconnect (upstream issue #1); endpoint triggers run on the main thread with request bodies pre-read off it; closes a path traversal hole in the static site handler; `responseBody()` returns the sent body; case-insensitive method matching; recreatable deleted endpoints.
- HTTP client: sync `send request` no longer blocks the thread (suspends and resumes like `wait`), so self-requests work; `the response` resolves inside the section; failed requests run the section with no response set and clear `last http response`; per-builder state no longer shared through a static field.
- WebSockets: events dispatched on the main thread (previously crashed on modern Paper); correct binary decoding.
- JSON: works with variables (was ClassCastException); removal by value matches correctly; `use-skript-index` consistent; non-JSON text becomes a string value.

Behavior changes: sync `send request` counts as a delay (use `send async request` in delay-forbidden contexts like endpoint triggers); `... has key/value` on an empty json list is now false.

Requirements: Java 21+, Skript 2.10+ (tested 2.15.4). For older servers (Java 17, Skript 2.7+), use 1.5.
