package de.esempe.rext.restapitest.itemmgmt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
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


@DisplayName("REST-API Test für Item-Resource")
@TestClassOrder(40)
@TestMethodOrder(OrderAnnotation.class)
//@Disabled
public class ItemResourceTest extends AbstractResourceTest
{
	final static String field_id = "itemid";
	final static String field_title = "title";
	final static String field_content = "content";
	final static String field_project = "projektobjid";
	final static String field_creator = "creatorobjid";
	final static String field_priority = "priority";
	final static String field_taggedvalues = "taggedvalues";

	final static String baseURL = "http://localhost:8080/monolith/rext/itemmgmt/items";

	// Echte Objekt-ID vom GET-Abruf - wird für weitere Aufrufe benötigt
	static String realEntityID;

	public ItemResourceTest()
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
		final var projectId = UUID.randomUUID().toString();
		final var creatorId = UUID.randomUUID().toString();
		final JsonArray taggedValues = this.createJsonTaggedValues(Arrays.asList("TEST", "REST-TEST"));
		final JsonObject priority = this.createJsonPriority("Hoch", "Beschreibung für Hoch", 75);
		final var jsonItem = this.createNewItem("Tests ertellen", "Weiter Unit-Tests schreiben", projectId, creatorId, priority, taggedValues);
	
		// act
		super.postResourceOk("", jsonItem.toString());
	}

	

	@Order(40)
	@DisplayName("Befehl 'HTTP GET' für: " + baseURL)
	// HTTP GET ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void get() throws IOException, InterruptedException
	{
		final JsonArray items = super.getResource("");
		final JsonObject jsonItem = (JsonObject) items.get(0);

		//@formatter:off
		assertAll("Verify content",
				() -> assertThat(jsonItem.containsKey(field_id)).isTrue(),
				() -> assertThat(jsonItem.containsKey(field_title)).isTrue(),
				() -> assertThat(jsonItem.containsKey(field_content)).isTrue(),
				() -> assertThat(jsonItem.containsKey(field_project)).isTrue(),
				() -> assertThat(jsonItem.containsKey(field_creator)).isTrue());
		//@formatter:on

		// echte item-Id für nachfolgende Test intern vermerken
		ItemResourceTest.realEntityID = jsonItem.getString(field_id);
	}

	@Order(50)
	@DisplayName("Befehl 'HTTP OPTION' für: " + baseURL + "/id")
	// HTTP OPTION ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void optionWithId() throws IOException, InterruptedException
	{
		super.optionResourceId(ItemResourceTest.realEntityID);

	}

	@Order(60)
	@DisplayName("Befehl 'HTTP HEAD' für: " + baseURL + "/id")
	// HTTP HEAD ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void headWithId() throws IOException, InterruptedException
	{
		super.headResourceId(ItemResourceTest.realEntityID);
	}

	@Order(70)
	@DisplayName("Befehl 'HTTP GET' für: " + baseURL + "/id")
	// HTTP GET ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void getWithId() throws IOException, InterruptedException
	{
		final var jsonItem = super.getSingleResource(ItemResourceTest.realEntityID);

		//@formatter:off
		assertAll("Verify content",
				() -> assertThat(jsonItem.containsKey(field_id)).isTrue(),
				() -> assertThat(jsonItem.containsKey(field_title)).isTrue(),
				() -> assertThat(jsonItem.containsKey(field_content)).isTrue(),
				() -> assertThat(jsonItem.containsKey(field_project)).isTrue(),
				() -> assertThat(jsonItem.containsKey(field_creator)).isTrue());
		//@formatter:on

	}

	@Order(80)
	@DisplayName("Befehl 'HTTP PUT' für: " + baseURL + "/id")
	// HTTP PUT ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void put() throws IOException, InterruptedException
	{
		// prepare
		var jsonitemBeforeUpdate = this.getSingleResource(ItemResourceTest.realEntityID);
		final var builder = Json.createPatchBuilder();
		jsonitemBeforeUpdate = builder.replace("/firstname", "Etienne").build().apply(jsonitemBeforeUpdate);
		// act & assert
		super.putResourceId(ItemResourceTest.realEntityID, jsonitemBeforeUpdate.toString());

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
		super.deleteResourceIdWithExistingResource(ItemResourceTest.realEntityID);
	}

	@Test
	@Order(110)
	@DisplayName("Befehl 'HTTP DELETE' für: " + baseURL + "/id")
	void deleteWithNonExistingEntity() throws IOException, InterruptedException
	{
		super.deleteResourceIdWithNonExistingResource(UUID.randomUUID().toString());
	}

	// ****************** Helper-Methoden **********************************************

	private JsonArray createJsonTaggedValues(List<String> values)
	{
		var builder = Json.createArrayBuilder();
		values.forEach(v -> builder.add(this.createJsonTaggedValue(v)));
		return builder.build();
	}

	private JsonObject createJsonTaggedValue(String value)
	{
		//@formatter:off
		final var result = Json.createObjectBuilder()
				.add("valueid", UUID.randomUUID().toString())
				.add("value", value)
				.build();
		//@formatter:on
		return result;
	}

	private JsonObject createJsonPriority(final String name, String description, final int value)
	{
		//@formatter:off
		final var result = Json.createObjectBuilder()
				.add("name", name)
				.add("description", description)
				.add("value", value)
				.build();
		//@formatter:on
		return result;
	}

	private JsonObject createNewItem(final String title, final String content, final String projectId, String creatorId, JsonObject jsonPrio, JsonArray jsonTaggedValues)
	{
		//@formatter:off
		final var result = Json.createObjectBuilder()
				.add(field_title, title)
				.add(field_content, content)
				.add(field_project, projectId)
				.add(field_creator, creatorId)
				.add(field_priority, jsonPrio)
				.add(field_taggedvalues, jsonTaggedValues)
				.build();
		//@formatter:on
		return result;
	}

	private JsonObject createJsonPriorityWithPrio()
	{
		//@formatter:off
		final var result = Json.createObjectBuilder()
				//.add("priorityid", objId)
				.add("name", "Hoch")
				.add("description", "Sehr wichtig")
				.add("value", 75)
				.build();
		//@formatter:on
		return result;
	}

}
