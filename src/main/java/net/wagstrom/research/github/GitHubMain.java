/*
 * Copyright 2011 IBM Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.wagstrom.research.github;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.wagstrom.research.github.v3.IssueMinerV3;
import net.wagstrom.research.github.v3.PullMinerV3;
import net.wagstrom.research.github.v3.RepositoryMinerV3;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.api.v2.schema.Gist;
import com.github.api.v2.schema.Issue;
import com.github.api.v2.schema.PullRequest;
import com.github.api.v2.schema.Repository;
import com.github.api.v2.schema.Team;
import com.github.api.v2.schema.User;
import com.github.api.v2.services.FeedService;
import com.github.api.v2.services.GitHubException;
import com.github.api.v2.services.GitHubServiceFactory;
import com.ibm.research.govsci.graph.GraphShutdownHandler;

/**
 * Main driver class for GitHub data processing.
 * 
 * @author Patrick Wagstrom (http://patrick.wagstrom.net/)
 *
 */
public class GitHubMain {
	Logger log = null;
	ApiThrottle throttle = null;
	long refreshTime = 0; // minimum age of a resource in milliseconds
	Properties p;
	
	public GitHubMain() {
		log = LoggerFactory.getLogger(this.getClass());		
        throttle = new ApiThrottle();
	}
	
	public void main() {

		ArrayList <String> projects = new ArrayList<String> ();
		ArrayList <String> users = new ArrayList<String> ();
		ArrayList <String> organizations = new ArrayList<String> ();
		GitHubServiceFactory factory = GitHubServiceFactory.newInstance();
		
		p = GithubProperties.props();

		// set the maximum rate as specificed in the configuration properties file
		int maxCalls = Integer.parseInt(p.getProperty("net.wagstrom.research.github.apiThrottle.maxCalls", "0"));
		int maxCallsInterval = Integer.parseInt(p.getProperty("net.wagstrom.research.github.apiThrottle.maxCallsInterval", "0"));
        if (maxCalls > 0 && maxCallsInterval > 0) {
        	log.info("Setting Max Call Rate: {}/{}", maxCalls, maxCallsInterval);
        	throttle.setMaxRate(maxCalls, maxCallsInterval);
        }
        
        // set the minimum age for an artifact in milliseconds
        double minAgeDouble = Double.parseDouble(p.getProperty("net.wagstrom.research.github.refreshTime", "0.0"));
        refreshTime = (long)minAgeDouble * 86400 * 1000;
        log.info("Minimum artifact refresh time: {}ms", refreshTime);
        
        // get the list of projects
		try {
			for (String proj : p.getProperty("net.wagstrom.research.github.projects").split(",")) {
				if (!proj.trim().equals("")) {
					projects.add(proj.trim());
				}
			}
		} catch (NullPointerException e) {
			log.error("property net.wagstrom.research.github.projects undefined");
			System.exit(1);
		}
		
		// get the list of users
		try{
			for (String user : p.getProperty("net.wagstrom.research.github.users").split(",")) {
				if (!user.trim().equals("")) {
					users.add(user.trim());
				}
			}
		} catch (NullPointerException e) {
			log.error("property net.wagstrom.research.github.users undefined");
			System.exit(1);
		}
		
		// get the list of organizations
		try {
			for (String organization : p.getProperty("net.wagstrom.research.github.organizations").split(",")){
				if (!organization.trim().equals("")) {
					organizations.add(organization.trim());
				}
			}
		} catch (NullPointerException e) {
			log.error("property net.wagstrom.research.github.organizations undefined");
			System.exit(1);
		}


		BlueprintsDriver bp = connectToGraph(p);
		
		// make sure that it gets shutdown properly
		GraphShutdownHandler gsh = new GraphShutdownHandler();
		gsh.addShutdownHandler(bp);
		Runtime.getRuntime().addShutdownHook(gsh);
		
		GitHubClient ghc = new GitHubClient();
		IssueMinerV3 imv3 = new IssueMinerV3(ghc);
		PullMinerV3 pmv3 = new PullMinerV3(ghc);
		RepositoryMinerV3 rmv3 = new RepositoryMinerV3(ghc);
		
		RepositoryMiner rm = new RepositoryMiner(ThrottledGitHubInvocationHandler.createThrottledRepositoryService(factory.createRepositoryService(), throttle));
		IssueMiner im = new IssueMiner(ThrottledGitHubInvocationHandler.createThrottledIssueService(factory.createIssueService(), throttle));
		
		PullMiner pm = new PullMiner(ThrottledGitHubInvocationHandler.createThrottledPullRequestService(factory.createPullRequestService(), throttle));
		UserMiner um = new UserMiner(ThrottledGitHubInvocationHandler.createThrottledUserService(factory.createUserService(), throttle));
		GistMiner gm = new GistMiner(ThrottledGitHubInvocationHandler.createThrottledGistService(factory.createGistService(), throttle));
		OrganizationMiner om = new OrganizationMiner(ThrottledGitHubInvocationHandler.createThrottledOrganizationService(factory.createOrganizationService(), throttle));
		
		if (p.getProperty("net.wagstrom.research.github.miner.repositories","true").equals("true")) {
			for (String proj : projects) {
				String [] projsplit = proj.split("/");

				// yay! full declarations! they're AWESOME!
				org.eclipse.egit.github.core.Repository repo = rmv3.getRepository(projsplit[0], projsplit[1]);
				
				bp.saveRepository(rm.getRepositoryInformation(projsplit[0], projsplit[1]));

				if (p.getProperty("net.wagstrom.research.github.miner.repositories.collaborators", "true").equals("true"))
					bp.saveRepositoryCollaborators(proj, rm.getRepositoryCollaborators(projsplit[0], projsplit[1]));
				if (p.getProperty("net.wagstrom.research.github.miner.repositories.contributors", "true").equals("true"))
					bp.saveRepositoryContributors(proj, rm.getRepositoryContributors(projsplit[0], projsplit[1]));
				if (p.getProperty("net.wagstrom.research.github.miner.repositories.watchers", "true").equals("true"))
					bp.saveRepositoryWatchers(proj, rm.getWatchers(projsplit[0], projsplit[1]));
				if (p.getProperty("net.wagstrom.research.github.miner.repositories.forks", "true").equals("true"))
					bp.saveRepositoryForks(proj, rm.getForks(projsplit[0], projsplit[1]));
				
				if (p.getProperty("net.wagstrom.research.github.miner.issues","true").equals("true")) {
					Collection<org.eclipse.egit.github.core.Issue> issues3 = imv3.getAllIssues(projsplit[0], projsplit[1]);
					if (issues3 != null) {
						bp.saveRepositoryIssues(repo, issues3);
						
						Map<Integer, Date> savedIssues = bp.getIssueCommentsAddedAt(proj);
						log.trace("SavedIssues Keys: {}", savedIssues.keySet());
	
						for (org.eclipse.egit.github.core.Issue issue : issues3) {
							// if an issue doesn't appear in the set, we always save it
							if (!needsUpdate(savedIssues.get(issue.getNumber()))) {
								log.debug("Skipping fetching issue {} - recently updated", issue.getNumber());
								continue;
							}
							try {
								bp.saveRepositoryIssueComments(proj, issue, im.getIssueComments(projsplit[0], projsplit[1], issue.getNumber()));
							} catch (NullPointerException e) {
								log.error("NullPointerException saving issue comments: {}:{}", proj, issue);
							}
						}
					} else {
						log.warn("No issues for repository {}/{} - probably disabled", projsplit[0], projsplit[1]);
					}
				}
				
				if (p.getProperty("net.wagstrom.research.github.miner.repositories.pullrequests", "true").equals("true")) {
					Collection<org.eclipse.egit.github.core.PullRequest> requests3 = pmv3.getAllPullRequests(repo);
					bp.saveRepositoryPullRequests(repo, requests3);
					
//					Collection<PullRequest> requests = pm.getAllPullRequests(projsplit[0], projsplit[1]);
//					log.debug("Saving repository pull requests");
//					bp.saveRepositoryPullRequests(proj, requests);
//					log.debug("Pull requests saved");

					Map<Integer, Date> savedRequests = bp.getPullRequestDiscussionsAddedAt(proj);
					log.debug("SavedPullRequest Keys: {}", savedRequests.keySet());
					for (org.eclipse.egit.github.core.PullRequest request : requests3) {
						if (savedRequests.containsKey(request.getNumber())) {
							if (!needsUpdate(savedRequests.get(request.getNumber()), true)) {
								log.debug("Skipping fetching pull request {} - recently updated {}", request.getNumber(), savedRequests.get(request.getNumber()));
								continue;								
							}						
						}
						try {
							// Fetch it BOTH ways
							bp.saveRepositoryPullRequest(proj, pm.getPullRequest(projsplit[0], projsplit[1], request.getNumber()), true);
							bp.saveRepositoryPullRequest(repo, pmv3.getPullRequest(repo, request.getNumber()), true);
						} catch (NullPointerException e) {
							log.error("NullPointerException saving pull request: {}:{}", proj, request.getNumber());
						}
					}
				}
				
				if (p.getProperty("net.wagstrom.research.github.miner.repositories.users", "true").equals("true")) {
					log.info("calling getProjectUsersLastFullUpdate");
					Map<String, Date> allProjectUsers = bp.getProjectUsersLastFullUpdate(proj);
					log.info("keyset: {}", allProjectUsers.keySet());
					int ctr = 1;
					int numUsers = allProjectUsers.size();
					for (Map.Entry<String, Date> entry : allProjectUsers.entrySet()) {
						String username = entry.getKey();
						Date date = entry.getValue();
						if (username == null || username.trim().equals("")) {
							log.warn("null/empty username! continuing");
							continue;
						}
 						if (needsUpdate(date, true)) {
							log.debug("Fetching {} user {}/{}: {}", new Object[]{proj, ctr++, numUsers, username});
							fetchAllUserData(bp, um, rm, gm, username);
						} else {
							log.debug("Fecthing {} user {}/{}: {} needs no update", new Object[]{proj, ctr++, numUsers, username});
						}
					}
				}
			}
			
			// this iterates over the projects by itself
			bruteForceIssueComments(im, bp, p);
		}

		// FIXME: this should check for when the user was last updated
		if (p.getProperty("net.wagstrom.research.github.miner.users","true").equals("true")) {
			for (String username : users) {
				fetchAllUserData(bp, um, rm, gm, username);
			}
		}
	
		if (p.getProperty("net.wagstrom.research.github.miner.organizations","true").equals("true")) {
			for (String organization : organizations) {
				log.warn("Fetching organization: {}", organization);
				bp.saveOrganization(om.getOrganizationInformation(organization));
				// This method fails when you're not an administrator of the organization
	//			try {
	//				bp.saveOrganizationOwners(organization, om.getOrganizationOwners(organization));
	//			} catch (GitHubException e) {
	//				log.info("Unable to fetch owners: {}", GitHubErrorPrimative.createGitHubErrorPrimative(e).getError());
	//			}
				bp.saveOrganizationPublicMembers(organization, om.getOrganizationPublicMembers(organization));
				bp.saveOrganizationPublicRepositories(organization, om.getOrganizationPublicRepositories(organization));
				// This fails when not an administrator of the organization
	//			try {
	//				List<Team> teams = om.getOrganizationTeams(organization);
	//				bp.saveOrganizationTeams(organization, teams);
	//				for (Team team : teams) {
	//					bp.saveTeamMembers(team.getId(), om.getOrganizationTeamMembers(team.getId()));
	//					bp.saveTeamRepositories(team.getId(), om.getOrganizationTeamRepositories(team.getId()));
	//				}
	//			} catch (GitHubException e) {
	//				log.info("Unable to fetch teams: {}", GitHubErrorPrimative.createGitHubErrorPrimative(e).getError());
	//			}
			}
		}

		log.info("Shutting down graph");
		bp.shutdown();
	}
	
	private void bruteForceIssueComments(IssueMiner im, BlueprintsDriver bp, Properties p) {
		ArrayList<String> projects = new ArrayList<String>();
		if (p.getProperty("net.wagstrom.research.github.miner.issues.bruteforce","true").equals("true")) {
	        // get the list of projects
			try {
				for (String proj : p.getProperty("net.wagstrom.research.github.projects.bruteforce").split(",")) {
					if (!proj.trim().equals("")) {
						projects.add(proj.trim());
					}
				}
			} catch (NullPointerException e) {
				log.error("property net.wagstrom.research.github.projects.bruteforce undefined");
				System.exit(1);
			}	
		}
		
		for (String proj : projects) {
			Map<Integer, Date> issues = bp.getIssueCommentsAddedAtBruteForce(proj);
			for (Map.Entry<Integer, Date> entry : issues.entrySet()) {
			    int issueID = entry.getKey();
				Date date = entry.getValue();
				if (needsUpdate(date, true)) {
					try {
						Issue issue = im.getIssue(proj, issueID);
						if (issue != null) {
							bp.saveRepositoryIssueComments(proj, issue, im.getIssueComments(proj, issue.getNumber()));					
						}
					} catch (GitHubException e) {
						log.error("Error fetching issue {}:{}", proj, issueID);
					} catch (NullPointerException e) {
						log.error("Somehow got a null pointer exception on issue {}:{}", proj, issueID);
					}
				}
			}
		}
		return;
	}
	
	/**
	 * Helper function for {@link #needsUpdate(Date, boolean)} that defaults to false
	 * 
	 * @param elementDate Date to check
	 * @return boolean whether or not the element needs to be updated
	 */
	private boolean needsUpdate(Date elementDate) {
		return needsUpdate(elementDate, false);
	}
	
	/**
	 * Simple helper function that is used to determine if a given date is outside
	 * of the window for being updated.
	 * 
	 * For example if we wanted to make sure that elements were more than day old,
	 * we'd set refreshTime to 86400000 (number of milliseconds in a day). If we wanted
	 * null values to evaluate as true (indicating that we should update such values),
	 * then we'd set nullTrueFalse to true.
	 * 
	 * @param elementDate date to check
	 * @param nullTrueFalse return value if elementDate is null
	 * @return whether or not it has been at least refreshTime milliseconds since elementDate
	 */
	private boolean needsUpdate(Date elementDate, boolean nullTrueFalse) {
		Date currentDate = new Date();
		if (elementDate == null) return nullTrueFalse;		
		return ((currentDate.getTime() - elementDate.getTime()) >= refreshTime);
	}

	private void fetchAllUserData(BlueprintsDriver bp, UserMiner um, RepositoryMiner rm, GistMiner gm, String user) {
		List<String> followers = um.getUserFollowers(user);
		if (followers != null) {
			bp.saveUserFollowers(user, followers);
		} else {
			log.debug("user: {} null followers", user);
		}
		
		List<String> following = um.getUserFollowing(user);
		if (following != null) {
			bp.saveUserFollowing(user, following);
		} else {
			log.debug("user: {} null fullowing", user);
		}
		
		List<Repository> watchedRepos = um.getWatchedRepositories(user);
		if (watchedRepos != null) {
			bp.saveUserWatchedRepositories(user, watchedRepos);
		} else {
			log.debug("user: {} null watched repositories", user);
		}
		
		List<Repository> userRepos = rm.getUserRepositories(user);
		if (userRepos != null) {
			bp.saveUserRepositories(user, userRepos);
		} else {
			log.debug("user: {} null user repositries", user);
			
		}
		
		if (p.getProperty("net.wagstrom.research.github.miner.gists","true").equals("true")) {
			List<Gist> gists = gm.getUserGists(user);
			if (gists != null) {
				bp.saveUserGists(user, gists);
			} else {
				log.debug("user: {} null gists", user);
			}
		}
		
		// yes, the user is saved last, this way if any of the other parts
		// fail we don't accidentally say the user was updated
		User userInfo = um.getUserInformation(user);
		if (userInfo != null) {
			bp.saveUser(userInfo, true);
		} else {
			log.debug("user: {} null user information", user);
		}
	}
	
	private BlueprintsDriver connectToGraph(Properties p) {
		BlueprintsDriver bp = null;
        
		// pass through all the db.XYZ properties to the database
		HashMap<String, String> dbprops = new HashMap<String, String>();
        for (Object o : p.keySet()) {
            String s = (String) o;
            if (s.startsWith("db.")) {
                dbprops.put(s.substring(3), p.getProperty(s));
            }
        }
        
		try {
			String dbengine = p.getProperty("net.wagstrom.research.github.dbengine").trim();
			String dburl = p.getProperty("net.wagstrom.research.github.dburl").trim();
			bp = new BlueprintsDriver(dbengine, dburl, dbprops);
		} catch (NullPointerException e) {
			log.error("properties undefined, must define both net.wagstrom.research.github.dbengine and net.wagstrom.research.github.dburl");
		}
		return bp;
	}
}
