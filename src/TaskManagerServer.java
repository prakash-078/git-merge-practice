import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskManagerServer {
    private static final int PORT = 8080;
    private static final List<Task> tasks = new ArrayList<>();
    private static final AtomicInteger nextId = new AtomicInteger(1);

    public static void main(String[] args) throws IOException {
        tasks.add(new Task(nextId.getAndIncrement(), "Explore the application", false));
        tasks.add(new Task(nextId.getAndIncrement(), "Practise Git commits", false));

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/api/tasks", TaskManagerServer::handleTasks);
        server.createContext("/", TaskManagerServer::serveFrontend);
        server.setExecutor(null);
        server.start();

        System.out.println("Task Manager is running at http://localhost:" + PORT);
        System.out.println("Press Ctrl+C to stop the server.");
    }

    private static void handleTasks(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        String method = exchange.getRequestMethod();

        if ("GET".equalsIgnoreCase(method)) {
            send(exchange, 200, tasksToJson());
            return;
        }

        if ("POST".equalsIgnoreCase(method)) {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String title = extractTitle(body);

            if (title.isBlank()) {
                send(exchange, 400, "{\"error\":\"Task title cannot be empty\"}");
                return;
            }

            synchronized (tasks) {
                tasks.add(new Task(nextId.getAndIncrement(), title, false));
            }
            send(exchange, 201, tasksToJson());
            return;
        }

        exchange.getResponseHeaders().set("Allow", "GET, POST");
        send(exchange, 405, "{\"error\":\"Method not allowed\"}");
    }

    private static void serveFrontend(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            send(exchange, 405, "Method not allowed");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        if ("/".equals(path)) {
            path = "/index.html";
        }

        // Only serve files from the public directory.
        String filename = path.substring(1).replace("\\", "/");
        if (filename.contains("..")) {
            send(exchange, 400, "Invalid path");
            return;
        }

        Path file = Path.of("public", filename);
        if (!Files.isRegularFile(file)) {
            send(exchange, 404, "File not found");
            return;
        }

        String contentType = filename.endsWith(".css") ? "text/css; charset=UTF-8"
                : filename.endsWith(".js") ? "text/javascript; charset=UTF-8"
                : "text/html; charset=UTF-8";
        exchange.getResponseHeaders().set("Content-Type", contentType);
        byte[] bytes = Files.readAllBytes(file);
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private static String extractTitle(String json) {
        // Small dependency-free parser for this app's simple {"title":"..."} request.
        String key = "\"title\"";
        int keyIndex = json.indexOf(key);
        if (keyIndex < 0) return "";
        int colon = json.indexOf(':', keyIndex + key.length());
        int firstQuote = colon < 0 ? -1 : json.indexOf('"', colon + 1);
        if (firstQuote < 0) return "";
        StringBuilder result = new StringBuilder();
        boolean escaped = false;
        for (int i = firstQuote + 1; i < json.length(); i++) {
            char ch = json.charAt(i);
            if (escaped) {
                result.append(ch == 'n' ? '\n' : ch == 't' ? '\t' : ch);
                escaped = false;
            } else if (ch == '\\') {
                escaped = true;
            } else if (ch == '"') {
                return result.toString().trim();
            } else {
                result.append(ch);
            }
        }
        return "";
    }

    private static String tasksToJson() {
        StringBuilder json = new StringBuilder("[");
        synchronized (tasks) {
            for (int i = 0; i < tasks.size(); i++) {
                Task task = tasks.get(i);
                if (i > 0) json.append(',');
                json.append("{\"id\":").append(task.id)
                    .append(",\"title\":\"").append(escapeJson(task.title))
                    .append("\",\"completed\":").append(task.completed).append('}');
            }
        }
        return json.append(']').toString();
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private static void send(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private static class Task {
        private final int id;
        private final String title;
        private final boolean completed;

        private Task(int id, String title, boolean completed) {
            this.id = id;
            this.title = title;
            this.completed = completed;
        }
    }
}
