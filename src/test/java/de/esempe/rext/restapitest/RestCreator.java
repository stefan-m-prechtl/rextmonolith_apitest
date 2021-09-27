package de.esempe.rext.restapitest;

import java.util.concurrent.TimeUnit;

import javax.json.JsonObject;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

public class RestCreator
{
	public static String postEntity(JsonObject jsonResource, String url)
	{
		var client = ClientBuilder.newBuilder().connectTimeout(100, TimeUnit.MILLISECONDS).readTimeout(2, TimeUnit.SECONDS).build();
		var target = client.target(url);
		var invocationBuilder = target.request(MediaType.APPLICATION_JSON);
		final var res = invocationBuilder.post(Entity.json(jsonResource.toString()));
		final var link = res.getHeaderString("link");

		client.close();
		return "dummy";
	}
}
