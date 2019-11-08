package picocli.nativeimage.demo.https;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;
import picocli.jansi.graalvm.AnsiConsole;


import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.io.*;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import java.security.cert.CertificateException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Example HTTPS client.
 * <p>
 * Gets certificates from a keystore in /testkey.jks in the classpath.
 * </p><p>
 * Create the keystore with this command:
 * </p>
 * <pre>
 * keytool -genkeypair -keyalg RSA -alias selfsigned -keystore testkey.jks -storepass password -validity 360 -keysize 2048
 * </pre>
 */
@Command(name = "httpsget", mixinStandardHelpOptions = true,
        version = "httpsget 4.0",
        description = "Uses https protocol to get a remote resource.")
public class HttpsClient implements Callable<Integer> {

    private static final String DEFAULT_URL = "https://raw.githubusercontent.com/remkop/picocli-native-image-demo/master/java.security.overrides";

    @Parameters(description = "The URL to download", defaultValue = DEFAULT_URL)
    String url;

    @Option(names = {"-c", "--no-certificates"}, defaultValue = "true", negatable = true,
            description = "Show server certificates (${DEFAULT-VALUE} by default)")
    boolean showCertificates;

    @Option(names = {"-H", "--headers"}, defaultValue = "false",
            description = "Print response headers (${DEFAULT-VALUE} by default)")
    boolean showHeaders;

    @Option(names = {"--use-local-keystore"},
            description = "Use this when connecting to local SimpleHttpsServer")
    boolean useLocalKeyStore;

    @Spec
    CommandSpec spec;

    public static void main(String[] args) {
        int exitCode;
        try (AnsiConsole console = AnsiConsole.windowsInstall()) {
            exitCode = new CommandLine(new HttpsClient()).execute(args);
        }
        System.exit(exitCode);
    }

    public Integer call() throws Exception {
        if (useLocalKeyStore) {
            HttpsURLConnection.setDefaultSSLSocketFactory(customSSLSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier ((hostname, session) -> true);
        }
        HttpsURLConnection con = (HttpsURLConnection) new URL(url).openConnection();
        PrintWriter out = spec.commandLine().getOut();

        if (showCertificates) {
            try {
                printCertificates(con, out);
            } catch (SSLException ex) {
                if (!useLocalKeyStore) {
                    System.err.println(ex);
                    System.err.println("Try the --use-local-keystore option when connecting to the demo https server on localhost");
                } else {
                    throw ex;
                }
            }
        }
        if (showHeaders) {
            printHeaders(con, out);
        }
        printContents(con, out);
        return 0;
    }

    private void printHeaders(HttpsURLConnection con, PrintWriter out) {
        out.println("****** Response Headers ********");
        Map<String, List<String>> headerFields = con.getHeaderFields();
        for (String header : headerFields.keySet()) {
            out.printf("%s: %s%n", header, headerFields.get(header));
        }
        out.println();
    }

    private void printCertificates(HttpsURLConnection con, PrintWriter out) throws IOException {
        out.println("Response Code : " + con.getResponseCode());
        out.println("Cipher Suite : " + con.getCipherSuite());
        out.println();

        Certificate[] certs = con.getServerCertificates();
        for (Certificate cert : certs) {
            out.println("Cert Type : " + cert.getType());
            out.println("Cert Hash Code : " + cert.hashCode());
            out.println("Cert Public Key Algorithm : "
                    + cert.getPublicKey().getAlgorithm());
            out.println("Cert Public Key Format : "
                    + cert.getPublicKey().getFormat());
            out.println();
        }
    }

    private void printContents(HttpsURLConnection con, PrintWriter out) throws IOException {
        out.println("****** Content of the URL ********");

        try (InputStream in = con.getInputStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(in))) {

            String input;
            while ((input = br.readLine()) != null) {
                out.println(input);
            }
        }
    }

    private SSLSocketFactory customSSLSocketFactory() throws NoSuchAlgorithmException,
            KeyStoreException, KeyManagementException, IOException, CertificateException {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        char[] password = "password".toCharArray();
        URL keystore = getClass().getResource("/testkey.jks");
        if (keystore == null) {
            throw new IllegalStateException("Could not find keystore resource /testkey.jks in classpath");
        }
        ks.load(keystore.openStream(), password);

        SSLContext context = SSLContext.getInstance("TLS");
        TrustManagerFactory tmf =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);
        X509TrustManager defaultTrustManager = (X509TrustManager)tmf.getTrustManagers()[0];
        context.init(null, new TrustManager[] {defaultTrustManager}, null);
        SSLSocketFactory factory = context.getSocketFactory();
        return factory;
    }
}
