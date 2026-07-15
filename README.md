# SkHttp

[![SkriptHubViewTheDocs](http://skripthub.net/static/addon/ViewTheDocsButton.png)](http://skripthub.net/docs/?addon=SkHttp)

HTTP client, HTTP server, WebSockets and Discord webhooks for [Skript](https://github.com/SkriptLang/Skript).

This is a maintained fork of [Fusezion/SkHttp](https://github.com/Fusezion/SkHttp) (originally by aabss), which was archived in 2026. The fork modernizes the build, fixes long-standing bugs and keeps the addon working on current Skript releases; the 2.x line moves onto Skript's modern addon API.

## Requirements

- Java 21 or newer
- Skript 2.15 or newer (2.x uses Skript's modern addon API; tested against 2.15.4)
- A Bukkit-based server (Paper recommended)

For Skript 2.10 through 2.14, use the 1.6.x line (maintained on the `1.6.x` branch). Versions 1.5 and older support Skript 2.7+ on Java 17 and remain available from the original repository's releases.

## Features

- **HTTP server**: host endpoints and static sites from Skript, respond to requests with JSON
- **HTTP client**: build and send sync or async requests with headers, bodies, timeouts and file uploads
- **WebSockets**: connect to WebSocket servers and react to messages with Skript events
- **Discord webhooks**: build embeds and send webhook messages
- **JSON**: create, read and edit JSON values (for heavy JSON work, consider the excellent [skJson](https://github.com/cooffeeRequired/skJson))

## Behavior differences from upstream

If you are coming from the original addon, a few behaviors changed in the fork. The highlights are below; see [CHANGELOG.md](CHANGELOG.md) for the full per-release breakdown.

- Endpoint triggers run on the main server thread, so the Bukkit API is safe to use inside them. Request bodies are read before the trigger runs, so remote clients can no longer stall the server.
- `send request` (the sync form) no longer blocks the thread: the trigger pauses and resumes when the response arrives, like Skript's `wait`. Code after the effect still sees the response, and requests to the server's own endpoints work instead of freezing the server. It counts as a delay, so use `send async request` in delay-forbidden contexts like endpoint triggers.
- When a request fails, the section body still runs but `the response` and `last http response` are not set. Check `if last http response is set` to detect failures.
- `send text message` / `send binary message` send a complete message; use `send partial text message` for a fragment.
- Use `http server`; the `https server` syntax was removed (it silently served plaintext).
- `... has key/value` on an empty json list is now false instead of true.

## Example

```applescript
on script load:
    set {-server} to new http server with port 8080
    create endpoint using {-server}:
        method: "GET"
        path: "status"
        trigger:
            respond with code 200 and message "{""online"": true}"
    start {-server}
```

## Building

```
./gradlew shadowJar
```

The jar is written to `build/libs/`. Java 21+ is required to build.

## Changelog

See [CHANGELOG.md](CHANGELOG.md) for what changed in each release.
