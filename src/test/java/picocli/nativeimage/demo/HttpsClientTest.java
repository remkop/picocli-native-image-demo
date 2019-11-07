package picocli.nativeimage.demo;

import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HttpsClientTest {
    
    @Test
    public void testMissingRequiredParamPrintsToStdErr() {
        PrintStream oldErr = System.err;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            System.setErr(new PrintStream(baos));
            new CommandLine(new HttpsClient()).execute();
            
            String expected = String.format("" +
                    "Missing required parameter: <file>%n" +
                    "Usage: httpsclient [-hV] [-o=<logfile>] <url>%n" +
                    "Prints the checksum (MD5 by default) of a file to STDOUT.%n" +
                    "      <url>       The URL to download.%n" +
                    "  -a, --algorithm=<algorithm>%n" +
                    "                  MD5, SHA-1, SHA-256, ...%n" +
                    "  -h, --help      Show this help message and exit.%n" +
                    "  -V, --version   Print version information and exit.%n");
            assertEquals(expected, baos.toString());
        } finally {
            System.setErr(oldErr);
        }
    }

    @Test
    public void testDefaultAlgorithm() throws IOException, InterruptedException {
        File tempFile = CheckSumImageTest.createTempDataFile();

        PrintStream oldOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(baos));
            int exitCode = new CommandLine(new HttpsClient()).execute(tempFile.getAbsolutePath());
            tempFile.delete();

            String exected = String.format("764efa883dda1e11db47671c4a3bbd9e%n");

            assertEquals(exected, baos.toString());
            assertEquals(0, exitCode);
        } finally {
            System.setOut(oldOut);
        }
    }

    @Test
    public void testMissingRequiredParamGivesExitCode2() {
        int exitCode = new CommandLine(new HttpsClient()).setErr(devNull()).execute();
        assertEquals(CommandLine.ExitCode.USAGE, exitCode);
    }

    private PrintWriter devNull() {
        return new PrintWriter(new StringWriter());
    }

}
