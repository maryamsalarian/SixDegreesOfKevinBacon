package ca.yorku.eecs;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.driver.v1.util.Consumer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class RequestHandler implements HttpHandler {
    private final Neo4jElements neo4j;
    private final Map<String, Consumer<HttpExchange>> routes = new HashMap<>();
    public RequestHandler(Neo4jElements neo4j) {
        this.neo4j = neo4j;
        initializeRoutes();
    }
    private void initializeRoutes() {
        routes.put("/api/v1/addActor", exchange -> {
            try {
                handleAddActor(exchange);
            } catch (IOException | JSONException e) {
                throw new RuntimeException(e);
            }
        });
    }
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        Consumer<HttpExchange> handler = routes.get(path);
        if (handler != null && method.equals("PUT")) {
            handler.accept(exchange);
        } else {
            try {
                sendResponse(exchange, 404, new JSONObject().put("error", "Not Found"));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, JSONObject jsonResponse) throws IOException {
        byte[] responseByte = jsonResponse.toString().getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseByte.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(responseByte);
        }

//        outputStream.close();
    }

    private JSONObject parseRequestBody(HttpExchange exchange) throws IOException, JSONException {
        InputStream inputStream = exchange.getRequestBody();
        StringBuilder requestBody = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line = reader.readLine();
            while (line != null) {
                requestBody.append(line);
            }
        }
        return new JSONObject(requestBody.toString());
    }

    private void handleAddActor(HttpExchange exchange) throws IOException, JSONException {
        try {
            JSONObject requestJson = parseRequestBody(exchange);
            String name = requestJson.optString("name");
            String actorId = requestJson.optString("actorId");
            if (name.isEmpty() || actorId.isEmpty()) {
                sendResponse(exchange, 400, new JSONObject().put("error", "Missing Parameters"));
            }
            neo4j.addActor(name, actorId);
            sendResponse(exchange, 200, new JSONObject().put("message", "Actor Added Successfully"));
        } catch (Exception e) {
            sendResponse(exchange, 500, new JSONObject().put("error", "Error adding actor: " + e.getMessage()));

        }
    }
}
