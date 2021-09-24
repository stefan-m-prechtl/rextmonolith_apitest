package de.esempe.rext.restapitest.usermgmt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.core.MediaType;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import de.esempe.rext.restapitest.AbstractResourceTest;
import de.esempe.rext.restapitest.extensions.TestClassOrder;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@DisplayName("REST-API Test für User-Resource")
@TestClassOrder(10)
@TestMethodOrder(OrderAnnotation.class)
class UserResourceTest extends AbstractResourceTest
{
	final static String field_id = "userid";
	final static String field_login = "userlogin";
	final static String field_firstname = "firstname";
	final static String field_lastname = "lastname";

	final static String baseURL = "http://localhost:8080/monolith/rext/usermgmt/users";

	// Echte Objekt-ID vom GET-Abruf - wird für weitere Aufrufe benötigt
	static String realEntityID;

	@BeforeAll
	static void setUpBeforeClass() throws Exception
	{
		target = client.target(baseURL);
	}

	@Order(10)
	@DisplayName("Befehl 'HTTP OPTION' für: " + baseURL)
	// HTTP OPTION ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	@Tag("integrationTest")
	void option()
	{
		super.optionResource();
	}

	@Order(20)
	@DisplayName("Befehl 'HTTP HEAD' für: " + baseURL)
	// HTTP HEAD ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void head()
	{
		super.headResource();
	}

	@Test
	@Order(30)
	@DisplayName("Befehl 'HTTP DELETE ' für: " + baseURL + "?flag=all")
	void deleteAll()
	{
		super.deleteAllResource();
	}

	@Test
	@Order(35)
	@DisplayName("Befehl 'HTTP POST (ok)' für: " + baseURL)
	void postOk()
	{
		// prepare
		final var jsonUser = this.createEntity("EMU", "Eva", "Mustermann");

		// act
		super.postResourceOk(jsonUser, baseURL);
	}

	@Test
	@Order(36)
	@DisplayName("Befehl 'HTTP POST (fail)' für: " + baseURL)
	void postFail()
	{
		// prepare
		final var jsonUser = this.createFromString("{}"); // this.createNewUser("ZULANGER_LOGIN", "Eva", "Mustermann");

		// act
		super.postResourceFail(jsonUser, baseURL);
	}

	@Order(40)
	@DisplayName("Befehl 'HTTP GET' für: " + baseURL)
	// HTTP GET ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void get()
	{
		final var jsonUser = super.getResource();

		//@formatter:off
		assertAll("Verify content",
				() -> assertThat(jsonUser.containsKey(field_id)).isTrue(),
				() -> assertThat(jsonUser.containsKey(field_login)).isTrue(),
				() -> assertThat(jsonUser.containsKey(field_firstname)).isTrue(),
				() -> assertThat(jsonUser.containsKey(field_lastname)).isTrue()
				);
		//@formatter:on

		// echte User-Id für nachfolgende Test intern vermerken
		UserResourceTest.realEntityID = jsonUser.getString(field_id);
	}

	@Order(50)
	@DisplayName("Befehl 'HTTP OPTION' für: " + baseURL + "/id")
	// HTTP OPTION ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void optionWithId()
	{
		super.optionResourceId(UserResourceTest.realEntityID);

	}

	@Order(60)
	@DisplayName("Befehl 'HTTP HEAD' für: " + baseURL + "/id")
	// HTTP HEAD ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void headWithId()
	{
		super.headResourceId(UserResourceTest.realEntityID);
	}

	@Order(70)
	@DisplayName("Befehl 'HTTP GET' für: " + baseURL + "/id")
	// HTTP GET ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void getWithId()
	{
		final var jsonUser = super.getResourceId(UserResourceTest.realEntityID);

		//@formatter:off
		assertAll("Verify content",
				() -> assertThat(jsonUser.containsKey(field_id)).isTrue(),
				() -> assertThat(jsonUser.containsKey(field_login)).isTrue(),
				() -> assertThat(jsonUser.containsKey(field_firstname)).isTrue(),
				() -> assertThat(jsonUser.containsKey(field_lastname)).isTrue()
				);
		//@formatter:on

	}

	@Order(80)
	@DisplayName("Befehl 'HTTP PUT' für: " + baseURL + "/id")
	// HTTP PUT ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void put()
	{
		// prepare
		var jsonUserBeforeUpdate = this.getResourceById(UserResourceTest.realEntityID);
		final var builder = Json.createPatchBuilder();
		jsonUserBeforeUpdate = builder.replace("/firstname", "Etienne").build().apply(jsonUserBeforeUpdate);
		// act & assert
		super.putResourceId(UserResourceTest.realEntityID, jsonUserBeforeUpdate);

	}

	@Order(90)
	@DisplayName("Befehl 'HTTP GET' für: " + baseURL + "/search")
	// HTTP GET ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void getBySearch()
	{
		// act
		invocationBuilder = target.path("/search").queryParam("login", "EMU").request(MediaType.APPLICATION_JSON);
		final var res = invocationBuilder.get();

		// assert
		//@formatter:off
		assertAll("Result of 'get",
				() -> assertThat(res).isNotNull(),
				() -> assertThat(res.getStatus()).isEqualTo(200),
				() -> assertThat(res.getHeaderString("content-type")).isNotBlank(),
				() -> assertThat(res.getHeaderString("content-type")).contains("application/json")
				);
		//@formatter:on

		final var jsonString = res.readEntity(String.class);
		assertThat(jsonString).isNotBlank();
		final var jsonUser = this.getJsonObjectFromString(jsonString);
		assertThat(jsonUser).isNotNull();

	}

	@Test
	@Order(100)
	@DisplayName("Befehl 'HTTP DELETE' für: " + baseURL + "/id")
	void deleteWithExistingEntity()
	{
		// act
		super.deleteResourceIdWithExistingResource(UserResourceTest.realEntityID);
	}

	@Test
	@Order(110)
	@DisplayName("Befehl 'HTTP DELETE' für: " + baseURL + "/id")
	void deleteWithNonExistingEntity()
	{
		super.deleteResourceIdWithNonExistingResource();
	}

	// ****************** Helper-Methoden *******************************

	private JsonObject createEntity(final String login, final String firstname, final String lastname)
	{
		//@formatter:off
		final var result = Json.createObjectBuilder()
				.add(field_login, login)
				.add(field_firstname, firstname)
				.add(field_lastname, lastname)
				.build();
		//@formatter:on
		return result;
	}

}
