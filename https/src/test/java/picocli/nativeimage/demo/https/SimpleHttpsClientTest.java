package picocli.nativeimage.demo.https;

import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SimpleHttpsClientTest {
    
    @Test
    public void testResponseForDefaultUrl() {
        PrintStream oldOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(baos));
            new CommandLine(new SimpleHttpsClient()).execute();
            
            String expected = String.format("" +
                    "Response Code : 200%n" +
                    "Cipher Suite : TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256%n" +
                    "%n" +
                    "Cert Type : X.509%n" +
                    "Cert Hash Code : -898597699%n" +
                    "Cert Public Key Algorithm : RSA%n" +
                    "Cert Public Key Format : X.509%n" +
                    "%n" +
                    "Cert Type : X.509%n" +
                    "Cert Hash Code : -599509715%n" +
                    "Cert Public Key Algorithm : RSA%n" +
                    "Cert Public Key Format : X.509%n" +
                    "%n" +
                    "****** Content of the URL ********%n" +
                    "security.provider.3=notSunEC%n");
            assertEquals(expected, baos.toString());
        } finally {
            System.setOut(oldOut);
        }
    }

    @Test
    public void testNoCertificates() throws IOException, InterruptedException {

        PrintStream oldOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(baos));
            int exitCode = new CommandLine(new SimpleHttpsClient()).execute("--no-certificates");

            String expected = String.format("" +
                    "****** Content of the URL ********%n" +
                    "security.provider.3=notSunEC%n");

            assertEquals(expected, baos.toString());
            assertEquals(0, exitCode);
        } finally {
            System.setOut(oldOut);
        }
    }

    @Test
    public void testHeaders() throws IOException, InterruptedException {

        PrintStream oldOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(baos));
            int exitCode = new CommandLine(new SimpleHttpsClient()).execute("--no-certificates", "--headers");

            String actual = baos.toString();
            assertTrue(actual.contains("null: [HTTP/1.1 200 OK]"));
            assertTrue(actual.contains("Content-Length: [29]"));
            assertTrue(actual.contains("Content-Type: [text/plain; charset=utf-8]"));

//            String expected = String.format("" +
//                    "null: [HTTP/1.1 200 OK]%n" +
//                    "Access-Control-Allow-Origin: [*]%n" +
//                    "Source-Age: [0]%n" +
//                    "X-Timer: [S1573120940.436118,VS0,VE225]%n" +
//                    "X-Frame-Options: [deny]%n" +
//                    "Strict-Transport-Security: [max-age=31536000]%n" +
//                    "Content-Security-Policy: [default-src 'none'; style-src 'unsafe-inline'; sandbox]%n" +
//                    "Content-Length: [29]%n" +
//                    "X-XSS-Protection: [1; mode=block]%n" +
//                    "X-Geo-Block-List: []%n" +
//                    "X-GitHub-Request-Id: [35BA:3692:6F2485:777F5F:5DC3EBAB]%n" +
//                    "Content-Type: [text/plain; charset=utf-8]%n" +
//                    "X-Cache: [MISS]%n" +
//                    "X-Content-Type-Options: [nosniff]%n" +
//                    "Connection: [keep-alive]%n" +
//                    "Date: [Thu, 07 Nov 2019 10:02:20 GMT]%n" +
//                    "Via: [1.1 varnish]%n" +
//                    "Accept-Ranges: [bytes]%n" +
//                    "Cache-Control: [max-age=300]%n" +
//                    "ETag: [\"2cb748e0b5a6f0f1b5a231fb56b1db5f82f1b590f9d260c38d98e12737e242ea\"]%n" +
//                    "Vary: [Authorization,Accept-Encoding, Accept-Encoding]%n" +
//                    "Expires: [Thu, 07 Nov 2019 10:07:20 GMT]%n" +
//                    "X-Cache-Hits: [0]%n" +
//                    "X-Fastly-Request-ID: [73ee5bb3cd15fd096c09a47cfc5bffa86a069e13]%n" +
//                    "****** Content of the URL ********%n" +
//                    "security.provider.3=notSunEC%n");
//
//            assertEquals(expected, baos.toString());
            assertEquals(0, exitCode);
        } finally {
            System.setOut(oldOut);
        }
    }

    @Test
    public void testUnknownOptionGivesExitCode2() {
        PrintStream oldErr = System.err;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            System.setErr(new PrintStream(baos));
            int exitCode = new CommandLine(new SimpleHttpsClient()).execute("--xxx");

            String expected = String.format("" +
                    "Unknown option: '--xxx'%n" +
                    "Usage: https-client [-chHV] [--use-local-keystore] <url>%n" +
                    "Uses https protocol to get a remote resource.%n" +
                    "      <url>                  The URL to download%n" +
                    "  -c, --[no-]certificates    Show server certificates (true by default)%n" +
                    "  -h, --help                 Show this help message and exit.%n" +
                    "  -H, --headers              Print response headers (false by default)%n" +
                    "      --use-local-keystore   Use this when connecting to local SimpleHttpsServer%n" +
                    "  -V, --version              Print version information and exit.%n");

            assertEquals(expected, baos.toString());
            assertEquals(CommandLine.ExitCode.USAGE, exitCode);
        } finally {
            System.setErr(oldErr);
        }
    }

    private PrintWriter devNull() {
        return new PrintWriter(new StringWriter());
    }

}
