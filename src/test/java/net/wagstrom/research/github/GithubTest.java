package net.wagstrom.research.github;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class GithubTest extends TestCase {
	/**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public GithubTest(String testName)
    {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite(GithubTest.class);
    }

    /**
     * Rigorous Test :-)
     */
    public void testApp()
    {
        assertTrue(true);
    }
}
