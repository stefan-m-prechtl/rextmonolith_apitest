package de.esempe.rext.restapitest;

import java.util.concurrent.TimeUnit;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class Main
{
	private Client client;

	private Invocation.Builder invocationBuilder;

	public static void main(String[] args)
	{
		final Main theApp = new Main();
		theApp.run();
	}

	public void run()
	{
		this.client = ClientBuilder.newBuilder().connectTimeout(100, TimeUnit.MILLISECONDS).readTimeout(2, TimeUnit.SECONDS).build();

		System.out.println(this.ping());
		System.out.println(this.createUser("SMP2", "'Stefan Maria", "Prechtl"));

		this.client.close();
	}

	private int ping()
	{
		final String baseURL = "http://localhost:8080/monolith/rext/usermgmt/ping";
		this.invocationBuilder = this.createBuilder(baseURL);
		final Response res = this.invocationBuilder.get();

		return res.getStatus();

	}


	private int createUser(final String login, final String firstname, final String lastname)
	{
		final String baseURL = "http://localhost:8080/monolith/rext/usermgmt/users";
		this.invocationBuilder = this.createBuilder(baseURL);

		final JsonObject jsonUser = this.createNewUser(login, firstname, lastname);
		final Response res = this.invocationBuilder.post(Entity.json(jsonUser.toString()));

		return res.getStatus();

	}


	private JsonObject createNewUser(final String login, final String firstname, final String lastname)
	{
		//@formatter:off
		final JsonObject result = Json.createObjectBuilder()
				//.add(field_id, user.getObjid().toString())
				.add("userlogin", login)
				.add("firstname", firstname)
				.add("lastname", lastname)
				.build();
		//@formatter:on
		return result;
	}

	private Invocation.Builder createBuilder(String baseURL)
	{
		final WebTarget target = this.client.target(baseURL);
		final Invocation.Builder result  = target.request(MediaType.APPLICATION_JSON);

		return result;

	}

}
