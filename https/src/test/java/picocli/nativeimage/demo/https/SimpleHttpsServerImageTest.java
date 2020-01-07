package picocli.nativeimage.demo.https;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static picocli.nativeimage.demo.https.NativeImageHelper.executable;
import static picocli.nativeimage.demo.https.NativeImageHelper.getStdErr;
import static picocli.nativeimage.demo.https.NativeImageHelper.getStdOut;

public class SimpleHttpsServerImageTest {

    @DisabledOnOs(OS.WINDOWS)
    @Tag("native-image")
    @Test
    public void testUsageHelp() throws IOException, InterruptedException {
        Process process = new ProcessBuilder(executable(), "https-server", "--help").start();

        String expected = String.format("" +
                "Usage: demo https-server [-dhvV] [--stay-alive] [-p=<port>]%n" +
                "Starts a HTTPS server running on the specified port.%n" +
                "  -d, --debug         Print debug information.%n" +
                "  -h, --help          Show this help message and exit.%n" +
                "  -p, --port=<port>   The port to listen on. Default: 8000.%n" +
                "      --stay-alive    Keep main thread alive.%n" +
                "  -v, --verbose       Print requests received.%n" +
                "  -V, --version       Print version information and exit.%n");
        assertEquals(expected, getStdOut(process));
        assertEquals("", getStdErr(process));
        process.waitFor(3, TimeUnit.SECONDS);
        assertEquals(0, process.exitValue());
        process.destroy();
    }

    @DisabledOnOs(OS.WINDOWS)
    @Tag("native-image")
    @Test
    public void testHttps() throws IOException, InterruptedException {

        int port = 8000;

        System.out.println("Starting https-server process...");
        Process server = new ProcessBuilder(executable(), "https-server", "--stay-alive", "-p=" + port).start();
        assertTrue(server.isAlive(), "https-server process must be alive after it is started");
        System.out.println("Started https-server process OK.");

        Process client = new ProcessBuilder(executable(),
                "https-client", "--use-local-keystore", "https://localhost:" + port).start();
        assertTrue(client.isAlive(), "https-client process must be alive after it is started");
        System.out.println("Started https-client process OK.");

        client.waitFor(30, TimeUnit.SECONDS);
        System.out.println(client.isAlive());
        assertFalse(client.isAlive(), "https-client process must not be alive after 20 seconds");

        //String cipherSuite = "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384"; // TLS_RSA_WITH_AES_256_CBC_SHA256 without SunEC
        //String altCiphersuite = "TLS_AES_128_GCM_SHA256";

        String expectedPrefix = String.format("" +
                "Response Code : 200%n" +
                "Cipher Suite : "); // asserting on cipher suite makes test fragile
        String expectedPostfix = String.format("" +
                "%n" +
                "Cert Type : X.509%n" +
                "Cert Hash Code : -2137823083%n" +
                "Cert Public Key Algorithm : RSA%n" +
                "Cert Public Key Format : X.509%n" +
                "%n" +
                "****** Content of the URL ********%n" +
                "You asked for /; This is the response%n");
        String actualClientOut = getStdOut(client);
        assertTrue(actualClientOut.startsWith(expectedPrefix), actualClientOut);
        assertTrue(actualClientOut.endsWith(expectedPostfix), actualClientOut);
        assertEquals("", getStdErr(client));
        assertEquals(0, client.exitValue());

        System.out.println("Client response OK");

        //assertEquals("", getStdErr(server));
        //System.out.println("Received no standard err from server.");
        //
        //String expected = String.format("Server started OK on port %d%n", port);
        //assertEquals(expected, getStdOut(server));
        //
        //System.out.println("Received standard out from server.");

        server.destroy();
        client.destroy();
    }
}
