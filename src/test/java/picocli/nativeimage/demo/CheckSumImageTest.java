package picocli.nativeimage.demo;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static picocli.nativeimage.demo.NativeImageHelper.createTempDataFile;
import static picocli.nativeimage.demo.NativeImageHelper.executable;
import static picocli.nativeimage.demo.NativeImageHelper.getStdErr;
import static picocli.nativeimage.demo.NativeImageHelper.getStdOut;

class CheckSumImageTest {

    @Tag("native-image")
    @Test
    public void testUsageHelp() throws IOException, InterruptedException {
        Process process = new ProcessBuilder(executable, "--help").start();

        String expected = String.format("" +
                "Usage: checksum [-hV] [-a=<algorithm>] <file>%n" +
                "Prints the checksum (MD5 by default) of a file to STDOUT.%n" +
                "      <file>      The file whose checksum to calculate.%n" +
                "  -a, --algorithm=<algorithm>%n" +
                "                  MD5, SHA-1, SHA-256, ...%n" +
                "  -h, --help      Show this help message and exit.%n" +
                "  -V, --version   Print version information and exit.%n");
        assertEquals(expected, getStdOut(process));
        assertEquals("", getStdErr(process));
        process.waitFor(3, TimeUnit.SECONDS);
        assertEquals(0, process.exitValue());
        process.destroyForcibly();
    }

    @Tag("native-image")
    @Test
    public void testVersionInfo() throws IOException, InterruptedException {
        Process process = new ProcessBuilder(executable, "--version").start();

        String expected = String.format("checksum 4.0%n"); // JVM: 1.8.0_222 (Oracle Corporation Substrate VM GraalVM dev)

        assertEquals(expected, getStdOut(process));
        assertEquals("", getStdErr(process));
        process.waitFor(3, TimeUnit.SECONDS);
        assertEquals(0, process.exitValue());
        process.destroyForcibly();
    }

    @Tag("native-image")
    @Test
    public void testDefaultAlgorithm() throws IOException, InterruptedException {
        File tempFile = createTempDataFile();

        Process process = new ProcessBuilder(executable, tempFile.getAbsolutePath()).start();

        String expected = String.format("764efa883dda1e11db47671c4a3bbd9e%n");

        assertEquals("", getStdErr(process));
        assertEquals(expected, getStdOut(process));
        process.waitFor(3, TimeUnit.SECONDS);
        assertEquals(0, process.exitValue());
        tempFile.delete();
        process.destroyForcibly();
    }

    @Tag("native-image")
    @Test
    public void testMd5Algorithm() throws IOException, InterruptedException {
        File tempFile = createTempDataFile();

        Process process = new ProcessBuilder(executable, "-a", "md5", tempFile.getAbsolutePath()).start();

        String expected = String.format("764efa883dda1e11db47671c4a3bbd9e%n");

        assertEquals(expected, getStdOut(process));
        assertEquals("", getStdErr(process));
        process.waitFor(3, TimeUnit.SECONDS);
        assertEquals(0, process.exitValue());
        tempFile.delete();
        process.destroyForcibly();
    }

    @Tag("native-image")
    @Test
    public void testSha1Algorithm() throws IOException, InterruptedException {
        File tempFile = createTempDataFile();

        Process process = new ProcessBuilder(executable, "-a", "sha1", tempFile.getAbsolutePath()).start();

        String expected = String.format("55ca6286e3e4f4fba5d0448333fa99fc5a404a73%n");

        assertEquals(expected, getStdOut(process));
        assertEquals("", getStdErr(process));
        process.waitFor(3, TimeUnit.SECONDS);
        assertEquals(0, process.exitValue());
        tempFile.delete();
        process.destroyForcibly();
    }

    @Tag("native-image")
    @Test
    public void testInvalidInput_MissingRequiredArg() throws IOException, InterruptedException {
        Process process = new ProcessBuilder(executable).start();

        String expected = String.format("" +
                "Missing required parameter: <file>%n" +
                "Usage: checksum [-hV] [-a=<algorithm>] <file>%n" +
                "Prints the checksum (MD5 by default) of a file to STDOUT.%n" +
                "      <file>      The file whose checksum to calculate.%n" +
                "  -a, --algorithm=<algorithm>%n" +
                "                  MD5, SHA-1, SHA-256, ...%n" +
                "  -h, --help      Show this help message and exit.%n" +
                "  -V, --version   Print version information and exit.%n");
        assertEquals(expected, getStdErr(process));
        assertEquals("", getStdOut(process));
        process.waitFor(3, TimeUnit.SECONDS);
        assertEquals(2, process.exitValue());
        process.destroyForcibly();
    }

    @Tag("native-image")
    @Test
    public void testInvalidInput_UnknownOption() throws IOException, InterruptedException {
        Process process = new ProcessBuilder(executable, "file", "--unknown").start();

        String expected = String.format("" +
                "Unknown option: '--unknown'%n" +
                "Usage: checksum [-hV] [-a=<algorithm>] <file>%n" +
                "Prints the checksum (MD5 by default) of a file to STDOUT.%n" +
                "      <file>      The file whose checksum to calculate.%n" +
                "  -a, --algorithm=<algorithm>%n" +
                "                  MD5, SHA-1, SHA-256, ...%n" +
                "  -h, --help      Show this help message and exit.%n" +
                "  -V, --version   Print version information and exit.%n");
        assertEquals(expected, getStdErr(process));
        assertEquals("", getStdOut(process));
        process.waitFor(3, TimeUnit.SECONDS);
        assertEquals(2, process.exitValue());
        process.destroyForcibly();
    }

}
