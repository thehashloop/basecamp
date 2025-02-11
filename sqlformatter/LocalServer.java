
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.nio.file.Paths;

public class LocalServer {
    public static void main(String[] args) throws IOException {
        // Create server on port 8000
        HttpServer server = HttpServer.create(new InetSocketAddress(8101), 0);
        
        // Set up context handler for all files
        server.createContext("/", new StaticFileHandler());
        
        // Start the server
        server.start();
        System.out.println("Server running on http://localhost:8101");
    }

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String path = exchange.getRequestURI().getPath();
                System.out.println("Requested path: " + path);
                
                if (path.equals("/")) path = "/index.html";
                String filePath = "." + path;
                System.out.println("Attempting to serve: " + filePath);
                
                // Get file from current directory
                FileInputStream fis = new FileInputStream(filePath);
                
                // Set content type
                String mimeType = "text/plain";
                if (filePath.endsWith(".html")) mimeType = "text/html";
                else if (filePath.endsWith(".js")) mimeType = "application/javascript";
                else if (filePath.endsWith(".css")) mimeType = "text/css";
                else if (filePath.endsWith(".map")) mimeType = "application/json";
                
                // Send response headers
                exchange.getResponseHeaders().set("Content-Type", mimeType);
                exchange.sendResponseHeaders(200, fis.available());
                
                // Send file content
                OutputStream os = exchange.getResponseBody();
                byte[] buffer = new byte[1024];
                int count;
                while ((count = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, count);
                }
                
                fis.close();
                os.close();
            } catch (IOException e) {
                exchange.sendResponseHeaders(404, 0);
                exchange.getResponseBody().close();
                System.err.println("File not found: " + e.getMessage());
            } catch (Exception e) {
                exchange.sendResponseHeaders(500, 0);
                exchange.getResponseBody().close();
                System.err.println("Server error: " + e.getMessage());
            }
        }
    }
} 
