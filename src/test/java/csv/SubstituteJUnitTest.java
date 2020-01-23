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
import java.util.stream.Stream;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * @author James Richardson
 */
public class SubstituteJUnitTest {
    
    private final CSVSubstitutor csvSubstitutor = new CSVSubstitutor();
    private final String inputFilename = "target/test_input.csv";
    private final String outputFilename = "target/test_output.csv";
    private final File inputFile = new File(inputFilename);
    private final File outputFile = new File(outputFilename);
    
    @After
    public void cleanUp() {
        inputFile.delete();
        outputFile.delete();
    }
    
    @Test(expected = FileNotFoundException.class)
    public void aFileNotFoundExceptionIsThrownIfTheFileCannotBeFound() throws IOException {
        csvSubstitutor.toListOfStringLines("nonexistent.csv");
    }
    
    @Test
    public void theLinesOfAFileAreReturnedAsAListOfStrings() throws IOException {
        createInputFile(
                "heading0, heading1, heading2, heading3",
                "field0, field1, field2, field3",
                "field0, field1, field2, field3",
                "field0, field1, field2, field3",
                "field0, field1, field2, field3");
        List<String> lines = csvSubstitutor.toListOfStringLines(inputFilename);
        
        assertEquals(5, lines.size());
        assertEquals("heading0, heading1, heading2, heading3", lines.get(0));
        assertEquals("field0, field1, field2, field3", lines.get(1));
        assertEquals("field0, field1, field2, field3", lines.get(2));
        assertEquals("field0, field1, field2, field3", lines.get(3));
        assertEquals("field0, field1, field2, field3", lines.get(4));
    }
    
    @Test
    public void ifAGivenColumnHeadingIsPresentThenTheIndexIsReturned() {
        Optional<Integer> index = csvSubstitutor.getIndexOf("heading2", "heading0, heading1, heading2, heading3");
        
        assertTrue(index.isPresent());
        assertEquals(new Integer(2), index.get());
    }
    
    @Test
    public void ifAGivenColumnHeadingIsNotPresentThenEmptyIsReturned() {
        Optional<Integer> index = csvSubstitutor.getIndexOf("hello", "heading0, heading1, heading2, heading3");
        
        assertFalse(index.isPresent());
    }
    
    @Test
    public void ifAGivenValueIsPresentThenItIsSubstituted() {
        String output = csvSubstitutor.replaceWithinLine(2, "AAAAAA", "ZZZZZZ", "field0, field1, AAAAAA, field3");
        
        assertEquals("field0, field1,ZZZZZZ, field3", output);
    }
    
    @Test
    public void ifAGivenValueIsNotPresentThenItIsNotSubstituted() {
        String output
                = csvSubstitutor.replaceWithinLine(2, "BBBBBB", "ZZZZZZ", "field0, field1, AAAAAA, field3");
        
        assertEquals("field0, field1, AAAAAA, field3", output);
    }
    
    @Test
    public void linesAreWrittenToAFile() throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("line0");
        lines.add("line1");
        lines.add("line2");
        lines.add("line3");
        lines.add("line4");
        
        csvSubstitutor.writeToFile(lines, outputFilename);
        
        assertOutputFileContainsLines("line0", "line1", "line2", "line3", "line4");
    }
    
    @Test
    public void allGivenValuesInAFileInAColumnWithAGivenHeaderAreReplacedWithAGivenNewValueAndWrittenToAGivenOutputFile()
            throws IOException {
        createInputFile(
                "heading0, heading1, heading2, heading3",
                "field0, field1, field2, field3",
                "field0, field1, AAAAAA, field3",
                "field0, field1, field2, field3",
                "field0, field1, AAAAAA, field3");
        
        csvSubstitutor.makeSubstitutions(inputFilename, "heading2", "AAAAAA", "ZZZZZZ", outputFilename);
        
        assertOutputFileContainsLines(
                "heading0, heading1, heading2, heading3",
                "field0, field1, field2, field3",
                "field0, field1,ZZZZZZ, field3",
                "field0, field1, field2, field3",
                "field0, field1,ZZZZZZ, field3");
    }
    
    @Test
    public void ifGivenHeaderDoesNotExistThenNoOutputFileIsCreated() throws IOException {
        createInputFile(
                "heading0, heading1, heading2, heading3",
                "field0, field1, field2, field3",
                "field0, field1, AAAAAA, field3",
                "field0, field1, field2, field3",
                "field0, field1, AAAAAA, field3");
        
        csvSubstitutor.makeSubstitutions(inputFilename, "NON_EXISTENT_HEADING", "AAAAAA", "ZZZZZZ", outputFilename);
        
        assertFalse("Output file created", outputFile.exists());
    }
    
    @Test
    public void substitutionsAreNotMadeToTheColumnHeadings() throws IOException {
        createInputFile(
                "field0, field1, AAAAAA, field3",
                "field0, field1, field2, field3",
                "field0, field1, AAAAAA, field3",
                "field0, field1, field2, field3",
                "field0, field1, AAAAAA, field3");
        
        csvSubstitutor.makeSubstitutions(inputFilename, "AAAAAA", "AAAAAA", "ZZZZZZ", outputFilename);
        
        assertOutputFileContainsLines(
                "field0, field1, AAAAAA, field3",
                "field0, field1, field2, field3",
                "field0, field1,ZZZZZZ, field3",
                "field0, field1, field2, field3",
                "field0, field1,ZZZZZZ, field3");
    }
    
    @Test
    public void substitutionsAreNotMadeToOtherColumns() throws IOException {
        createInputFile(
                "heading0, heading1, heading2, heading3",
                "field0, field1, field2, field3",
                "field0, AAAAAA, AAAAAA, field3",
                "field0, AAAAAA, field2, field3",
                "field0, field1, AAAAAA, field3");
        
        csvSubstitutor.makeSubstitutions(inputFilename, "heading2", "AAAAAA", "ZZZZZZ", outputFilename);
        
        assertOutputFileContainsLines(
                "heading0, heading1, heading2, heading3",
                "field0, field1, field2, field3",
                "field0, AAAAAA,ZZZZZZ, field3",
                "field0, AAAAAA, field2, field3",
                "field0, field1,ZZZZZZ, field3");
    }
    
    private void createInputFile(String... lines) throws FileNotFoundException {
        try (PrintWriter writer = new PrintWriter(inputFile)) {
            Stream.of(lines).forEach(writer::println);
        }
    }
    
    private void assertOutputFileContainsLines(String... expectedLines) throws IOException {
        assertTrue("No output file", outputFile.exists());
        assertTrue(outputFile.isFile());
        
        try (
                InputStream inputStream = new FileInputStream(outputFile);
                Reader reader = new InputStreamReader(inputStream);
                LineNumberReader in = new LineNumberReader(reader);
        ) {
            for (String expectedLine : expectedLines) {
                String actualLine = in.readLine();
                
                assertNotNull("The output file has fewer lines than expected", actualLine);
                assertEquals(expectedLine, actualLine);
            }
            
            String nextLine = in.readLine();
            
            assertNull("The output file has more lines than expected", nextLine);
        }
    }
}