package de.esempe.rext.restapitest;

import static org.assertj.core.api.Assertions.assertThat;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Smoketest für User-Resource")
class PingResourceTest extends AbstractResourceTest
{
	static String baseURL = "http://localhost:8080/monolith/rext/usermgmt/ping";

	@BeforeAll
	static void setUpBeforeClass() throws Exception
	{
		target = client.target(PingResourceTest.baseURL);
	}

	@Test
	@DisplayName("Ping")
	void ping()
	{
		// act
		invocationBuilder = target.request(MediaType.APPLICATION_JSON);
		final Response res = invocationBuilder.get();

		// assert
		assertThat(res).isNotNull();
		assertThat(res.getStatus()).isEqualTo(200);
	}
}
