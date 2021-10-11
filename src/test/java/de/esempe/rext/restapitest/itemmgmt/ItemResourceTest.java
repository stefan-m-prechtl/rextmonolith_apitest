package de.esempe.rext.restapitest.itemmgmt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.core.MediaType;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.RepeatedTest;
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

	// Echte ItemID vom ersten Item aus GET-Abruf - wird für weitere Aufrufe benötigt
	static String realItemID;

	@BeforeAll
	static void setUpBeforeClass() throws Exception
	{
		target = client.target(baseURL);
	}

	@Order(10)
	@DisplayName("Befehl 'HTTP OPTION' für: " + baseURL)
	// HTTP OPTION ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void optionItems()
	{
		super.optionResource();
	}

	@Order(20)
	@DisplayName("Befehl 'HTTP HEAD' für: " + baseURL)
	// HTTP HEAD ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void headItems()
	{
		super.headResource();
	}

	@Test
	@Order(30)
	@DisplayName("Befehl 'HTTP DELETE ' für: " + baseURL + "?flag=all")
	void deleteAllItems()
	{
		super.deleteAllResource();
	}

	@Test
	@Order(35)
	@DisplayName("Befehl 'HTTP POST' für: " + baseURL)
	void postItem()
	{
		// prepare
		final var projectId = UUID.randomUUID().toString();
		final var creatorId = UUID.randomUUID().toString();
		final JsonArray taggedValues = this.createJsonTaggedValues(Arrays.asList("TEST", "REST-TEST"));
		final JsonObject priority = this.createJsonPriority("Hoch", "Beschreibung für Hoch", 75);
		final var jsonItem = this.createNewItem("Tests ertellen", "Weiter Unit-Tests schreiben", projectId, creatorId, priority, taggedValues);

		// act
		super.postResourceOk(jsonItem, baseURL);
	}

	@Order(40)
	@DisplayName("Befehl 'HTTP GET' für: " + baseURL)
	// HTTP GET ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void getItems()
	{
		final var jsonItem = super.getResource();

		//@formatter:off
		assertAll("Verify content",
				() -> assertThat(jsonItem.containsKey(field_id)).isTrue(),
				() -> assertThat(jsonItem.containsKey(field_title)).isTrue(),
				() -> assertThat(jsonItem.containsKey(field_content)).isTrue(),
				() -> assertThat(jsonItem.containsKey(field_project)).isTrue(),
				() -> assertThat(jsonItem.containsKey(field_creator)).isTrue()
				);
		//@formatter:on

		// echte Item-Id für nachfolgende Test intern vermerken
		ItemResourceTest.realItemID = jsonItem.getString(field_id);
	}

	@Order(50)
	@DisplayName("Befehl 'HTTP OPTION' für: " + baseURL + "/id")
	// HTTP OPTION ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void optionItemsId()
	{
		super.optionResourceId(ItemResourceTest.realItemID);

	}

	@Order(60)
	@DisplayName("Befehl 'HTTP HEAD' für: " + baseURL + "/id")
	// HTTP HEAD ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void headItemsId()
	{
		super.headResourceId(ItemResourceTest.realItemID);
	}

	@Order(70)
	@DisplayName("Befehl 'HTTP GET' für: " + baseURL + "/id")
	// HTTP GET ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void getItemsId()
	{
		final var jsonItem = super.getResourceId(ItemResourceTest.realItemID);

		//@formatter:off
		assertAll("Verify content",
				() -> assertThat(jsonItem.containsKey(field_id)).isTrue(),
				() -> assertThat(jsonItem.containsKey(field_title)).isTrue(),
				() -> assertThat(jsonItem.containsKey(field_content)).isTrue(),
				() -> assertThat(jsonItem.containsKey(field_project)).isTrue(),
				() -> assertThat(jsonItem.containsKey(field_creator)).isTrue()
				);
		//@formatter:on

	}

	@Order(80)
	@DisplayName("Befehl 'HTTP PUT' für: " + baseURL + "/id")
	// HTTP PUT ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void putItemsId()
	{
		// prepare
		var jsonItemBeforeUpdate = this.getResourceById(ItemResourceTest.realItemID);
		final var builder = Json.createPatchBuilder();
		jsonItemBeforeUpdate = builder.replace("/content", "Weiter Unit-Tests implementieren").build().apply(jsonItemBeforeUpdate);
		// act & assert
		super.putResourceId(ItemResourceTest.realItemID, jsonItemBeforeUpdate);

	}

	@Order(90)
	@DisplayName("Befehl 'HTTP GET' für: " + baseURL + "/search")
	// HTTP GET ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void getItemsSearch()
	{
		// act
		invocationBuilder = target.path("/search").queryParam("title", "Tests ertellen").request(MediaType.APPLICATION_JSON);
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
		final var jsonItem = this.getJsonObjectFromString(jsonString);
		assertThat(jsonItem).isNotNull();

	}

	@Test
	@Order(100)
	@DisplayName("Befehl 'HTTP DELETE' für: " + baseURL + "/id")
	void deleteItemsIdWithExistingItem()
	{
		// prepare: get object id from "posted" user from previous test
		invocationBuilder = target.path("/search").queryParam("title", "Tests ertellen").request(MediaType.APPLICATION_JSON);
		final var resSearch = invocationBuilder.get();
		final var resObj = this.createFromString(resSearch.readEntity(String.class));
		final var objId = resObj.getString(field_id);

		// act
		super.deleteResourceIdWithExistingResource(objId);
	}

	@Test
	@Order(110)
	@DisplayName("Befehl 'HTTP DELETE' für: " + baseURL + "/id")
	void deleteItemsIdWithNonExistingItem()
	{
		super.deleteResourceIdWithNonExistingResource();
	}

	// ****************** Helper-Methoden **********************************************

	private JsonObject createJsonItem(final String projectId, final String creatorId, final String title, final String content, JsonObject jsonPrio, JsonArray jsonTaggedValues)
	{
		//@formatter:off
		final var result = Json.createObjectBuilder()
				.add("title", title)
				.add("content", content)
				.add("projektobjid", projectId)
				.add("creatorobjid", creatorId)
				.add("priority", jsonPrio)
				.add("taggedvalues", jsonTaggedValues)
				.build();
		//@formatter:on
		return result;
	}

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
