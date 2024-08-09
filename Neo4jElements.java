package ca.yorku.eecs;

import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.exceptions.Neo4jException;
import static org.neo4j.driver.v1.Values.parameters;

public class Neo4jElements implements AutoCloseable {
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

    public void addMovie(String name, String movieId) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run("MERGE (m:Movie {movieId: $movieId})" +
                                "SET m.name = $name",
                        parameters("name", name, "movieId", movieId));
                return null;
            });
        } catch (Neo4jException e) {
            throw new RuntimeException("Failed to add movie: " + e.getMessage(), e);
        }
    }

    public void addRelationship(String actorId, String movieId) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run("MATCH (a:Actor {actorId: $actorId}), (m:Movie {movieId: $movieId})" +
                                "MERGE (a)-[:ACTED_IN]->(m)",
                        parameters("actorId", actorId, "movieId", movieId));
                return null;
            });
        } catch (Neo4jException e) {
            throw new RuntimeException("Failed to add relationship: " + e.getMessage(), e);
        }
    }

    public void addMovieRating(String movieId, int rating) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run("MATCH (m:Movie {movieId: $movieId}) " +
                                "SET m.rating = $rating",
                        parameters("movieId", movieId, "rating", rating));
                return null;
            });
        } catch (Neo4jException e) {
            throw new RuntimeException("Failed to add movie rating: " + e.getMessage(), e);
        }
    }

    public void addMovieYear(String movieId, int year) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run("MATCH (m:Movie {movieId: $movieId}) " +
                                "SET m.year = $year",
                        parameters("movieId", movieId, "year", year));
                return null;
            });
        } catch (Neo4jException e) {
            throw new RuntimeException("Failed to add movie year: " + e.getMessage(), e);
        }
    }

    public void addActorAward(String actorId, String award) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run("MATCH (a:Actor {actorId: $actorId}) " +
                                "SET a.award = $award",
                        parameters("actorId", actorId, "award", award));
                return null;
            });
        } catch (Neo4jException e) {
            throw new RuntimeException("Failed to add award to actor: " + e.getMessage(), e);
        }
    }

    public String getActor(String actorId) {
        StringBuilder actorInfo = new StringBuilder();
        try (Session session = driver.session()) {
            StatementResult result = session.run(
                    "MATCH (a:Actor {actorId: $actorId})-[:ACTED_IN]->(m:Movie) " +
                            "RETURN a.actorId AS actorId, a.name AS name, collect(m.movieId) AS movies",
                    parameters("actorId", actorId)
            );
            if (result.hasNext()) {
                Record record = result.next();
                actorInfo.append("Actor ID: ").append(record.get("actorId").asString()).append("\n")
                        .append("Name: ").append(record.get("name").asString()).append("\n")
                        .append("Movies: ").append(String.join(", ", record.get("movies").asList(Values.ofString()))).append("\n");
            } else {
                actorInfo.append("Actor not found.");
            }
        } catch (Neo4jException e) {
            throw new RuntimeException("Failed to retrieve actor: " + e.getMessage(), e);
        }
        return actorInfo.toString().trim();
    }

    public String getMovie(String movieId) {
        StringBuilder movieInfo = new StringBuilder();
        try (Session session = driver.session()) {
            StatementResult result = session.run(
                    "MATCH (m:Movie {movieId: $movieId})<-[:ACTED_IN]-(a:Actor) " +
                            "RETURN m.movieId AS movieId, m.name AS name, collect(a.actorId) AS actors",
                    parameters("movieId", movieId)
            );
            if (result.hasNext()) {
                Record record = result.next();
                movieInfo.append("Movie ID: ").append(record.get("movieId").asString()).append("\n")
                        .append("Name: ").append(record.get("name").asString()).append("\n")
                        .append("Actors: ").append(String.join(", ", record.get("actors").asList(Values.ofString()))).append("\n");
            } else {
                movieInfo.append("Movie not found.");
            }
        } catch (Neo4jException e) {
            throw new RuntimeException("Failed to retrieve movie: " + e.getMessage(), e);
        }
        return movieInfo.toString().trim();
    }

    public String hasRelationship(String actorId, String movieId) {
        StringBuilder resultInfo = new StringBuilder();
        try (Session session = driver.session()) {
            StatementResult result = session.run(
                    "MATCH (a:Actor {actorId: $actorId}), (m:Movie {movieId: $movieId}) " +
                            "RETURN a.actorId AS actorId, m.movieId AS movieId, " +
                            "EXISTS((a)-[:ACTED_IN]->(m)) AS hasRelationship",
                    parameters("actorId", actorId, "movieId", movieId)
            );
            if (result.hasNext()) {
                Record record = result.next();
                resultInfo.append("Actor ID: ").append(record.get("actorId").asString()).append("\n")
                        .append("Movie ID: ").append(record.get("movieId").asString()).append("\n")
                        .append("Has Relationship: ").append(record.get("hasRelationship").asBoolean()).append("\n");
            } else {
                resultInfo.append("Relationship not found.");
            }
        } catch (Neo4jException e) {
            throw new RuntimeException("Failed to check relationship: " + e.getMessage(), e);
        }
        return resultInfo.toString().trim();
    }

    public int computeBaconNumber(String targetActorId) {
        String kevinBaconId = "";
        try (Session session = driver.session()) {
            StatementResult result = session.run(
                    "MATCH (bacon:Actor {name: 'Kevin Bacon'}) RETURN bacon.actorId AS actorId"
            );
            if (result.hasNext()) {
                kevinBaconId = result.single().get("actorId").asString();
            }
        } catch (Neo4jException e) {
            throw new RuntimeException("Failed to retrieve Kevin Bacon's ID: " + e.getMessage(), e);
        }
        if (targetActorId.equals(kevinBaconId)) {
            return 0;
        }
        int baconNumber = -1;
        try (Session session = driver.session()) {
            StatementResult result = session.run(
                    "MATCH (bacon:Actor {name: 'Kevin Bacon'}), (target:Actor {actorId: $targetActorId}) " +
                            "MATCH p = shortestPath((target)-[:ACTED_IN*]-(bacon)) " +
                            "RETURN length(p)/2 AS baconNumber",
                    Values.parameters("targetActorId", targetActorId)
            );
            if (result.hasNext()) {
                Record record = result.single();
                baconNumber = record.get("baconNumber").asInt();
            }
        } catch (Neo4jException e) {
            throw new RuntimeException("Failed to compute Bacon number: " + e.getMessage(), e);
        }
        return baconNumber;
    }

    public String computeBaconPath(String targetActorId) {
        String kevinBaconId = "";
        try (Session session = driver.session()) {
            StatementResult result = session.run(
                    "MATCH (bacon:Actor {name: 'Kevin Bacon'}) RETURN bacon.actorId AS actorId"
            );
            if (result.hasNext()) {
                kevinBaconId = result.single().get("actorId").asString();
            }
        } catch (Neo4jException e) {
            throw new RuntimeException("Failed to retrieve Kevin Bacon's ID: " + e.getMessage(), e);
        }
        if (targetActorId.equals(kevinBaconId)) {
            return "[" + kevinBaconId + "]";
        }
        StringBuilder baconPath = new StringBuilder();
        try (Session session = driver.session()) {
            StatementResult result = session.run(
                    "MATCH (bacon:Actor {name: 'Kevin Bacon'}), (target:Actor {actorId: $targetActorId}) " +
                            "MATCH p = shortestPath((target)-[:ACTED_IN*]-(bacon)) " +
                            "WITH nodes(p) AS path_nodes " +
                            "RETURN [node IN path_nodes | CASE " +
                            "    WHEN node:Actor THEN node.actorId " +
                            "    WHEN node:Movie THEN node.movieId " +
                            "END] AS baconPath",
                    Values.parameters("targetActorId", targetActorId)
            );
            if (result.hasNext()) {
                Record record = result.single();
                baconPath.append(record.get("baconPath").asList().toString());
            }
        } catch (Neo4jException e) {
            throw new RuntimeException("Failed to compute Bacon path: " + e.getMessage(), e);
        }
        return baconPath.toString();
    }

    public String getMoviesAboveRating(int rating) {
        StringBuilder movieIds = new StringBuilder();
        try (Session session = driver.session()) {
            StatementResult result = session.run(
                    "MATCH (m:Movie) WHERE m.rating > $rating RETURN m.movieId AS movieId",
                    Values.parameters("rating", rating)
            );
            while (result.hasNext()) {
                Record record = result.next();
                movieIds.append(record.get("movieId").asString()).append("\n");
            }
        } catch (Neo4jException e) {
            throw new RuntimeException("Failed to retrieve movies: " + e.getMessage(), e);
        }
        return movieIds.toString().trim();
    }

    public String getMoviesByYear(int year) {
        StringBuilder movieIds = new StringBuilder();
        try (Session session = driver.session()) {
            StatementResult result = session.run(
                    "MATCH (m:Movie) WHERE m.year = $year RETURN m.movieId AS movieId",
                    Values.parameters("year", year)
            );
            while (result.hasNext()) {
                Record record = result.next();
                movieIds.append(record.get("movieId").asString()).append("\n");
            }
        } catch (Neo4jException e) {
            throw new RuntimeException("Failed to retrieve movies: " + e.getMessage(), e);
        }
        return movieIds.toString().trim();
    }

    public String getActorsByAward(String award) {
        StringBuilder actorIds = new StringBuilder();
        try (Session session = driver.session()) {
            StatementResult result = session.run(
                    "MATCH (a:Actor) WHERE a.award = $award RETURN a.actorId AS actorId",
                    Values.parameters("award", award)
            );
            while (result.hasNext()) {
                Record record = result.next();
                actorIds.append(record.get("actorId").asString()).append("\n");
            }
        } catch (Neo4jException e) {
            throw new RuntimeException("Failed to retrieve actors by award: " + e.getMessage(), e);
        }
        return actorIds.toString().trim();
    }

    @Override
    public void close() throws Exception {
        driver.close();
    }

}
