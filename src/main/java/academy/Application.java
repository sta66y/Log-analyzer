package academy;

import academy.cli.LogAnalyzerCommand;
import picocli.CommandLine;

public class Application {
    public static void main(String[] args) {
        LogAnalyzerCommand command = new LogAnalyzerCommand();
        CommandLine cmd = new CommandLine(command);

        int exitCode = cmd.execute(args);
        System.exit(exitCode);
    }
}
