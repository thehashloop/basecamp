import java.io.*;
import java.util.*;

public class SchemaParser {
    public static void main(String[] args) {
        String inputFile = "C:/your/path/to/input.txt";
        String outputFile = "C:/your/path/to/output.csv";
        
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            
            writer.println("Schema,Table,Column,Alias");
            
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
                    
                    schema = escapeCsvField(schema);
                    table = escapeCsvField(table);
                    column = escapeCsvField(column);
                    alias = escapeCsvField(alias);
                    
                    writer.printf("%s,%s,%s,%s%n",
                        schema,
                        table,
                        column,
                        alias
                    );
                }
            }
            System.out.println("CSV file generated successfully!");
            
        } catch (IOException e) {
            System.err.println("Error processing file: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static String escapeCsvField(String field) {
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
}
