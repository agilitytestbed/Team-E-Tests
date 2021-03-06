package nl.utwente.ing.team.e.testing.sessions;

import org.junit.Test;

import static com.jayway.restassured.RestAssured.when;


public class SessionBaseTest {

    public final static String HOSTNAME = "http://localhost:8080";
    public final static String VERSION = "/api/v1";
    private static final String SESSIONS = HOSTNAME + VERSION + "/sessions";

    @Test
    public void getSession() {
        /**
         * Tests whether sessions work
         */
        when().
                post(SESSIONS).
                then().assertThat().
                statusCode(201).
                contentType("application/json");
    }
}
