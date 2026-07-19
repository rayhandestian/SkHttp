package lol.aabss.skhttp.objects;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import java.net.http.HttpClient;
import java.util.Arrays;
import java.util.LinkedHashSet;

/**
 * Builds {@link java.net.http.HttpClient}s that can actually negotiate TLS 1.3.
 *
 * Some hosting JVM images ship a java.security whose *default* client TLS config only
 * enables TLSv1.2 and omits the TLS 1.3 cipher suites (TLS_AES_*), even though 1.3 is
 * fully supported and is NOT listed in jdk.tls.disabledAlgorithms. A bare
 * {@code HttpClient.newHttpClient()} then can only speak TLS 1.2 and fails against
 * TLS-1.3-only endpoints (e.g. Cloudflare zones with min-TLS 1.3) with
 * "Received fatal alert: protocol_version".
 *
 * We work around it per-client by explicitly enabling TLSv1.3/1.2 and merging the TLS 1.3
 * cipher suites into the enabled set. On a normally-configured JVM this just restates the
 * defaults, so it is safe to apply unconditionally.
 */
public final class HttpClientFactory {

    private HttpClientFactory() {}

    private static final String[] TLS13_CIPHERS = {
            "TLS_AES_256_GCM_SHA384",
            "TLS_AES_128_GCM_SHA256",
            "TLS_CHACHA20_POLY1305_SHA256"
    };

    private static SSLParameters tlsParameters() {
        SSLParameters params;
        try {
            params = SSLContext.getDefault().getDefaultSSLParameters();
        } catch (Exception e) {
            params = new SSLParameters();
        }
        params.setProtocols(new String[]{"TLSv1.3", "TLSv1.2"});

        // Merge the TLS 1.3 cipher suites in front without dropping the existing 1.2 ones.
        LinkedHashSet<String> ciphers = new LinkedHashSet<>(Arrays.asList(TLS13_CIPHERS));
        String[] current = params.getCipherSuites();
        if (current != null) {
            ciphers.addAll(Arrays.asList(current));
        }
        params.setCipherSuites(ciphers.toArray(new String[0]));

        // Keep hostname verification on (custom SSLParameters can otherwise clear it).
        params.setEndpointIdentificationAlgorithm("HTTPS");
        return params;
    }

    /** A client builder pre-configured to allow TLS 1.3. */
    public static HttpClient.Builder builder() {
        return HttpClient.newBuilder().sslParameters(tlsParameters());
    }

    /** A ready client that can negotiate TLS 1.3. Drop-in for {@code HttpClient.newHttpClient()}. */
    public static HttpClient newClient() {
        return builder().build();
    }
}
