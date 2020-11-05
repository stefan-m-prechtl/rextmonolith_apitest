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
	}

	//@formatter:off
	@SuppressWarnings("preview") public record PostResult(int status, String objid){}
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
		this.deleteAllProjects();
		this.deleteAllUsers();

		// Benutzer anlegen
		final var iduser01 = this.createUser("SMP", "Stefan M.", "Prechtl");
		final var iduser02 = this.createUser("PRS", "Stefan", "Prechtl");
		final var iduser03 = this.createUser("WEY", "Thomas", "Weyrath");

		// Projekt anlegen
		final var idproj01 = this.createProject("TST", "Testprojekt 1", iduser01.objid());

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

	private PostResult createUser(final String login, final String firstname, final String lastname)
	{
		final var baseURL = "http://localhost:8080/monolith/rext/usermgmt/users";
		final var invocationBuilder = this.createBuilder(baseURL);

		final var jsonUser = this.createNewUser(login, firstname, lastname);
		final var res = invocationBuilder.post(Entity.json(jsonUser.toString()));

		final var selfLink = res.getLink("self");
		final var uri = selfLink.getUri().getPath();
		final var objid = uri.substring(uri.lastIndexOf('/') + 1);
		final var status = res.getStatus();

		return new PostResult(status, objid);

	}

	private JsonObject createNewUser(final String login, final String firstname, final String lastname)
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
	private PostResult createProject(final String projectname, final String description, final String userId)
	{
		final var baseURL = "http://localhost:8080/monolith/rext/projectmgmt/projects";
		final var invocationBuilder = this.createBuilder(baseURL);

		final var jsonProject = this.createNewProject(projectname, description, userId);
		final var res = invocationBuilder.post(Entity.json(jsonProject.toString()));

		final var selfLink = res.getLink("self");
		final var uri = selfLink.getUri().getPath();
		final var objid = uri.substring(uri.lastIndexOf('/') + 1);
		final var status = res.getStatus();

		return new PostResult(status, objid);

	}

	private JsonObject createNewProject(final String projectname, final String description, final String userId)
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

	// ********** Helper **********

	private Invocation.Builder createBuilder(String baseURL)
	{
		final var target = this.client.target(baseURL);
		final var result = target.request(MediaType.APPLICATION_JSON);

		return result;

	}

}
