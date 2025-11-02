import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * EmployeeManager - simple CLI tool to manage a comma-separated employees.txt file.
 *
 * Commands:
 *  l               - list all employees
 *  s <name>        - search for a name (case-insensitive substring)
 *  + <name>        - add a new employee (name may include spaces; use quotes in shell)
 *  c               - count employees
 *  u <old> <new>   - update an employee name (exact match)
 *  ?               - show help
 *
 * This file demonstrates:
 *  - proper argument validation (Task #2, #9)
 *  - meaningful variable names (Task #3)
 *  - reusable file helper methods (Task #4)
 *  - constants in Constants.java (Task #5)
 *  - no unnecessary temporary variables or "done" control variable (Task #6, #7)
 *  - simplified count logic (Task #8)
 */
public class EmployeeManager {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Error: Missing command. Use '?' for help.");
            System.exit(1);
        }

        String command = args[0].trim();

        try {
            switch (command) {
                case "l":
                    listEmployees();
                    break;
                case "s":
                    if (args.length < 2) {
                        System.err.println("Error: search requires a name. Usage: s <name>");
                        System.exit(1);
                    }
                    searchEmployee(joinArgs(args, 1));
                    break;
                case "+":
                    if (args.length < 2) {
                        System.err.println("Error: add requires a name. Usage: + <name>");
                        System.exit(1);
                    }
                    addEmployee(joinArgs(args, 1));
                    break;
                case "c":
                    countEmployees();
                    break;
                case "u":
                    if (args.length < 3) {
                        System.err.println("Error: update requires old and new names. Usage: u <oldName> <newName>");
                        System.exit(1);
                    }
                    updateEmployee(joinArgs(args, 1), joinArgs(args, 2));
                    break;
                case "?":
                    printHelp();
                    break;
                default:
                    System.err.println("Error: Unknown command '" + command + "'. Use '?' for help.");
                    System.exit(1);
            }
        } catch (IOException e) {
            System.err.println("IO Error: " + e.getMessage());
            System.exit(2);
        }
    }

    // ---------- Command implementations ----------

    private static void listEmployees() throws IOException {
        List<String> employees = readEmployeeList();
        if (employees.isEmpty()) {
            System.out.println("(no employees found)");
            return;
        }
        System.out.println("Employees:");
        for (String emp : employees) {
            System.out.println("- " + emp);
        }
    }

    private static void searchEmployee(String query) throws IOException {
        String lowerQuery = query.toLowerCase();
        List<String> employees = readEmployeeList();

        List<String> matches = employees.stream()
                .filter(name -> name.toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());

        if (matches.isEmpty()) {
            System.out.println("No matches found for: " + query);
        } else {
            System.out.println("Matches:");
            matches.forEach(name -> System.out.println("- " + name));
        }
    }

    private static void addEmployee(String nameToAdd) throws IOException {
        List<String> employees = readEmployeeList();
        if (employees.stream().anyMatch(e -> e.equalsIgnoreCase(nameToAdd))) {
            System.out.println("Employee already exists: " + nameToAdd);
            return;
        }
        employees.add(nameToAdd);
        writeEmployeeList(employees);
        System.out.println("Added: " + nameToAdd);
    }

    private static void countEmployees() throws IOException {
        List<String> employees = readEmployeeList();
        System.out.println("Count: " + employees.size());
    }

    private static void updateEmployee(String oldName, String newName) throws IOException {
        List<String> employees = readEmployeeList();
        boolean updated = false;
        List<String> updatedList = new ArrayList<>();

        for (String e : employees) {
            if (!updated && e.equalsIgnoreCase(oldName)) {
                updatedList.add(newName);
                updated = true;
            } else {
                updatedList.add(e);
            }
        }

        if (!updated) {
            System.out.println("No employee found with name: " + oldName);
            return;
        }

        writeEmployeeList(updatedList);
        System.out.println("Updated '" + oldName + "' to '" + newName + "'");
    }

    private static void printHelp() {
        System.out.println("EmployeeManager - usage:");
        System.out.println("  l               - list all employees");
        System.out.println("  s <name>        - search for a name (substring)");
        System.out.println("  + <name>        - add a new employee");
        System.out.println("  c               - count employees");
        System.out.println("  u <old> <new>   - update an employee name");
        System.out.println("  ?               - show this help");
    }

    // ---------- File helper methods (Task #4) ----------

    /**
     * Read the employee file (single line CSV) into a list of trimmed non-empty names.
     */
    private static List<String> readEmployeeList() throws IOException {
        Path path = Paths.get(Constants.EMPLOYEE_FILE);
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8).trim();
        if (content.isEmpty()) {
            return new ArrayList<>();
        }
        String[] tokens = content.split(",");
        return Arrays.stream(tokens)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Write employee list back to the file as a single comma-separated line.
     */
    private static void writeEmployeeList(List<String> employees) throws IOException {
        String output = String.join(",", employees);
        Files.write(Paths.get(Constants.EMPLOYEE_FILE), output.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    // ---------- Utility ----------

    /**
     * Join args from an index to end with spaces (so names with spaces are preserved).
     * Example: args = ["+", "John", "Doe"] -> joinArgs(args, 1) => "John Doe"
     */
    private static String joinArgs(String[] args, int startIndex) {
        if (startIndex >= args.length) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = startIndex; i < args.length; i++) {
            if (i > startIndex) sb.append(' ');
            sb.append(args[i]);
        }
        return sb.toString().trim();
    }
}
