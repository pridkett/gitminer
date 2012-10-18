/*
 * Copyright (c) 2011-2012 IBM Corporation
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

import net.wagstrom.research.github.v3.CollaboratorMinerV3;
import net.wagstrom.research.github.v3.EventMinerV3;
import net.wagstrom.research.github.v3.GistMinerV3;
import net.wagstrom.research.github.v3.IssueMinerV3;
import net.wagstrom.research.github.v3.OrganizationMinerV3;
import net.wagstrom.research.github.v3.PullMinerV3;
import net.wagstrom.research.github.v3.RepositoryMinerV3;
import net.wagstrom.research.github.v3.ThrottledGitHubInvocationHandler;
import net.wagstrom.research.github.v3.UserMinerV3;
import net.wagstrom.research.github.v3.WatcherMinerV3;

import org.eclipse.egit.github.core.IssueEvent;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.IGitHubClient;
import org.eclipse.egit.github.core.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.research.govsci.graph.GraphShutdownHandler;

/**
 * Main driver class for GitHub data processing.
 * 
 * @author Patrick Wagstrom (http://patrick.wagstrom.net/)
 *
 */
public class GitHubMain {
    private static final Logger log = LoggerFactory.getLogger(GitHubMain.class); // NOPMD
    private final ApiThrottle v3throttle;
    private long refreshTime = 0; // minimum age of a resource in milliseconds
    private Properties props;
    protected BlueprintsDriver bp;

    public GitHubMain() {
        v3throttle = new ApiThrottle();
    }

    public void main() {

        ArrayList <String> projects = new ArrayList<String> ();
        ArrayList <String> users = new ArrayList<String> ();
        ArrayList <String> organizations = new ArrayList<String> ();
        props = GithubProperties.props();

        int v3MaxCalls = Integer.parseInt(props.getProperty(PropNames.GITHUB_THROTTLE_MAX_CALLS, PropDefaults.GITHUB_THROTTLE_MAX_CALLS));
        int v3MaxCallsInterval = Integer.parseInt(props.getProperty(PropNames.GITHUB_THROTTLE_MAX_CALLS_INTERVAL, PropDefaults.GITHUB_THROTTLE_MAX_CALLS_INTERVAL));
        if (v3MaxCalls >0 && v3MaxCallsInterval > 0) {
            log.info("Setting v3 Max Call Rate: {}/{}", v3MaxCalls, v3MaxCallsInterval);
            v3throttle.setMaxRate(v3MaxCalls, v3MaxCallsInterval);
        }
        v3throttle.setId("v3");

        // set the minimum age for an artifact in milliseconds
        double minAgeDouble = Double.parseDouble(props.getProperty(PropNames.GITHUB_REFRESH_TIME, PropDefaults.GITHUB_REFRESH_TIME));
        refreshTime = (long)minAgeDouble * 86400 * 1000;
        log.info("Minimum artifact refresh time: {}ms", refreshTime);

        // get the list of projects
        for (String proj : props.getProperty(PropNames.GITHUB_PROJECT_NAMES, PropDefaults.GITHUB_PROJECT_NAMES).split(",")) {
            if (!proj.trim().equals("")) {
                projects.add(proj.trim());
            }
        }

        for (String user : props.getProperty(PropNames.GITHUB_USERNAMES, PropDefaults.GITHUB_USERNAMES).split(",")) {
            if (!user.trim().equals("")) {
                users.add(user.trim());
            }
        }

        // get the list of organizations
        for (String organization : props.getProperty(PropNames.GITHUB_ORGANIZATIONS, PropDefaults.GITHUB_ORGANIZATIONS).split(",")){
            if (!organization.trim().equals("")) {
                organizations.add(organization.trim());
            }
        }


        connectToGraph(props);

        // make sure that it gets shutdown properly
        GraphShutdownHandler gsh = new GraphShutdownHandler();
        gsh.addShutdownHandler(bp);
        Runtime.getRuntime().addShutdownHook(gsh);

        GitHubClient ghc = new GitHubClient();

        IssueMinerV3 imv3 = new IssueMinerV3(ThrottledGitHubInvocationHandler.createThrottledGitHubClient((IGitHubClient)ghc, v3throttle));
        PullMinerV3 pmv3 = new PullMinerV3(ThrottledGitHubInvocationHandler.createThrottledGitHubClient((IGitHubClient)ghc, v3throttle));
        RepositoryMinerV3 rmv3 = new RepositoryMinerV3(ThrottledGitHubInvocationHandler.createThrottledGitHubClient((IGitHubClient)ghc, v3throttle));
        UserMinerV3 umv3 = new UserMinerV3(ThrottledGitHubInvocationHandler.createThrottledGitHubClient((IGitHubClient)ghc, v3throttle));
        OrganizationMinerV3 omv3 = new OrganizationMinerV3(ThrottledGitHubInvocationHandler.createThrottledGitHubClient((IGitHubClient)ghc, v3throttle));
        GistMinerV3 gmv3 = new GistMinerV3(ThrottledGitHubInvocationHandler.createThrottledGitHubClient((IGitHubClient)ghc, v3throttle));
        WatcherMinerV3 wmv3 = new WatcherMinerV3(ThrottledGitHubInvocationHandler.createThrottledGitHubClient((IGitHubClient)ghc, v3throttle));
        CollaboratorMinerV3 cmv3 = new CollaboratorMinerV3(ThrottledGitHubInvocationHandler.createThrottledGitHubClient((IGitHubClient)ghc, v3throttle));
        EventMinerV3 emv3 = new EventMinerV3(ThrottledGitHubInvocationHandler.createThrottledGitHubClient((IGitHubClient)ghc, v3throttle));

        if (props.getProperty(PropNames.GITHUB_MINE_REPOS, PropDefaults.GITHUB_MINE_REPOS).equals("true")) {
            for (String proj : projects) {
                String [] projsplit = proj.split("/");

                Repository repo = rmv3.getRepository(projsplit[0], projsplit[1]);
                if (repo == null) {
                    continue;
                }
                bp.saveRepository(repo);
                log.warn("handling project owner...");
                handleProjectOwner(repo.getOwner(), umv3, omv3);

                if (props.getProperty(PropNames.GITHUB_MINE_REPO_COLLABORATORS, PropDefaults.GITHUB_MINE_REPO_COLLABORATORS).equals("true")) {
                    bp.saveRepositoryCollaborators(repo, cmv3.getCollaborators(repo));
                }
                if (props.getProperty(PropNames.GITHUB_MINE_REPO_CONTRIBUTORS, PropDefaults.GITHUB_MINE_REPO_CONTRIBUTORS).equals("true")) {
                    bp.saveRepositoryContributors(repo, rmv3.getContributors(repo));
                }
                if (props.getProperty(PropNames.GITHUB_MINE_REPO_WATCHERS, PropDefaults.GITHUB_MINE_REPO_WATCHERS).equals("true")) {
                    bp.saveRepositoryWatchers(repo, wmv3.getWatchers(repo));
                }
                if (props.getProperty(PropNames.GITHUB_MINE_REPO_FORKS, PropDefaults.GITHUB_MINE_REPO_FORKS).equals("true")) {
                    bp.saveRepositoryForks(repo, rmv3.getForks(repo));
                }

                if (props.getProperty(PropNames.GITHUB_MINE_REPO_ISSUES, PropDefaults.GITHUB_MINE_REPO_ISSUES).equals("true")) {
                    if (repo.isHasIssues()) {
                        Collection<org.eclipse.egit.github.core.Issue> issues3 = imv3.getAllIssues(projsplit[0], projsplit[1]);
                        if (issues3 != null) {
                            bp.saveRepositoryIssues(repo, issues3);

                            Map<Integer, Date> savedIssues = bp.getIssueCommentsAddedAt(proj);
                            log.trace("SavedIssues Keys: {}", savedIssues.keySet());

                            for (org.eclipse.egit.github.core.Issue issue : issues3) {
                                String issueId = repo.generateId() + ":" + issue.getNumber();
                                if (!needsUpdate(savedIssues.get(issue.getNumber()), true)) {
                                    log.debug("Skipping fetching comments for issue {} - recently updated {}", issueId, savedIssues.get(issue.getNumber()));
                                    continue;
                                }
                                log.debug("Pulling comments for issue: {} - last update: {}", issueId, savedIssues.get(issue.getNumber()));
                                try {
                                    // Fetch comments BOTH ways -- using the v2 and the v3 apis
                                    bp.saveIssueComments(repo, issue, imv3.getIssueComments(repo, issue));
                                } catch (NullPointerException e) {
                                    log.error("NullPointerException saving issue comments: {}:{}", proj, issue);
                                }
                            }

                            savedIssues = bp.getIssueEventsAddedAt(repo);
                            for (org.eclipse.egit.github.core.Issue issue : issues3) {
                                String issueId = repo.generateId() + ":" + issue.getNumber();
                                if (!needsUpdate(savedIssues.get(issue.getNumber()), true)) {
                                    log.debug("Skipping fetching events for issue {} - recently updated - {}", new Object[]{issueId, savedIssues.get(issue.getNumber())});
                                    continue;
                                } else {
                                    log.warn("issue {} - last updated: {}", issue.getNumber(), savedIssues.get(issue.getNumber()));
                                }
                                log.debug("Pulling events for issue: {} - {}", new Object[]{issueId, savedIssues.get(issue.getNumber())});
                                Collection<IssueEvent> evts = imv3.getIssueEvents(repo, issue);
                                log.trace("issue {} events: {}", new Object[]{issueId, evts.size()});
                                try {
                                    bp.saveIssueEvents(repo, issue, evts);
                                } catch (NullPointerException e) {
                                    log.error("NullPointer exception saving issue events: {}", issueId);
                                }
                            }
                        } else {
                            log.warn("No issues for repository {}/{} - probably disabled", projsplit[0], projsplit[1]);
                        }
                    } else {
                        log.warn("Repository {} does not have issues enabled", repo.generateId());
                    }
                }

                if (props.getProperty(PropNames.GITHUB_MINE_REPO_PULLREQUESTS, PropDefaults.GITHUB_MINE_REPO_PULLREQUESTS).equals("true")) {
                    Collection<org.eclipse.egit.github.core.PullRequest> requests3 = pmv3.getAllPullRequests(repo);
                    if (requests3 != null) {
                        bp.savePullRequests(repo, requests3);

                        Map<Integer, Date> savedRequests = bp.getPullRequestDiscussionsAddedAt(proj);
                        log.trace("SavedPullRequest Keys: {}", savedRequests.keySet());
                        for (org.eclipse.egit.github.core.PullRequest request : requests3) {
                            if (savedRequests.containsKey(request.getNumber())) {
                                if (!needsUpdate(savedRequests.get(request.getNumber()), true)) {
                                    log.debug("Skipping fetching pull request {} - recently updated {}", request.getNumber(), savedRequests.get(request.getNumber()));
                                    continue;
                                }
                            }
                            try {
                                PullRequest pullRequest= pmv3.getPullRequest(repo, request.getNumber());
                                bp.savePullRequest(repo, null, pullRequest, true);
                                bp.savePullRequestComments(repo, pullRequest, imv3.getPullRequestComments(repo, pullRequest));
                            } catch (NullPointerException e) {
                                log.error("NullPointerException saving pull request: {}:{}", proj, request.getNumber());
                            }
                        }
                    } else {
                        log.warn("No pull requests for repository {} - probably disabled", repo.generateId());
                    }
                }

                if (props.getProperty(PropNames.GITHUB_MINE_REPO_USERS, PropDefaults.GITHUB_MINE_REPO_USERS).equals("true")) {
                    log.trace("calling getProjectUsersLastFullUpdate");
                    Map<String, Date> allProjectUsers = bp.getProjectUsersLastFullUpdate(proj);
                    Map<String, Date> allProjectUsersGists = bp.getProjectUsersLastGistsUpdate(proj);
                    Map<String, Date> allProjectUsersEvents = bp.getProjectUsersLastEventsUpdate(proj);
                    log.trace("keyset: {}", allProjectUsers.keySet());
                    int ctr = 0;
                    int numUsers = allProjectUsers.size();
                    for (Map.Entry<String, Date> entry : allProjectUsers.entrySet()) {
                        String username = entry.getKey();
                        Date lastFullUpdate = entry.getValue();
                        Date lastGistsUpdate = allProjectUsersGists.get(username);
                        Date lastEventsUpdate = allProjectUsersEvents.get(username);
                        if (username == null || username.trim().equals("")) {
                            log.warn("null/empty username! continuing");
                            continue;
                        }
                        ++ctr;
                        // FIXME: these should be extracted into a single method...
                        if (needsUpdate(lastFullUpdate, true)) {
                            log.trace("last updated: {}", lastFullUpdate);
                            log.debug("Fetching {} user {}/{}: {}", new Object[]{proj, ctr, numUsers, username});
                            fetchAllUserData(bp, umv3, rmv3, wmv3, username);
                        } else {
                            log.debug("Fecthing {} user {}/{}: {} needs no update - last update {}", new Object[]{proj, ctr, numUsers, username, lastFullUpdate});
                        }
                        
                        if (props.getProperty(PropNames.GITHUB_MINE_USER_EVENTS, PropDefaults.GITHUB_MINE_USER_EVENTS).equals("true") &&
                                needsUpdate(lastEventsUpdate, true)) {
                            log.debug("Fetching {} events for user {}/{}: {} - last update: {}", new Object[]{proj, ctr, numUsers, username, lastEventsUpdate});
                            fetchAllUserEvents(bp, emv3, username);
                        } else {
                            log.debug("Fetching {} events for user {}/{}: {} needs no update/disabled - last update: {}", new Object[]{proj, ctr, numUsers, username, lastEventsUpdate});
                        }
                        
                        if (props.getProperty(PropNames.GITHUB_MINE_USER_GISTS, PropDefaults.GITHUB_MINE_USER_GISTS).equals("true") &&
                                needsUpdate(lastGistsUpdate, true)) {
                            log.debug("Fetching {} gists for user {}/{}: {} - last update: {}", new Object[]{proj, ctr, numUsers, username, lastEventsUpdate});
                            fetchAllUserGists(bp, gmv3, username);
                        } else {
                            log.debug("Fetching {} gists for user {}/{}: {} needs no update/disabled - last update: {}", new Object[]{proj, ctr, numUsers, username, lastGistsUpdate});
                        }
                    }
                }
            }
        }

        // FIXME: this should check for when the user was last updated
        if (props.getProperty(PropNames.GITHUB_MINE_USERS, PropDefaults.GITHUB_MINE_USERS).equals("true")) {
            for (String username : users) {
                fetchAllUserData(bp, umv3, rmv3, wmv3, username);
                if (props.getProperty(PropNames.GITHUB_MINE_USER_EVENTS, PropDefaults.GITHUB_MINE_USER_EVENTS).equals("true")) {
                    fetchAllUserEvents(bp, emv3, username);
                }
                if (props.getProperty(PropNames.GITHUB_MINE_USER_GISTS, PropDefaults.GITHUB_MINE_USER_GISTS).equals("true")) {
                    fetchAllUserGists(bp, gmv3, username);
                }
            }
        }

        if (props.getProperty(PropNames.GITHUB_MINE_ORGANIZATIONS, PropDefaults.GITHUB_MINE_ORGANIZATIONS).equals("true")) {
            for (String organizationName : organizations) {
                log.warn("Fetching organization: {}", organizationName);
                User organization = omv3.getOrganization(organizationName);
                bp.saveUser(organization);
                // This method fails when you're not an administrator of the organization
                //			try {
                //				bp.saveOrganizationOwners(organization, om.getOrganizationOwners(organization));
                //			} catch (GitHubException e) {
                //				log.info("Unable to fetch owners: {}", GitHubErrorPrimative.createGitHubErrorPrimative(e).getError());
                //			}
                bp.saveOrganizationPublicMembers(organization, omv3.getPublicMembers(organization.getLogin()));
                bp.saveOrganizationPublicRepositories(organizationName, rmv3.getRepositories(organization.getLogin()));
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

    /**
     * @param owner
     */
    private void handleProjectOwner(final User owner, final UserMinerV3 umv3, final OrganizationMinerV3 omv3) {
        User user = umv3.getUser(owner.getLogin());
        
        if (user.getType() == null) {
            log.warn("User has no type: {}", user);
            return;
        }
        
        if (user.getType().toLowerCase().equals("organization")) {
            Collection<User> members = omv3.getPublicMembers(user.getLogin());
            bp.saveOrganizationPublicMembers(user, members);
        } else {
            log.warn("Project owner is not an organization: {}", user.getType());
        }
    }

    /**
     * Helper function for {@link #needsUpdate(Date, boolean)} that defaults to false
     * 
     * @param elementDate Date to check
     * @return boolean whether or not the element needs to be updated
     */
    private boolean needsUpdate(final Date elementDate) {
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
    private boolean needsUpdate(final Date elementDate, final boolean nullTrueFalse) {
        Date currentDate = new Date();
        if (elementDate == null) {
            return nullTrueFalse;
        }
        return ((currentDate.getTime() - elementDate.getTime()) >= refreshTime);
    }

    private void fetchAllUserEvents(final BlueprintsDriver bp, final EventMinerV3 emv3,
            final String username) {
        List<Event> events = emv3.getUserEvents(username);
        if (events != null) {
            bp.saveUserEvents(username, events);
        } else {
            log.debug("user: {} null events", username);
        }
    }
    
    private void fetchAllUserData(final BlueprintsDriver bp, final UserMinerV3 umv3, final RepositoryMinerV3 rmv3, final WatcherMinerV3 wmv3, final String user) {
        List<User> followers = umv3.getFollowers(user);
        if (followers != null) {
            bp.saveUserFollowers(user, followers);
        } else {
            log.debug("user: {} null followers", user);
        }

        List<org.eclipse.egit.github.core.User> following = umv3.getFollowing(user);
        if (following != null) {
            bp.saveUserFollowing(user, following);
        } else {
            log.debug("user: {} null fullowing", user);
        }

        List<org.eclipse.egit.github.core.Repository> watchedRepos = wmv3.getWatched(user);
        if (watchedRepos != null) {
            bp.saveUserWatchedRepositories(user, watchedRepos);
        } else {
            log.debug("user: {} null watched repositories", user);
        }

        List<org.eclipse.egit.github.core.Repository> userRepos = rmv3.getRepositories(user);
        if (userRepos != null) {
            bp.saveUserRepositories(user, userRepos);
        } else {
            log.debug("user: {} null user repositries", user);

        }

        // yes, the user is saved last, this way if any of the other parts
        // fail we don't accidentally say the user was updated
        User userInfo = umv3.getUser(user);
        if (userInfo != null) {
            bp.saveUser(userInfo, true);
        } else {
            log.debug("user: {} null user information", user);
        }
    }
    //    private void fetchAllUserData(BlueprintsDriver bp, UserMiner um, RepositoryMiner rm, GistMiner gm, String user) {
    //        List<String> followers = um.getUserFollowers(user);
    //        if (followers != null) {
    //            bp.saveUserFollowers(user, followers);
    //        } else {
    //            log.debug("user: {} null followers", user);
    //        }
    //
    //        List<String> following = um.getUserFollowing(user);
    //        if (following != null) {
    //            bp.saveUserFollowing(user, following);
    //        } else {
    //            log.debug("user: {} null fullowing", user);
    //        }
    //
    //        List<Repository> watchedRepos = um.getWatchedRepositories(user);
    //        if (watchedRepos != null) {
    //            bp.saveUserWatchedRepositories(user, watchedRepos);
    //        } else {
    //            log.debug("user: {} null watched repositories", user);
    //        }
    //
    //        List<Repository> userRepos = rm.getUserRepositories(user);
    //        if (userRepos != null) {
    //            bp.saveUserRepositories(user, userRepos);
    //        } else {
    //            log.debug("user: {} null user repositries", user);
    //
    //        }
    //
    //        if (p.getProperty("net.wagstrom.research.github.miner.gists","true").equals("true")) {
    //            List<Gist> gists = gm.getUserGists(user);
    //            if (gists != null) {
    //                bp.saveUserGists(user, gists);
    //            } else {
    //                log.debug("user: {} null gists", user);
    //            }
    //        }
    //
    //        // yes, the user is saved last, this way if any of the other parts
    //        // fail we don't accidentally say the user was updated
    //        User userInfo = um.getUserInformation(user);
    //        if (userInfo != null) {
    //            bp.saveUser(userInfo, true);
    //        } else {
    //            log.debug("user: {} null user information", user);
    //        }
    //    }

    private void fetchAllUserGists(final BlueprintsDriver bp,
            final GistMinerV3 gmv3, final String user) {
        if (props.getProperty(PropNames.GITHUB_MINE_GISTS, PropDefaults.GITHUB_MINE_GISTS).equals("true")) {
            bp.saveUserGists(user, gmv3.getGists(user));
        }
    }

    protected BlueprintsDriver connectToGraph(Properties p) {
        // pass through all the db.XYZ properties to the database
        HashMap<String, String> dbprops = new HashMap<String, String>();
        for (Object o : p.keySet()) {
            String s = (String) o;
            if (s.startsWith("db.")) {
                dbprops.put(s.substring(3), p.getProperty(s));
            }
        }

        try {
            String dbengine = p.getProperty(PropNames.DBENGINE, PropDefaults.DBURL).trim();
            String dburl = p.getProperty(PropNames.DBURL, PropDefaults.DBURL).trim();
            bp = new BlueprintsDriver(dbengine, dburl, dbprops);
        } catch (NullPointerException e) {
            log.error("properties undefined, must define both {} and {}", PropNames.DBENGINE, PropNames.DBURL);
            bp = null;
        }
        return bp;
    }
}
