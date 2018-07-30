package nl.utwente.ing.team.e.testing.transactions;

import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class Transaction_base_test {

    private final static String HOSTNAME = "http://localhost:8080";
    private final static String VERSION = "/api/v1";
    private static final String SESSIONS = HOSTNAME + VERSION + "/sessions";
    private static final String TRANSACTIONS = HOSTNAME + VERSION + "/transactions";
    private static final String TRANSACTIONS_ID = HOSTNAME + VERSION + "/transactions/{id}";
    private static final String TRANSACTIONS_ID_CATEGORY = HOSTNAME + VERSION + "/transactions/{id}/category";

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

    /**
     * Used to create a new session for every test
     */
    private int setup() {
        return given().
                when().
                post(SESSIONS).
                then().
                assertThat().
                statusCode(201).
                extract()
                .path("id");
    }

    @Test
    public void transactionsTest() {
        int id = setup();

        createTransactions(id);

        given().
                header("X-session-ID", id).
                when().
                get(TRANSACTIONS).
                then().assertThat().
                statusCode(200).
                contentType("application/json").
                body("size()", equalTo(10));
    }

    /**
     * Checks whether a list of transactions from a certain category are from that category
     */
    @Test
    public void transactionsTestWithParameters() {
        int id = setup();

        createTransactions(id);
        given().
                header("X-session-ID", id).
                parameters("offset", 0, "limit", 5).
                when().
                get(TRANSACTIONS).
                then().assertThat().
                contentType("application/json").
                body("size()", equalTo(5));

        given().
                header("X-session-ID", id).
                parameters("offset", 1, "limit", 5).
                when().
                get(TRANSACTIONS).
                then().assertThat().
                contentType("application/json").
                body("size()", equalTo(5));
    }

    private void createTransactions(int id) {
        for (int i = 0; i < 10; i++) {
            JSONObject transaction = new JSONObject();
            transaction.put("date", "2018-05-05T12:51:59.197Z");
            transaction.put("amount", 100);
            transaction.put("externalIBAN", "NL39RABO0300065264");
            transaction.put("type", "deposit");
            given().header("X-session-ID", id).
                    header("Content-Type", "application/json").
                    body(transaction.toString()).
                    when().
                    post(TRANSACTIONS).
                    then().assertThat().
                    statusCode(201);
        }
    }

    /**
     *
     */
    @Test
    public void transactionIdTest() {
        int id = setup();

        JSONObject transaction = new JSONObject();
        transaction.put("date", "2018-05-05T12:51:59.197Z");
        transaction.put("amount", 100);
        transaction.put("externalIBAN", "NL39RABO0300065264");
        transaction.put("type", "deposit");
        int transactionId = given().header("X-session-ID", id).
                header("Content-Type", "application/json").
                body(transaction.toString()).
                when().
                post(TRANSACTIONS).
                then().assertThat().
                statusCode(201).
                extract().path("id");

        // This test will never work if you don't insert a transaction before you run this test
        given().
                header("X-session-ID", id).
                when().
                get(TRANSACTIONS_ID, transactionId).
                then().
                statusCode(200).
                contentType("application/json").
                body("id", equalTo(transactionId),
                        "date", equalTo("1525524719197"),
                        "amount", equalTo("100.0"),
                        "externalIBAN", equalTo("NL39RABO0300065264"),
                        "type", equalTo("deposit"));

        given().
                header("X-session-ID", id).
                when().
                get(TRANSACTIONS_ID, "Some String").
                then().
                statusCode(400);

        given().
                header("X-session-ID", id).
                when().
                get(TRANSACTIONS_ID, Double.POSITIVE_INFINITY).
                then().
                statusCode(404);
    }

    @Test
    @Ignore
    public void transactionIdCategory() {
        int id = setup();

        JSONObject CAT_ID_CAT = new JSONObject();
        CAT_ID_CAT.append("category_id", 313);
        JSONObject WRONG_CAT_ID_CAT = new JSONObject();
        WRONG_CAT_ID_CAT.put("category_id", Double.MAX_VALUE);
        given().
                header("X-session-ID", id).
                parameters("transactionid", 0, "category_id", CAT_ID_CAT).
                when().
                patch(TRANSACTIONS_ID_CATEGORY).
                then().
                statusCode(200).
                contentType("aaplication/json").
                body("id", equalTo(0),
                        "date", equalTo(0),
                        "amount", equalTo(0),
                        "external-iban", equalTo("string"),
                        "type:", equalTo("deposit"),
                        "category.id", equalTo(0),
                        "category.name", equalTo("groceries"));

        given().
                header("X-session-ID", id).
                parameters("transactionid", 0, "category_id", WRONG_CAT_ID_CAT).
                when().
                patch(TRANSACTIONS_ID_CATEGORY).
                then().
                statusCode(404);
    }
}
