package filehandling;

import model.Incident;
import model.Resource;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CsvExporter {

    /** Character stream: FileWriter. */
    public int exportIncidents(List<Incident> incidents, String fileName) throws IOException {
        FileWriter fw = null;
        try {
            fw = new FileWriter(fileName);
            fw.write(Incident.CSV_HEADER + "\n");
            for (Incident i : incidents) fw.write(i.toCsvLine() + "\n");
            return incidents.size();
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e) {
                    System.out.println("[WARN] Could not close file: " + e.getMessage());
                }
            }
        }
    }

    /** Byte stream: FileOutputStream. StringBuffer builds the text. */
    public int exportResources(List<Resource> resources, String fileName) throws IOException {
        StringBuffer text = new StringBuffer(Resource.CSV_HEADER).append("\n");
        for (Resource r : resources) text.append(r.toCsvLine()).append("\n");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(fileName);
            fos.write(text.toString().getBytes());
            return resources.size();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    System.out.println("[WARN] Could not close file: " + e.getMessage());
                }
            }
        }
    }
}
