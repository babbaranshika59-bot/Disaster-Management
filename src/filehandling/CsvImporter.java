package filehandling;

import exception.InvalidInputException;
// import exception.RescueException;
import service.IncidentService;
import service.ResourceService;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/** Reads CSV files with StreamTokenizer, validates every row, skips bad rows with a message. */
public class CsvImporter {

    /** Returns {imported, skipped}. Uses FileReader (character stream). */
    public int[] importIncidents(String fileName, IncidentService service) throws IOException {
        Reader reader = new FileReader(fileName);
        List<List<String>> rows = readRows(reader);
        int imported = 0, skipped = 0;
        for (int n = 1; n < rows.size(); n++) {          // row 0 is the header
            List<String> f = rows.get(n);
            try {
                if (f.size() < 7) throw new InvalidInputException("expected 8 columns, found " + f.size());
                String extra = f.size() > 7 ? f.get(7) : "";
                int affected = Integer.parseInt(f.get(4));
                int severity = Integer.parseInt(f.get(5));
                service.createIncident(f.get(1), f.get(2), f.get(3), affected, severity, extra);
                imported++;
            } catch (NumberFormatException | InvalidInputException e) {
                skipped++;
                System.out.println("  [SKIPPED] line " + (n + 1) + ": " + e.getMessage());
            }
        }
        return new int[]{imported, skipped};
    }

    /** Returns {imported, skipped}. Uses FileInputStream (byte stream) wrapped by InputStreamReader. */
    public int[] importResources(String fileName, ResourceService service) throws IOException {
        Reader reader = new InputStreamReader(new FileInputStream(fileName));
        List<List<String>> rows = readRows(reader);
        int imported = 0, skipped = 0;
        for (int n = 1; n < rows.size(); n++) {
            List<String> f = rows.get(n);
            try {
                if (f.size() < 3) throw new InvalidInputException("expected at least 3 columns, found " + f.size());
                int total = Integer.parseInt(f.get(2));
                if (total < 0) throw new InvalidInputException("negative quantity");
                service.addResource(f.get(0), f.get(1), total);
                imported++;
            } catch (NumberFormatException | InvalidInputException e) {
                skipped++;
                System.out.println("  [SKIPPED] line " + (n + 1) + ": " + e.getMessage());
            }
        }
        return new int[]{imported, skipped};
    }

    /** Splits the file into rows of fields using StreamTokenizer. */
    private List<List<String>> readRows(Reader reader) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        try {
            StreamTokenizer st = new StreamTokenizer(new BufferedReader(reader));
            st.resetSyntax();
            st.wordChars(32, 126);               // printable characters form words
            st.ordinaryChar(',');                // comma separates fields
            st.whitespaceChars('\r', '\r');
            st.whitespaceChars('\n', '\n');
            st.eolIsSignificant(true);           // end of line = end of row

            List<String> fields = new ArrayList<>();
            boolean lastWasComma = true;         // true at the start of a row
            int token;
            while ((token = st.nextToken()) != StreamTokenizer.TT_EOF) {
                if (token == StreamTokenizer.TT_WORD) {
                    fields.add(st.sval.trim());
                    lastWasComma = false;
                } else if (token == ',') {
                    if (lastWasComma) fields.add("");   // empty field
                    lastWasComma = true;
                } else if (token == StreamTokenizer.TT_EOL) {
                    if (!fields.isEmpty()) {
                        if (lastWasComma) fields.add("");
                        rows.add(fields);
                    }
                    fields = new ArrayList<>();
                    lastWasComma = true;
                }
            }
            if (!fields.isEmpty()) rows.add(fields);   // last line without newline
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                System.out.println("[WARN] Could not close file: " + e.getMessage());
            }
        }
        return rows;
    }
}
