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

    public String addRelationship(String actorId, String movieId) {
        String returnStatement = "";
        try (Session session = driver.session()) {
            StatementResult result = session.run(
                    "MATCH (a:Actor {actorId: $actorId}) " +
                            "MATCH (m:Movie {movieId: $movieId}) " +
                            "RETURN a.actorId AS actorId, m.movieId as movieId",
                    parameters("actorId", actorId, "movieId", movieId)
            );
            if (!result.hasNext()) return "";
        } catch (Neo4jException e) {
            throw new RuntimeException("Failed to retrieve IDs: " + e.getMessage(), e);
        }
        try (Session session = driver.session()) {
            session.run(
                    "MATCH (a:Actor {actorId: $actorId}), (m:Movie {movieId: $movieId})" +
                            "MERGE (a)-[:ACTED_IN]->(m)",
                    Values.parameters("actorId", actorId, "movieId", movieId)
            );
            returnStatement = "Relationship added successfully";
        } catch (Neo4jException e) {
            throw new RuntimeException("Failed to add relationship: " + e.getMessage(), e);
        }
        return returnStatement;
    }

    public String addMovieRating(String movieId, int rating) {
        String returnStatement = "";
        try (Session session = driver.session()) {
            StatementResult result = session.run(
                    "MATCH (m:Movie {movieId: $movieId}) " +
                            "RETURN m.movieId as movieId ",
                    parameters("movieId", movieId)
            );
            if (!result.hasNext()) return "";
        } catch (Neo4jException e) {
            throw new RuntimeException("Failed to retrieve movie ID: " + e.getMessage(), e);
        }
        try (Session session = driver.session()) {
            session.run(
                    "MATCH (m:Movie {movieId: $movieId}) " +
                            "SET m.rating = $rating",
                    Values.parameters("movieId", movieId, "rating", rating)
            );
            returnStatement = "Movie Rating added successfully";
        } catch (Neo4jException e) {
            throw new RuntimeException("Failed to add movie rating: " + e.getMessage(), e);
        }
        return returnStatement;
    }

    public String addMovieYear(String movieId, int year) {
        String returnStatement = "";
        try (Session session = driver.session()) {
            StatementResult result = session.run(
                    "MATCH (m:Movie {movieId: $movieId}) " +
                            "RETURN m.movieId as movieId",
                    parameters("movieId", movieId)
            );
            if (!result.hasNext()) return "";
        } catch (Neo4jException e) {
            throw new RuntimeException("Failed to retrieve movie ID: " + e.getMessage(), e);
        }
        try (Session session = driver.session()) {
            session.run(
                    "MATCH (m:Movie {movieId: $movieId})" +
                            "SET m.year = $year",
                    Values.parameters("movieId", movieId, "year", year)
            );
            returnStatement = "Movie year added successfully";
        } catch (Neo4jException e) {
            throw new RuntimeException("Failed to add movie year: " + e.getMessage(), e);
        }
        return returnStatement;
    }

    public String addActorAward(String actorId, String award) {
        String returnStatement = "";
        try (Session session = driver.session()) {
            StatementResult result = session.run(
                    "MATCH (a:Actor {actorId: $actorId}) " +
                            "RETURN a.actorId AS actorId",
                    parameters("actorId", actorId)
            );
            if (!result.hasNext()) return "";
        } catch (Neo4jException e) {
            throw new RuntimeException("Failed to retrieve actor ID: " + e.getMessage(), e);
        }
        try (Session session = driver.session()) {
            session.run(
                    "MATCH (a:Actor {actorId: $actorId}) " +
                                "SET a.award = $award",
                    Values.parameters("actorId", actorId, "award", award)
            );
            returnStatement = "Actor award added successfully";
        } catch (Neo4jException e) {
            throw new RuntimeException("Failed to add actor award: " + e.getMessage(), e);
        }
        return returnStatement;
    }

    public String getActor(String actorId) {
        StringBuilder actorInfo = new StringBuilder();
        try (Session session = driver.session()) {
            StatementResult result = session.run(
                    "MATCH (a:Actor {actorId: $actorId}) " +
                            "OPTIONAL MATCH (a)-[:ACTED_IN]->(m:Movie) " +
                            "RETURN a.actorId AS actorId, a.name AS name, collect(m.movieId) AS movies",
                    parameters("actorId", actorId)
            );
            if (result.hasNext()) {
                Record record = result.next();
                actorInfo.append("actorId: ").append(actorId).append("\n")
                        .append("name: ").append(record.get("name").asString()).append("\n")
                        .append("movies: ").append(String.join(", ", record.get("movies").asList(Values.ofString()))).append("\n");
            } else {
                return "";
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
                    "MATCH (m:Movie {movieId: $movieId}) " +
                            "OPTIONAL MATCH (m)<-[:ACTED_IN]-(a:Actor) " +
                            "RETURN m.movieId AS movieId, m.name AS name, collect(a.actorId) AS actors",
                    parameters("movieId", movieId)
            );
            if (result.hasNext()) {
                Record record = result.next();
                movieInfo.append("movieId: ").append(movieId).append("\n")
                        .append("name: ").append(record.get("name").asString()).append("\n")
                        .append("actors: ").append(String.join(", ", record.get("actors").asList(Values.ofString()))).append("\n");
            } else {
                return "";
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
                resultInfo.append("actorId: ").append(actorId).append("\n")
                        .append("movieId: ").append(movieId).append("\n")
                        .append("hasRelationship: ").append(record.get("hasRelationship").asBoolean()).append("\n");
            } else {
                return "";
            }
        } catch (Neo4jException e) {
            throw new RuntimeException("Failed to check relationship: " + e.getMessage(), e);
        }
        return resultInfo.toString().trim();
    }

    public String computeBaconNumber(String targetActorId) {
        String kevinBaconId = "";
        try (Session session = driver.session()) {
            StatementResult result = session.run(
                    "MATCH (bacon:Actor {name: 'Kevin Bacon'}) RETURN bacon.actorId AS actorId"
            );
            if (result.hasNext()) {
                kevinBaconId = result.single().get("actorId").asString();
            } else {
                return "";
            }
        } catch (Neo4jException e) {
            throw new RuntimeException("Failed to retrieve Kevin Bacon's ID: " + e.getMessage(), e);
        }
        if (targetActorId.equals(kevinBaconId)) {
            return "baconNumber: 0";
        }
        int baconNumber = -1;
        StringBuilder resultInfo = new StringBuilder();
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
                resultInfo.append("baconNumber: ").append(baconNumber);
            }
        } catch (Neo4jException e) {
            throw new RuntimeException("Failed to compute Bacon number: " + e.getMessage(), e);
        }
        return resultInfo.toString().trim();
    }

    public String computeBaconPath(String targetActorId) {
        String kevinBaconId = "";
        try (Session session = driver.session()) {
            StatementResult result = session.run(
                    "MATCH (bacon:Actor {name: 'Kevin Bacon'}) RETURN bacon.actorId AS actorId"
            );
            if (result.hasNext()) {
                kevinBaconId = result.single().get("actorId").asString();
            } else {
                return "";
            }
        } catch (Neo4jException e) {
            throw new RuntimeException("Failed to retrieve Kevin Bacon's ID: " + e.getMessage(), e);
        }
        if (targetActorId.equals(kevinBaconId)) {
            return "baconPath: " + kevinBaconId;
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
                            "END] AS baconPath LIMIT 1",
                    Values.parameters("targetActorId", targetActorId)
            );
            if (result.hasNext()) {
                Record record = result.single();
                baconPath.append("baconPath: ").append(String.join(", ", record.get("baconPath").asList(Values.ofString())));
            }
        } catch (Neo4jException e) {
            throw new RuntimeException("Failed to compute Bacon path: " + e.getMessage(), e);
        }
        return baconPath.toString();
    }

    public String getMoviesAboveRating(int rating) {
        StringBuilder moviesAboveRating = new StringBuilder();
        try (Session session = driver.session()) {
            StatementResult result = session.run(
                    "MATCH (m:Movie) WHERE m.rating > $rating RETURN m.movieId AS movieId",
                    Values.parameters("rating", rating)
            );
            if (!result.hasNext()) return "";
            moviesAboveRating.append("rating: ").append(rating).append("\n");
            while (result.hasNext()) {
                Record record = result.next();
                moviesAboveRating.append("movies: ").append(record.get("movieId").asString()).append("\n");
            }
        } catch (Neo4jException e) {
            throw new RuntimeException("Failed to retrieve movies: " + e.getMessage(), e);
        }
        return moviesAboveRating.toString().trim();
    }

    public String getMoviesByYear(int year) {
        StringBuilder movieIds = new StringBuilder();
        try (Session session = driver.session()) {
            StatementResult result = session.run(
                    "MATCH (m:Movie) WHERE m.year = $year RETURN m.movieId AS movieId",
                    Values.parameters("year", year)
            );
            if (!result.hasNext()) return "";
            movieIds.append("year: ").append(year).append("\n");
            while (result.hasNext()) {
                Record record = result.next();
                movieIds.append("movies: ").append(record.get("movieId").asString()).append("\n");
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
            if (!result.hasNext()) return "";
            actorIds.append("award: ").append(award).append("\n");
            while (result.hasNext()) {
                Record record = result.next();
                actorIds.append("actors: ").append(record.get("actorId").asString()).append("\n");
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
