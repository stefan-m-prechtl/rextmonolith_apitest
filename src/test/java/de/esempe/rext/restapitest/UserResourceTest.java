package de.esempe.rext.restapitest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.UUID;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonPatchBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@DisplayName("REST-API Test für User-Resource")
@TestMethodOrder(OrderAnnotation.class)
class UserResourceTest extends AbstractResourceTest
{
	final static String field_id = "userid";
	final static String field_login = "userlogin";
	final static String field_firstname = "firstname";
	final static String field_lastname = "lastname";

	final static String baseURL = "http://localhost:8080/monolith/rext/usermgmt/users";

	// Echte User-ID vom ersten User aus GET-Abruf
	static String realUserID;

	@BeforeAll
	static void setUpBeforeClass() throws Exception
	{
		target = client.target(baseURL);
	}

	@Test
	@Order(10)
	@DisplayName(" Befehl 'HTTP OPTION' für: " + baseURL)
	// HTTP OPTION ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void optionUsers()
	{
		// act
		invocationBuilder = target.request(MediaType.APPLICATION_JSON);
		final Response res = invocationBuilder.options();

		// assert
		//@formatter:off
		assertAll("Result of 'option",
				() -> assertThat(res).isNotNull(),
				() -> assertThat(res.getStatus()).isEqualTo(200),
				() -> assertThat(res.getHeaderString("allow")).isNotBlank(),
				() -> assertThat(res.getHeaderString("allow")).contains("HEAD"),
				() -> assertThat(res.getHeaderString("allow")).contains("GET"),
				() -> assertThat(res.getHeaderString("allow")).contains("OPTIONS")
				);
		//@formatter:on
	}

	@Test
	@Order(20)
	@DisplayName(" Befehl 'HTTP HEAD' für: " + baseURL)
	// HTTP HEAD ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void headUsers()
	{
		// act
		invocationBuilder = target.request(MediaType.APPLICATION_JSON);
		final Response res = invocationBuilder.head();

		// assert
		//@formatter:off
		assertAll("Result of 'head",
				() -> assertThat(res).isNotNull(),
				() -> assertThat(res.getStatus()).isEqualTo(200),
				() -> assertThat(res.getHeaderString("content-type")).isNotBlank(),
				() -> assertThat(res.getHeaderString("content-type")).contains("application/json")
				);
		//@formatter:on
	}
	@Test
	@Order(30)
	@DisplayName(" Befehl 'HTTP DELETE ' für: " + baseURL + "?flag=all")
	void deleteAllUsers()
	{

		// act
		invocationBuilder = target.queryParam("flag", "all").request(MediaType.APPLICATION_JSON);
		final Response res = invocationBuilder.delete();

		/// assert
		//@formatter:off
		assertAll("Result of 'delete",
				() -> assertThat(res).isNotNull(),
				() -> assertThat(res.getStatus()).isEqualTo(204)
				);
		//@formatter:on
	}


	@Test
	@Order(35)
	@DisplayName(" Befehl 'HTTP POST' für: " + baseURL)
	void postUser()
	{
		// prepare
		final JsonObject jsonUser = this.createNewUser("EMU", "Eva", "Mustermann");

		// act
		invocationBuilder = target.request(MediaType.APPLICATION_JSON);
		final Response res = invocationBuilder.post(Entity.json(jsonUser.toString()));

		// assert
		//@formatter:off
		assertAll("Result of 'post",
				() -> assertThat(res).isNotNull(),
				() -> assertThat(res.getStatus()).isEqualTo(204)
				);
		//@formatter:on

		// "link" -->
		// "<http://localhost:8080/user_mgmt/rest/users/cfb1be3b-da31-4f09-8aae-27b0f92707e1>;
		// rel="self";
		// type="application/json"
		final String link = res.getHeaderString("link");

		//@formatter:off
		assertAll("Verify link",
				() -> assertThat(link).isNotBlank(),
				() -> assertThat(link).contains(baseURL),
				() -> assertThat(link).contains("rel=\"self\""),
				() -> assertThat(link).contains("type=\"application/json\"")
				);
		//@formatter:on

	}


	@Test
	@Order(40)
	@DisplayName(" Befehl 'HTTP GET' für: " + baseURL)
	// HTTP GET ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void getUsers()
	{
		// act
		invocationBuilder = target.request(MediaType.APPLICATION_JSON);
		final Response res = invocationBuilder.get();

		// System.out.println(res.readEntity(String.class));

		// assert
		//@formatter:off
		assertAll("Verify meta data",
				() -> assertThat(res).isNotNull(),
				() -> assertThat(res.getStatus()).isEqualTo(200),
				() -> assertThat(res.getHeaderString("content-type")).isNotBlank(),
				() -> assertThat(res.getHeaderString("content-type")).contains("application/json")
				);
		//@formatter:on

		final String jsonString = res.readEntity(String.class);
		assertThat(jsonString).isNotBlank();
		final JsonArray jsonArray = this.getJsonArrrayFromString(jsonString);
		assertThat(jsonArray).isNotNull();
		assertThat(jsonArray.size()).isGreaterThan(0);
		final JsonObject jsonUser = jsonArray.getJsonObject(0);
		assertThat(jsonUser).isNotNull();

		//@formatter:off
		assertAll("Verify content",
				() -> assertThat(jsonUser.containsKey(field_id)).isTrue(),
				() -> assertThat(jsonUser.containsKey(field_login)).isTrue(),
				() -> assertThat(jsonUser.containsKey(field_firstname)).isTrue(),
				() -> assertThat(jsonUser.containsKey(field_lastname)).isTrue()
				);
		//@formatter:on

		// echte User-Id für nachfolgende Test intern vermerken
		UserResourceTest.realUserID = jsonUser.getString(field_id);
	}

	@Test
	@Order(50)
	@DisplayName(" Befehl 'HTTP OPTION' für: " + baseURL + "/id")
	// HTTP OPTION ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void optionUsersId()
	{
		// act
		invocationBuilder = target.path("/{id}").resolveTemplate("id", UserResourceTest.realUserID).request(MediaType.APPLICATION_JSON);
		final Response res = invocationBuilder.options();

		// assert
		//@formatter:off
		assertAll("Result of 'option",
				() -> assertThat(res).isNotNull(),
				() -> assertThat(res.getStatus()).isEqualTo(200),
				() -> assertThat(res.getHeaderString("allow")).isNotBlank(),
				() -> assertThat(res.getHeaderString("allow")).contains("HEAD"),
				() -> assertThat(res.getHeaderString("allow")).contains("GET"),
				() -> assertThat(res.getHeaderString("allow")).contains("PUT"),
				() -> assertThat(res.getHeaderString("allow")).contains("DELETE"),
				() -> assertThat(res.getHeaderString("allow")).contains("OPTIONS")
				);
		//@formatter:on
	}

	@Test
	@Order(60)
	@DisplayName(" Befehl 'HTTP HEAD' für: " + baseURL + "/id")
	// HTTP HEAD ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void headUsersId()
	{
		// act
		invocationBuilder = target.path("/{id}").resolveTemplate("id", UserResourceTest.realUserID).request(MediaType.APPLICATION_JSON);
		final Response res = invocationBuilder.head();

		// assert
		//@formatter:off
		assertAll("Result of 'head",
				() -> assertThat(res).isNotNull(),
				() -> assertThat(res.getStatus()).isEqualTo(200),
				() -> assertThat(res.getHeaderString("content-type")).isNotBlank(),
				() -> assertThat(res.getHeaderString("content-type")).contains("application/json")
				);
		//@formatter:on
	}

	@Test
	@Order(70)
	@DisplayName(" Befehl 'HTTP GET' für: " + baseURL + "/id")
	// HTTP GET ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void getUsersId()
	{
		// act
		invocationBuilder = target.path("/{id}").resolveTemplate("id", UserResourceTest.realUserID).request(MediaType.APPLICATION_JSON);
		final Response res = invocationBuilder.get();

		// assert
		//@formatter:off
		assertAll("Result of 'head",
				() -> assertThat(res).isNotNull(),
				() -> assertThat(res.getStatus()).isEqualTo(200),
				() -> assertThat(res.getHeaderString("content-type")).isNotBlank(),
				() -> assertThat(res.getHeaderString("content-type")).contains("application/json")
				);
		//@formatter:on

		final String jsonString = res.readEntity(String.class);
		assertThat(jsonString).isNotBlank();
		final JsonObject jsonUser = this.getJsonObjectFromString(jsonString);
		assertThat(jsonUser).isNotNull();

		//@formatter:off
		assertAll("Verify content",
				() -> assertThat(jsonUser.containsKey(field_id)).isTrue(),
				() -> assertThat(jsonUser.containsKey(field_login)).isTrue(),
				() -> assertThat(jsonUser.containsKey(field_firstname)).isTrue(),
				() -> assertThat(jsonUser.containsKey(field_lastname)).isTrue()
				);
		//@formatter:on

	}

	@Test
	@Order(80)
	@DisplayName(" Befehl 'HTTP PUT' für: " + baseURL + "/id")
	// HTTP PUT ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void putUsersId()
	{
		// prepare
		JsonObject jsonUserBeforeUpdate = this.getResourceById(UserResourceTest.realUserID);
		final JsonPatchBuilder builder = Json.createPatchBuilder();
		jsonUserBeforeUpdate = builder.replace("/firstname", "Etienne").build().apply(jsonUserBeforeUpdate);

		// act
		invocationBuilder = target.path("/{id}").resolveTemplate("id", UserResourceTest.realUserID).request(MediaType.APPLICATION_JSON);
		final Response res = invocationBuilder.put(Entity.json(jsonUserBeforeUpdate.toString()));

		// assert
		//@formatter:off
		assertAll("Result of 'put",
				() -> assertThat(res).isNotNull(),
				() -> assertThat(res.getStatus()).isEqualTo(204)
				);
		//@formatter:on
	}


	@Test
	@Order(90)
	@DisplayName(" Befehl 'HTTP GET' für: " + baseURL + "/search")
	// HTTP GET ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void getUsersSearch()
	{
		// act
		invocationBuilder = target.path("/search").queryParam("login", "EMU").request(MediaType.APPLICATION_JSON);
		final Response res = invocationBuilder.get();

		// assert
		//@formatter:off
		assertAll("Result of 'get",
				() -> assertThat(res).isNotNull(),
				() -> assertThat(res.getStatus()).isEqualTo(200),
				() -> assertThat(res.getHeaderString("content-type")).isNotBlank(),
				() -> assertThat(res.getHeaderString("content-type")).contains("application/json")
				);
		//@formatter:on

		final String jsonString = res.readEntity(String.class);
		assertThat(jsonString).isNotBlank();
		final JsonObject jsonUser = this.getJsonObjectFromString(jsonString);
		assertThat(jsonUser).isNotNull();

	}

	@Test
	@Order(100)
	@DisplayName(" Befehl 'HTTP DELETE' für: " + baseURL + "/id")
	void deleteUsersIdWithExistingUser()
	{
		// prepare: get object id from "posted" user from previous test
		invocationBuilder = target.path("/search").queryParam("login", "EMU").request(MediaType.APPLICATION_JSON);
		final Response resSearch = invocationBuilder.get();
		final JsonObject resObj = this.createFromString(resSearch.readEntity(String.class));
		final String objId = resObj.getString("userid");

		// act
		invocationBuilder = target.path("/{id}").resolveTemplate("id", objId).request(MediaType.APPLICATION_JSON);
		final Response res = invocationBuilder.delete();

		// assert
		//@formatter:off
		assertAll("Result of 'delete",
				() -> assertThat(res).isNotNull(),
				() -> assertThat(res.getStatus()).isEqualTo(204)
				);
		//@formatter:on
	}

	@Test
	@Order(110)
	@DisplayName(" Befehl 'HTTP DELETE' für: " + baseURL + "/id")
	void deleteUsersIdWithNonExistingUser()
	{
		// act
		invocationBuilder = target.path("/{id}").resolveTemplate("id", UUID.randomUUID().toString()).request(MediaType.APPLICATION_JSON);
		final Response res = invocationBuilder.delete();

		// assert
		//@formatter:off
		assertAll("Result of 'delete",
				() -> assertThat(res).isNotNull(),
				() -> assertThat(res.getStatus()).isEqualTo(404)
				);
		//@formatter:on
	}

	private JsonObject createNewUser(final String login, final String firstname, final String lastname)
	{
		//@formatter:off
		final JsonObject result = Json.createObjectBuilder()
				//.add(field_id, user.getObjid().toString())
				.add(field_login, login)
				.add(field_firstname, firstname)
				.add(field_lastname, lastname)
				.build();
		//@formatter:on
		return result;
	}

}
