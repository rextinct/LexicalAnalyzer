package lexer;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Main.java
 * Entry point of the GamiPhi lexical analyzer.
 *
 * What it does:
 *   1. Reads the GamiPhi source code from a file (never hardcoded).
 *   2. Runs the two-pass Lexer.
 *   3. Prints the token table and, if needed, the error table.
 *   4. Writes GamiPhi_Tokens.html (a ready-made table you can open in a
 *      browser) into the main GamiPhiLexer project directory.
 *
 * How to run (from the GamiPhiLexer folder):
 *   javac -d bin src/lexer/*.java
 *   java -cp bin lexer.Main input.txt
 * If no file name is given, the program asks for one.
 */
public class Main {

    private static final String HTML_NAME = "GamiPhi_Tokens.html";
    private static final String DEFAULT_INPUT_NAME = "input.txt";
    private static final String PROJECT_FOLDER_NAME = "GamiPhiLexer";

    public static void main(String[] args) {
        useUtf8Console();

        try {
            Path projectDir = findProjectDirectory();
            Path inputPath = resolveInputPath(args, projectDir);

            // ----- Read the source code -----
            String source = readSource(inputPath);
            System.out.println("Source file : " + inputPath.toAbsolutePath());
            System.out.println("Characters  : " + source.length());
            System.out.println();

            // ----- Run both passes -----
            Lexer lexer = new Lexer(source);
            List<Token> tokens = lexer.tokenize();
            List<Lexer.LexicalError> errors = lexer.getErrors();

            // ----- Console output -----
            printTokenTable(tokens);
            if (errors.isEmpty()) {
                System.out.println("No lexical errors found.");
            } else {
                printErrorTable(errors);
            }
            System.out.println();
            System.out.println("Total tokens (including EOF): " + tokens.size());
            System.out.println("Lexical errors              : " + errors.size());

            // ----- HTML table output (open in any browser) -----
            Path htmlReport = projectDir.resolve(HTML_NAME);
            Files.write(htmlReport, buildHtmlReport(tokens, errors).getBytes(StandardCharsets.UTF_8));
            System.out.println();
            System.out.println("HTML table written to: " + htmlReport.toAbsolutePath());

        } catch (IOException e) {
            System.err.println("File error: " + e.getMessage());
            System.exit(1);
        }
    }

    // =====================================================================
    // Input handling
    // =====================================================================

    /** Uses the first command-line argument as the file path, or asks the user. */
    private static Path resolveInputPath(String[] args, Path projectDir) throws IOException {
        String name;
        if (args.length > 0 && !args[0].trim().isEmpty()) {
            name = args[0].trim();
        } else {
            System.out.print("Enter the GamiPhi source file (press Enter for '"
                    + DEFAULT_INPUT_NAME + "'): ");
            System.out.flush();
            Scanner scanner = new Scanner(System.in);
            name = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
            if (name.isEmpty()) {
                name = DEFAULT_INPUT_NAME;
            }
        }

        Path path = Paths.get(name);
        if (!Files.isRegularFile(path) && !path.isAbsolute()) {
            Path inProject = projectDir.resolve(name);     // try the project folder too
            if (Files.isRegularFile(inProject)) {
                path = inProject;
            }
        }
        if (!Files.isRegularFile(path)) {
            throw new IOException("Source file not found: " + name);
        }
        return path;
    }

    /** Reads the whole file as UTF-8. There is no fixed size limit. */
    private static String readSource(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    /**
     * Finds the main GamiPhiLexer directory where the output files are saved.
     * Order: a parent folder named GamiPhiLexer; the current folder if it has
     * a src folder; the parent if we run from src, bin, or out; otherwise the
     * current folder.
     */
    private static Path findProjectDirectory() {
        Path current = Paths.get(System.getProperty("user.dir")).toAbsolutePath();

        for (Path dir = current; dir != null; dir = dir.getParent()) {
            Path folder = dir.getFileName();
            if (folder != null && folder.toString().equals(PROJECT_FOLDER_NAME)) {
                return dir;
            }
        }
        if (Files.isDirectory(current.resolve("src"))) {
            return current;
        }
        Path folder = current.getFileName();
        if (folder != null && current.getParent() != null) {
            String n = folder.toString();
            if (n.equals("src") || n.equals("bin") || n.equals("out")) {
                return current.getParent();
            }
        }
        return current;
    }

    /** Makes the console use UTF-8 so unusual invalid characters print correctly. */
    private static void useUtf8Console() {
        try {
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, "UTF-8"));
            System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err), true, "UTF-8"));
        } catch (IOException e) {
            // If this fails, the default console encoding is used.
        }
    }

    // =====================================================================
    // Console tables
    // =====================================================================

    private static void printTokenTable(List<Token> tokens) {
        String[] headers = {"No.", "Lexeme", "Token Type", "Line", "Column"};
        List<String[]> rows = new ArrayList<>();
        int number = 1;
        for (Token t : tokens) {
            rows.add(new String[] {
                    String.valueOf(number++),
                    t.getDisplayLexeme(),
                    t.getType().name(),
                    String.valueOf(t.getLine()),
                    String.valueOf(t.getColumn())
            });
        }
        System.out.println("TOKEN TABLE");
        printTable(headers, rows, new boolean[] {true, false, false, true, true});
        System.out.println();
    }

    private static void printErrorTable(List<Lexer.LexicalError> errors) {
        String[] headers = {"No.", "Lexeme", "Error Description", "Line", "Column"};
        List<String[]> rows = new ArrayList<>();
        int number = 1;
        for (Lexer.LexicalError e : errors) {
            rows.add(new String[] {
                    String.valueOf(number++),
                    e.getDisplayLexeme(),
                    e.getDescription(),
                    String.valueOf(e.getLine()),
                    String.valueOf(e.getColumn())
            });
        }
        System.out.println("LEXICAL ERROR TABLE");
        printTable(headers, rows, new boolean[] {true, false, false, true, true});
    }

    /** Prints a bordered table. Column widths grow to fit the data, so nothing is cut off. */
    private static void printTable(String[] headers, List<String[]> rows, boolean[] rightAligned) {
        int columns = headers.length;
        int[] widths = new int[columns];
        for (int c = 0; c < columns; c++) {
            widths[c] = headers[c].length();
        }
        for (String[] row : rows) {
            for (int c = 0; c < columns; c++) {
                widths[c] = Math.max(widths[c], row[c].length());
            }
        }

        String border = buildBorder(widths);
        System.out.println(border);
        System.out.println(buildRow(headers, widths, new boolean[columns]));
        System.out.println(border);
        for (String[] row : rows) {
            System.out.println(buildRow(row, widths, rightAligned));
        }
        System.out.println(border);
    }

    private static String buildBorder(int[] widths) {
        StringBuilder sb = new StringBuilder("+");
        for (int w : widths) {
            sb.append(repeat('-', w + 2)).append('+');
        }
        return sb.toString();
    }

    private static String buildRow(String[] cells, int[] widths, boolean[] rightAligned) {
        StringBuilder sb = new StringBuilder("|");
        for (int c = 0; c < cells.length; c++) {
            sb.append(' ');
            int padding = widths[c] - cells[c].length();
            if (rightAligned[c]) {
                sb.append(repeat(' ', padding)).append(cells[c]);
            } else {
                sb.append(cells[c]).append(repeat(' ', padding));
            }
            sb.append(" |");
        }
        return sb.toString();
    }

    private static String repeat(char ch, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(ch);
        }
        return sb.toString();
    }

    // =====================================================================
    // HTML table generation
    // =====================================================================

    private static String htmlEscape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }

    /** Builds a web page with the token table (and the error table if needed). */
    private static String buildHtmlReport(List<Token> tokens, List<Lexer.LexicalError> errors) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n<html><head><meta charset=\"UTF-8\">")
                .append("<title>GamiPhi Lexer Output</title>\n")
                .append("<style>body{font-family:Arial,sans-serif;margin:20px}")
                .append("table{border-collapse:collapse;margin-bottom:30px}")
                .append("th,td{border:1px solid #000;padding:4px 10px;text-align:left}")
                .append("td.num{text-align:right}")
                .append("td.lex{font-family:Consolas,monospace}</style></head><body>\n");

        sb.append("<h2>Token Table</h2>\n<table>\n")
                .append("<tr><th>No.</th><th>Lexeme</th><th>Token Type</th><th>Line</th><th>Column</th></tr>\n");
        int number = 1;
        for (Token t : tokens) {
            sb.append("<tr><td class=\"num\">").append(number++).append("</td>")
                    .append("<td class=\"lex\">").append(htmlEscape(t.getDisplayLexeme())).append("</td>")
                    .append("<td>").append(t.getType().name()).append("</td>")
                    .append("<td class=\"num\">").append(t.getLine()).append("</td>")
                    .append("<td class=\"num\">").append(t.getColumn()).append("</td></tr>\n");
        }
        sb.append("</table>\n");

        if (!errors.isEmpty()) {
            sb.append("<h2>Lexical Error Table</h2>\n<table>\n")
                    .append("<tr><th>No.</th><th>Lexeme</th><th>Error Description</th><th>Line</th><th>Column</th></tr>\n");
            number = 1;
            for (Lexer.LexicalError e : errors) {
                sb.append("<tr><td class=\"num\">").append(number++).append("</td>")
                        .append("<td class=\"lex\">").append(htmlEscape(e.getDisplayLexeme())).append("</td>")
                        .append("<td>").append(htmlEscape(e.getDescription())).append("</td>")
                        .append("<td class=\"num\">").append(e.getLine()).append("</td>")
                        .append("<td class=\"num\">").append(e.getColumn()).append("</td></tr>\n");
            }
            sb.append("</table>\n");
        }
        sb.append("</body></html>\n");
        return sb.toString();
    }
}


