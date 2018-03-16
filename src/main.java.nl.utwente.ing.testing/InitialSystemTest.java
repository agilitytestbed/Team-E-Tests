package nl.utwente.ing.testing;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;

public class InitialSystemTest {

    @Test
    public void transactionIdTest() {

        when().
                get("/transactions/{id}", 0).
                then().
                statusCode(200).
                body("id", equalTo(0),
                        "date", /* this is where the date should be */,
                        "amount", equalTo(0),
                        "external-iban", equalTo("string"),
                        "type:", equalTo("deposit"),
                        "category.id", equalTo(0),
                        "category.name", equalTo("string"));
    }

    @Test
    public void categoryIdTest() {
        when.
                get("/categories/{id", 0) /
                then().
                        statusCode(200).
                        body("id", equalTo(0),
                                "name", equalTo("string"));
    }

}