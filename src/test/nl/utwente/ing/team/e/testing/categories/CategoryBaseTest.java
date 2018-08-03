package nl.utwente.ing.team.e.testing.categories;

import org.json.JSONObject;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;


public class CategoryBaseTest {

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
    public void categoryCreateTest() {
        int id = setup();

        JSONObject category = new JSONObject();
        category.put("name", "groceries");
        JSONObject WRONG_category = new JSONObject();
        WRONG_category.put("some string", "some more string");
        given().
                header("X-session-ID",id).
                parameters("offset", 0, "limit", 0, "category", "category-name").
                when().
                get(CATEGORIES).
                then().
                assertThat().
                statusCode(200).
                contentType("application/json").
                body("size()", equalTo(0));

        given().
                header("X-session-ID",id).
                header("Content-Type", "application/json").
                body(category.toString()).
                when().
                post(CATEGORIES).
                then().
                assertThat().
                statusCode(201).
                contentType("application/json").
                body("name", equalTo("groceries"));

        given().
                header("X-session-ID",id).
                parameters("offset", 0, "limit", 0, "category", "category-name").
                when().
                get(CATEGORIES).
                then().
                assertThat().
                statusCode(200).
                contentType("application/json").
                body("size()", equalTo(1));

        given().
                header("X-session-ID",id).
                parameter(WRONG_category.toString()).
                when().
                put(CATEGORIES).
                then().
                statusCode(405);
    }

    @Test
    public void categoryIdTest() {
        int id = setup();

        JSONObject category = new JSONObject();
        category.put("name", "string");

        int categoryid = given().
                header("X-session-ID",id).
                header("Content-Type", "application/json").
                body(category.toString()).
                when().
                post(CATEGORIES).
                then().
                assertThat().
                statusCode(201).
                contentType("application/json").
                body("name", equalTo("string")).
                extract().path("id");

        given().
                header("X-session-ID",id).
                when().
                get(CATEGORIES_ID, categoryid).
                then().
                statusCode(200);

        given().
                header("X-session-ID",id).
                when().
                get(CATEGORIES_ID, 45656).
                then().
                statusCode(404);

    }

    @Test
    public void categoryDeleteTest() {
        int id = setup();

        JSONObject category = new JSONObject();
        category.put("name", "string");

        int categoryid = given().
                header("X-session-ID",id).
                header("Content-Type", "application/json").
                body(category.toString()).
                when().
                post(CATEGORIES).
                then().
                assertThat().
                statusCode(201).
                contentType("application/json").
                body("name", equalTo("string")).
                extract().path("id");

        given().
                header("X-session-ID",id).
                when().
                delete(CATEGORIES_ID, 45455).
                then().
                statusCode(404);

        given().
                header("X-session-ID",id).
                when().
                delete(CATEGORIES_ID, categoryid).
                then().
                statusCode(204);

    }

    @Test
    public void categoryUpdateTest() {
        int id = setup();

        JSONObject category = new JSONObject();
        category.put("name", "string");

        int categoryid = given().
                header("X-session-ID", id).
                header("Content-Type", "application/json").
                body(category.toString()).
                when().
                post(CATEGORIES).
                then().
                assertThat().
                statusCode(201).
                contentType("application/json").
                body("name", equalTo("string")).
                extract().path("id");

        given().
                header("X-session-ID", id).
                when().
                get(CATEGORIES_ID, categoryid).
                then().
                body("name", equalTo("string")).
                statusCode(200);

        JSONObject updateCategory = new JSONObject();
        updateCategory.put("name", "UPDATE");

        given().
                header("X-session-ID", id).
                header("Content-Type", "application/json").
                body(updateCategory.toString()).
                when().
                put(CATEGORIES_ID, categoryid).
                then().
                assertThat().
                statusCode(200).
                contentType("application/json").
                body("name", equalTo("UPDATE"),
                        "id", equalTo(categoryid));

        given().
                header("X-session-ID", id).
                when().
                get(CATEGORIES_ID, categoryid).
                then().
                body("name", equalTo("UPDATE")).
                statusCode(200);

    }

}
