# SkHttp

[![SkriptHubViewTheDocs](http://skripthub.net/static/addon/ViewTheDocsButton.png)](http://skripthub.net/docs/?addon=SkHttp)

HTTP client, HTTP server, WebSockets and Discord webhooks for [Skript](https://github.com/SkriptLang/Skript).

This is a maintained fork of [Fusezion/SkHttp](https://github.com/Fusezion/SkHttp) (originally by aabss), which was archived in 2026. The 1.6.0 line modernizes the build, fixes long-standing bugs and keeps the addon working on current Skript releases.

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

## Behavior changes in 1.6.0

- Endpoint triggers now run on the main server thread (Bukkit API is safe to use inside them). Request bodies are read before the trigger runs, so remote clients can no longer stall the server.
- `send request` (the sync form) no longer blocks the thread: the trigger pauses and resumes when the response arrives, like Skript's `wait`. Code after the effect still sees the response, and requests to the server's own endpoints now work instead of freezing the server.
- When a request fails, the section body still runs but `the response` and `last http response` are not set (previously `last http response` kept a stale response from an earlier request). Check `if last http response is set` to detect failures.
- `... has key/value` on an empty json list is now false instead of true.
- (1.6.1) `send text message`/`send binary message` now send a complete message; use `send partial text message` for a fragment.
- (1.6.1) `https server` no longer parses (it silently served plaintext). Use `http server`.

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
