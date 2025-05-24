import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;


public class PatientIntegrationTest {

  @BeforeAll
  static void setup() {
    // Set the base URI for the REST Assured tests
    RestAssured.baseURI = "http://localhost:4004";
  }

  @Test
  public void shouldReturnPatientDetailsWithValidToken() {

    String loginPayload = """
        {
          "email": "adminuser@test.com",
          "password": "password123"
        }
        """;

    String token = RestAssured.given()
        .contentType("application/json")
        .body(loginPayload)
        .when()
        .post("/auth/login")
        .then()
        .statusCode(200)
        .body("token", notNullValue())
        .extract()
        .jsonPath()
        .get("token");

    RestAssured.given()
        .header("Authorization", "Bearer " + token)
        .contentType("application/json")
        .when()
        .get("/api/patients")
        .then()
        .statusCode(200)
        .body("patients", notNullValue());
  }
}
