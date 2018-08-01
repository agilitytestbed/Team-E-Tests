package nl.utwente.ing.team.e.testing.transactions;

import org.json.JSONObject;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class TransactionBaseTest {

    private final static String HOSTNAME = "http://localhost:8080";
    private final static String VERSION = "/api/v1";
    private static final String SESSIONS = HOSTNAME + VERSION + "/sessions";
    private static final String TRANSACTIONS = HOSTNAME + VERSION + "/transactions";
    private static final String TRANSACTIONS_ID = HOSTNAME + VERSION + "/transactions/{id}";
    private static final String TRANSACTIONS_ID_CATEGORY = HOSTNAME + VERSION + "/transactions/{id}/category";

    private static final String CATEGORIES = HOSTNAME + VERSION + "/categories";
    private static final String CATEGORIES_ID = HOSTNAME + VERSION + "/categories/{id}";

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
                        "date", equalTo(1525524719197L),
                        "amount", equalTo(100.0f),
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
                get(TRANSACTIONS_ID, 4545).
                then().
                statusCode(404);
    }

    @Test
    public void transactionCategory() {
        int id = setup();

        JSONObject categoryObj = new JSONObject();
        categoryObj.put("name", "test");

        int categoryId = given().
                header("X-session-ID", id).
                header("Content-Type", "application/json").
                body(categoryObj.toString()).
                when().
                post(CATEGORIES).
                then().
                assertThat().
                statusCode(201).
                contentType("application/json").
                extract().path("id");

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

        JSONObject assigncategory = new JSONObject();
        assigncategory.put("category_id", categoryId);

        given().
                header("X-session-ID", id).
                header("Content-Type", "application/json").
                body(assigncategory.toString()).
                when().
                patch(TRANSACTIONS + "/" + transactionId + "/category").
                then().assertThat().
                statusCode(200);

        given().
                header("X-session-ID", id).
                when().
                get(TRANSACTIONS_ID, transactionId).
                then().
                statusCode(200).
                contentType("application/json").
                body("id", equalTo(transactionId),
                        "date", equalTo(1525524719197L),
                        "amount", equalTo(100.0f),
                        "externalIBAN", equalTo("NL39RABO0300065264"),
                        "type", equalTo("deposit"),
                        "category.id", equalTo(categoryId));

    }

    @Test
    public void deleteTransaction() {
        int id = setup();

        JSONObject categoryObj = new JSONObject();
        categoryObj.put("name", "test");

        int categoryId = given().
                header("X-session-ID", id).
                header("Content-Type", "application/json").
                body(categoryObj.toString()).
                when().
                post(CATEGORIES).
                then().
                assertThat().
                statusCode(201).
                contentType("application/json").
                extract().path("id");

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

        JSONObject assigncategory = new JSONObject();
        assigncategory.put("category_id", categoryId);

        given().
                header("X-session-ID", id).
                header("Content-Type", "application/json").
                body(assigncategory.toString()).
                when().
                patch(TRANSACTIONS + "/" + transactionId + "/category").
                then().assertThat().
                statusCode(200);

        given().
                header("X-session-ID", id).
                when().
                delete(TRANSACTIONS_ID, transactionId).
                then().assertThat().
                statusCode(204);
    }
}
