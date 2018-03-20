package nl.utwente.ing.team.e.testing;

import org.junit.Test;

import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;

public class BaseTest {

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

}
