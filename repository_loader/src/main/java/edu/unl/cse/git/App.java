package edu.unl.cse.git;

/**
 * Hello world!
 *
 */
public class App 
{
	public static void main( String[] args )
    {
        new LocalRepositoryLoader( "/Users/corey/java/jgit/.git" ).main();
    }
}
