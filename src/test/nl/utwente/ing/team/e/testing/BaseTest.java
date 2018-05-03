package nl.utwente.ing.team.e.testing;

import org.json.JSONObject;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;


public class BaseTest {

    public final static String HOSTNAME = "http://localhost:8080";
    public final static String VERSION = "/api/v1";
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

    public int setup() {
        // TODO: Use this to create a new session for each test
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
    public void getSession() {
        when().
                post(SESSIONS).
                then().assertThat().
                statusCode(201).
                contentType("application/json").
                body("id", equalTo(313)); //Still needs a body
    }

    @Test
    public void transcationsTest() {
        int id = setup();
        given().
                // This is how you use session id to actually be authorized
                        header("X-session-ID", id).
                when().
                get(TRANSACTIONS).
                then().assertThat()
                .statusCode(200)
                .contentType("application/json");
    }

    @Test
    public void transcationsTestWithParameters() {
        int id = setup();
        //Need to add items before you do a get test
        //Test with offset and limit to see whether they work
        given().
                // TODO: Add parameters
                        when().
                get(TRANSACTIONS).
                then().
                body("category.name", equalTo("category-name"));
    }

    @Test
    public void transactionIdTest() {
        int id = setup();

        // This test will never work if you don't insert a transaction before you run this test
        when().
                get(TRANSACTIONS_ID, 0).
                then().
                statusCode(200).
                body("id", equalTo(0),
                        "date", equalTo(0),
                        "amount", equalTo(0),
                        "externalIBAN", equalTo("string"),
                        "type:", equalTo("deposit"),
                        "category.id", equalTo(0),
                        "category.name", equalTo("string"));

        when().
                get(TRANSACTIONS_ID, "Some String").
                then().
                statusCode(400);

        when().
                get(TRANSACTIONS_ID, Double.POSITIVE_INFINITY).
                then().
                statusCode(404);
    }

    @Test
    public void categoryIdCategory() {
        int id = setup();

        JSONObject CAT_ID_CAT = new JSONObject();
        CAT_ID_CAT.append("category_id", 313);
        JSONObject WRONG_CAT_ID_CAT = new JSONObject();
        WRONG_CAT_ID_CAT.append("category_id", Double.POSITIVE_INFINITY);
        given().
                parameters("transactionid", 0, "category_id", CAT_ID_CAT).
                when().
                patch(TRANSACTIONS_ID_CATEGORY).
                then().
                statusCode(200).
                body("id", equalTo(0),
                        "date", equalTo(0),
                        "amount", equalTo(0),
                        "external-iban", equalTo("string"),
                        "type:", equalTo("deposit"),
                        "category.id", equalTo(0),
                        "category.name", equalTo("groceries"));

        given().
                parameters("transactionid", 0, "category_id", WRONG_CAT_ID_CAT).
                when().
                patch(TRANSACTIONS_ID_CATEGORY).
                then().
                statusCode(404);
    }

    @Test
    public void categoryTest() {
        int id = setup();

        JSONObject GROCERIES = new JSONObject();
        GROCERIES.append("name", "groceries");
        JSONObject WRONG_GROCERIES = new JSONObject();
        WRONG_GROCERIES.append("some string", "some more string");
        given().
                parameters("offset", 0, "limit", 0, "category", "category-name").
                when().
                get(CATEGORIES).
                then().
                statusCode(200).
                body("category.name", equalTo("category-name"));

        given().
                parameter(GROCERIES.toString()).
                when().
                put(CATEGORIES).
                then().
                statusCode(201).
                body("id", equalTo(25),
                        "name", equalTo("groceries"));

        given().
                parameter(WRONG_GROCERIES.toString()).
                when().
                put(CATEGORIES).
                then().
                statusCode(405);
    }

    @Test
    public void categoryIdTest() {
        int id = setup();

        JSONObject CATEGORY_PUT = new JSONObject();
        CATEGORY_PUT.append("name", "string");
//        JSONObject CATEGORY_PUT_BODY = new JSONObject();
//        CATEGORY_PUT_BODY.append("id", 25);
//        CATEGORY_PUT_BODY.append("name", "groceries");
        when().
                get(CATEGORIES_ID, 0).
                then().
                statusCode(200).
                body("id", equalTo(0),
                        "name", equalTo("string"));
        when().
                get(CATEGORIES_ID, "Some String").
                then().
                statusCode(400);

        when().
                get(CATEGORIES_ID, Double.POSITIVE_INFINITY).
                then().
                statusCode(404);

        given().
                parameters("categoryId", 25, "body", CATEGORY_PUT).
                when().
                put(CATEGORIES_ID).
                then().
                body("id", equalTo(25), "name", equalTo("groceries"));

        when().
                delete(CATEGORIES_ID, Double.POSITIVE_INFINITY).
                then().
                statusCode(404);

        when().
                delete(CATEGORIES_ID, 0).
                then().
                statusCode(204);

    }


}
