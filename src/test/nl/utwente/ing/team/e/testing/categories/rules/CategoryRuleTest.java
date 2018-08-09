package nl.utwente.ing.team.e.testing.categories.rules;

import org.json.JSONObject;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;

/**
 * @author Martijn Noorlander
 */
public class CategoryRuleTest {

    private final static String HOSTNAME = "http://localhost:8080";
    private final static String VERSION = "/api/v1";
    private static final String SESSIONS = HOSTNAME + VERSION + "/sessions";
    private static final String CATEGORIES = HOSTNAME + VERSION + "/categories";
    private static final String TRANSACTIONS = HOSTNAME + VERSION + "/transactions";
    private static final String CATEGORIES_ID = HOSTNAME + VERSION + "/categories/{id}";
    private static final String TRANSACTIONS_ID = HOSTNAME + VERSION + "/transactions/{id}";
    private static final String TRANSACTIONS_ID_CATEGORY = HOSTNAME + VERSION + "/transactions/{id}/category";
    private static final String CATEGORYRULE = HOSTNAME + VERSION + "/categoryRules";
    private static final String CATEGORYRULE_ID = HOSTNAME + VERSION + "/categoryRules/{id}";


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
     * Test the creation of rules, and check if applying them works
     */
    @Test
    public void categoryRuleCreate() {
        int id = setup();

        JSONObject category = new JSONObject();
        category.put("name", "groceries");
        JSONObject categoryRule = new JSONObject();
        categoryRule.put("description", "");
        categoryRule.put("iBAN", "NL39RABO0300065264");
        categoryRule.put("applyOnHistory", true);
        categoryRule.put("type", "deposit");

        JSONObject transaction = new JSONObject();
        transaction.put("date", "2018-05-05T12:51:59.197Z");
        transaction.put("amount", 100);
        transaction.put("externalIBAN", "NL39RABO0300065264");
        transaction.put("type", "deposit");
        int transactionid = given().header("X-session-ID", id).
                header("Content-Type", "application/json").
                body(transaction.toString()).
                when().
                post(TRANSACTIONS).
                then().assertThat().
                statusCode(201).
                extract().
                path("id");

        given().
                header("X-session-ID", id).
                when().
                get(TRANSACTIONS_ID, transactionid).
                then().
                statusCode(200).
                contentType("application/json").
                body("id", equalTo(transactionid),
                        "date", equalTo(1525524719197L),
                        "amount", equalTo(100.0f),
                        "externalIBAN", equalTo("NL39RABO0300065264"),
                        "type", equalTo("deposit"),
                        "$", not(hasKey("category.id")));

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
                body("name", equalTo("groceries")).
                extract().
                path("id");

        categoryRule.put("category_id", categoryid);
        given().header("X-session-ID", id).
                header("Content-Type", "application/json").
                body(categoryRule.toString()).
                when().
                post(CATEGORYRULE).
                then().assertThat().
                statusCode(201);

        given().
                header("X-session-ID", id).
                when().
                get(TRANSACTIONS_ID, transactionid).
                then().
                statusCode(200).
                contentType("application/json").
                body("id", equalTo(transactionid),
                        "date", equalTo(1525524719197L),
                        "amount", equalTo(100.0f),
                        "externalIBAN", equalTo("NL39RABO0300065264"),
                        "type", equalTo("deposit"),
                        "category.id", equalTo(categoryid));

        transaction.put("amount", 250);
        int transactionid2 = given().header("X-session-ID", id).
                header("Content-Type", "application/json").
                body(transaction.toString()).
                when().
                post(TRANSACTIONS).
                then().assertThat().
                statusCode(201).
                extract().
                path("id");

        given().
                header("X-session-ID", id).
                when().
                get(TRANSACTIONS_ID, transactionid2).
                then().
                statusCode(200).
                contentType("application/json").
                body("id", equalTo(transactionid2),
                        "date", equalTo(1525524719197L),
                        "amount", equalTo(250.0f),
                        "externalIBAN", equalTo("NL39RABO0300065264"),
                        "type", equalTo("deposit"),
                        "category.id", equalTo(categoryid));


    }

    /**
     * Check if retrieving all or a single rule works
     */
    @Test
    public void categoryRuleGet() {
        int id = setup();

        JSONObject category = new JSONObject();
        category.put("name", "groceries");

        JSONObject categoryRule = new JSONObject();
        categoryRule.put("description", "");
        categoryRule.put("iBAN", "NL39RABO0300065264");
        categoryRule.put("applyOnHistory", true);
        categoryRule.put("type", "deposit");
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
                body("name", equalTo("groceries")).
                extract().
                path("id");

        given().
                header("X-session-ID", id).
                when().
                get(CATEGORYRULE).
                then().
                assertThat().
                statusCode(200).
                contentType("application/json").
                body("size()", equalTo(0));

        categoryRule.put("category_id", categoryid);
        int ruleid = given().header("X-session-ID", id).
                header("Content-Type", "application/json").
                body(categoryRule.toString()).
                when().
                post(CATEGORYRULE).
                then().assertThat().
                statusCode(201).extract().path("id");

        given().
                header("X-session-ID", id).
                when().
                get(CATEGORYRULE).
                then().
                assertThat().
                statusCode(200).
                contentType("application/json").
                body("size()", equalTo(1));

        given().
                header("X-session-ID", id).
                when().
                get(CATEGORYRULE_ID, ruleid).
                then().
                assertThat().
                statusCode(200).
                contentType("application/json").
                body("id", equalTo(ruleid),
                        "description", equalTo(""),
                        "iBAN", equalTo("NL39RABO0300065264"),
                        "applyOnHistory", equalTo(true),
                        "type", equalTo("deposit"));
    }

    @Test
    public void categoryRuleUpdate() {
        int id = setup();

        JSONObject category = new JSONObject();
        category.put("name", "groceries");

        JSONObject categoryRule = new JSONObject();
        categoryRule.put("description", "");
        categoryRule.put("iBAN", "NL39RABO0300065264");
        categoryRule.put("applyOnHistory", true);
        categoryRule.put("type", "deposit");
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
                body("name", equalTo("groceries")).
                extract().
                path("id");

        categoryRule.put("category_id", categoryid);
        int ruleid = given().header("X-session-ID", id).
                header("Content-Type", "application/json").
                body(categoryRule.toString()).
                when().
                post(CATEGORYRULE).
                then().assertThat().
                statusCode(201).extract().path("id");

        categoryRule.put("iBAN", "NL39RABO0000000");
        given().header("X-session-ID", id).
                header("Content-Type", "application/json").
                body(categoryRule.toString()).
                when().
                put(CATEGORYRULE_ID, ruleid).
                then().assertThat().
                statusCode(200).
                body("id", equalTo(ruleid),
                        "description", equalTo(""),
                        "iBAN", equalTo("NL39RABO0000000"),
                        "applyOnHistory", equalTo(true),
                        "type", equalTo("deposit"));
    }

    /**
     * Test removal
     */
    @Test
    public void categoryRuleDelete() {
        int id = setup();

        JSONObject category = new JSONObject();
        category.put("name", "groceries");

        JSONObject categoryRule = new JSONObject();
        categoryRule.put("description", "");
        categoryRule.put("iBAN", "NL39RABO0300065264");
        categoryRule.put("applyOnHistory", true);
        categoryRule.put("type", "deposit");
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
                body("name", equalTo("groceries")).
                extract().
                path("id");

        categoryRule.put("category_id", categoryid);
        int ruleid = given().header("X-session-ID", id).
                header("Content-Type", "application/json").
                body(categoryRule.toString()).
                when().
                post(CATEGORYRULE).
                then().assertThat().
                statusCode(201).extract().path("id");

        given().header("X-session-ID", id).
                header("Content-Type", "application/json").
                when().
                delete(CATEGORYRULE_ID, ruleid).
                then().assertThat().
                statusCode(204);

        given().
                header("X-session-ID", id).
                header("Content-Type", "application/json").
                when().
                get(CATEGORYRULE_ID, ruleid).
                then().
                assertThat().
                statusCode(404);
    }

    /**
     * Test to make sure rule that is first added is applied first
     */
    @Test
    public void categoryRuleOrder() {
        int id = setup();

        JSONObject category = new JSONObject();
        category.put("name", "groceries");

        JSONObject categoryRule = new JSONObject();
        categoryRule.put("description", "");
        categoryRule.put("iBAN", "NL39RABO0300065264");
        categoryRule.put("applyOnHistory", true);
        categoryRule.put("type", "deposit");
        int categoryid = given().
                header("X-session-ID", id).
                header("Content-Type", "application/json").
                body(category.toString()).
                when().
                post(CATEGORIES).
                then().
                assertThat().
                statusCode(201).
                extract().
                path("id");

        category.put("name", "newer");
        int categoryid2 = given().
                header("X-session-ID", id).
                header("Content-Type", "application/json").
                body(category.toString()).
                when().
                post(CATEGORIES).
                then().
                assertThat().
                statusCode(201).
                extract().
                path("id");

        categoryRule.put("category_id", categoryid);
        int ruleid = given().header("X-session-ID", id).
                header("Content-Type", "application/json").
                body(categoryRule.toString()).
                when().
                post(CATEGORYRULE).
                then().assertThat().
                statusCode(201).extract().path("id");

        categoryRule.put("category_id",categoryid2);
        int ruleid2 = given().header("X-session-ID", id).
                header("Content-Type", "application/json").
                body(categoryRule.toString()).
                when().
                post(CATEGORYRULE).
                then().assertThat().
                statusCode(201).extract().path("id");

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
                statusCode(201).
                body("category.id",equalTo(categoryid));
    }
}
