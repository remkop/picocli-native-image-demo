package picocli.nativeimage.demo;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.jansi.graalvm.AnsiConsole;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.Certificate;
import java.io.*;

import java.util.concurrent.Callable;

@Command(name = "demo", mixinStandardHelpOptions = true,
        version = "demo 4.0",
        subcommands = { CheckSum.class, HttpsClient.class, HelpCommand.class },
        description = "Demonstrates picocli-based native image applications.")
public class Demo implements Callable<Integer> {

    public static void main(String[] args) {
        boolean windows = System.getProperty("os.name").toLowerCase().startsWith("win");
        if (windows) { AnsiConsole.systemInstall(); }
        int exitCode = new CommandLine(new Demo()).execute(args);
        if (windows) { AnsiConsole.systemUninstall(); }
        System.exit(exitCode);
    }

    public Integer call() throws Exception {
        System.err.println("Please specify a subcommand.");
        CommandLine.usage(this, System.err);
    }
}
