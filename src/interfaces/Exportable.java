package interfaces;

/** Anything that can be written as one CSV line. */
public interface Exportable {
    String toCsvLine();
    String getCsvHeader();
}
