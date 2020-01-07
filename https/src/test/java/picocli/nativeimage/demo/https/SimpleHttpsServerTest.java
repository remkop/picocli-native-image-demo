package picocli.nativeimage.demo.https;

import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static picocli.nativeimage.demo.https.NativeImageHelper.getStdOut;

public class SimpleHttpsServerTest {

    @Test
    public void testUsageHelp() throws IOException, InterruptedException {
        PrintStream oldOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(baos));
            new CommandLine(new SimpleHttpsServer()).execute("--help");

            String expected = String.format("" +
                    "Usage: https-server [-dhvV] [--stay-alive] [-p=<port>]%n" +
                    "Starts a HTTPS server running on the specified port.%n" +
                    "  -d, --debug         Print debug information.%n" +
                    "  -h, --help          Show this help message and exit.%n" +
                    "  -p, --port=<port>   The port to listen on. Default: 8000.%n" +
                    "      --stay-alive    Keep main thread alive.%n" +
                    "  -v, --verbose       Print requests received.%n" +
                    "  -V, --version       Print version information and exit.%n");
            assertEquals(expected, baos.toString());
        } finally {
            System.setOut(oldOut);
        }
    }

    @Test
    public void testHttps() throws IOException, InterruptedException {
        PrintStream oldOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SimpleHttpsServer server = new SimpleHttpsServer();
        try {
            System.setOut(new PrintStream(baos));
            new CommandLine(server).execute("--port=7999");

            String expected = String.format("Server started OK on port 7999%n");
            assertEquals(expected, baos.toString());

            // client request
            baos = new ByteArrayOutputStream();
            System.setOut(new PrintStream(baos));
            new CommandLine(new SimpleHttpsClient()).execute("--use-local-keystore", "https://localhost:7999");

//            String clientOutput = String.format("" +
//                    "Response Code : 200%n" +
//                    "Cipher Suite : TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384%n" +
//                    "%n" +
//                    "Cert Type : X.509%n" +
//                    "Cert Hash Code : -2137823083%n" +
//                    "Cert Public Key Algorithm : RSA%n" +
//                    "Cert Public Key Format : X.509%n" +
//                    "%n" +
//                    "****** Content of the URL ********%n" +
//                    "You asked for /; This is the response%n");
//            assertEquals(clientOutput, baos.toString());
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
            String actualClientOut = baos.toString();
            assertTrue(actualClientOut.startsWith(expectedPrefix), actualClientOut);
            assertTrue(actualClientOut.endsWith(expectedPostfix), actualClientOut);
        } finally {
            server.stop(1);
            System.setOut(oldOut);
        }

    }
}
