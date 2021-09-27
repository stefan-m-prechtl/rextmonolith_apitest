package de.esempe.rext.restapitest.workflowmgmt;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import de.esempe.rext.restapitest.AbstractResourceTest;
import de.esempe.rext.restapitest.extensions.TestClassOrder;

@DisplayName("REST-API Test für Status-Resource")
@TestMethodOrder(OrderAnnotation.class)
@TestClassOrder(31)
public class TransistionResourceTest extends AbstractResourceTest
{
	final static String baseURL = "http://localhost:8080/monolith/rext/workflowmgmt/transitions";

	// Echte Objekt-ID GET-Abruf - wird für weitere Aufrufe benötigt
	static String realTransitionID;

	@BeforeAll
	static void setUpBeforeClass() throws Exception
	{
		target = client.target(baseURL);
	}

	@Order(10)
	@DisplayName("Befehl 'HTTP OPTION' für: " + baseURL)
	// HTTP OPTION ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void option()
	{
		super.optionResource();
	}

	@Order(20)
	@DisplayName("Befehl 'HTTP HEAD' für: " + baseURL)
	// HTTP HEAD ist idempotent --> Test wiederholen
	@RepeatedTest(value = 2, name = "{currentRepetition}/{totalRepetitions}")
	void head()
	{
		super.headResource();
	}

	@Test
	@Order(30)
	@DisplayName("Befehl 'HTTP DELETE ' für: " + baseURL + "?flag=all")
	void deleteAll()
	{
		super.deleteAllResource();
	}

}
