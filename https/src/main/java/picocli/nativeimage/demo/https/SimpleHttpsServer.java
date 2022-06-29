package picocli.nativeimage.demo.https;

import java.io.*;
import java.net.InetSocketAddress;
import java.lang.*;
import java.net.URL;

import com.sun.net.httpserver.HttpsServer;

import java.security.KeyManagementException;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

import com.sun.net.httpserver.*;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;

import javax.net.ssl.SSLContext;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.Callable;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpsExchange;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.jansi.graalvm.AnsiConsole;

/**
 * Example HTTPS server.
 * <p>
 * Gets certificates from a keystore in /testkey.jks in the classpath.
 * </p><p>
 * Create the keystore with this command:
 * </p>
 * <pre>
 * keytool -genkeypair -keyalg RSA -alias selfsigned -keystore testkey.jks -storepass password -validity 360 -keysize 2048
 * </pre>
 */
@Command(name = "https-server", mixinStandardHelpOptions = true,
        version = "simple https server 1.0",
        description = "Starts a HTTPS server running on the specified port.")
public class SimpleHttpsServer implements Callable<Integer> {

    @Option(names = { "-p", "--port"}, defaultValue = "8000",
            description = "The port to listen on. Default: ${DEFAULT-VALUE}.")
    int port;

    @Option(names = { "-v", "--verbose"}, description = "Print requests received.")
    boolean verbose;

    @Option(names = { "-d", "--debug"}, description = "Print debug information.")
    boolean debug;

    @Option(names = { "--stay-alive"}, description = "Keep main thread alive.")
    boolean stayAlive;

    HttpsServer httpsServer;
    volatile boolean running;

    @Override
    public Integer call() throws IOException, NoSuchAlgorithmException,
            KeyStoreException, CertificateException, UnrecoverableKeyException,
            KeyManagementException {

        httpsServer = HttpsServer.create(new InetSocketAddress(port), 0);
        httpsServer.setHttpsConfigurator(createHttpsConfigurator());
        httpsServer.createContext("/", new MyHandler());
        httpsServer.createContext("/test", new MyHandler());
        // creates a default single-threaded executor
        httpsServer.setExecutor(null);
        httpsServer.start();
        System.out.println("Server started OK on port " + httpsServer.getAddress().getPort());

        if (debug) {
            printThreads();
        }
        if (stayAlive) {
            stayAlive();
        }
        return 0;
    }

    public static void main(String[] args) throws Exception {
        int exitCode;
        try (AnsiConsole console = AnsiConsole.windowsInstall()) {
            exitCode = new CommandLine(new SimpleHttpsServer()).execute(args);
        }
        if (exitCode != CommandLine.ExitCode.OK) {
            System.exit(exitCode);
        }
    }

    public void stop(int timeoutMillis) {
        running = false;
        if (debug) {
            System.err.println("Stopping the HTTPS server (timeout " + timeoutMillis + " ms)");
        }
        httpsServer.stop(timeoutMillis);
    }

    private void stayAlive() {
        // for some reason the native image exits immediately when the main thread completes...
        // try to keep the process running by keeping the main thread alive
        running = true;
        while (running) {
            try {
                if (debug) {
                    System.err.println(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").format(new Date()) + " - alive");
                }
                Thread.sleep(60*1000L);
            } catch (InterruptedException e) {
                if (debug) {
                    System.err.println("Interrupted.");
                    stop(0);
                }
            }
        }
    }

    private void printThreads() {
        System.err.println("Threads:");
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.isAlive() && !t.isInterrupted()) {
                String deamon = t.isDaemon() ? "daemon" : "";
                System.err.printf("  %2d - %s [%s] %s%n", t.getId(), t.getName(), t.getPriority(), deamon);
            }
        }
    }

    class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            HttpsExchange httpsExchange = (HttpsExchange) t;
            if (verbose) {
                System.out.println(t.getRemoteAddress() + "\t" + t.getRequestURI() +
                        "\t" + httpsExchange.getSSLSession().getCipherSuite());
            }
            String response = "You asked for " + t.getRequestURI() + "; This is the response";
            t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            t.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    private HttpsConfigurator createHttpsConfigurator()
            throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException,
            KeyStoreException, KeyManagementException, IOException {

        SSLContext sslContext = createSslContext();
        if (debug) {
            System.err.println("Created SSL context: protocol=" + sslContext.getProtocol() +
                    ", provider=" + sslContext.getProvider().getInfo());
        }

        return new HttpsConfigurator(sslContext) {
            @Override
            public void configure(HttpsParameters params) {
                try {
                    // initialise the SSL context
                    SSLContext context = getSSLContext();
                    SSLEngine engine = context.createSSLEngine();
                    params.setNeedClientAuth(false);
                    params.setCipherSuites(engine.getEnabledCipherSuites());
                    params.setProtocols(engine.getEnabledProtocols());

                    // Set the SSL parameters
                    SSLParameters sslParameters = context.getSupportedSSLParameters();
                    params.setSSLParameters(sslParameters);

                    if (debug) {
                        System.err.println("Configuring HttpsParameters with " +
                                Arrays.toString(sslParameters.getCipherSuites()) + " " +
                                Arrays.toString(sslParameters.getProtocols()));
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    throw new IllegalStateException("Failed to create HTTPS port", ex);
                }
            }
        };
    }

    private SSLContext createSslContext() throws NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException, UnrecoverableKeyException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("TLS");

        // initialise the keystore
        char[] password = "password".toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        URL keystore = getClass().getResource("/testkey.jks");
        if (debug) {
            System.err.println("Found " + keystore + " for resource '/testkey.jks'");
        }
        if (keystore == null) {
            throw new IllegalStateException("Could not find keystore resource /testkey.jks in classpath");
        }
        ks.load(keystore.openStream(), password);

        // setup the key manager factory
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, password);

        // setup the trust manager factory
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        // setup the HTTPS context and parameters
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        return sslContext;
    }
}
