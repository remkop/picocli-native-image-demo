package picocli.nativeimage.demo.https;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

class NativeImageHelper {
    static String executable() {
        return executableLocation() + "/demo" + extension();
    }

    private static String executableLocation() {
        return System.getProperty("executable-location", "build/native-image");
    }

    private static String extension() {
        return System.getProperty("os.name").toLowerCase().startsWith("win") ? ".exe" : "";
    }

    static File createTempDataFile() throws IOException {
        File tempFile = File.createTempFile("checksum", "test");
        try (FileOutputStream fous = new FileOutputStream(tempFile)) {
            fous.write("hi\n".getBytes());
            fous.flush();
        }
        return tempFile;
    }

    static String getStdOut(Process process) throws IOException {
        return readFully(process.getInputStream());
    }

    static String getStdErr(Process process) throws IOException {
        return readFully(process.getErrorStream());
    }

    private static String readFully(InputStream in) throws IOException {
        byte[] buff = new byte[10 * 1024];
        int len = 0;
        int total = 0;
        while ((len = in.read(buff, total, buff.length - total)) > 0) {
            total += len;
        }
        return new String(buff, 0, total);
    }
}
