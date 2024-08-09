package ca.yorku.eecs;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class App {
    static int PORT = 8080;

    public static void main(String[] args) throws IOException {
        Neo4jElements neo4j = new Neo4jElements();
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
        server.createContext("/api/v1", new RequestHandler(neo4j));
        server.start();
        System.out.printf("Server started on port %d...\n", PORT);

        neo4j.addActor("Kevin Bacon", "A1000");
        neo4j.addActor("Leonardo DiCaprio", "A1001");
        neo4j.addActor("Meryl Streep", "A1002");
    }
}
