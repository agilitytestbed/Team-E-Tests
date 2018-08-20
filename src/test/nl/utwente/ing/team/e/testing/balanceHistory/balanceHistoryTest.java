package nl.utwente.ing.team.e.testing.balanceHistory;

import org.json.JSONObject;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;


public class balanceHistoryTest {

    private final static String HOSTNAME = "http://localhost:8080";
    private final static String VERSION = "/api/v1";
    private static final String SESSIONS = HOSTNAME + VERSION + "/sessions";
    private static final String TRANSACTIONS = HOSTNAME + VERSION + "/transactions";
    private static final String BALANCEHISTORY = HOSTNAME + VERSION + "/balance/history";

    //Here should come all the endpoints of the api
    /*
     - Add javadoc to each test, no clue what they do
     - Each endpoint should have at least a test
     - Add data before that test is ran
     - Test execution order is random, so new session for each test
     - Make sure to check whether each endpoint returns multiple or single values,
       currently you this does not match for some endpoints
     */
    //SESSIONS

    public int setup() {
        /**
         * Used to create a new session for every test
         */
        return given().
                when().
                post(SESSIONS).
                then().
                assertThat().
                statusCode(201).
                extract()
                .path("id");
    }

    /**
     * First add 500 euros to the account, then withdraw 300 euros in the same month
     * The history of that month is: open: 0, close: 200, high: 500, low: 0, volume: 800,
     * timestamp is the first second of August 2018
     */
    @Test
    public void volumeTest(){
        int id = setup();

        JSONObject transaction1 = new JSONObject();
        transaction1.put("date", "2018-08-05T12:51:59.197Z");
        transaction1.put("amount", 500);
        transaction1.put("externalIBAN", "NL39RABO0300065264");
        transaction1.put("type", "deposit");
        given().header("X-session-ID", id).
                header("Content-Type", "application/json").
                body(transaction1.toString()).
                when().
                post(TRANSACTIONS).
                then().assertThat().
                statusCode(201).
                extract().
                path("id");

        JSONObject transaction2 = new JSONObject();
        transaction2.put("date", "2018-08-06T12:51:59.197Z");
        transaction2.put("amount", 300);
        transaction2.put("externalIBAN", "NL39RABO0300065264");
        transaction2.put("type", "withdrawal");
        given().header("X-session-ID", id).
                header("Content-Type", "application/json").
                body(transaction2.toString()).
                when().
                post(TRANSACTIONS).
                then().assertThat().
                statusCode(201).
                extract().
                path("amount");
        given().
                header("X-session-ID", id).
                parameters("interval", "month", "intervals", 1).
                when().
                get(BALANCEHISTORY).
                then().
                statusCode(200).
                body("[0].volume", equalTo(800.0f));
    }

    @Test
    public void lowTest(){
        int id = setup();

        JSONObject transaction3 = new JSONObject();
        transaction3.put("date", "2018-07-05T12:51:59.197Z");
        transaction3.put("amount", 500);
        transaction3.put("externalIBAN", "NL39RABO0300065264");
        transaction3.put("type", "deposit");
        given().header("X-session-ID", id).
                header("Content-Type", "application/json").
                body(transaction3.toString()).
                when().
                post(TRANSACTIONS).
                then().assertThat().
                statusCode(201).
                extract().
                path("id");

        JSONObject transaction4 = new JSONObject();
        transaction4.put("date", "2018-08-06T12:51:59.197Z");
        transaction4.put("amount", 300);
        transaction4.put("externalIBAN", "NL39RABO0300065264");
        transaction4.put("type", "withdrawal");
        given().header("X-session-ID", id).
                header("Content-Type", "application/json").
                body(transaction4.toString()).
                when().
                post(TRANSACTIONS).
                then().assertThat().
                statusCode(201).
                extract().
                path("amount");
        given().
                header("X-session-ID", id).
                parameters("interval", "month", "intervals", 2).
                when().
                get(BALANCEHISTORY).
                then().
                statusCode(200).
                body("[1].open", equalTo(500.0f),
                        "[1].close", equalTo(200.0f),
                        "[1].volume", equalTo(300.0f),
                        "[1].low", equalTo(200.0f),
                        "[1].high", equalTo(500.0f));
    }

    @Test
    public void multipleTransactionTest(){
        int id = setup();

        JSONObject transaction3 = new JSONObject();
        transaction3.put("date", "2018-07-05T12:51:59.197Z");
        transaction3.put("amount", 10000);
        transaction3.put("externalIBAN", "NL39RABO0300065264");
        transaction3.put("type", "deposit");
        given().header("X-session-ID", id).
                header("Content-Type", "application/json").
                body(transaction3.toString()).
                when().
                post(TRANSACTIONS).
                then().assertThat().
                statusCode(201).
                extract().
                path("id");

        JSONObject transaction4 = new JSONObject();
        transaction4.put("date", "2018-08-06T12:51:59.197Z");
        transaction4.put("amount", 15000);
        transaction4.put("externalIBAN", "NL39RABO0300065264");
        transaction4.put("type", "withdrawal");
        given().header("X-session-ID", id).
                header("Content-Type", "application/json").
                body(transaction4.toString()).
                when().
                post(TRANSACTIONS).
                then().assertThat().
                statusCode(201).
                extract().
                path("amount");
        JSONObject transaction5 = new JSONObject();
        transaction5.put("date", "2018-08-07T12:51:59.197Z");
        transaction5.put("amount", 5000);
        transaction5.put("externalIBAN", "NL39RABO0300065264");
        transaction5.put("type", "deposit");
        given().header("X-session-ID", id).
                header("Content-Type", "application/json").
                body(transaction5.toString()).
                when().
                post(TRANSACTIONS).
                then().assertThat().
                statusCode(201).
                extract().
                path("amount");
        given().
                header("X-session-ID", id).
                parameters("interval", "year", "intervals", 1).
                when().
                get(BALANCEHISTORY).
                then().
                statusCode(200).
                body("[0].open", equalTo(0.0f),
                        "[0].close", equalTo(0.0f),
                        "[0].volume", equalTo(30000.0f),
                        "[0].low", equalTo(-5000.0f),
                        "[0].high", equalTo(10000.0f));
    }

    @Test
    public void dayTest(){
        int id = setup();

        JSONObject transaction5 = new JSONObject();
        transaction5.put("date", "2018-08-20T9:51:59.197Z");
        transaction5.put("amount", 5000);
        transaction5.put("externalIBAN", "NL39RABO0300065264");
        transaction5.put("type", "deposit");
        given().header("X-session-ID", id).
                header("Content-Type", "application/json").
                body(transaction5.toString()).
                when().
                post(TRANSACTIONS).
                then().assertThat().
                statusCode(201).
                extract().
                path("amount");
        given().
                header("X-session-ID", id).
                parameters("interval", "day", "intervals", 1).
                when().
                get(BALANCEHISTORY).
                then().
                statusCode(200).
                body("[0].open", equalTo(0.0f),
                        "[0].close", equalTo(5000.0f),
                        "[0].volume", equalTo(5000.0f),
                        "[0].low", equalTo(0.0f),
                        "[0].high", equalTo(5000.0f));
    }

    @Test
    public void weekTest(){
        int id = setup();

        JSONObject transaction5 = new JSONObject();
        transaction5.put("date", "2018-08-20T9:51:59.197Z");
        transaction5.put("amount", 50000);
        transaction5.put("externalIBAN", "NL39RABO0300065264");
        transaction5.put("type", "deposit");
        given().header("X-session-ID", id).
                header("Content-Type", "application/json").
                body(transaction5.toString()).
                when().
                post(TRANSACTIONS).
                then().assertThat().
                statusCode(201).
                extract().
                path("amount");
        given().
                header("X-session-ID", id).
                parameters("interval", "week", "intervals", 1).
                when().
                get(BALANCEHISTORY).
                then().
                statusCode(200).
                body("[0].open", equalTo(0.0f),
                        "[0].close", equalTo(50000.0f),
                        "[0].volume", equalTo(50000.0f),
                        "[0].low", equalTo(0.0f),
                        "[0].high", equalTo(50000.0f));
    }

    @Test
    public void hourTest(){
        int id = setup();

        JSONObject transaction5 = new JSONObject();
        transaction5.put("date", "2018-08-20T10:34:59.197Z");
        transaction5.put("amount", 50000);
        transaction5.put("externalIBAN", "NL39RABO0300065264");
        transaction5.put("type", "deposit");
        given().header("X-session-ID", id).
                header("Content-Type", "application/json").
                body(transaction5.toString()).
                when().
                post(TRANSACTIONS).
                then().assertThat().
                statusCode(201).
                extract().
                path("amount");
        given().
                header("X-session-ID", id).
                parameters("interval", "hour", "intervals", 1).
                when().
                get(BALANCEHISTORY).
                then().
                statusCode(200).
                body("[0].open", equalTo(0.0f),
                        "[0].close", equalTo(50000.0f),
                        "[0].volume", equalTo(50000.0f),
                        "[0].low", equalTo(0.0f),
                        "[0].high", equalTo(50000.0f));
    }

    @Test
    public void earlierTransactionTest(){
        int id = setup();

        JSONObject transaction5 = new JSONObject();
        transaction5.put("date", "2018-08-20T0:34:59.197Z");
        transaction5.put("amount", 50000);
        transaction5.put("externalIBAN", "NL39RABO0300065264");
        transaction5.put("type", "deposit");
        given().header("X-session-ID", id).
                header("Content-Type", "application/json").
                body(transaction5.toString()).
                when().
                post(TRANSACTIONS).
                then().assertThat().
                statusCode(201).
                extract().
                path("amount");
        given().
                header("X-session-ID", id).
                parameters("interval", "hour", "intervals", 1).
                when().
                get(BALANCEHISTORY).
                then().
                statusCode(200).
                body("[0].open", equalTo(50000.0f),
                        "[0].close", equalTo(50000.0f),
                        "[0].volume", equalTo(0.0f),
                        "[0].low", equalTo(50000.0f),
                        "[0].high", equalTo(50000.0f));
    }
}
