package lol.aabss.skhttp.objects;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lol.aabss.skhttp.SkHttp;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class WebHandler implements HttpHandler {
    private final File webFilesDirectory;

    public WebHandler(File webFilesDirectory) {
        this.webFilesDirectory = webFilesDirectory;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try (exchange) {
            String contextPath = exchange.getHttpContext().getPath();
            String requestPath = exchange.getRequestURI().getPath();
            String relativePath = requestPath.substring(Math.min(contextPath.length(), requestPath.length()));
            while (relativePath.startsWith("/")) {
                relativePath = relativePath.substring(1);
            }
            File fileRequested = relativePath.isEmpty()
                    ? webFilesDirectory
                    : new File(webFilesDirectory, relativePath);
            // The URI path arrives percent-decoded, so ".." segments must not escape the site directory.
            String canonicalRoot = webFilesDirectory.getCanonicalPath();
            String canonicalRequested = fileRequested.getCanonicalPath();
            if (!canonicalRequested.equals(canonicalRoot) && !canonicalRequested.startsWith(canonicalRoot + File.separator)) {
                respondNotFound(exchange);
                return;
            }
            if (fileRequested.isDirectory()) {
                // Relative asset links in the served pages only resolve when directory URLs end with a slash.
                if (!requestPath.endsWith("/")) {
                    exchange.getResponseHeaders().set("Location", requestPath + "/");
                    exchange.sendResponseHeaders(301, -1);
                    return;
                }
                fileRequested = new File(fileRequested, "index.html");
            }
            if (fileRequested.isFile()) {
                byte[] bytes = Files.readAllBytes(fileRequested.toPath());
                exchange.getResponseHeaders().set("Content-Type", getMimeType(fileRequested.getName()));
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            } else {
                respondNotFound(exchange);
            }
        } catch (IOException e) {
            SkHttp.LOGGER.debug("Client disconnected while being served a site file: " + e.getMessage());
        }
    }

    private void respondNotFound(HttpExchange exchange) throws IOException {
        byte[] response = "404 (Not Found)".getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(404, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }

    // Only common is listed, full list here:
    // https://www.iana.org/assignments/media-types/media-types.xhtml
    private String getMimeType(String fileName) {
        String[] split = fileName.split("\\.");
        String extension = split[split.length-1].toLowerCase();
        return switch (extension) {
            case "3pg" -> "audio/3gpp";
            case "3g2" -> "audio/3gpp2";
            case "7z" -> "application/x-7z-compressed";
            case "aac" -> "audio/aac";
            case "abw" -> "application/x-abiword";
            case "apng" -> "image/apng";
            case "arc" -> "application/x-freearc";
            case "avif" -> "image/avif";
            case "avi" -> "video/x-msvideo";
            case "azw" -> "application/vnd.amazon.ebook";
            case "bin" -> "application/octet-stream";
            case "bmp" -> "image/bmp";
            case "bz" -> "application/x-bzip";
            case "bz2" -> "application/x-bzip2";
            case "cda" -> "application/x-cdf";
            case "csh" -> "application/x-csh";
            case "css" -> "text/css";
            case "csv" -> "text/csv";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "eot" -> "application/vnd.ms-fontobject";
            case "epub" -> "application/epub+zip";
            case "gz" -> "application/gzip";
            case "gif" -> "image/gif";
            case "html", "htm" -> "text/html";
            case "ico" -> "image/vnd.microsoft.icon";
            case "ics" -> "text/calendar";
            case "jar" -> "application/java-archive";
            case "jpeg", "jpg" -> "image/jpeg";
            case "js", "mjs" -> "text/javascript";
            case "json" -> "application/json";
            case "jsonld" -> "application/ld+json";
            case "mid", "midi" -> "audio/midi";
            case "mp3" -> "audio/mpeg";
            case "mp4" -> "video/mp4";
            case "mpeg" -> "video/mpeg";
            case "mpkg" -> "application/vnd.apple.installer+xml";
            case "odp" -> "application/vnd.oasis.opendocument.presentation";
            case "ods" -> "application/vnd.oasis.opendocument.spreadsheet";
            case "odt" -> "application/vnd.oasis.opendocument.text";
            case "oga", "ogg" -> "audio/ogg";
            case "ogv" -> "video/ogg";
            case "ogx" -> "application/ogg";
            case "opus" -> "audio/ogg";
            case "otf" -> "font/otf";
            case "png" -> "image/png";
            case "pdf" -> "application/pdf";
            case "php" -> "application/x-httpd-php";
            case "ppt" -> "application/vnd.ms-powerpoint";
            case "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "rar" -> "application/vnd.rar";
            case "rtf" -> "application/rtf";
            case "sh" -> "application/x-sh";
            case "svg" -> "image/svg+xml";
            case "tar" -> "application/x-tar";
            case "tif", "tiff" -> "image/tiff";
            case "ts" -> "video/mp2t";
            case "ttf" -> "font/ttf";
            case "vsd" -> "application/vnd.visio";
            case "wav" -> "audio/wav";
            case "weba" -> "audio/webm";
            case "webm" -> "video/webm";
            case "webp" -> "image/webp";
            case "woff" -> "font/woff";
            case "woff2" -> "font/woff2";
            case "xhtml" -> "application/xhtml+xml";
            case "xls" -> "application/vnd.ms-excel";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "xml" -> "application/xml";
            case "xul" -> "application/vnd.mozilla.xul+xml";
            case "zip" -> "application/zip";
            default -> "text/plain";
        };
    }
}
