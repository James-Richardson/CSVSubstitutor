package csv;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author James Richardson
 */
public class CSVSubstitutor {
    
    private static final String DELIMITER = ",";
    
    /**
     * Set the values in the column with the given heading, to the given new value, in the given CSV file,
     * where the values are the given value to replace.
     * @param inputFilename Full path of file to read in
     * @param columnHeading Heading of column to set values in
     * @param valueToReplace Only replace values that are equal to this.
     * @param newValue New value for the field
     * @param outputFilename Full path of file to write to
     * @throws IOException If a file with the given filename cannot be created or written to,
     * or some other I/O error occurs
     */
    public void makeSubstitutions(
            String inputFilename,
            String columnHeading,
            String valueToReplace,
            String newValue,
            String outputFilename)
            throws IOException {
        List<String> inputLines = toListOfStringLines(inputFilename);
        String header = inputLines.get(0);
        Optional<Integer> index = getIndexOf(columnHeading, header);
        
        if (index.isPresent()) {
            List<String> outputLines
                    = inputLines
                            .stream()
                            .skip(1) // Skip header
                            .map(inputLine -> replaceWithinLine(index.get(), valueToReplace, newValue, inputLine))
                            .collect(Collectors.toList());
            
            outputLines.add(0, header); // Insert header
            
            writeToFile(outputLines, outputFilename);
        }
    }
    
    /**
     * Convert the lines of text in the given file into a list of strings.
     * @param filename Full path of file to read in
     * @return List of string lines, in the order that they appear in the file
     * @throws IOException If an I/O error occurs
     */
    List<String> toListOfStringLines(String filename) throws IOException {
        File file = new File(filename);
        List<String> result = new ArrayList<>();
        
        try (
                InputStream inputStream = new FileInputStream(file);
                Reader reader = new InputStreamReader(inputStream);
                LineNumberReader in = new LineNumberReader(reader);
        ) {
            String line;
            
            while ((line = in.readLine()) != null) {
                result.add(line);
            }
        }
        
        return result;
    }
    
    /**
     * Get the index of the given field in the given line of comma-separated values.
     * @param field Field to get the index of
     * @param csv Line of comma-separated values
     * @return The index (equal to, or greater than, zero) of the given field,
     * or empty if the given line of comma-separated values does not contain the given field
     */
    Optional<Integer> getIndexOf(String field, String csv) {
        String[] headers = csv.split(DELIMITER);
        
        for (int index = 0; index < headers.length; index++) {
            if (headers[index].trim().equals(field)) {
                return Optional.of(index);
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Set the value in the field with the given index, to the given new value,
     * in the given line of comma-separated values, if the value is the given value to replace.
     * @param fieldIndex Index (equal to, or greater than, zero) of the field in which to replace the value
     * @param valueToReplace Only replace the value if it is equal to this.
     * @param newValue New value for the field
     * @param csv Line of comma-separated values
     * @return The comma-separated values, with or without the substitution
     */
    String replaceWithinLine(int fieldIndex, String valueToReplace, String newValue, String csv) {
        String[] fields = csv.split(DELIMITER);
        
        if (fields[fieldIndex].trim().equals(valueToReplace)) {
            fields[fieldIndex] = newValue;
        }
        
        return String.join(DELIMITER, fields);
    }
    
    /**
     * Create a text file with the given filename, and containing the given lines of text.
     * @param lines The lines of text to write to the file
     * @param filename The name of the file to create
     * @throws FileNotFoundException If a file with the given filename cannot be created or written to
     */
    void writeToFile(List<String> lines, String filename) throws FileNotFoundException {
        File file = new File(filename);
        
        try (PrintWriter writer = new PrintWriter(file)) {
            lines.forEach(writer::println);
        }
    }
}
