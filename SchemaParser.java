import java.io.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

public class SchemaParser {
    public static void main(String[] args) {
        String textInputFile = "C:/path/to/input.txt";
        String jsonInputFile = "C:/path/to/input.json";
        String outputFile = "C:/path/to/output.csv";
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            writer.println("Schema,Table,Column,Alias,FactField");
            processTextFile(textInputFile, writer);
            processJsonFile(jsonInputFile, writer);
            System.out.println("CSV file generated successfully!");
        } catch (IOException e) {
            System.err.println("Error processing file: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void processTextFile(String inputFile, PrintWriter writer) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                String[] parts = line.split("\\s+as\\s+", 2);
                String fullPath = parts[0].trim();
                String alias = parts.length > 1 ? parts[1].trim() : "";
                
                String[] pathParts = fullPath.split("\\s*\\.\\s*");
                if (pathParts.length >= 3) {
                    String schema = pathParts[0].trim();
                    String table = pathParts[1].trim();
                    String column = pathParts[2].trim();
                    String factField = alias.isEmpty() ? column : 
                        (column.toLowerCase().equals(alias.toLowerCase()) ? column : alias);
                    
                    writeCSVLine(writer, schema, table, column, alias, factField);
                }
            }
        }
    }
    
    private static void processJsonFile(String inputFile, PrintWriter writer) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new File(inputFile));
        
        String className = root.path("classname").asText();
        JsonNode tableColumns = root.path("table_columns");
        
        for (JsonNode column : tableColumns) {
            String columnName = column.path("column_name").asText();
            String alias = column.path("field").asText();
            
            String[] parts = className.split("\\.");
            if (parts.length >= 2) {
                String schema = parts[0];
                String table = parts[1];
                String factField = alias.isEmpty() ? columnName :
                    (columnName.toLowerCase().equals(alias.toLowerCase()) ? columnName : alias);
                
                writeCSVLine(writer, schema, table, columnName, alias, factField);
            }
        }
    }
    
    private static void writeCSVLine(PrintWriter writer, String schema, String table, 
                                   String column, String alias, String factField) {
        writer.printf("%s,%s,%s,%s,%s%n",
            escapeCsvField(schema),
            escapeCsvField(table),
            escapeCsvField(column),
            escapeCsvField(alias),
            escapeCsvField(factField));
    }
    
    private static String escapeCsvField(String field) {
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
}
