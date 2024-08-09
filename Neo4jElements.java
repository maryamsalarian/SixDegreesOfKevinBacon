package ca.yorku.eecs;

import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.exceptions.Neo4jException;

import static org.neo4j.driver.v1.Values.parameters;

public class Neo4jElements {
    private Driver driver;

    public Neo4jElements() {
        String uri = "bolt://localhost:7687";
        String username = "neo4j";
        String password = "12345678";
        Config config = Config.builder().withoutEncryption().build();
        driver = GraphDatabase.driver(uri, AuthTokens.basic(username, password), config);

        try (Session session = driver.session()) {
            StatementResult result = session.run("RETURN 'Connection to Neo4j successful!' AS greeting");
            String greeting = result.single().get("greeting").asString();
            System.out.println(greeting);
        } catch (Exception e) {
            System.err.println("Neo4j connection error: " + e.getMessage());
        }
    }

    public void addActor(String name, String actorId) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run("MERGE (a:Actor {actorId: $actorId})" +
                                "SET a.name = $name",
                        parameters("name", name, "actorId", actorId));
                return null;
            });
        } catch (Neo4jException e) {
            throw new RuntimeException("Failed to add actor: " + e.getMessage(), e);
        }
    }

}
