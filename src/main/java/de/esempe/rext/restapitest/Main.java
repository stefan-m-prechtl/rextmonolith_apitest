package de.esempe.rext.restapitest;

import java.util.concurrent.TimeUnit;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;

public class Main
{
	private Client client;

	public static void main(String[] args)
	{
		final var theApp = new Main();
		theApp.run();
		System.out.println("REST-Daten erzeugt!");
	}

	//@formatter:off
	public record PostResult(int status, String objid){}
	//@formatter:on

	public void run()
	{
		this.client = ClientBuilder.newBuilder().connectTimeout(100, TimeUnit.MILLISECONDS).readTimeout(2, TimeUnit.SECONDS).build();

		// WS-Verfügbarkeit prüfen
		if (this.ping() != 200)
		{
			return;
		}

		// Alle Daten löschen
		this.deleteAllStatus();
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

		// Items anlegen
		final var item01 = this.postItem(idproj01.objid, iduser01.objid, "Item 1", "Content of 1", jsonPrio01);
		final var item02 = this.postItem(idproj01.objid, iduser02.objid, "Item 2", "Content of 2", jsonPrio02);
		final var item03 = this.postItem(idproj01.objid, iduser02.objid, "Item 3", "Content of 3", jsonPrio01);

		// *** Workflow ***
		// Status anlegen
		final var status01 = this.postState("Erstellt", "Erstellt");
		final var status02 = this.postState("InBearbeitung", "In Bearbeitung");
		final var status03 = this.postState("Abgeschlossen", "Abgeschlossen");

		this.client.close();
	}

	// #### Ping

	private int ping()
	{
		final var baseURL = "http://localhost:8080/monolith/rext/usermgmt/ping";
		final var invocationBuilder = this.createBuilder(baseURL);
		final var res = invocationBuilder.get();

		return res.getStatus();

	}

	// ********** Löschen **********
	int deleteAllStatus()
	{
		return this.deleteAllResource("http://localhost:8080/monolith/rext/workflowmgmt/status");
	}

	int deleteAllItems()
	{
		return this.deleteAllResource("http://localhost:8080/monolith/rext/itemmgmt/items");
	}

	int deleteAllPriorities()
	{
		return this.deleteAllResource("http://localhost:8080/monolith/rext/itemmgmt/priorities");
	}

	int deleteAllUsers()
	{
		return this.deleteAllResource("http://localhost:8080/monolith/rext/usermgmt/users");
	}

	int deleteAllProjects()
	{
		return this.deleteAllResource("http://localhost:8080/monolith/rext/projectmgmt/projects");
	}

	int deleteAllResource(String url)
	{
		final var invocationBuilder = this.client.target(url).queryParam("flag", "all").request(MediaType.APPLICATION_JSON);
		final var res = invocationBuilder.delete();
		return res.getStatus();
	}

	// ********** Benutzer **********

	private PostResult postUser(String login, String firstname, String lastname)
	{
		final var baseURL = "http://localhost:8080/monolith/rext/usermgmt/users";
		final var invocationBuilder = this.createBuilder(baseURL);

		final var jsonUser = this.createJsonUser(login, firstname, lastname);
		final var res = invocationBuilder.post(Entity.json(jsonUser.toString()));

		final var selfLink = res.getLink("self");
		final var uri = selfLink.getUri().getPath();
		final var objid = uri.substring(uri.lastIndexOf('/') + 1);
		final var status = res.getStatus();

		return new PostResult(status, objid);

	}

	private JsonObject createJsonUser(final String login, final String firstname, final String lastname)
	{
		//@formatter:off
		final var result = Json.createObjectBuilder()
				//.add(field_id, user.getObjid().toString())
				.add("userlogin", login)
				.add("firstname", firstname)
				.add("lastname", lastname)
				.build();
		//@formatter:on
		return result;
	}

	// ********** Projekte **********
	private PostResult postProject(String projectname, String description, String userId)
	{
		final var baseURL = "http://localhost:8080/monolith/rext/projectmgmt/projects";
		final var invocationBuilder = this.createBuilder(baseURL);

		final var jsonProject = this.createJsonProject(projectname, description, userId);
		final var res = invocationBuilder.post(Entity.json(jsonProject.toString()));

		final var selfLink = res.getLink("self");
		final var uri = selfLink.getUri().getPath();
		final var objid = uri.substring(uri.lastIndexOf('/') + 1);
		final var status = res.getStatus();

		return new PostResult(status, objid);

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

	// ********** Items **********
	private PostResult postItem(final String projectId, final String creatorId, final String title, final String content, JsonObject jsonPrio)
	{
		final var baseURL = "http://localhost:8080/monolith/rext/itemmgmt/items";
		final var invocationBuilder = this.createBuilder(baseURL);

		final var jsonItem = this.createJsonItem(projectId, creatorId, title, content, jsonPrio);
		final var res = invocationBuilder.post(Entity.json(jsonItem.toString()));

		final var selfLink = res.getLink("self");
		final var uri = selfLink.getUri().getPath();
		final var objid = uri.substring(uri.lastIndexOf('/') + 1);
		final var status = res.getStatus();

		return new PostResult(status, objid);

	}

	private JsonObject createJsonItem(final String projectId, final String creatorId, final String title, final String content, JsonObject jsonPrio)
	{

		//@formatter:off
		final var result = Json.createObjectBuilder()
				.add("title", title)
				.add("content", content)
				.add("projektobjid", projectId)
				.add("creatorobjid", creatorId)
				.add("priority", jsonPrio)
				.build();
		//@formatter:on
		return result;
	}

	// ********** Priorities **********
	private PostResult postPriority(final String name, final String description, int value)
	{
		final var baseURL = "http://localhost:8080/monolith/rext/itemmgmt/priorities";
		final var invocationBuilder = this.createBuilder(baseURL);

		final var jsonObj = this.createJsonPriority(name, description, value);
		final var res = invocationBuilder.post(Entity.json(jsonObj.toString()));

		final var selfLink = res.getLink("self");
		final var uri = selfLink.getUri().getPath();
		final var objid = uri.substring(uri.lastIndexOf('/') + 1);
		final var status = res.getStatus();

		return new PostResult(status, objid);

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

	private JsonObject createJsonPriorityWithPrio(final String objId, final String name, String description, final int value)
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
	private PostResult postState(final String name, final String description)
	{
		final var baseURL = "http://localhost:8080/monolith/rext/workflowmgmt/status";
		final var invocationBuilder = this.createBuilder(baseURL);

		final var jsonObj = this.createJsonStatus(name, description);
		final var res = invocationBuilder.post(Entity.json(jsonObj.toString()));

		final var status = res.getStatus();
		final var selfLink = res.getLink("self");
		final var uri = selfLink.getUri().getPath();
		final var objid = uri.substring(uri.lastIndexOf('/') + 1);

		return new PostResult(status, objid);

	}

	private JsonObject createJsonStatus(final String name, String description)
	{
		//@formatter:off
		final var result = Json.createObjectBuilder()
				.add("name", name)
				.add("description", description)
				.build();
		//@formatter:on
		return result;
	}

	// ********** Helper **********

	private Invocation.Builder createBuilder(String baseURL)
	{
		final var target = this.client.target(baseURL);
		final var result = target.request(MediaType.APPLICATION_JSON);

		return result;

	}

}
