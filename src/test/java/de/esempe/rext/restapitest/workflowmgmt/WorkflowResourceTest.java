package de.esempe.rext.restapitest.workflowmgmt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import de.esempe.rext.restapitest.AbstractResourceTest;
import de.esempe.rext.restapitest.extensions.TestClassOrder;

@DisplayName("REST-API Test für Workflow-Resource")
@TestClassOrder(32)
@TestMethodOrder(OrderAnnotation.class)
public class WorkflowResourceTest extends AbstractResourceTest
{
	final static String field_id = "workflowid";
	final static String field_name = "name";
	final static String field_description = "description";
	final static String field_firststate_id = "firststateid";

	final static String baseURL = "http://localhost:8080/monolith/rext/workflowmgmt/workflows";
	
	// Echte Objekt-ID vom GET-Abruf - wird für weitere Aufrufe benötigt
	static String realEntityID;

	public WorkflowResourceTest()
	{
		super(baseURL);
	}

	@Order(10)
	@DisplayName("Befehl 'HTTP OPTION' für: " + baseURL)
	// HTTP OPTION ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	@Tag("integrationTest")
	void option() throws IOException, InterruptedException
	{
		super.optionResource("");
	}

	@Order(20)
	@DisplayName("Befehl 'HTTP HEAD' für: " + baseURL)
	// HTTP HEAD ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void head() throws IOException, InterruptedException
	{
		super.headResource("");
	}

	@Test
	@Order(30)
	@DisplayName("Befehl 'HTTP DELETE ' für: " + baseURL + "?flag=all")
	void deleteAll() throws IOException, InterruptedException
	{
		super.deleteAllResource("");
	}

	@Test
	@Order(35)
	@DisplayName("Befehl 'HTTP POST (ok)' für: " + baseURL)
	@Disabled
	void postOk() throws IOException, InterruptedException

	{
		// prepare
		final var jsonWorkflow = createEntity("NEU", "Beschreibung für Neu");
		
		// act
		super.postResourceOk("", jsonWorkflow.toString());
	}

	

	@Order(40)
	@DisplayName("Befehl 'HTTP GET' für: " + baseURL)
	// HTTP GET ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void get() throws IOException, InterruptedException
	{
		final JsonArray items = super.getResource("");
		final JsonObject jsonWorkflow = (JsonObject) items.get(0);

		//@formatter:off
		assertAll("Verify content",
				() -> assertThat(jsonWorkflow.containsKey(field_id)).isTrue(),
				() -> assertThat(jsonWorkflow.containsKey(field_name)).isTrue(),
				() -> assertThat(jsonWorkflow.containsKey(field_description)).isTrue(),
				() -> assertThat(jsonWorkflow.containsKey(field_firststate_id)).isTrue());
		//@formatter:on

		// echte item-Id für nachfolgende Test intern vermerken
		WorkflowResourceTest.realEntityID = jsonWorkflow.getString(field_id);
	}

	@Order(50)
	@DisplayName("Befehl 'HTTP OPTION' für: " + baseURL + "/id")
	// HTTP OPTION ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void optionWithId() throws IOException, InterruptedException
	{
		super.optionResourceId(WorkflowResourceTest.realEntityID);

	}

	@Order(60)
	@DisplayName("Befehl 'HTTP HEAD' für: " + baseURL + "/id")
	// HTTP HEAD ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void headWithId() throws IOException, InterruptedException
	{
		super.headResourceId(WorkflowResourceTest.realEntityID);
	}

	@Order(70)
	@DisplayName("Befehl 'HTTP GET' für: " + baseURL + "/id")
	// HTTP GET ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void getWithId() throws IOException, InterruptedException
	{
		final var jsonWorkflow = super.getSingleResource(WorkflowResourceTest.realEntityID);

		//@formatter:off
		assertAll("Verify content",
				() -> assertThat(jsonWorkflow.containsKey(field_id)).isTrue(),
				() -> assertThat(jsonWorkflow.containsKey(field_name)).isTrue(),
				() -> assertThat(jsonWorkflow.containsKey(field_description)).isTrue(),
				() -> assertThat(jsonWorkflow.containsKey(field_firststate_id)).isTrue());
		//@formatter:on

	}

	@Order(80)
	@DisplayName("Befehl 'HTTP PUT' für: " + baseURL + "/id")
	// HTTP PUT ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void put() throws IOException, InterruptedException
	{
		// prepare
		var jsonWorkflowBeforeUpdate = this.getSingleResource(WorkflowResourceTest.realEntityID);
		final var builder = Json.createPatchBuilder();
		jsonWorkflowBeforeUpdate = builder.replace("/firstname", "Etienne").build().apply(jsonWorkflowBeforeUpdate);
		// act & assert
		super.putResourceId(WorkflowResourceTest.realEntityID, jsonWorkflowBeforeUpdate.toString());

	}

	@Order(90)
	@DisplayName("Befehl 'HTTP GET' für: " + baseURL + "/search")
	// HTTP GET ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void getBySearch() throws IOException, InterruptedException
	{
		// act
		final var pathExtension = "/search?login=EMU";
		final var request = HttpRequest.newBuilder().uri(URI.create(baseURL + pathExtension)).header("Content-Type", "application/json").GET().build();
		final var res = client.send(request, HttpResponse.BodyHandlers.ofString());

		// assert
		//@formatter:off
		assertAll("Result of 'get",
				() -> assertThat(res).isNotNull(),
				() -> assertThat(res.statusCode()).isEqualTo(200),
				() -> assertThat(res.headers().allValues("content-type")).isNotEmpty(),
				() -> assertThat(res.headers().allValues("content-type")).contains("application/json")
				);
		//@formatter:on

		final var data = res.body();
		assertThat(data).isNotBlank();
		final var jsonObj = this.getJsonObjectFromString(data);
		assertThat(jsonObj).isNotNull();

	}

	@Test
	@Order(100)
	@DisplayName("Befehl 'HTTP DELETE' für: " + baseURL + "/id")
	void deleteWithExistingEntity() throws IOException, InterruptedException
	{
		// act
		super.deleteResourceIdWithExistingResource(WorkflowResourceTest.realEntityID);
	}

	@Test
	@Order(110)
	@DisplayName("Befehl 'HTTP DELETE' für: " + baseURL + "/id")
	void deleteWithNonExistingEntity() throws IOException, InterruptedException
	{
		super.deleteResourceIdWithNonExistingResource(UUID.randomUUID().toString());
	}

	// ****************** Helper-Methoden *******************************

	static JsonObject createEntity(final String name, final String description)
	{
		//@formatter:off
		final var result = Json.createObjectBuilder()
				.add(field_name, name)
				.add(field_description, description)
				.add(field_firststate_id, "")
				.build();
		//@formatter:on
		return result;
	}

}
