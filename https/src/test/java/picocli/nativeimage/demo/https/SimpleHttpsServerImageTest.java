package picocli.nativeimage.demo.https;

import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static picocli.nativeimage.demo.https.NativeImageHelper.executable;
import static picocli.nativeimage.demo.https.NativeImageHelper.getStdErr;
import static picocli.nativeimage.demo.https.NativeImageHelper.getStdOut;

public class SimpleHttpsServerImageTest {

    @DisabledOnOs(OS.WINDOWS)
    @Tag("native-image")
    @Test
    public void testUsageHelp() throws IOException, InterruptedException {
        Process process = new ProcessBuilder(executable, "start-server", "--help").start();

        String expected = String.format("" +
                "Usage: demo start-server [-dhvV] [--stay-alive] [-p=<port>]%n" +
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
    }

    @Ignore
    @DisabledOnOs(OS.WINDOWS)
    @Tag("native-image")
    @Test
    public void testHttps() throws IOException, InterruptedException {
        Process server = new ProcessBuilder(executable, "start-server", "--stay-alive").start();

        String expected = String.format("Server started OK on port 8000%n");
        assertEquals(expected, getStdOut(server));
        //assertEquals("", getStdErr(server));

        Process client = new ProcessBuilder(executable,
                "httpsget", "--use-local-keystore", "https://localhost:8000").start();

        String clientOutput = String.format("" +
                "Response Code : 200%n" +
                "Cipher Suite : TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384%n" +
                "%n" +
                "Cert Type : X.509%n" +
                "Cert Hash Code : -2137823083%n" +
                "Cert Public Key Algorithm : RSA%n" +
                "Cert Public Key Format : X.509%n" +
                "%n" +
                "****** Content of the URL ********%n" +
                "You asked for /; This is the response%n");
        client.waitFor(10, TimeUnit.SECONDS);
        assertEquals(clientOutput, getStdOut(client));
        assertEquals("", getStdErr(client));
        assertEquals(0, client.exitValue());
        
        server.destroy();
    }
}
