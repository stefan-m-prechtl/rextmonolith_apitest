package de.esempe.rext.restapitest;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.bind.Jsonb;

public class Main
{
	protected HttpClient client;
	protected Jsonb jsonb;

	public static void main(final String[] args) // throws ScriptException
	{
		/*
		 * var factory = new ScriptEngineManager(); var engine = factory.getEngineByName("groovy"); engine.put("first", "HELLO"); engine.put("second", "Grooy World!");
		 * var result = (String) engine.eval("first.toLowerCase() + ' ' + second.toUpperCase()"); System.out.println(result);
		 */
		final var theApp = new Main();
		try
		{
			theApp.run();
		}
		catch (IOException | InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("REST-Daten erzeugt!");
	}

	public record PostResult(int status, String objid)
	{
		void validate()
		{
			if (this.status() != 204)
			{
				throw new IllegalStateException("Status-Code nach POST-Request ungleich 204!");
			}
		}
	}

	public void run() throws IOException, InterruptedException
	{
		this.client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).connectTimeout(Duration.ofSeconds(3)).build();

		// WS-Verfügbarkeit prüfen
		if (this.ping() != 200)
		{
			return;
		}

		// Alle Daten löschen
		this.deleteAllTransitions();
		this.deleteAllStatus();
		this.deleteAllWorkflows();
		this.deleteAllItems();
		this.deleteAllPriorities();
		this.deleteAllProjects();
		this.deleteAllUsers();

		// Benutzer anlegen
		final var iduser01 = this.postUser("SMP", "Stefan M.", "Prechtl");
		final var iduser02 = this.postUser("PRS", "Stefan", "Prechtl");
		final var iduser03 = this.postUser("EMU", "Eva", "Mustermann");

		// Projekt anlegen
		final var idproj01 = this.postProject("TST", "Testprojekt 1", iduser01.objid());

		// Prioritäten anlegen
		final var prio01 = this.postPriority("Hoch", "Sehr wichtig!", 75);
		final var jsonPrio01 = this.createJsonPriorityWithPrio(prio01.objid, "Hoch", "Sehr wichtig!", 75);
		final var prio02 = this.postPriority("Niedrig", "Unwichtig!", 25);
		final var jsonPrio02 = this.createJsonPriorityWithPrio(prio02.objid, "Niedrig", "Unwichtig!", 25);

		// TaggedValue anlegen
		// final var taggedValues = this.createJsonTaggedValues(Arrays.asList("TEST", "REST-TEST"));
		final var taggedValues = this.createJsonTaggedValues(Arrays.asList("REST-TEST"));

		// Items anlegen
		final var item01 = this.postItem(idproj01.objid, iduser01.objid, "Item 1", "Content of 1", jsonPrio01, taggedValues);
		final var item02 = this.postItem(idproj01.objid, iduser02.objid, "Item 2", "Content of 2", jsonPrio02, taggedValues);
		final var item03 = this.postItem(idproj01.objid, iduser02.objid, "Item 3", "Content of 3", jsonPrio01, taggedValues);

		// *** Workflow ***
		// Status anlegen
		final var status01 = this.postState("Erstellt", "Erstellt");
		final var status02 = this.postState("InBearbeitung", "In Bearbeitung");
		final var status03 = this.postState("Abgeschlossen", "Abgeschlossen");
		final var status04 = this.postState("Zurückgestellt", "Zurückgestellt");

		// Workflow anlegen
		final var workflow = this.postWorkflow("Basisworkflow", "Beschreibung für 'Basisworkflow'");

		// Transitionen anlegen
//		final var tranisition01 = this.postTransition(workflow, status01, status02, "bearbeiten (Erstellt -> In Bearbeitung)");
//		final var tranisition02 = this.postTransition(workflow, status02, status02, "überarbeiten (In Bearbeitung -> In Bearbeitung)");
//		final var tranisition03 = this.postTransition(workflow, status02, status03, "abschliessen (In Bearbeitung -> Abgeschlossen)");
//		final var tranisition04 = this.postTransition(workflow, status01, status04, "zurückstellen (Erstellt -> Zurückgestellt)");
//		final var tranisition05 = this.postTransition(workflow, status02, status04, "zurückstellen (In Bearbeitung -> Zurückgestellt)");
//		final var tranisition06 = this.postTransition(workflow, status04, status02, "bearbeiten (Zurückgestellt -> In Bearbeitung)");

	}

	// #### Ping
	private int ping() throws IOException, InterruptedException
	{
		final var baseURL = "http://localhost:8080/monolith/rext/usermgmt/ping";
		final var request = HttpRequest.newBuilder().uri(URI.create(baseURL)).header("Content-Type", "application/json").GET().build();
		final var response = this.client.send(request, HttpResponse.BodyHandlers.ofString());

		return response.statusCode();

	}

	// ********** Löschen **********
	int deleteAllTransitions() throws IOException, InterruptedException
	{
		return this.deleteAllResource("http://localhost:8080/monolith/rext/workflowmgmt/transitions?flag=all");
	}

	int deleteAllStatus() throws IOException, InterruptedException
	{
		return this.deleteAllResource("http://localhost:8080/monolith/rext/workflowmgmt/status?flag=all");
	}

	int deleteAllWorkflows() throws IOException, InterruptedException
	{
		return this.deleteAllResource("http://localhost:8080/monolith/rext/workflowmgmt/workflows?flag=all");
	}

	int deleteAllItems() throws IOException, InterruptedException
	{
		return this.deleteAllResource("http://localhost:8080/monolith/rext/itemmgmt/items?flag=all");
	}

	int deleteAllPriorities() throws IOException, InterruptedException
	{
		return this.deleteAllResource("http://localhost:8080/monolith/rext/itemmgmt/priorities?flag=all");
	}

	int deleteAllUsers() throws IOException, InterruptedException
	{
		return this.deleteAllResource("http://localhost:8080/monolith/rext/usermgmt/users?flag=all");
	}

	int deleteAllProjects() throws IOException, InterruptedException
	{
		return this.deleteAllResource("http://localhost:8080/monolith/rext/projectmgmt/projects?flag=all");
	}

	int deleteAllResource(final String url) throws IOException, InterruptedException
	{
		final var request = HttpRequest.newBuilder().uri(URI.create(url)).header("Content-Type", "application/json").DELETE().build();
		final var response = this.client.send(request, HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() != 204)
		{
			throw new IllegalStateException("Status-Code nach DELETE-Request: '" + url + "' ungleich 204!");
		}
		return response.statusCode();
	}

	// ********** Benutzer **********
	private PostResult postResource(final String url, final JsonObject jsonData) throws IOException, InterruptedException
	{

		final var request = HttpRequest.newBuilder().uri(URI.create(url)).header("Content-Type", "application/json").POST(BodyPublishers.ofString(jsonData.toString())).build();
		final var response = this.client.send(request, HttpResponse.BodyHandlers.ofString());

		final var link = response.headers().firstValue("link").get();
		final var uri = link.split(";")[0];
		final var uri2 = uri.substring(1, uri.length() - 1);
		final var objid = uri2.substring(uri2.lastIndexOf("/") + 1);

		final var status = response.statusCode();

		final var result = new PostResult(status, objid);
		result.validate();
		return result;
	}

	private PostResult postUser(final String login, final String firstname, final String lastname) throws IOException, InterruptedException
	{
		final var baseURL = "http://localhost:8080/monolith/rext/usermgmt/users";
		final var jsonUser = this.createJsonUser(login, firstname, lastname);

		return this.postResource(baseURL, jsonUser);
	}

	private JsonObject createJsonUser(final String login, final String firstname, final String lastname)
	{
		//@formatter:off
		final var result = Json.createObjectBuilder()
				//.add(field_id, user.getObjid().toString())
				.add("login", login)
				.add("clearpassword", "muster99")
				.add("firstname", firstname)
				.add("lastname", lastname)
				.build();
		//@formatter:on
		return result;
	}

	// ********** Projekte **********
	private PostResult postProject(final String projectname, final String description, final String userId) throws IOException, InterruptedException
	{
		final var baseURL = "http://localhost:8080/monolith/rext/projectmgmt/projects";
		final var jsonProject = this.createJsonProject(projectname, description, userId);

		return this.postResource(baseURL, jsonProject);
	}

	private JsonObject createJsonProject(final String projectname, final String description, final String userId)
	{
		//@formatter:off
		final var result = Json.createObjectBuilder()
				.add("projectname", projectname)
				.add("description", description)
				.add("owner", userId)
				.build();
		//@formatter:on
		return result;
	}

	// ********** Items, TaggedValues **********
	private PostResult postItem(
			final String projectId, final String creatorId, final String title, final String content, final JsonObject jsonPrio, final JsonArray jsonTaggedValues
	) throws IOException, InterruptedException
	{
		final var baseURL = "http://localhost:8080/monolith/rext/itemmgmt/items";
		final var jsonItem = this.createJsonItem(projectId, creatorId, title, content, jsonPrio, jsonTaggedValues);

		return this.postResource(baseURL, jsonItem);
	}

	private JsonObject createJsonItem(
			final String projectId, final String creatorId, final String title, final String content, final JsonObject jsonPrio, final JsonArray jsonTaggedValues
	)
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

	private JsonArray createJsonTaggedValues(final List<String> values)
	{
		final var builder = Json.createArrayBuilder();
		values.forEach(v -> builder.add(this.createJsonTaggedValue(v)));
		return builder.build();
	}

	private JsonObject createJsonTaggedValue(final String value)
	{
		//@formatter:off
		final var result = Json.createObjectBuilder()
				.add("valueid", UUID.randomUUID().toString())
				.add("value", value)
				.build();
		//@formatter:on
		return result;
	}

	// ********** Priorities **********
	private PostResult postPriority(final String name, final String description, final int value) throws IOException, InterruptedException
	{
		final var baseURL = "http://localhost:8080/monolith/rext/itemmgmt/priorities";
		final var jsonObj = this.createJsonPriority(name, description, value);
		return this.postResource(baseURL, jsonObj);
	}

	private JsonObject createJsonPriority(final String name, final String description, final int value)
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

	private JsonObject createJsonPriorityWithPrio(final String objId, final String name, final String description, final int value)
	{
		//@formatter:off
		final var result = Json.createObjectBuilder()
				.add("priorityid", objId)
				.add("name", name)
				.add("description", description)
				.add("value", value)
				.build();
		//@formatter:on
		return result;
	}

	// ********** State **********
	private PostResult postState(final String name, final String description) throws IOException, InterruptedException
	{
		final var baseURL = "http://localhost:8080/monolith/rext/workflowmgmt/status";
		final var jsonData = this.createJsonStatus(name, description);

		return this.postResource(baseURL, jsonData);
	}

	private JsonObject createJsonStatus(final String name, final String description)
	{
		//@formatter:off
		final var result = Json.createObjectBuilder()
				.add("name", name)
				.add("description", description)
				.build();
		//@formatter:on
		return result;
	}

	// ********** Workflow **********
	private PostResult postWorkflow(final String name, final String description) throws IOException, InterruptedException
	{
		final var baseURL = "http://localhost:8080/monolith/rext/workflowmgmt/workflows";
		final var jsonData = this.createJsonWorkflow(name, description);

		return this.postResource(baseURL, jsonData);
	}

	private JsonObject createJsonWorkflow(final String name, final String description)
	{
		//@formatter:off
		final var result = Json.createObjectBuilder()
				.add("name", name)
				.add("description", description)
				.add("firststateid", "")
				.build();
		//@formatter:on
		return result;
	}

	// ********** Transition **********
	private PostResult postTransition(final PostResult workflow, final PostResult statusFrom, final PostResult statusTo, final String description)
			throws IOException, InterruptedException
	{
		final var workflowObjId = workflow.objid();

		final var jsonStatusFrom = this.getResource("http://localhost:8080/monolith/rext/workflowmgmt/status" + statusFrom.objid());
		final var jsonStatusTo = this.getResource("http://localhost:8080/monolith/rext/workflowmgmt/status" + statusTo.objid());

		final var baseURL = "http://localhost:8080/monolith/rext/workflowmgmt/transitions";
		final var jsonData = this.createJsonTransition(workflowObjId, jsonStatusFrom, jsonStatusTo, description);

		return this.postResource(baseURL, jsonData);

	}

	protected String getResource(final String url) throws IOException, InterruptedException
	{

		final var request = HttpRequest.newBuilder().uri(URI.create(url)).header("Content-Type", "application/json").GET().build();
		final var response = this.client.send(request, HttpResponse.BodyHandlers.ofString());

		return response.body();

	}

	private JsonObject createJsonTransition(final String workflowObjId, final String jsonStatusFrom, final String jsonStatusTo, final String description)
	{

		var jsonReader = Json.createReader(new StringReader(jsonStatusFrom));
		final var statusFrom = jsonReader.readObject();
		jsonReader = Json.createReader(new StringReader(jsonStatusTo));
		final var statusTo = jsonReader.readObject();

		//@formatter:off
		final var result = Json.createObjectBuilder()
						.add("workflowobjid", workflowObjId)
						.add("fromstate", statusFrom)
						.add("tostate", statusTo)
						.add("description", description)
						.build();
		//@formatter:on

		// {
		// "transitionid":"80112b73-eb90-4c04-b952-6f0f80b8d575",
		// "workflowobjid":"73977e3e-dfab-4ac5-be2c-cf17cc3e7e64",
		// "fromstate":{"stateid":"06470263-2444-4e4a-8473-0396872ca1e8","name":"Neu","description":""},
		// "tostate":{"stateid":"ac3e21e5-e433-4694-9090-197aea6d39ec","name":"InBearbeitung","description":""},
		// "description":"Beschreibung für 'Transition'"
		// }

		return result;
	}

}
