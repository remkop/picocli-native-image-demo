package picocli.nativeimage.demo.https;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.Option;
import picocli.jansi.graalvm.AnsiConsole;

import java.security.Provider;
import java.security.Security;
import java.util.Enumeration;
import java.util.concurrent.Callable;

@Command(name = "demo", mixinStandardHelpOptions = true,
        version = "demo 4.0",
        subcommands = {
                SimpleHttpsClient.class,
                SimpleHttpsServer.class,
                HelpCommand.class},
        description = "Demonstrates picocli-based HTTPS native image applications.")
public class Demo implements Callable<Integer> {

    @Option(names = {"-l", "--list-providers"}, description = "List available security providers")
    boolean listSecurityProviders;

    @Option(names = {"-a", "--list-algorithms"}, description = "List available security providers and supported algorithms")
    boolean listProviderDetails;

    public static void main(String[] args) {
        int exitCode;
        try (AnsiConsole ansi = AnsiConsole.windowsInstall()) {
            exitCode = new CommandLine(new Demo()).execute(args);
        }
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        if (listSecurityProviders || listProviderDetails) {
            listSecurityProviders(listProviderDetails);
        } else {
            System.err.println("Please specify a subcommand.");
            CommandLine.usage(this, System.err);
        }
        return 0;
    }

    private void listSecurityProviders(boolean details) {
        Provider[] providers = Security.getProviders();
        for (Provider provider : providers) {
            System.out.println(provider);
            if (details) {
                for (Enumeration<Object> e = provider.keys(); e.hasMoreElements(); ) {
                    System.out.println("\t" + e.nextElement());
                }
            }
        }

    }
}
