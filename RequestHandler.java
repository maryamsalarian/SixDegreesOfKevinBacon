package ca.yorku.eecs;

import org.json.JSONException;
import org.json.JSONObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class RequestHandler implements HttpHandler {
    private final Neo4jElements neo4j;

    public RequestHandler(Neo4jElements neo4j) {
        this.neo4j = neo4j;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        URI requestUri = exchange.getRequestURI();
        String response = "";
        int statusCode = 200;

        try {
            Map<String, String> queryParams = parseQueryParams(requestUri.getQuery());
            String requestBody = readRequestBody(exchange);

            if (method.equals("PUT")) {
                String path = requestUri.getPath();
                switch (path) {
                    case "/api/v1/addActor":
                        if (requestBody.contains("name") && requestBody.contains("actorId")) {
                            handleAddActor(requestBody);
                            response = "Actor added successfully";
                        } else {
                            statusCode = 400;
                            response = "Missing parameters";
                        }
                        break;
                    case "/api/v1/addMovie":
                        if (requestBody.contains("name") && requestBody.contains("movieId")) {
                            handleAddMovie(requestBody);
                            response = "Movie added successfully";
                        } else {
                            statusCode = 400;
                            response = "Missing parameters";
                        }
                        break;
                    case "/api/v1/addRelationship":
                        if (requestBody.contains("actorId") && requestBody.contains("movieId")) {
                            response = handleAddRelationship(requestBody);
                            if (response.isEmpty()) {
                                statusCode = 404;
                                response = "Actor and/or Movie Not Found";
                            }
                        } else {
                            statusCode = 400;
                            response = "Missing parameters";
                        }
                        break;
                    case "/api/v1/addMovieRating":
                        if (requestBody.contains("movieId") && requestBody.contains("rating")) {
                            response = handleAddMovieRating(requestBody);
                            if (response.isEmpty()) {
                                statusCode = 404;
                                response = "Movie Not Found";
                            }
                        } else {
                            statusCode = 400;
                            response = "Missing parameters";
                        }
                        break;
                    case "/api/v1/addMovieYear":
                        if (requestBody.contains("movieId") && requestBody.contains("year")) {
                            response = handleAddMovieYear(requestBody);
                            if (response.isEmpty()) {
                                statusCode = 404;
                                response = "Movie Not Found";
                            }
                        } else {
                            statusCode = 400;
                            response = "Missing parameters";
                        }
                        break;
                    case "/api/v1/addActorAward":
                        if (requestBody.contains("actorId") && requestBody.contains("award")) {
                            response = handleAddActorAward(requestBody);
                            if (response.isEmpty()) {
                                statusCode = 404;
                                response = "Actor Not Found";
                            }
                        } else {
                            statusCode = 400;
                            response = "Missing parameters";
                        }
                        break;
                    default:
                        statusCode = 404;
                        response = "Endpoint not found";
                        break;
                }
            } else if (method.equals("GET")) {
                String path = requestUri.getPath();
                switch (path) {
                    case "/api/v1/getActor":
                        if (requestBody.contains("actorId")) {
                            response = handleGetActor(requestBody);
                            if (response.isEmpty()) {
                                statusCode = 404;
                                response = "Actor Not Found";
                            }
                        } else {
                            statusCode = 400;
                            response = "Missing actorId parameter";
                        }
                        break;
                    case "/api/v1/getMovie":
                        if (requestBody.contains("movieId")) {
                            response = handleGetMovie(requestBody);
                            if (response.isEmpty()) {
                                statusCode = 404;
                                response = "Movie Not Found";
                            }
                        } else {
                            statusCode = 400;
                            response = "Missing movieId parameter";
                        }
                        break;
                    case "/api/v1/hasRelationship":
                        if (requestBody.contains("actorId") && requestBody.contains("movieId")) {
                            response = handleHasRelationship(requestBody);
                            if (response.isEmpty()) {
                                statusCode = 404;
                                response = "Actor and/or Movie Not Found";
                            }
                        } else {
                            statusCode = 400;
                            response = "Missing actorId and/or movieId parameter(s)";
                        }
                        break;
                    case "/api/v1/computeBaconNumber":
                        if (requestBody.contains("actorId")) {
                            response = handleComputeBaconNumber(requestBody);
                            if (response.isEmpty()) {
                                statusCode = 404;
                                response = "Actor Not Found";
                            }
                        } else {
                            statusCode = 400;
                            response = "Missing actorId parameter";
                        }
                        break;
                    case "/api/v1/computeBaconPath":
                        if (requestBody.contains("actorId")) {
                            response = handleComputeBaconPath(requestBody);
                            if (response.isEmpty()) {
                                statusCode = 404;
                                response = "Actor Not Found";
                            }
                        } else {
                            statusCode = 400;
                            response = "Missing actorId parameter";
                        }
                        break;
                    case "/api/v1/getMoviesAboveRating":
                        if (requestBody.contains("rating")) {
                            response = handleGetMoviesAboveRating(requestBody);
                            if (response.isEmpty()) {
                                statusCode = 404;
                                response = "No movies found";
                            }
                        } else {
                            statusCode = 400;
                            response = "Missing rating parameter";
                        }
                        break;
                    case "/api/v1/getMoviesByYear":
                        if (requestBody.contains("year")) {
                            response = handleGetMoviesByYear(requestBody);
                            if (response.isEmpty()) {
                                statusCode = 404;
                                response = "No movies found";
                            }
                        } else {
                            statusCode = 400;
                            response = "Missing year parameter";
                        }
                        break;
                    case "/api/v1/getActorsByAward":
                        if (requestBody.contains("award")) {
                            response = handleGetActorsByAward(requestBody);
                            if (response.isEmpty()) {
                                statusCode = 404;
                                response = "No actors found";
                            }
                        } else {
                            statusCode = 400;
                            response = "Missing year parameter";
                        }
                        break;
                    default:
                        statusCode = 404;
                        response = "Endpoint not found";
                        break;
                }
            } else {
                statusCode = 405;
                response = "Method not allowed";
            }
        } catch (JSONException e) {
            statusCode = 400;
            response = "Invalid JSON: " + e.getMessage();
        } catch (Exception e) {
            statusCode = 500;
            response = "Internal server error: " + e.getMessage();
        }

        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), "utf-8"));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        reader.close();
        return stringBuilder.toString();
    }

    private void handleAddActor(String requestBody) throws JSONException {
        JSONObject json = new JSONObject(requestBody);
        String name = json.getString("name");
        String actorId = json.getString("actorId");
        neo4j.addActor(name, actorId);
    }

    private void handleAddMovie(String requestBody) throws JSONException {
        JSONObject json = new JSONObject(requestBody);
        String name = json.getString("name");
        String movieId = json.getString("movieId");
        neo4j.addMovie(name, movieId);
    }

    private String handleAddRelationship(String requestBody) throws JSONException {
        JSONObject json = new JSONObject(requestBody);
        String actorId = json.getString("actorId");
        String movieId = json.getString("movieId");
        return neo4j.addRelationship(actorId, movieId);
    }

    private String handleAddMovieRating(String requestBody) throws JSONException {
        JSONObject json = new JSONObject(requestBody);
        String movieId = json.getString("movieId");
        int rating = json.getInt("rating");
        return neo4j.addMovieRating(movieId, rating);
    }

    private String handleAddMovieYear(String requestBody) throws JSONException {
        JSONObject json = new JSONObject(requestBody);
        String movieId = json.getString("movieId");
        int year = json.getInt("year");
        return neo4j.addMovieYear(movieId, year);
    }

    private String handleAddActorAward(String requestBody) throws JSONException {
        JSONObject json = new JSONObject(requestBody);
        String actorId = json.getString("actorId");
        String award = json.getString("award");
        return neo4j.addActorAward(actorId, award);
    }

    private String handleGetActor(String requestBody) throws JSONException {
        JSONObject json = new JSONObject(requestBody);
        String actorId = json.getString("actorId");
        return neo4j.getActor(actorId);
    }

    private String handleGetMovie(String requestBody) throws JSONException {
        JSONObject json = new JSONObject(requestBody);
        String movieId = json.getString("movieId");
        return neo4j.getMovie(movieId);
    }

    private String handleHasRelationship(String requestBody) throws JSONException {
        JSONObject json = new JSONObject(requestBody);
        String actorId = json.getString("actorId");
        String movieId = json.getString("movieId");
        return neo4j.hasRelationship(actorId, movieId);
    }

    private String handleComputeBaconNumber(String requestBody) throws JSONException {
        JSONObject json = new JSONObject(requestBody);
        String actorId = json.getString("actorId");
        return neo4j.computeBaconNumber(actorId);
    }

    private String handleComputeBaconPath(String requestBody) throws JSONException {
        JSONObject json = new JSONObject(requestBody);
        String actorId = json.getString("actorId");
        return neo4j.computeBaconPath(actorId);
    }

    private String handleGetMoviesAboveRating(String requestBody) throws JSONException {
        JSONObject json = new JSONObject(requestBody);
        int rating = json.getInt("rating");
        return neo4j.getMoviesAboveRating(rating);
    }

    private String handleGetMoviesByYear(String requestBody) throws JSONException {
        JSONObject json = new JSONObject(requestBody);
        int year = json.getInt("year");
        return neo4j.getMoviesByYear(year);
    }

    private String handleGetActorsByAward(String requestBody) throws JSONException {
        JSONObject json = new JSONObject(requestBody);
        String award = json.getString("award");
        return neo4j.getActorsByAward(award);
    }


    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query != null && !query.isEmpty()) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    params.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return params;
    }
}
