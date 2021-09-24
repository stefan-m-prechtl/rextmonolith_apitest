package de.esempe.rext.restapitest.workflowmgmt;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@DisplayName("REST-API Test für Status-Resource")
@TestMethodOrder(OrderAnnotation.class)
@TestClassOrder(30)
public class StateResourceTest extends AbstractResourceTest
{
	final static String field_id = "stateid";
	final static String field_name = "name";
	final static String field_description = "description";

	final static String baseURL = "http://localhost:8080/monolith/rext/workflowmgmt/status";

	// Echte Objekt-ID GET-Abruf - wird für weitere Aufrufe benötigt
	static String realStateID;

	@BeforeAll
	static void setUpBeforeClass() throws Exception
	{
		target = client.target(baseURL);
	}

	@Order(10)
	@DisplayName("Befehl 'HTTP OPTION' für: " + baseURL)
	// HTTP OPTION ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
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
		final var jsonUser = this.createEntity("NEU", "Beschreibung für Neu");

		// act
		super.postResourceOk(jsonUser, baseURL);
	}

	@Test
	@Order(36)
	@DisplayName("Befehl 'HTTP POST (fail)' für: " + baseURL)
	void postFail()
	{
		// prepare
		final var jsonUser = this.createFromString("{}");

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
				() -> assertThat(jsonUser.containsKey(field_name)).isTrue(),
				() -> assertThat(jsonUser.containsKey(field_description)).isTrue()
				);
		//@formatter:on

		// echte State-Id für nachfolgende Test intern vermerken
		StateResourceTest.realStateID = jsonUser.getString(field_id);
	}

	@Order(50)
	@DisplayName("Befehl 'HTTP OPTION' für: " + baseURL + "/id")
	// HTTP OPTION ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void optionWithId()
	{
		super.optionResourceId(StateResourceTest.realStateID);
	}

	@Order(60)
	@DisplayName("Befehl 'HTTP HEAD' für: " + baseURL + "/id")
	// HTTP HEAD ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void headWithId()
	{
		super.headResourceId(StateResourceTest.realStateID);
	}

	@Order(70)
	@DisplayName("Befehl 'HTTP GET' für: " + baseURL + "/id")
	// HTTP GET ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void getWithId()
	{
		final var jsonUser = super.getResourceId(StateResourceTest.realStateID);

		//@formatter:off
		assertAll("Verify content",
				() -> assertThat(jsonUser.containsKey(field_id)).isTrue(),
				() -> assertThat(jsonUser.containsKey(field_name)).isTrue(),
				() -> assertThat(jsonUser.containsKey(field_description)).isTrue()
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
		var jsonStateBeforeUpdate = this.getResourceById(StateResourceTest.realStateID);
		final var builder = Json.createPatchBuilder();
		jsonStateBeforeUpdate = builder.replace("/description", "Neu2Bearbeitung").build().apply(jsonStateBeforeUpdate);
		// act & assert
		super.putResourceId(StateResourceTest.realStateID, jsonStateBeforeUpdate);
	}

	@Order(90)
	@DisplayName("Befehl 'HTTP GET' für: " + baseURL + "/search")
	// HTTP GET ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void getBySearch()
	{
		// act
		invocationBuilder = target.path("/search").queryParam("name", "Neu").request(MediaType.APPLICATION_JSON);
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
		final var jsonState = this.getJsonObjectFromString(jsonString);
		assertThat(jsonState).isNotNull();
	}

	@Test
	@Order(100)
	@DisplayName("Befehl 'HTTP DELETE' für: " + baseURL + "/id")
	void deleteExistingEnity()
	{
		// act
		super.deleteResourceIdWithExistingResource(StateResourceTest.realStateID);
	}

	@Test
	@Order(110)
	@DisplayName("Befehl 'HTTP DELETE' für: " + baseURL + "/id")
	void deleteNonExistingEntity()
	{
		super.deleteResourceIdWithNonExistingResource();
	}

	// ****************** Helper-Methoden *******************************
	private JsonObject createEntity(final String name, final String description)
	{
		//@formatter:off
		final var result = Json.createObjectBuilder()
				//.add(field_id, user.getObjid().toString())
				.add(field_name, name)
				.add(field_description, description)
				.build();
		//@formatter:on
		return result;
	}

}
