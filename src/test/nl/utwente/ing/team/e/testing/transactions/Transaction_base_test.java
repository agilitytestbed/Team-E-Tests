package nl.utwente.ing.team.e.testing.transactions;

import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.util.Date;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
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

    /**
     * Tests whether sessions work
     */
    @Test
    public void getSession() {
        when().
                post(SESSIONS).
                then().assertThat().
                statusCode(201).
                contentType("application/json").
                body("id", equalTo(313)); //Still needs a body
    }

    /**
     * @return statuscode 200, json body
     */

    private void setupTransaction() {
        int id = setup();
        JSONObject category = new JSONObject();
        category.append("id", 10);
        category.append("name", "groceries");
        for(int i=0; i<10; i++){
            JSONObject transaction = new JSONObject();
            transaction.append("date", "2018-05-05T12:51:59.197Z");
            transaction.append("amount", 100);
            transaction.append("externalIBAN", "NL39RABO0300065264");
            transaction.append("type", "deposit");
            given().header("X-session-ID", id).
                    parameter("body", transaction.toString()).
                    when().
                    post(TRANSACTIONS);
        }


    }
    @Test
    public void transcationsTest() {

        com.jayway.restassured.response.Response response;
        String jsonasstring;
        int id = setup();
        response =
                given().
                        header("X-session-ID", id).
                        when().
                        get(TRANSACTIONS).
                        then().assertThat()
                        .statusCode(200)
                        .contentType("application/json")
                        .extract().response();
        jsonasstring = response.asString();
        JSONObject transactionObject = new JSONObject(response);
        JSONArray transactionArray = transactionObject.getJSONArray("transactions");
        for (int i = 0; i < transactionArray.length(); i++) {
            JSONObject explrObject = transactionArray.getJSONObject(i);

        }
    }

    /**
     * Checks whether a list of transactions from a certain category are from that category
     */
    @Test
    public void transactionsTestWithParameters() {
        int id = setup();

        //Need to add items before you do a get test
        //Test with offset and limit to see whether they work
        given().
                header("X-session-ID",id).
                parameters("offset", 0, "limit", 10, "category", "groceries").
                when().
                get(TRANSACTIONS).
                then().
                contentType("aaplication/json").
                body("category.name", equalTo("groceries"));
    }

    /**
     *
     */
    @Test
    public void transactionIdTest() {
        int id = setup();

        // This test will never work if you don't insert a transaction before you run this test
        given().
                header("X-session-ID",id).
                when().
                get(TRANSACTIONS_ID, 0).
                then().
                statusCode(200).
                contentType("aaplication/json").
                body("id", equalTo(0),
                        "date", equalTo(0),
                        "amount", equalTo(0),
                        "externalIBAN", equalTo("string"),
                        "type:", equalTo("deposit"),
                        "category.id", equalTo(0),
                        "category.name", equalTo("string"));

        given().
                header("X-session-ID",id).
                when().
                get(TRANSACTIONS_ID, "Some String").
                then().
                statusCode(400);

        given().
                header("X-session-ID",id).
                when().
                get(TRANSACTIONS_ID, Double.POSITIVE_INFINITY).
                then().
                statusCode(404);
    }

    @Test
    public void transactionIdCategory() {
        int id = setup();

        JSONObject CAT_ID_CAT = new JSONObject();
        CAT_ID_CAT.append("category_id", 313);
        JSONObject WRONG_CAT_ID_CAT = new JSONObject();
        WRONG_CAT_ID_CAT.append("category_id", Double.POSITIVE_INFINITY);
        given().
                header("X-session-ID",id).
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
                header("X-session-ID",id).
                parameters("transactionid", 0, "category_id", WRONG_CAT_ID_CAT).
                when().
                patch(TRANSACTIONS_ID_CATEGORY).
                then().
                statusCode(404);
    }
}
