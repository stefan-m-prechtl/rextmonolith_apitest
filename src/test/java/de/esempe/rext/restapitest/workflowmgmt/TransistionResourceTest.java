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
	final static String field_id = "transitionid";
	final static String field_workflow = "workflowobjid";
	final static String field_fromstate = "fromstate";
	final static String field_tostate = "tostate";
	final static String field_description = "description";

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

	/*
	@Test
	@Order(35)
	@DisplayName("Befehl 'HTTP POST (ok)' für: " + baseURL)
	void postOk()
	{
		var jsonWorklow = WorkflowResourceTest.createEntity("Demoworkflow", "Demoworkflow");
		var jsonWorklowResult = RestCreator.postEntity(jsonWorklow, WorkflowResourceTest.baseURL);

		// prepare
		String workflowid = "";
		JsonObject fromState = null;
		JsonObject toState = null;
		String descripion = "";

		final var jsonState = createEntity(workflowid, fromState, toState, descripion);

		// act
		super.postResourceOk(jsonState, baseURL);
	}

	static JsonObject createEntity(String workflowid, JsonObject fromState, JsonObject toState, String descripion)
	{
		//@formatter:off
		final var result = Json.createObjectBuilder()
				//.add(field_id, user.getObjid().toString())
				.add(field_workflow, workflowid)
				.add(field_fromstate, fromState.toString())
				.add(field_tostate, toState)
				.add(field_description, descripion)
				.build();
		//@formatter:on
		return result;
	}
	*/

}
