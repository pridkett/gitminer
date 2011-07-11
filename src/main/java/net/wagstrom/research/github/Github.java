package net.wagstrom.research.github;

import java.util.ArrayList;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.api.v2.schema.User;
import com.github.api.v2.services.GitHubServiceFactory;
import com.github.api.v2.services.UserService;

/**
 * Main driver class for Github research
 * 
 * @author patrick
 *
 */
public class Github {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
        Logger log = LoggerFactory.getLogger(Github.class);

		ArrayList <String> projects = new ArrayList<String> ();
		ArrayList <String> users = new ArrayList<String> ();
		GitHubServiceFactory factory = GitHubServiceFactory.newInstance();
		
		Properties p = GithubProperties.props();
		
		try {
			for (String proj : p.getProperty("net.wagstrom.research.github.projects").split(",")) {
				projects.add(proj.trim());
			}
		} catch (NullPointerException e) {
			log.error("property net.wagstrom.research.github.projects undefined");
			System.exit(1);
		}
		
		try{
			for (String user : p.getProperty("net.wagstrom.research.github.users").split(",")) {
				users.add(user.trim());
			}
		} catch (NullPointerException e) {
			log.error("property net.wagstrom.research.github.users undefined");
			System.exit(1);
		}
		
		RepositoryMiner rm = new RepositoryMiner(factory.createRepositoryService());
		for (String proj : projects) {
			String [] projsplit = proj.split("/");
			rm.getRepositoryInformation(projsplit[0], projsplit[1]);
		}
	
		UserMiner um = new UserMiner(factory.createUserService());
		for (String user : users) {
			um.getUserInformation(user);
		}
	}
}
