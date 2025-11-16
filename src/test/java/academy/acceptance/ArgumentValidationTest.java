package academy.acceptance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import academy.cli.LogAnalyzerCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import picocli.CommandLine;

public class ArgumentValidationTest {

    private LogAnalyzerCommand logAnalyzerCommand;
    private CommandLine cmd;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setup() {
        logAnalyzerCommand = new LogAnalyzerCommand();
        cmd = new CommandLine(logAnalyzerCommand);
    }

    @Test
    @DisplayName("На вход передан несуществующий локальный файл")
    void test1() {
        int exitCode = cmd.execute(
            "-p", "/net/takogo/asdf.log",
            "-f", "json",
            "-o", "output.json"
        );

        assertEquals(2, exitCode, "exitCode отличается от ожидаемого (2)");
    }

    @Test
    @DisplayName("На вход передан несуществующий удаленный файл")
    void test2() {
        int exitCode = cmd.execute(
            "-p", "https://netunetu.lol",
            "-f", "json",
            "-o", "output.json"
        );

        assertEquals(2, exitCode);
    }

    @ParameterizedTest
    @ValueSource(strings = ".docx")
    @DisplayName("На вход передан файл в неподдерживаемом формате")
    void test3(String extension) throws IOException {
        Path invalidFile = tempDir.resolve("test" + extension);
        Files.createFile(invalidFile);

        int exitCode = cmd.execute(
            "-p", invalidFile.toString(),
            "-f", "json",
            "-o", "output.json"
        );

        assertEquals(2, exitCode);
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {"2025.01.01 10:30", "today"})
    @DisplayName("На вход переданы невалидные параметры --from / --to - {0}")
    void test4(String from) throws IOException {
        Path logFile = tempDir.resolve("access.log");
        Files.createFile(logFile);


        int exitCode = cmd.execute(
            "-p", logFile.toString(),
            "-f", "json",
            "-o", "output.json",
            "--from", from
        );
        assertEquals(2, exitCode);
    }

    @ParameterizedTest
    @MethodSource("test6ArgumentsSource")
    @DisplayName("По пути в аргументе --output указан файл с некоректным расширением")
    void test6(String format, String output) throws IOException {
        Path logFile = tempDir.resolve("access.log");
        Files.createFile(logFile);

        int exitCode = cmd.execute(
            "-p", logFile.toString(),
            "-f", format,
            "-o", output
        );

        assertEquals(2, exitCode,
            "Должна быть ошибка при несоответствии формата " + format + " и расширения файла " + output);
    }

    @Test
    @DisplayName("По пути в аргументе --output уже существует файл")
    void test7() throws IOException {
        Path logFile = tempDir.resolve("access.log");
        Files.createFile(logFile);
        Path outputFile = tempDir.resolve("asd.json");
        Files.createFile(outputFile);

        int exitCode = cmd.execute(
            "-p", logFile.toString(),
            "-f", "json",
            "-o", outputFile.toString()
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"--path", "--output", "--format", "-p", "-o", "-f"})
    @DisplayName("На вход не передан обязательный параметр \"{0}\"")
    void test8(String missingArgument) throws IOException {
        Path logFile = tempDir.resolve("access.log");
        Files.createFile(logFile);

        List<String> args = new ArrayList<>();

        if (!missingArgument.equals("--path") && !missingArgument.equals("-p")) {
            args.addAll(List.of("-p", logFile.toString()));
        }

        if (!missingArgument.equals("--format") && !missingArgument.equals("-f")) {
            args.addAll(List.of("-f", "json"));
        }

        if (!missingArgument.equals("--output") && !missingArgument.equals("-o")) {
            args.addAll(List.of("-o", tempDir.resolve("output.json").toString()));
        }

        int exitCode = cmd.execute(args.toArray(new String[0]));
        assertEquals(2, exitCode);
    }

    @ParameterizedTest
    @ValueSource(strings = {"--input", "--filter", "--verbose", "--debug", "--help-me"})
    @DisplayName("На вход передан неподдерживаемый параметр \"{0}\"")
    void test9(String unsupportedArgument) throws IOException {
        Path logFile = tempDir.resolve("access.log");
        Files.createFile(logFile);

        Path outputFile = tempDir.resolve("output.json");

        int exitCode = cmd.execute(
            "-p", logFile.toString(),
            "-f", "json",
            "-o", outputFile.toString(),
            unsupportedArgument, "чото"
        );


        assertEquals(2, exitCode);
    }

    @Test
    @DisplayName("Значение параметра --from больше, чем значение параметра --to")
    void test10() throws IOException {
        Path logFile = tempDir.resolve("access.log");
        Files.createFile(logFile);

        Path outputFile = tempDir.resolve("output.json");

        int exitCode = cmd.execute(
            "-p", logFile.toString(),
            "-f", "json",
            "-o", outputFile.toString(),
            "--from", "2025-01-02",
            "--to", "2025-01-01"
        );

        assertEquals(2, exitCode);
    }

    private static Stream<Arguments> test6ArgumentsSource() {
        return Stream.of(
                Arguments.of("markdown", "./results.txt"),
                Arguments.of("json", "./results.md"),
                Arguments.of("adoc", "./results.ad1"));
    }
}
