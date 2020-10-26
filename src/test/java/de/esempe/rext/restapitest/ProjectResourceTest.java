package de.esempe.rext.restapitest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.UUID;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
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

@DisplayName("REST-API Test für Role-Resource")
@TestMethodOrder(OrderAnnotation.class)
public class ProjectResourceTest extends AbstractResourceTest
{
	final static String field_id = "projectid";
	final static String field_name = "projectname";
	final static String field_description = "description";

	final static String baseURL = "http://localhost:8080/monolith/rext/projectmgmt/projects";

	// Echte Project-ID vom der ersten Project aus GET-Abruf
	static String realProjectID;

	@BeforeAll
	static void setUpBeforeClass() throws Exception
	{
		target = client.target(baseURL);
	}

	@Test
	@Order(1)
	@DisplayName(" Befehl 'HTTP OPTION' für: " + baseURL)
	// HTTP OPTION ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void optionRoles()
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
	@Order(2)
	@DisplayName(" Befehl 'HTTP HEAD' für: " + baseURL)
	// HTTP HEAD ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void headRoles()
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
	@Order(3)
	@DisplayName(" Befehl 'HTTP GET' für: " + baseURL)
	// HTTP GET ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void getRoles()
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
		assertThat(jsonArray.size()).as("Keine Daten vorhanden").isGreaterThan(0);
		final JsonObject jsonObject = jsonArray.getJsonObject(0);
		assertThat(jsonObject).isNotNull();

		//@formatter:off
		assertAll("Verify content",
		  () -> assertThat(jsonObject.containsKey(field_id)).isTrue(),
		  () -> assertThat(jsonObject.containsKey(field_name)).isTrue(),
		  () -> assertThat(jsonObject.containsKey(field_description)).isTrue()
		);
		//@formatter:on

		// echte Objekt-Id für nachfolgende Test intern vermerken
		ProjectResourceTest.realProjectID = jsonObject.getString(field_id);
	}

	@Test
	@Order(4)
	@DisplayName(" Befehl 'HTTP OPTION' für: " + baseURL + "/id")
	// HTTP OPTION ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void optionRolesId()
	{
		// act
		invocationBuilder = target.path("/{id}").resolveTemplate("id", ProjectResourceTest.realProjectID).request(MediaType.APPLICATION_JSON);
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
	@Order(6)
	@DisplayName(" Befehl 'HTTP GET' für: " + baseURL + "/id")
	// HTTP GET ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void getRolesId()
	{
		// act
		invocationBuilder = target.path("/{id}").resolveTemplate("id", ProjectResourceTest.realProjectID).request(MediaType.APPLICATION_JSON);
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
		final JsonObject jsonObject = this.getJsonObjectFromString(jsonString);
		assertThat(jsonObject).isNotNull();

		//@formatter:off
		assertAll("Verify content",
		  () -> assertThat(jsonObject.containsKey(field_id)).isTrue(),
		  () -> assertThat(jsonObject.containsKey(field_name)).isTrue(),
		  () -> assertThat(jsonObject.containsKey(field_description)).isTrue()
		);
		//@formatter:on

	}

	@Test
	@Order(8)
	@DisplayName(" Befehl 'HTTP POST' für: " + baseURL)
	void postUser()
	{
		// prepare
		final JsonObject jsonUser = this.createNewRole("ProjAdm", "Rolle für Projektadministration");

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
	@Order(9)
	@DisplayName(" Befehl 'HTTP GET' für: " + baseURL + "/search")
	// HTTP GET ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void getRolesSearch()
	{
		// act
		invocationBuilder = target.path("/search").queryParam("name", "Demo").request(MediaType.APPLICATION_JSON);
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
	@Order(10)
	@DisplayName(" Befehl 'HTTP DELETE' für: " + baseURL + "/id")
	void deleteRoleByIdWithExistingRole()
	{
		// prepare: get object id from "posted" object from previous test
		invocationBuilder = target.path("/search").queryParam("name", "ProjAdm").request(MediaType.APPLICATION_JSON);
		final Response resSearch = invocationBuilder.get();
		final JsonObject resObj = this.createFromString(resSearch.readEntity(String.class));
		final String objId = resObj.getString(field_id);

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
	@Order(11)
	@DisplayName(" Befehl 'HTTP DELETE' für: " + baseURL + "/id")
	void deleteRoleByIdWithNonExistingRole()
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

	private JsonObject createNewRole(final String name, final String description)
	{
		//@formatter:off
		final JsonObject result = Json.createObjectBuilder()
				//.add(field_id, user.getObjid().toString())
				.add(field_name, name)
				.add(field_description, description)
				.build();
		//@formatter:on
		return result;
	}

}
