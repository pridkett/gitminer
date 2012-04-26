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

public final class VertexType {
    public static final String COMMIT = "COMMIT"; // also used by RepositoryLoader
    public static final String FILE = "FILE"; // used for RepositoryLoader
    public static final String GIT_USER = "GIT_USER"; // also used by RepositoryLoader
    public static final String EMAIL = "EMAIL"; // also used by RepositoryLoader
    public static final String NAME = "NAME"; // used by RepositoryLoader
    public static final String USER = "USER";
    public static final String REPOSITORY = "REPOSITORY";
    public static final String ORGANIZATION = "ORGANIZATION";
    public static final String TEAM = "TEAM";
    public static final String GIST = "GIST";
    public static final String ISSUE = "ISSUE";
    public static final String ISSUE_EVENT = "ISSUE_EVENT";
    public static final String LABEL = "LABEL";
    public static final String MILESTONE = "MILESTONE";
    public static final String COMMENT = "COMMENT";
    public static final String GISTFILE = "GISTFILE";
    public static final String PULLREQUEST = "PULLREQUEST";
    public static final String PULLREQUESTMARKER = "PULLREQUESTMARKER";
    public static final String PULLREQUESTREVIEWCOMMENT = "PULLREQUESTREVIEWCOMMENT";
    public static final String DISCUSSION = "DISCUSSION";
    public static final String GRAVATAR = "GRAVATAR";
}
