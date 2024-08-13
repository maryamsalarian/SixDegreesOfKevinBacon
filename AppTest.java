package ca.yorku.eecs;

import com.sun.net.httpserver.HttpServer;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Unit test for simple App.
 */
public class AppTest
    extends TestCase
{
    private HttpServer server;
    private Neo4jElements neo4j;
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
//    public void testApp()
//    {
//        assertTrue( true );
//    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        if (!isPortInUse(8080)) {
            neo4j = new Neo4jElements();
            server = HttpServer.create(new InetSocketAddress("0.0.0.0", 8080), 0);
            server.createContext("/api/v1", new RequestHandler(neo4j));
            server.start();
            System.out.println("Server started on port 8080");
        } else {
            System.out.println("Port 8080 already in use, skipping server startup.");
        }
    }

    private boolean isPortInUse(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            socket.setReuseAddress(true);
            return false;
        } catch (IOException e) {
            return true;
        }
    }

    @Override
    protected void tearDown() throws Exception {
        if (server != null) {
            server.stop(0);
            System.out.println("Server stopped");
        }
        if (neo4j != null) {
            neo4j.close();
        }
        super.tearDown();
    }

    private String getResponseBody(HttpURLConnection connection) throws IOException {
        InputStream inputStream = connection.getResponseCode() >= 400 ? connection.getErrorStream() : connection.getInputStream();
        return new BufferedReader(new InputStreamReader(inputStream))
                .lines().collect(Collectors.joining("\n"));
    }

    public void testAddActorPass() throws IOException {
        URL url = new URL("http://localhost:8080/api/v1/addActor");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        String json = "{\"name\":\"Kevin Bacon\",\"actorId\":\"A1000\"}";
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = json.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        int responseCode = connection.getResponseCode();
        try {
            assertEquals(HttpURLConnection.HTTP_OK, responseCode);
            System.out.println("testAddActorPass: Passed. Expected HTTP_OK, got " + responseCode + ": " + getResponseBody(connection));
        } catch (AssertionError e) {
            System.err.println("testAddActorPass: Failed. Expected HTTP_OK, but got " + responseCode);
            throw e;
        }
    }

    public void testAddActorFail() throws IOException {
        URL url = new URL("http://localhost:8080/api/v1/addActor");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        String json = "{\"name\":\"Kevin Bacon\"}";
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = json.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        int responseCode = connection.getResponseCode();
        try {
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, responseCode);
            System.out.println("testAddActorFail: Passed. Expected HTTP_BAD_REQUEST, got " + responseCode + ": " + getResponseBody(connection));
        } catch (AssertionError e) {
            System.err.println("testAddActorFail: Failed. Expected HTTP_BAD_REQUEST, but got " + responseCode);
            throw e;
        }
    }

    public void testAddMoviePass() throws IOException {
        URL url = new URL("http://localhost:8080/api/v1/addMovie");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        String json = "{\"name\":\"Apollo 13\",\"movieId\":\"M1006\"}";
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = json.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        int responseCode = connection.getResponseCode();
        try {
            assertEquals(HttpURLConnection.HTTP_OK, responseCode);
            System.out.println("testAddMoviePass: Passed. Expected HTTP_OK, got " + responseCode + ": " + getResponseBody(connection));
        } catch (AssertionError e) {
            System.err.println("testAddMoviePass: Failed. Expected HTTP_OK, but got " + responseCode);
            throw e;
        }
    }

    public void testAddMovieFail() throws IOException {
        URL url = new URL("http://localhost:8080/api/v1/addMovie");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        String json = "{\"movieId\":\"M1006\"}";
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = json.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        int responseCode = connection.getResponseCode();
        try {
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, responseCode);
            System.out.println("testAddMovieFail: Passed. Expected HTTP_BAD_REQUEST, got " + responseCode + ": " + getResponseBody(connection));
        } catch (AssertionError e) {
            System.err.println("testAddMovieFail: Failed. Expected HTTP_BAD_REQUEST, but got " + responseCode);
            throw e;
        }
    }

    public void testAddRelationshipPass() throws IOException {
        testAddActorPass();
        testAddMoviePass();
        URL url = new URL("http://localhost:8080/api/v1/addRelationship");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        String json = "{\"actorId\":\"A1000\",\"movieId\":\"M1006\"}";
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = json.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        int responseCode = connection.getResponseCode();
        try {
            assertEquals(HttpURLConnection.HTTP_OK, responseCode);
            System.out.println("testAddRelationshipPass: Passed. Expected HTTP_OK, got " + responseCode  + ": " + getResponseBody(connection));
        } catch (AssertionError e) {
            System.err.println("testAddRelationshipPass: Failed. Expected HTTP_OK, but got " + responseCode);
            throw e;
        }
    }

    public void testAddRelationshipFail() throws IOException {
        testAddActorPass();
        testAddMoviePass();
        URL url = new URL("http://localhost:8080/api/v1/addRelationship");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        String json = "{\"actorId\":\"A1000\",\"movieId\":\"\"}";
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = json.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        int responseCode = connection.getResponseCode();
        try {
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, responseCode);
            System.out.println("testAddRelationshipFail: Passed. Expected HTTP_NOT_FOUND, got " + responseCode + ": " + getResponseBody(connection));
        } catch (AssertionError e) {
            System.err.println("testAddRelationshipFail: Failed. Expected HTTP_NOT_FOUND, but got " + responseCode);
            throw e;
        }
    }

    public void testAddMovieRatingPass() throws IOException {
        testAddMoviePass();
        URL url2 = new URL("http://localhost:8080/api/v1/addMovieRating");
        HttpURLConnection connection = (HttpURLConnection) url2.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        String json = "{\"movieId\":\"M1006\",\"rating\":7}";
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = json.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        int responseCode = connection.getResponseCode();
        try {
            assertEquals(HttpURLConnection.HTTP_OK, responseCode);
            System.out.println("testAddMovieRatingPass: Passed. Expected HTTP_OK, got " + responseCode + ": " + getResponseBody(connection));
        } catch (AssertionError e) {
            System.err.println("testAddMovieRatingPass: Failed. Expected HTTP_OK, but got " + responseCode);
            throw e;
        }
    }

    public void testAddMovieRatingFail() throws IOException {
        testAddMovieRatingPass();
        URL url = new URL("http://localhost:8080/api/v1/addMovieRating");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        String json = "{\"rating\":7}";
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = json.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        int responseCode = connection.getResponseCode();
        try {
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, responseCode);
            System.out.println("testAddMovieRatingFail: Passed. Expected HTTP_BAD_REQUEST, got " + responseCode + ": " + getResponseBody(connection));
        } catch (AssertionError e) {
            System.err.println("testAddMovieRatingFail: Failed. Expected HTTP_BAD_REQUEST, but got " + responseCode);
            throw e;
        }
    }

    public void testAddMovieYearPass() throws IOException {
        testAddMoviePass();
        URL url = new URL("http://localhost:8080/api/v1/addMovieYear");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        String json = "{\"movieId\":\"M1006\",\"year\":1995}";
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = json.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        int responseCode = connection.getResponseCode();
        try {
            assertEquals(HttpURLConnection.HTTP_OK, responseCode);
            System.out.println("testAddMovieYearPass: Passed. Expected HTTP_OK, got " + responseCode + ": " + getResponseBody(connection));
        } catch (AssertionError e) {
            System.err.println("testAddMovieYearPass: Failed. Expected HTTP_OK, but got " + responseCode);
            throw e;
        }
    }

    public void testAddMovieYearFail() throws IOException {
        testAddMoviePass();
        URL url = new URL("http://localhost:8080/api/v1/addMovieYear");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        String json = "{\"movieId\":\"00000\",\"year\":1995}";
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = json.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        int responseCode = connection.getResponseCode();
        try {
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, responseCode);
            System.out.println("testAddMovieYearFail: Passed. Expected HTTP_NOT_FOUND, got " + responseCode + ": " + getResponseBody(connection));
        } catch (AssertionError e) {
            System.err.println("testAddMovieYearFail: Failed. Expected HTTP_NOT_FOUND, but got " + responseCode);
            throw e;
        }
    }

    public void testAddActorAwardPass() throws IOException {
        testAddActorPass();
        URL url = new URL("http://localhost:8080/api/v1/addActorAward");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        String json = "{\"actorId\":\"A1000\",\"award\":\"Golden Globe\"}";
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = json.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        int responseCode = connection.getResponseCode();
        try {
            assertEquals(HttpURLConnection.HTTP_OK, responseCode);
            System.out.println("testAddActorAwardPass: Passed. Expected HTTP_OK, got " + responseCode + ": " + getResponseBody(connection));
        } catch (AssertionError e) {
            System.err.println("testAddActorAwardPass: Failed. Expected HTTP_OK, but got " + responseCode);
            throw e;
        }
    }

    public void testAddActorAwardFail() throws IOException {
        testAddActorPass();
        URL url = new URL("http://localhost:8080/api/v1/addActorAward");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        String json = "{\"actorId\":\"A1000\"}";
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = json.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        int responseCode = connection.getResponseCode();
        try {
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, responseCode);
            System.out.println("testAddActorAwardFail: Passed. Expected HTTP_BAD_REQUEST, got " + responseCode + ": " + getResponseBody(connection));
        } catch (AssertionError e) {
            System.err.println("testAddActorAwardFail: Failed. Expected HTTP_BAD_REQUEST, but got " + responseCode);
            throw e;
        }
    }

    public void testGetActorPass() throws IOException {
        testAddActorPass();
        URL url = new URL("http://localhost:8080/api/v1/getActor?actorId=A1000");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        int responseCode = connection.getResponseCode();
        try {
            assertEquals(HttpURLConnection.HTTP_OK, responseCode);
            String responseBody = getResponseBody(connection);
            assertTrue(responseBody.contains("Kevin Bacon"));
            System.out.println("testGetActorPass: Passed. Expected HTTP_OK, got " + responseCode + " and response body is\n" + responseBody);
        } catch (AssertionError e) {
            System.err.println("testGetActorPass: Failed. Expected HTTP_OK and actor details");
            throw e;
        }
    }

    public void testGetActorFail() throws IOException {
        testAddActorPass();
        URL url = new URL("http://localhost:8080/api/v1/getActor?actorId");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        int responseCode = connection.getResponseCode();
        try {
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, responseCode);
            String responseBody = getResponseBody(connection);
            assertFalse(responseBody.contains("Kevin Bacon"));
            System.out.println("testGetActorFail: Passed. Expected HTTP_NOT_FOUND, got " + responseCode + ": " + responseBody);
        } catch (AssertionError e) {
            System.err.println("testGetActorFail: Failed. Expected HTTP_NOT_FOUND.");
            throw e;
        }
    }

    public void testGetMoviePass() throws IOException {
        testAddMoviePass();
        URL url = new URL("http://localhost:8080/api/v1/getMovie?movieId=M1006");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        int responseCode = connection.getResponseCode();
        try {
            assertEquals(HttpURLConnection.HTTP_OK, responseCode);
            String responseBody = getResponseBody(connection);
            assertTrue(responseBody.contains("Apollo 13"));
            System.out.println("testGetMoviePass: Passed. Expected HTTP_OK, got " + responseCode + " and response body is\n" + responseBody);
        } catch (AssertionError e) {
            System.err.println("testGetMoviePass: Failed. Expected HTTP_OK and movie details");
            throw e;
        }
    }

    public void testGetMovieFail() throws IOException {
        testAddMoviePass();
        URL url = new URL("http://localhost:8080/api/v1/getMovie?=M1006");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        int responseCode = connection.getResponseCode();
        try {
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, responseCode);
            String responseBody = getResponseBody(connection);
            assertFalse(responseBody.contains("Apollo 13"));
            System.out.println("testGetMovieFail: Passed. Expected HTTP_BAD_REQUEST, got " + responseCode + ": " + responseBody);
        } catch (AssertionError e) {
            System.err.println("testGetMovieFail: Failed. Expected HTTP_OK and movie details");
            throw e;
        }
    }

    public void testHasRelationshipPass() throws IOException {
        testAddActorPass();
        testAddMoviePass();
        URL url = new URL("http://localhost:8080/api/v1/hasRelationship?actorId=A1000&movieId=M1006");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        int responseCode = connection.getResponseCode();
        try {
            assertEquals(HttpURLConnection.HTTP_OK, responseCode);
            System.out.println("testHasRelationshipPass: Passed. Exptected HTTP_OK, got " + responseCode + " and response body is\n" + getResponseBody(connection));
        } catch (AssertionError e) {
            System.err.println("testHasRelationshipPass: Failed. Exptected HTTP_OK.");
            throw e;
        }
    }

    public void testHasRelationshipFail() throws IOException {
        testAddActorPass();
        testAddMoviePass();
        URL url = new URL("http://localhost:8080/api/v1/hasRelationship?actorId=A1000&movieId=");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        int responseCode = connection.getResponseCode();
        try {
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, responseCode);
            System.out.println("testHasRelationshipFail: Passed. Expected HTTP_NOT_FOUND, got " + responseCode + ": " + getResponseBody(connection));
        } catch (AssertionError e) {
            System.err.println("testHasRelationshipFail: Failed. Expected HTTP_NOT_FOUND.");
            throw e;
        }
    }

    public void testComputeBaconNumberPass() throws IOException {
        URL urlActor= new URL("http://localhost:8080/api/v1/addActor");
        HttpURLConnection connectionActor = (HttpURLConnection) urlActor.openConnection();
        connectionActor.setRequestMethod("PUT");
        connectionActor.setRequestProperty("Content-Type", "application/json");
        connectionActor.setDoOutput(true);
        String jsonKevin = "{\"name\":\"Ed Harris\",\"actorId\":\"A2000\"}"; //kevin bacon
        try (OutputStream os = connectionActor.getOutputStream()) {
            byte[] input = jsonKevin.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        String jsonEd = "{\"name\":\"Ed Harris\",\"actorId\":\"A2000\"}"; //ed harris
        try (OutputStream os = connectionActor.getOutputStream()) {
            byte[] input = jsonEd.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        String jsonJen = "{\"name\":\"Jennifer Lawrence\",\"actorId\":\"A1003\"}"; //jennifer lawrence
        try (OutputStream os = connectionActor.getOutputStream()) {
            byte[] input = jsonJen.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        URL urlMovie = new URL("http://localhost:8080/api/v1/addMovie");
        HttpURLConnection connectionMovie = (HttpURLConnection) urlMovie.openConnection();
        connectionMovie.setRequestMethod("PUT");
        connectionMovie.setRequestProperty("Content-Type", "application/json");
        connectionMovie.setDoOutput(true);
        String jsonApollo = "{\"name\":\"Apollo 13\",\"movieId\":\"M1006\"}"; //apollo 13
        try (OutputStream os = connectionMovie.getOutputStream()) {
            byte[] input = jsonApollo.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        String jsonXmen = "{\"name\":\"X-Men: First Class\",\"movieId\":\"M1003\"}"; //x-men: first class
        try (OutputStream os = connectionMovie.getOutputStream()) {
            byte[] input = jsonXmen.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        String jsonMother = "{\"name\":\"Mother!\",\"movieId\":\"M2000\"}"; //mother!
        try (OutputStream os = connectionMovie.getOutputStream()) {
            byte[] input = jsonMother.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        URL urlRel = new URL("http://localhost:8080/api/v1/addRelationship");
        HttpURLConnection connectionRel = (HttpURLConnection) urlRel.openConnection();
        connectionRel.setRequestMethod("PUT");
        connectionRel.setRequestProperty("Content-Type", "application/json");
        connectionRel.setDoOutput(true);
        String jsonKA = "{\"actorId\":\"A1000\",\"movieId\":\"M1006\"}"; //kevin + apollo
        try (OutputStream os = connectionRel.getOutputStream()) {
            byte[] input = jsonKA.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        String jsonKX = "{\"actorId\":\"A1000\",\"movieId\":\"M1003\"}"; //kevin + x-men
        try (OutputStream os = connectionRel.getOutputStream()) {
            byte[] input = jsonKX.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        String jsonEA = "{\"actorId\":\"A2000\",\"movieId\":\"M1006\"}"; //edd + apollo
        try (OutputStream os = connectionRel.getOutputStream()) {
            byte[] input = jsonEA.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        String jsonEM = "{\"actorId\":\"A2000\",\"movieId\":\"M2000\"}"; //edd + mother!
        try (OutputStream os = connectionRel.getOutputStream()) {
            byte[] input = jsonEM.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        String jsonJX = "{\"actorId\":\"A1003\",\"movieId\":\"M1003\"}"; //jen + x-men
        try (OutputStream os = connectionRel.getOutputStream()) {
            byte[] input = jsonJX.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        String jsonJM = "{\"actorId\":\"A1003\",\"movieId\":\"M2000\"}"; //jen + mother!
        try (OutputStream os = connectionRel.getOutputStream()) {
            byte[] input = jsonJM.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        URL url = new URL("http://localhost:8080/api/v1/computeBaconNumber?actorId=A1003"); //kev and jen => 1
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        int responseCode = connection.getResponseCode();
        String responseBody = getResponseBody(connection);
        try {
            assertEquals(HttpURLConnection.HTTP_OK, responseCode);
            assertEquals("baconNumber: 1",responseBody);
            System.out.println("testComputeBaconNumberPass: Passed. Exptected HTTP_OK, got " + responseCode + " and response body is " + responseBody);
        } catch (AssertionError e) {
            System.err.println("testComputeBaconNumberPass: Failed. Exptected HTTP_OK.");
            throw e;
        }
    }

    public void testComputeBaconNumberFail() throws IOException {
        URL urlActor= new URL("http://localhost:8080/api/v1/addActor");
        HttpURLConnection connectionActor = (HttpURLConnection) urlActor.openConnection();
        connectionActor.setRequestMethod("PUT");
        connectionActor.setRequestProperty("Content-Type", "application/json");
        connectionActor.setDoOutput(true);
        String jsonKevin = "{\"name\":\"Ed Harris\",\"actorId\":\"A2000\"}"; //kevin bacon
        try (OutputStream os = connectionActor.getOutputStream()) {
            byte[] input = jsonKevin.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        String jsonJen = "{\"name\":\"Jennifer Lawrence\",\"actorId\":\"A1003\"}"; //jennifer lawrence
        try (OutputStream os = connectionActor.getOutputStream()) {
            byte[] input = jsonJen.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        URL urlMovie = new URL("http://localhost:8080/api/v1/addMovie");
        HttpURLConnection connectionMovie = (HttpURLConnection) urlMovie.openConnection();
        connectionMovie.setRequestMethod("PUT");
        connectionMovie.setRequestProperty("Content-Type", "application/json");
        connectionMovie.setDoOutput(true);
        String jsonXmen = "{\"name\":\"X-Men: First Class\",\"movieId\":\"M1003\"}"; //x-men: first class
        try (OutputStream os = connectionMovie.getOutputStream()) {
            byte[] input = jsonXmen.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        URL urlRel = new URL("http://localhost:8080/api/v1/addRelationship");
        HttpURLConnection connectionRel = (HttpURLConnection) urlRel.openConnection();
        connectionRel.setRequestMethod("PUT");
        connectionRel.setRequestProperty("Content-Type", "application/json");
        connectionRel.setDoOutput(true);
        String jsonKX = "{\"actorId\":\"A1000\",\"movieId\":\"M1003\"}"; //kevin + x-men
        try (OutputStream os = connectionRel.getOutputStream()) {
            byte[] input = jsonKX.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        String jsonJX = "{\"actorId\":\"A1003\",\"movieId\":\"M1003\"}"; //jen + x-men
        try (OutputStream os = connectionRel.getOutputStream()) {
            byte[] input = jsonJX.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        URL url = new URL("http://localhost:8080/api/v1/computeBaconNumber?actorId=");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        int responseCode = connection.getResponseCode();
        try {
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, responseCode);
            System.out.println("testComputeBaconNumberFail: Passed. Exptected HTTP_NOT_FOUND, got " + responseCode + ": " + getResponseBody(connection));
        } catch (AssertionError e) {
            System.err.println("testComputeBaconNumberFail: Failed. Exptected HTTP_NOT_FOUND.");
            throw e;
        }
    }

    public void testComputeBaconPathPass() throws IOException {
        URL urlActor= new URL("http://localhost:8080/api/v1/addActor");
        HttpURLConnection connectionActor = (HttpURLConnection) urlActor.openConnection();
        connectionActor.setRequestMethod("PUT");
        connectionActor.setRequestProperty("Content-Type", "application/json");
        connectionActor.setDoOutput(true);
        String jsonKevin = "{\"name\":\"Ed Harris\",\"actorId\":\"A2000\"}"; //kevin bacon
        try (OutputStream os = connectionActor.getOutputStream()) {
            byte[] input = jsonKevin.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        String jsonEd = "{\"name\":\"Ed Harris\",\"actorId\":\"A2000\"}"; //ed harris
        try (OutputStream os = connectionActor.getOutputStream()) {
            byte[] input = jsonEd.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        String jsonJen = "{\"name\":\"Jennifer Lawrence\",\"actorId\":\"A1003\"}"; //jennifer lawrence
        try (OutputStream os = connectionActor.getOutputStream()) {
            byte[] input = jsonJen.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        URL urlMovie = new URL("http://localhost:8080/api/v1/addMovie");
        HttpURLConnection connectionMovie = (HttpURLConnection) urlMovie.openConnection();
        connectionMovie.setRequestMethod("PUT");
        connectionMovie.setRequestProperty("Content-Type", "application/json");
        connectionMovie.setDoOutput(true);
        String jsonApollo = "{\"name\":\"Apollo 13\",\"movieId\":\"M1006\"}"; //apollo 13
        try (OutputStream os = connectionMovie.getOutputStream()) {
            byte[] input = jsonApollo.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        String jsonXmen = "{\"name\":\"X-Men: First Class\",\"movieId\":\"M1003\"}"; //x-men: first class
        try (OutputStream os = connectionMovie.getOutputStream()) {
            byte[] input = jsonXmen.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        String jsonMother = "{\"name\":\"Mother!\",\"movieId\":\"M2000\"}"; //mother!
        try (OutputStream os = connectionMovie.getOutputStream()) {
            byte[] input = jsonMother.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        URL urlRel = new URL("http://localhost:8080/api/v1/addRelationship");
        HttpURLConnection connectionRel = (HttpURLConnection) urlRel.openConnection();
        connectionRel.setRequestMethod("PUT");
        connectionRel.setRequestProperty("Content-Type", "application/json");
        connectionRel.setDoOutput(true);
        String jsonKA = "{\"actorId\":\"A1000\",\"movieId\":\"M1006\"}"; //kevin + apollo
        try (OutputStream os = connectionRel.getOutputStream()) {
            byte[] input = jsonKA.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        String jsonKX = "{\"actorId\":\"A1000\",\"movieId\":\"M1003\"}"; //kevin + x-men
        try (OutputStream os = connectionRel.getOutputStream()) {
            byte[] input = jsonKX.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        String jsonEA = "{\"actorId\":\"A2000\",\"movieId\":\"M1006\"}"; //edd + apollo
        try (OutputStream os = connectionRel.getOutputStream()) {
            byte[] input = jsonEA.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        String jsonEM = "{\"actorId\":\"A2000\",\"movieId\":\"M2000\"}"; //edd + mother!
        try (OutputStream os = connectionRel.getOutputStream()) {
            byte[] input = jsonEM.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        String jsonJX = "{\"actorId\":\"A1003\",\"movieId\":\"M1003\"}"; //jen + x-men
        try (OutputStream os = connectionRel.getOutputStream()) {
            byte[] input = jsonJX.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        String jsonJM = "{\"actorId\":\"A1003\",\"movieId\":\"M2000\"}"; //jen + mother!
        try (OutputStream os = connectionRel.getOutputStream()) {
            byte[] input = jsonJM.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        URL url = new URL("http://localhost:8080/api/v1/computeBaconPath?actorId=A1003"); //kev and jen => [A1003, M1003, A1000]
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        int responseCode = connection.getResponseCode();
        String responseBody = getResponseBody(connection);
        try {
            assertEquals(HttpURLConnection.HTTP_OK, responseCode);
            assertTrue(responseBody.contains("M1003")); //x-men is the shortest connection, anything else is not the shortest path
            System.out.println("testComputeBaconPathPass: Passed. Exptected HTTP_OK, got " + responseCode + " and response body is " + responseBody);
        } catch (AssertionError e) {
            System.err.println("testComputeBaconPathPass: Failed. Exptected HTTP_OK.");
            throw e;
        }
    }

    public void testComputeBaconPathFail() throws IOException {
        URL urlActor= new URL("http://localhost:8080/api/v1/addActor");
        HttpURLConnection connectionActor = (HttpURLConnection) urlActor.openConnection();
        connectionActor.setRequestMethod("PUT");
        connectionActor.setRequestProperty("Content-Type", "application/json");
        connectionActor.setDoOutput(true);
        String jsonKevin = "{\"name\":\"Ed Harris\",\"actorId\":\"A2000\"}"; //kevin bacon
        try (OutputStream os = connectionActor.getOutputStream()) {
            byte[] input = jsonKevin.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        String jsonJen = "{\"name\":\"Jennifer Lawrence\",\"actorId\":\"A1003\"}"; //jennifer lawrence
        try (OutputStream os = connectionActor.getOutputStream()) {
            byte[] input = jsonJen.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        URL urlMovie = new URL("http://localhost:8080/api/v1/addMovie");
        HttpURLConnection connectionMovie = (HttpURLConnection) urlMovie.openConnection();
        connectionMovie.setRequestMethod("PUT");
        connectionMovie.setRequestProperty("Content-Type", "application/json");
        connectionMovie.setDoOutput(true);
        String jsonXmen = "{\"name\":\"X-Men: First Class\",\"movieId\":\"M1003\"}"; //x-men: first class
        try (OutputStream os = connectionMovie.getOutputStream()) {
            byte[] input = jsonXmen.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        URL urlRel = new URL("http://localhost:8080/api/v1/addRelationship");
        HttpURLConnection connectionRel = (HttpURLConnection) urlRel.openConnection();
        connectionRel.setRequestMethod("PUT");
        connectionRel.setRequestProperty("Content-Type", "application/json");
        connectionRel.setDoOutput(true);
        String jsonKX = "{\"actorId\":\"A1000\",\"movieId\":\"M1003\"}"; //kevin + x-men
        try (OutputStream os = connectionRel.getOutputStream()) {
            byte[] input = jsonKX.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        String jsonJX = "{\"actorId\":\"A1003\",\"movieId\":\"M1003\"}"; //jen + x-men
        try (OutputStream os = connectionRel.getOutputStream()) {
            byte[] input = jsonJX.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        URL url = new URL("http://localhost:8080/api/v1/computeBaconPath?=A1003");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        int responseCode = connection.getResponseCode();
        try {
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, responseCode);
            System.out.println("testComputeBaconPathFail: Passed. Exptected HTTP_BAD_REQUEST, got " + responseCode + ": " + getResponseBody(connection));
        } catch (AssertionError e) {
            System.err.println("testComputeBaconPathFail: Failed. Exptected HTTP_BAD_REQUEST.");
            throw e;
        }
    }

    public void testGetMoviesAboveRatingPass() throws IOException {
        testAddMovieRatingPass();
        URL url = new URL("http://localhost:8080/api/v1/getMoviesAboveRating?rating=6");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        int responseCode = connection.getResponseCode();
        try {
            assertEquals(HttpURLConnection.HTTP_OK, responseCode);
            System.out.println("testGetMoviesAboveRatingPass: Passed. Exptected HTTP_OK, got " + responseCode + " and response body is\n" + getResponseBody(connection));
        } catch (AssertionError e) {
            System.err.println("testGetMoviesAboveRatingPass: Failed. Exptected HTTP_OK.");
            throw e;
        }
    }

    public void testGetMoviesAboveRatingFail() throws IOException {
        testAddMovieRatingPass();
        URL url = new URL("http://localhost:8080/api/v1/getMoviesAboveRating?=6");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        int responseCode = connection.getResponseCode();
        try {
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, responseCode);
            System.out.println("testGetMoviesAboveRatingFail: Passed. Exptected HTTP_BAD_REQUEST, got " + responseCode + ": " + getResponseBody(connection));
        } catch (AssertionError e) {
            System.err.println("testGetMoviesAboveRatingFail: Failed. Exptected HTTP_BAD_REQUEST.");
            throw e;
        }
    }

    public void testGetMoviesByYearPass() throws IOException {
        testAddMovieYearPass();
        URL url = new URL("http://localhost:8080/api/v1/getMoviesByYear?year=1995");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        int responseCode = connection.getResponseCode();
        try {
            assertEquals(HttpURLConnection.HTTP_OK, responseCode);
            System.out.println("testGetMoviesByYearPass: Passed. Exptected HTTP_OK, got " + responseCode + " and response body is\n" + getResponseBody(connection));
        } catch (AssertionError e) {
            System.err.println("testGetMoviesByYearPass: Failed. Exptected HTTP_OK.");
            throw e;
        }
    }

    public void testGetMoviesByYearFail() throws IOException {
        testAddMovieYearPass();
        URL url = new URL("http://localhost:8080/api/v1/getMoviesByYear?year=2400");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        int responseCode = connection.getResponseCode();
        try {
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, responseCode);
            System.out.println("testGetMoviesByYearFail: Passed. Exptected HTTP_NOT_FOUND, got " + responseCode + ": " + getResponseBody(connection));
        } catch (AssertionError e) {
            System.err.println("testGetMoviesByYearFail: Failed. Exptected HTTP_NOT_FOUND.");
            throw e;
        }
    }

    public void testGetMoviesByAwardPass() throws IOException {
        testAddActorAwardPass();
        URL url = new URL("http://localhost:8080/api/v1/getActorsByAward?award=Golden%20Globe");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        int responseCode = connection.getResponseCode();
        try {
            assertEquals(HttpURLConnection.HTTP_OK, responseCode);
            System.out.println("testGetMoviesByAwardPass: Passed. Exptected HTTP_OK, got " + responseCode + " and response body is\n" + getResponseBody(connection));
        } catch (AssertionError e) {
            System.err.println("testGetMoviesByAwardPass: Failed. Exptected HTTP_OK.");
            throw e;
        }
    }

    public void testGetMoviesByAwardFail() throws IOException {
        testAddActorAwardPass();
        URL url = new URL("http://localhost:8080/api/v1/getActorsByAward?=Golden");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        int responseCode = connection.getResponseCode();
        try {
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, responseCode);
            System.out.println("testGetMoviesByAwardFail: Passed. Exptected HTTP_BAD_REQUEST, got " + responseCode + ": " + getResponseBody(connection));
        } catch (AssertionError e) {
            System.err.println("testGetMoviesByAwardFail: Failed. Exptected HTTP_BAD_REQUEST.");
            throw e;
        }
    }
}
