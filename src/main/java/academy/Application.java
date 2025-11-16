package academy;

import academy.cli.LogAnalyzerCommand;
import picocli.CommandLine;

public class Application {
    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(new LogAnalyzerCommand());
        cmd.execute(args);
    }
}
