package nl.utwente.ing.team.e.testing;

import org.junit.Test;

import static com.jayway.restassured.RestAssured.when;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;


public class BaseTest {


    private static final String[][] endPoints = new String[][]{
            //Here should come all the endpooints of the api
    };
    //SESSIONS

    public void getSession() {
        when().
                post("/api/v1/sessions").
                then().assertThat().
                statusCode(200).
                contentType("application/json").
                body(null); //Still needs a body
    }

    @Test
    public void transactionIdTest() {


        when().
                get("/transactions/{id}", 0).
                then().
                statusCode(200).
                body("id", equalTo(0),
                        "date", equalTo(0),
                        "amount", equalTo(0),
                        "external-iban", equalTo("string"),
                        "type:", equalTo("deposit"),
                        "category.id", equalTo(0),
                        "category.name", equalTo("string"));
    }

    @Test
    public void categoryIdTest() {
        when().
                get("/categories/{id", 0).
                then().
                statusCode(200).
                body("id", equalTo(0),
                        "name", equalTo("string"));
    }

    @Test
    public void simpleTest() {

    }

}
