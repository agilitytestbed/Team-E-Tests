package nl.utwente.ing.team.e.testing.categories;

import org.json.JSONObject;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;


public class Category_base_test {

    private final static String HOSTNAME = "http://localhost:8080";
    private final static String VERSION = "/api/v1";
    private static final String SESSIONS = HOSTNAME + VERSION + "/sessions";
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

    @Test
    public void getSession() {
        /**
         * Tests whether sessions work
         */
        when().
                post(SESSIONS).
                then().assertThat().
                statusCode(201).
                contentType("application/json").
                body("id", equalTo(313)); //Still needs a body
    }


    @Test
    public void categoryTest() {
        int id = setup();

        JSONObject GROCERIES = new JSONObject();
        GROCERIES.append("name", "groceries");
        JSONObject WRONG_GROCERIES = new JSONObject();
        WRONG_GROCERIES.append("some string", "some more string");
        given().
                header("X-session-ID",id).
                parameters("offset", 0, "limit", 0, "category", "category-name").
                when().
                get(CATEGORIES).
                then().
                statusCode(200).
                contentType("aaplication/json").
                body("category.name", equalTo("category-name"));

        given().
                header("X-session-ID",id).
                parameter(GROCERIES.toString()).
                when().
                put(CATEGORIES).
                then().
                statusCode(201).
                contentType("aaplication/json").
                body("id", equalTo(25),
                        "name", equalTo("groceries"));

        given().
                header("X-session-ID",id).
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
        given().
                header("X-session-ID",id).
                when().
                get(CATEGORIES_ID, 0).
                then().
                statusCode(200).
                contentType("aaplication/json").
                body("id", equalTo(0),
                        "name", equalTo("string"));
        /**
         *
         */
        given().
                header("X-session-ID",id).
                when().
                get(CATEGORIES_ID, "Some String").
                then().
                statusCode(400);
        /**
         * Get category, id not found
         */
        given().
                header("X-session-ID",id).
                when().
                get(CATEGORIES_ID, Double.POSITIVE_INFINITY).
                then().
                statusCode(404);
        /**
         * Put category id: updating a category
         */
        given().
                header("X-session-ID",id).
                parameters("categoryId", 25, "body", CATEGORY_PUT).
                when().
                put(CATEGORIES_ID).
                then().
                contentType("aaplication/json").
                body("id", equalTo(25), "name", equalTo("groceries"));
        /**
         * Delete category: id not found
         */
        given().
                header("X-session-ID",id).
                when().
                delete(CATEGORIES_ID, Double.POSITIVE_INFINITY).
                then().
                statusCode(404);

        given().
                header("X-session-ID",id).
                when().
                delete(CATEGORIES_ID, 0).
                then().
                statusCode(204);

    }


}