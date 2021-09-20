package de.esempe.rext.restapitest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.io.StringReader;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

abstract class AbstractResourceTest
{
	protected static Client client = null;
	protected static WebTarget target = null;
	protected static Invocation.Builder invocationBuilder = null;

	@BeforeAll
	static void setUpBeforeBaseClass() throws Exception
	{
		client = ClientBuilder.newBuilder().connectTimeout(100, TimeUnit.MILLISECONDS).readTimeout(2, TimeUnit.SECONDS).build();
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception
	{
		client.close();
	}

	void optionResource()
	{
		// act
		invocationBuilder = target.request(MediaType.APPLICATION_JSON);
		final var res = invocationBuilder.options();

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

	void headResource()
	{
		// act
		invocationBuilder = target.request(MediaType.APPLICATION_JSON);
		final var res = invocationBuilder.head();

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

	void deleteAllResource()
	{

		// act
		invocationBuilder = target.queryParam("flag", "all").request(MediaType.APPLICATION_JSON);
		final var res = invocationBuilder.delete();

		/// assert
		//@formatter:off
		assertAll("Result of 'delete",
				() -> assertThat(res).isNotNull(),
				() -> assertThat(res.getStatus()).isEqualTo(204)
				);
		//@formatter:on
	}

	void postResourceOk(JsonObject jsonResource, String baseURL)
	{
		// prepare: in konkreter Testklasse

		// act
		invocationBuilder = target.request(MediaType.APPLICATION_JSON);
		final var res = invocationBuilder.post(Entity.json(jsonResource.toString()));

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
		final var link = res.getHeaderString("link");

		//@formatter:off
		assertAll("Verify link",
				() -> assertThat(link).isNotBlank(),
				() -> assertThat(link).contains(baseURL),
				() -> assertThat(link).contains("rel=\"self\""),
				() -> assertThat(link).contains("type=\"application/json\"")
				);
		//@formatter:on

	}

	void postResourceFail(JsonObject jsonResource, String baseURL)
	{
		// prepare: in konkreter Testklasse

		// act
		invocationBuilder = target.request(MediaType.APPLICATION_JSON);
		final var res = invocationBuilder.post(Entity.json(jsonResource.toString()));

		// assert
		//@formatter:off
		assertAll("Result of 'post",
				() -> assertThat(res).isNotNull(),
				() -> assertThat(res.getStatus()).isEqualTo(400),
				() -> assertThat(!res.getHeaderString("reason").isEmpty())
				);
		//@formatter:on

	}

	JsonObject getResource()
	{
		// act
		invocationBuilder = target.request(MediaType.APPLICATION_JSON);
		final var res = invocationBuilder.get();

		// assert
		//@formatter:off
		assertAll("Verify meta data",
				() -> assertThat(res).isNotNull(),
				() -> assertThat(res.getStatus()).isEqualTo(200),
				() -> assertThat(res.getHeaderString("content-type")).isNotBlank(),
				() -> assertThat(res.getHeaderString("content-type")).contains("application/json")
				);
		//@formatter:on

		final var jsonString = res.readEntity(String.class);
		assertThat(jsonString).isNotBlank();
		final var jsonArray = this.getJsonArrrayFromString(jsonString);
		assertThat(jsonArray).isNotNull();
		assertThat(jsonArray.size()).isGreaterThan(0);
		final var jsonResource = jsonArray.getJsonObject(0);
		assertThat(jsonResource).isNotNull();

		return jsonResource;

	}

	void optionResourceId(String resourceID)
	{
		// act
		invocationBuilder = target.path("/{id}").resolveTemplate("id", resourceID).request(MediaType.APPLICATION_JSON);
		final var res = invocationBuilder.options();

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

	void headResourceId(String resourceID)
	{
		// act
		invocationBuilder = target.path("/{id}").resolveTemplate("id", resourceID).request(MediaType.APPLICATION_JSON);
		final var res = invocationBuilder.head();

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

	JsonObject getResourceId(String resourceID)
	{
		// act
		invocationBuilder = target.path("/{id}").resolveTemplate("id", resourceID).request(MediaType.APPLICATION_JSON);
		final var res = invocationBuilder.get();

		// assert
		//@formatter:off
		assertAll("Result of 'head",
				() -> assertThat(res).isNotNull(),
				() -> assertThat(res.getStatus()).isEqualTo(200),
				() -> assertThat(res.getHeaderString("content-type")).isNotBlank(),
				() -> assertThat(res.getHeaderString("content-type")).contains("application/json")
				);
		//@formatter:on

		final var jsonString = res.readEntity(String.class);
		assertThat(jsonString).isNotBlank();
		final var jsonResource = this.getJsonObjectFromString(jsonString);
		assertThat(jsonResource).isNotNull();

		return jsonResource;
	}

	void putResourceId(String resourceID, JsonObject jsonResourceBeforeUpdate)
	{
		// act
		invocationBuilder = target.path("/{id}").resolveTemplate("id", resourceID).request(MediaType.APPLICATION_JSON);
		final var res = invocationBuilder.put(Entity.json(jsonResourceBeforeUpdate.toString()));

		// assert
		// @formatter:off
		assertAll("Result of 'put",
				() -> assertThat(res).isNotNull(),
				() -> assertThat(res.getStatus()).isEqualTo(204)
				);
		// @formatter:on
	}

	void deleteResourceIdWithExistingResource(String objId)
	{
		// act
		invocationBuilder = target.path("/{id}").resolveTemplate("id", objId).request(MediaType.APPLICATION_JSON);
		final var res = invocationBuilder.delete();

		// assert
		//@formatter:off
		assertAll("Result of 'delete",
				() -> assertThat(res).isNotNull(),
				() -> assertThat(res.getStatus()).isEqualTo(204)
				);
		//@formatter:on
	}

	void deleteResourceIdWithNonExistingResource()
	{
		// act
		invocationBuilder = target.path("/{id}").resolveTemplate("id", UUID.randomUUID().toString()).request(MediaType.APPLICATION_JSON);
		final var res = invocationBuilder.delete();

		// assert
		//@formatter:off
		assertAll("Result of 'delete",
				() -> assertThat(res).isNotNull(),
				() -> assertThat(res.getStatus()).isEqualTo(404)
				);
		//@formatter:on
	}

	// ****************** Helper-Methoden **********************************************

	protected JsonArray getJsonArrrayFromString(final String jsonString)
	{
		final var jsonReader = Json.createReader(new StringReader(jsonString));
		final var result = jsonReader.readArray();

		return result;
	}

	protected JsonObject getJsonObjectFromString(final String jsonString)
	{
		final var jsonReader = Json.createReader(new StringReader(jsonString));
		final var result = jsonReader.readObject();
		return result;
	}

	protected JsonObject getResourceById(final String objid)
	{
		invocationBuilder = target.path("/{id}").resolveTemplate("id", objid).request(MediaType.APPLICATION_JSON);
		final var res = invocationBuilder.get();
		final var jsonString = res.readEntity(String.class);
		final var jsonObj = this.getJsonObjectFromString(jsonString);

		return jsonObj;
	}

	protected JsonObject createFromString(final String jsonString)
	{
		final var jsonReader = Json.createReader(new StringReader(jsonString));
		final var result = jsonReader.readObject();

		return result;
	}

}
