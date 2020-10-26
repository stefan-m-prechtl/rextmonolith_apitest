package de.esempe.rext.restapitest;

import java.io.StringReader;
import java.util.concurrent.TimeUnit;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

class AbstractResourceTest
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

	protected JsonArray getJsonArrrayFromString(final String jsonString)
	{
		final JsonReader jsonReader = Json.createReader(new StringReader(jsonString));
		final JsonArray result = jsonReader.readArray();
		return result;
	}

	protected JsonObject getJsonObjectFromString(final String jsonString)
	{
		final JsonReader jsonReader = Json.createReader(new StringReader(jsonString));
		final JsonObject result = jsonReader.readObject();
		return result;
	}

	protected JsonObject getResourceById(final String objid)
	{
		invocationBuilder = target.path("/{id}").resolveTemplate("id", objid).request(MediaType.APPLICATION_JSON);
		final Response res = invocationBuilder.get();
		final String jsonString = res.readEntity(String.class);
		final JsonObject jsonObj = this.getJsonObjectFromString(jsonString);

		return jsonObj;

	}

	protected JsonObject createFromString(final String jsonString)
	{
		final JsonReader jsonReader = Json.createReader(new StringReader(jsonString));
		final JsonObject result = jsonReader.readObject();
		return result;
	}

}
