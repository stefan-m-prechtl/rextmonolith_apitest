package de.esempe.rext.restapitest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.core.MediaType;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@DisplayName("REST-API Test für Project-Resource")
@TestClassOrder(20)
@TestMethodOrder(OrderAnnotation.class)
public class ProjectResourceTest extends AbstractResourceTest
{
	final static String field_id = "projectid";
	final static String field_name = "projectname";
	final static String field_description = "description";
	final static String field_owner = "owner";

	final static String baseURL = "http://localhost:8080/monolith/rext/projectmgmt/projects";

	// Echte Project-ID vom der ersten Project aus GET-Abruf - wird für weitere Aufrufe benötigt
	static String realProjectID;

	@BeforeAll
	static void setUpBeforeClass() throws Exception
	{
		target = client.target(baseURL);
	}

	@Order(10)
	@DisplayName("Befehl 'HTTP OPTION' für: " + baseURL)
	// HTTP OPTION ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void optionProjects()
	{
		super.optionResource();
	}

	@Order(20)
	@DisplayName("Befehl 'HTTP HEAD' für: " + baseURL)
	// HTTP HEAD ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void headProjects()
	{
		super.headResource();
	}

	@Test
	@Order(30)
	@DisplayName("Befehl 'HTTP DELETE ' für: " + baseURL + "?flag=all")
	void deleteAllProjects()
	{
		super.deleteAllResource();
	}

	@Test
	@Order(35)
	@DisplayName("Befehl 'HTTP POST' für: " + baseURL)
	void postProject()
	{
		// prepare
		final var jsonProject = this.createNewProject("TST", "Testprojekt 1");

		// act
		super.postResourceOk(jsonProject, baseURL);
	}

	@Order(40)
	@DisplayName("Befehl 'HTTP GET' für: " + baseURL)
	// HTTP GET ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void getProjects()
	{
		final var jsonProject = super.getResource();

		//@formatter:off
		assertAll("Verify content",
				() -> assertThat(jsonProject.containsKey(field_id)).isTrue(),
				() -> assertThat(jsonProject.containsKey(field_name)).isTrue(),
				() -> assertThat(jsonProject.containsKey(field_description)).isTrue()
				);
		//@formatter:on

		// echte Project-Id für nachfolgende Test intern vermerken
		ProjectResourceTest.realProjectID = jsonProject.getString(field_id);
	}

	@Order(50)
	@DisplayName("Befehl 'HTTP OPTION' für: " + baseURL + "/id")
	// HTTP OPTION ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void optionProjectsId()
	{
		super.optionResourceId(ProjectResourceTest.realProjectID);
	}

	@Order(60)
	@DisplayName("Befehl 'HTTP HEAD' für: " + baseURL + "/id")
	// HTTP HEAD ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void headProjectsId()
	{
		super.headResourceId(ProjectResourceTest.realProjectID);
	}

	@Order(70)
	@DisplayName("Befehl 'HTTP GET' für: " + baseURL + "/id")
	// HTTP GET ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void getProjectsId()
	{
		final var jsonProject = super.getResourceId(ProjectResourceTest.realProjectID);

		//@formatter:off
		assertAll("Verify content",
				() -> assertThat(jsonProject.containsKey(field_id)).isTrue(),
				() -> assertThat(jsonProject.containsKey(field_name)).isTrue(),
				() -> assertThat(jsonProject.containsKey(field_description)).isTrue()
				);
		//@formatter:on

	}

	@Order(80)
	@DisplayName("Befehl 'HTTP PUT' für: " + baseURL + "/id")
	// HTTP PUT ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void putProjectsId()
	{
		// prepare
		var jsonProjectBeforeUpdate = this.getResourceById(ProjectResourceTest.realProjectID);
		final var builder = Json.createPatchBuilder();
		jsonProjectBeforeUpdate = builder.replace("/description", "Testprojekt ABC").build().apply(jsonProjectBeforeUpdate);
		// act & assert
		super.putResourceId(ProjectResourceTest.realProjectID, jsonProjectBeforeUpdate);
	}

	@Order(90)
	@DisplayName("Befehl 'HTTP GET' für: " + baseURL + "/search")
	// HTTP GET ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void getProjectsSearch()
	{
		// act
		invocationBuilder = target.path("/search").queryParam("name", "TST").request(MediaType.APPLICATION_JSON);
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
		final var jsonProject = this.getJsonObjectFromString(jsonString);
		assertThat(jsonProject).isNotNull();

	}

	@Test
	@Order(100)
	@DisplayName("Befehl 'HTTP DELETE' für: " + baseURL + "/id")
	void deleteProjectsIdWithExistingProject()
	{
		// prepare: get object id from "posted" user from previous test
		invocationBuilder = target.path("/search").queryParam("name", "TST").request(MediaType.APPLICATION_JSON);
		final var resSearch = invocationBuilder.get();
		final var resObj = this.createFromString(resSearch.readEntity(String.class));
		final var objId = resObj.getString("projectid");

		// act
		super.deleteResourceIdWithExistingResource(objId);
	}

	@Test
	@Order(110)
	@DisplayName("Befehl 'HTTP DELETE' für: " + baseURL + "/id")
	void deleteProjectsIdWithNonExistingProject()
	{
		super.deleteResourceIdWithNonExistingResource();
	}

	private JsonObject createNewProject(final String name, final String description)
	{
		final var objId = UUID.randomUUID().toString();

		//@formatter:off
		final var result = Json.createObjectBuilder()
				//.add(field_id, user.getObjid().toString())
				.add(field_name, name)
				.add(field_description, description)
				.add(field_owner, objId)
				.build();
		//@formatter:on
		return result;
	}
}
