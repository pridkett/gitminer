/*
 * Copyright (c) 2012 IBM Corporation
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

public final class EventType {
    /**
     * This is static class for constants only
     */
    private EventType() {};

    public static final String COMMIT_COMMENT_EVENT = "CommitCommentEvent";
    public static final String CREATE_EVENT = "CreateEvent";
    public static final String DELETE_EVENT = "DeleteEvent";
    public static final String DOWNLOAD_EVENT = "DownloadEvent";
    public static final String FOLLOW_EVENT = "FollowEvent";
    public static final String FORK_EVENT = "ForkEvent";
    public static final String FORK_APPLY_EVENT = "ForkApplyEvent";
    public static final String GIST_EVENT = "GistEvent";
    public static final String GOLLUM_EVENT = "GollumEvent";
    public static final String ISSUE_COMMENT_EVENT = "IssueCommentEvent";
    public static final String ISSUES_EVENT = "IssuesEvent";
    public static final String MEMBER_EVENT = "MemberEvent";
    public static final String PUBLIC_EVENT = "PublicEvent";
    public static final String PULL_REQUEST_EVENT = "PullRequestEvent";
    public static final String PULL_REQUEST_REVIEW_COMMENT_EVENT = "PullRequestReviewCommentEvent";
    public static final String PUSH_EVENT = "PushEvent";
    public static final String TEAM_ADD_EVENT = "TeamAddEvent";
    public static final String WATCH_EVENT = "WatchEvent";
}
