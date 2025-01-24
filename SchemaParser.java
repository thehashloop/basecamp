import java.io.*;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

public class SchemaParser {
    static class ColumnMapping {
        String columnName;
        String field;
        
        ColumnMapping(String columnName, String field) {
            this.columnName = columnName;
            this.field = field;
        }
    }
    
    public static void main(String[] args) {
        String textInputFile = "C:/path/to/input.txt";
        String jsonInputFile = "C:/path/to/input.json";
        String outputFile = "C:/path/to/output.csv";
        
        try {
            // First read JSON mappings
            List<ColumnMapping> mappings = readJsonMappings(jsonInputFile);
            
            // Process text file and generate CSV
            generateCSV(textInputFile, outputFile, mappings);
            
            System.out.println("CSV file generated successfully!");
            
        } catch (IOException e) {
            System.err.println("Error processing file: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static List<ColumnMapping> readJsonMappings(String jsonFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new File(jsonFile));
        List<ColumnMapping> mappings = new ArrayList<>();
        
        JsonNode tableColumns = root.path("table_columns");
        for (JsonNode column : tableColumns) {
            String columnName = column.path("column_name").asText();
            String field = column.path("field").asText();
            mappings.add(new ColumnMapping(columnName, field));
        }
        
        return mappings;
    }
    
    private static String findFactField(List<ColumnMapping> mappings, String column, String alias) {
        String searchTerm = alias.isEmpty() ? column : alias;
        
        for (ColumnMapping mapping : mappings) {
            if (mapping.columnName.equalsIgnoreCase(searchTerm)) {
                return mapping.field;
            }
        }
        return ""; // Return empty if no match found
    }
    
    private static void generateCSV(String inputFile, String outputFile, List<ColumnMapping> mappings) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            
            writer.println("Schema,Table,Column,Alias,FactField");
            
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                String[] parts = line.split("\\s+as\\s+");
                String fullPath = parts[0].trim();
                String alias = parts.length > 1 ? parts[1].trim() : "";
                
                String[] pathParts = fullPath.split("\\s*\\.\\s*");
                if (pathParts.length >= 3) {
                    String schema = pathParts[0].trim();
                    String table = pathParts[1].trim();
                    String column = pathParts[2].trim();
                    String factField = findFactField(mappings, column, alias);
                    
                    writer.printf("%s,%s,%s,%s,%s%n",
                        escapeCsvField(schema),
                        escapeCsvField(table),
                        escapeCsvField(column),
                        escapeCsvField(alias),
                        escapeCsvField(factField));
                }
            }
        }
    }
    
    private static String escapeCsvField(String field) {
        if (field == null || field.isEmpty()) return "";
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
}
