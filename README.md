# SkHttp

[![SkriptHubViewTheDocs](http://skripthub.net/static/addon/ViewTheDocsButton.png)](http://skripthub.net/docs/?addon=SkHttp)

HTTP client, HTTP server, WebSockets and Discord webhooks for [Skript](https://github.com/SkriptLang/Skript).

This is a maintained fork of [Fusezion/SkHttp](https://github.com/Fusezion/SkHttp) (originally by aabss), which was archived in 2026. The 1.6.0 line modernizes the build, fixes long-standing bugs and keeps the addon working on current Skript releases.

## Requirements

- Java 21 or newer
- Skript 2.10 or newer (tested against 2.15.4)
- A Bukkit-based server (Paper recommended)

Versions 1.5 and older support Skript 2.7+ on Java 17 and remain available from the original repository's releases.

## Features

- **HTTP server**: host endpoints and static sites from Skript, respond to requests with JSON
- **HTTP client**: build and send sync or async requests with headers, bodies, timeouts and file uploads
- **WebSockets**: connect to WebSocket servers and react to messages with Skript events
- **Discord webhooks**: build embeds and send webhook messages
- **JSON**: create, read and edit JSON values (for heavy JSON work, consider the excellent [skJson](https://github.com/cooffeeRequired/skJson))

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
