package de.esempe.rext.restapitest;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Testsuite für Integrationstest REXT-Monolith")
@SelectPackages("de.esempe.rext.restapitest")
//@ExcludeClassNamePatterns(".*ItemResourceTest")
public class IntegrationTestSuite
{

}
