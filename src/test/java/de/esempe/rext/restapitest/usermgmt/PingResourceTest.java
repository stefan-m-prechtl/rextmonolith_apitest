package de.esempe.rext.restapitest.usermgmt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import de.esempe.rext.restapitest.AbstractResourceTest;
import de.esempe.rext.restapitest.extensions.TestClassOrder;

@DisplayName("REST-API Ping für User-Resource")
@TestClassOrder(1)
class PingResourceTest extends AbstractResourceTest
{
	// Basis-URL für Usermanagement
	final static String urlforUserPing = "http://localhost:8080/monolith/rext/usermgmt/ping";

	public PingResourceTest()
	{
		super(urlforUserPing);
	}

	@Test
	@DisplayName("Befehl 'HTTP GET' für: " + urlforUserPing)
	void ping() throws IOException, InterruptedException
	{
		// act
		final var jsonObj = super.getSingleResource("");

		//@formatter:off
		assertAll("Verify content",
				() -> assertThat(jsonObj.containsKey("date")).isTrue(),
				() -> assertThat(jsonObj.containsKey("time")).isTrue()
				);
		//@formatter:on
	}
}
