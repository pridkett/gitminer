package net.wagstrom.research.github;

public class Github {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Hello There!");
		System.out.println(GithubProperties.props().getProperty("net.wagstrom.research.github.projects"));
		
	}

}
