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
        neo4j.addActor("Jennifer Lawrence", "A1003");
        neo4j.addMovie("The Hunger Games", "M1000");
        neo4j.addMovie("Shutter Island", "M1001");
        neo4j.addMovie("Don't Look Up", "M1002");
        neo4j.addMovie("X-Men: First Class", "M1003");
        neo4j.addMovie("Mortal Kombat", "M1004");
        neo4j.addMovie("Dumb and Dumber", "M1005");
        neo4j.addRelationship("A1001", "M1001");
        neo4j.addRelationship("A1003", "M1000");
        neo4j.addRelationship("A1001", "M1002");
        neo4j.addRelationship("A1003", "M1002");
        neo4j.addRelationship("A1003", "M1003");
        neo4j.addRelationship("A1000", "M1003");
        neo4j.addMovieRating("M1004", 5);
        neo4j.addMovieRating("M1000", 7);
        neo4j.addMovieRating("M1005", 7);
        neo4j.addMovieRating("M1001", 8);
        neo4j.addMovieYear("M1001", 2010);
        neo4j.addMovieYear("M1005", 1994);
        neo4j.addActorAward("A1000", "Golden Globe");
        neo4j.addActorAward("A1001", "Oscar");
        neo4j.addActorAward("A1002", "Oscar");

    }
}
