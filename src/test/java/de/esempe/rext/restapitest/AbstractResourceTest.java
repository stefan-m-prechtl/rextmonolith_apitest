package de.esempe.rext.restapitest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.time.Duration;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

public abstract class AbstractResourceTest
{
	protected HttpClient client;
	protected String baseURL;

	protected AbstractResourceTest(final String baseURL)
	{
		this.baseURL = baseURL;
		client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).connectTimeout(Duration.ofSeconds(3)).build();
	}

	protected void optionResource(final String pathExtension) throws IOException, InterruptedException
	{
		// act
		final var request = HttpRequest.newBuilder().uri(URI.create(baseURL + pathExtension)).header("Content-Type", "application/json")
				.method("OPTIONS", HttpRequest.BodyPublishers.noBody()).build();
		final var res = client.send(request, HttpResponse.BodyHandlers.ofString());

		// assert
		//@formatter:off
		assertAll("Result of 'option",
				() -> assertThat(res).isNotNull(),
				() -> assertThat(res.statusCode()).isEqualTo(200),
				() -> assertThat(res.headers().firstValue("allow")).isNotEmpty(),
				() -> assertThat(res.headers().firstValue("allow").get()).contains("HEAD"),
				() -> assertThat(res.headers().firstValue("allow").get()).contains("GET"),
				() -> assertThat(res.headers().firstValue("allow").get()).contains("OPTIONS")
				);
		//@formatter:on
	}

	protected void headResource(final String pathExtension) throws IOException, InterruptedException
	{
		// act
		final var request = HttpRequest.newBuilder().uri(URI.create(baseURL + pathExtension)).header("Content-Type", "application/json")
				.method("HEAD", HttpRequest.BodyPublishers.noBody()).build();
		final var res = client.send(request, HttpResponse.BodyHandlers.ofString());

		// assert
		//@formatter:off
		assertAll("Result of 'head",
				() -> assertThat(res).isNotNull(),
				() -> assertThat(res.statusCode()).isEqualTo(200),
				() -> assertThat(res.headers().firstValue("content-type")).isNotEmpty(),
				() -> assertThat(res.headers().firstValue("content-type").get()).contains("application/json")
				);
		//@formatter:on
	}

	protected void deleteAllResource(final String pathExtension) throws IOException, InterruptedException
	{

		// act
		final var request = HttpRequest.newBuilder().uri(URI.create(baseURL + pathExtension)).header("Content-Type", "application/json").DELETE().build();
		final var res = client.send(request, HttpResponse.BodyHandlers.ofString());

		/// assert
		//@formatter:off
		assertAll("Result of 'delete",
				() -> assertThat(res).isNotNull(),
				() -> assertThat(res.statusCode()).isEqualTo(204)
				);
		//@formatter:on
	}

	protected void postResourceOk(final String pathExtension, final String payload) throws IOException, InterruptedException
	{
		// prepare: in konkreter Testklasse

		// act
		final var request = HttpRequest.newBuilder().uri(URI.create(baseURL + pathExtension)).header("Content-Type", "application/json").POST(BodyPublishers.ofString(payload))
				.build();
		final var res = client.send(request, HttpResponse.BodyHandlers.ofString());

		// assert
		//@formatter:off
		assertAll("Result of 'post",
				() -> assertThat(res).isNotNull(),
				() -> assertThat(res.statusCode()).isEqualTo(204)
				);
		//@formatter:on

		// "link" -->
		// "<http://localhost:8080/user_mgmt/rest/users/cfb1be3b-da31-4f09-8aae-27b0f92707e1>;
		// rel="self";
		// type="application/json"
		final var link = res.headers().allValues("link");

		//@formatter:off
		assertAll("Verify link",
				() -> assertThat(link).isNotEmpty(),
				() -> assertThat(link).contains(baseURL),
				() -> assertThat(link).contains("rel=\"self\""),
				() -> assertThat(link).contains("type=\"application/json\"")
				);
		//@formatter:on

	}

	protected void postResourceFail(final String pathExtension, final String payload) throws IOException, InterruptedException
	{
		// prepare: in konkreter Testklasse

		// act
		final var request = HttpRequest.newBuilder().uri(URI.create(baseURL + pathExtension)).header("Content-Type", "application/json").POST(BodyPublishers.ofString(payload))
				.build();
		final var res = client.send(request, HttpResponse.BodyHandlers.ofString());

		// assert
		//@formatter:off
		assertAll("Result of 'post",
				() -> assertThat(res).isNotNull(),
				() -> assertThat(res.statusCode()).isEqualTo(400),
				() -> assertThat(!res.headers().allValues("reason").isEmpty())
				);
		//@formatter:on

	}

	protected JsonArray getResource(final String pathExtension) throws IOException, InterruptedException
	{
		// act
		final var request = HttpRequest.newBuilder().uri(URI.create(baseURL + pathExtension)).header("Content-Type", "application/json").GET().build();
		final var res = client.send(request, HttpResponse.BodyHandlers.ofString());
		// assert
		//@formatter:off
		assertAll("Verify meta data",
				() -> assertThat(res).isNotNull(),
				() -> assertThat(res.statusCode()).isEqualTo(200),
				() -> assertThat(res.headers().allValues("content-type")).isNotEmpty(),
				() -> assertThat(res.headers().allValues("content-type")).contains("application/json")
				);
		//@formatter:on

		final var data = res.body();
		assertThat(data).isNotBlank();

		final var jsonObj = this.getJsonArrrayFromString(data);
		assertThat(jsonObj).isNotNull();

		return jsonObj;

	}

	protected JsonObject getSingleResource(final String pathExtension) throws IOException, InterruptedException
	{
		// act
		final var request = HttpRequest.newBuilder().uri(URI.create(baseURL + pathExtension)).header("Content-Type", "application/json").GET().build();
		final var res = client.send(request, HttpResponse.BodyHandlers.ofString());
		// assert
		//@formatter:off
		assertAll("Verify meta data",
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

		return jsonObj;

	}

	protected void optionResourceId(final String pathExtension) throws IOException, InterruptedException
	{
		// act
		final var request = HttpRequest.newBuilder().uri(URI.create(baseURL + pathExtension)).header("Content-Type", "application/json")
				.method("OPTIONS", HttpRequest.BodyPublishers.noBody()).build();
		final var res = client.send(request, HttpResponse.BodyHandlers.ofString());

		// assert
		//@formatter:off
		assertAll("Result of 'option",
				() -> assertThat(res).isNotNull(),
				() -> assertThat(res.statusCode()).isEqualTo(200),
				() -> assertThat(res.headers().firstValue("allow")).isNotEmpty(),
				() -> assertThat(res.headers().firstValue("allow").get()).contains("HEAD"),
				() -> assertThat(res.headers().firstValue("allow").get()).contains("GET"),
				() -> assertThat(res.headers().firstValue("allow")).contains("PUT"),
				() -> assertThat(res.headers().firstValue("allow")).contains("DELETE"),
				() -> assertThat(res.headers().firstValue("allow").get()).contains("OPTIONS")
				);
		//@formatter:on
	}

	protected void headResourceId(final String pathExtension) throws IOException, InterruptedException
	{
		// act
		final var request = HttpRequest.newBuilder().uri(URI.create(baseURL + pathExtension)).header("Content-Type", "application/json")
				.method("HEAD", HttpRequest.BodyPublishers.noBody()).build();
		final var res = client.send(request, HttpResponse.BodyHandlers.ofString());

		// assert
		//@formatter:off
		assertAll("Result of 'head",
				() -> assertThat(res).isNotNull(),
				() -> assertThat(res.statusCode()).isEqualTo(200),
				() -> assertThat(res.headers().allValues("content-type")).isNotEmpty(),
				() -> assertThat(res.headers().firstValue("content-type")).contains("application/json")
				);
		//@formatter:on
	}

	protected JsonObject getResourceId(final String pathExtension) throws IOException, InterruptedException
	{
		// act
		final var request = HttpRequest.newBuilder().uri(URI.create(baseURL + pathExtension)).header("Content-Type", "application/json").GET().build();
		final var res = client.send(request, HttpResponse.BodyHandlers.ofString());

		// assert
		//@formatter:off
		assertAll("Result of 'head",
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

		return jsonObj;
	}

	protected void putResourceId(final String pathExtension, final String payload) throws IOException, InterruptedException
	{
		// act
		final var request = HttpRequest.newBuilder().uri(URI.create(baseURL + pathExtension)).header("Content-Type", "application/json").PUT(BodyPublishers.ofString(payload))
				.build();
		final var res = client.send(request, HttpResponse.BodyHandlers.ofString());
		// assert
		// @formatter:off
		assertAll("Result of 'put",
				() -> assertThat(res).isNotNull(),
				() -> assertThat(res.statusCode()).isEqualTo(204)
				);
		// @formatter:on
	}

	protected void deleteResourceIdWithExistingResource(final String pathExtension) throws IOException, InterruptedException
	{
		// act
		final var request = HttpRequest.newBuilder().uri(URI.create(baseURL + pathExtension)).header("Content-Type", "application/json").DELETE().build();
		final var res = client.send(request, HttpResponse.BodyHandlers.ofString());

		// assert
		//@formatter:off
		assertAll("Result of 'delete",
				() -> assertThat(res).isNotNull(),
				() -> assertThat(res.statusCode()).isEqualTo(204)
				);
		//@formatter:on
	}

	protected void deleteResourceIdWithNonExistingResource(final String pathExtension) throws IOException, InterruptedException
	{
		// act
		final var request = HttpRequest.newBuilder().uri(URI.create(baseURL + pathExtension)).header("Content-Type", "application/json").DELETE().build();
		final var res = client.send(request, HttpResponse.BodyHandlers.ofString());

		// assert
		//@formatter:off
		assertAll("Result of 'delete",
				() -> assertThat(res).isNotNull(),
				() -> assertThat(res.statusCode()).isEqualTo(404)
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

}
