GitMiner
========

Copyright (c) 2011-2012 by IBM and the University of Nebraska-Lincoln

By Patrick Wagstrom &lt;<patrick@wagstrom.net>&gt; and Corey Jurgenson

This project is from a joint research project between IBM Research and the
University of Nebraska-Lincoln. Under the terms of that agreement all output
from this project must be distributed under the terms of the [Apache License][license].

Compilation
-----------
This project uses Apache Maven to manage all dependencies and versioning. The
simplest way to get going is to run the following command:

    mvn clean compile package assembly:single

Configuration
-------------
GitMiner has many different properties that can be set to alter the behavior
of the program. The following is a simple configuration file that will be
enough to get you going. Copy this data to a file and name it something like
`configuration.properties` in the root directory of your GitMiner install.

    net.wagstrom.research.github.login=YOURGITHUBLOGIN
    net.wagstrom.research.github.password=YOURGITHUBPASSWORD
    net.wagstrom.research.github.email=YOUREMAILADDRESS
    
    net.wagstrom.research.github.dbengine=neo4j
    net.wagstrom.research.github.dburl=graph.db

    net.wagstrom.research.github.projects=pridkett/gitminer

Execution
---------
Execution of GitMiner is a two step process that consists of first using the
GitHub API to download project data and then later using git directly to process
project source code commits. The configuration file created in the last step
has all the settings you'll need for stages.

To begin, run GitMiner so it downloads data from using the GitHub API:

    ./github.sh -c configuration.properties

Next, use the repository loader functions of GitMiner to download the source
code history for the projects.

    ./repo-loader.sh -c configuration.properties


Configuration Parameters
------------------------

For the most part we have attempted to provide sensible defaults for
configuration parameters, however some parameters must have their values set
for the tool to function.

* **name:** `net.wagstrom.research.github.login`<br>
  **default:** no default<br>
  **description:** this parameter must be set. On October 14, 2012 [GitHub
  changed the way their API works][gh-api-limit] and reduced the number of anonymous API
  requests to 60/hour. With this parameter set you can get as many as 5000
  requests an hour. Without this parameter GitMiner will just refuse to run.
  
* **name:** `net.wagstrom.research.github.password`<br>
  **default:** no default<br>
  **description:** the companion to `net.wagstrom.research.github.login`.
  
* **name:** `net.wagstrom.research.github.email`<br>
  **default:** no default<br>
  **description:** this is your email address. GitHub has requested that all
  clients using the API provide additional mechanisms to identify themselves
  via the user-agent. One of the ways that GitMiner accomplishes this is by
  putting your email address in the user-agent string. Please be nice and set
  this value accordingly.
  
* **name:** `net.wagstrom.research.github.projects`<br>
  **default:** no default<br>
  **description:** a comma separated list of projects to begin spidering. For
  example `rails/rails,pridkett/gitminer,tinkerpop/blueprints`.
  
* **name:** `net.wagstrom.research.github.users`<br>
  **default:** no default<br>
  **description:** a comma separated list of users to spider. For example
  `pridkett,jurgns,dhh`.
  
* **name:** `net.wagstrom.research.github.organizations`<br>
  **default:** no default<br>
  **description:** a comma separated list of organizations to spider. For
  example, `37signals,tinkerpop`.
  
* **name:** `net.wagstrom.research.github.refreshTime`<br>
  **default:** `0.0`<br>
  **description:** minimum number of days since the last update to download
  information about a user or other element again. For most purposes you can
  probably set this much higher. This will GREATLY speed up your crawls if you
  set it to a high value.

**Important (April 13, 2012):** Small changes have been made to the packaging of the code
submitted along with the FSE2012 paper. Please see [artifacts.txt](https://github.com/pridkett/gitminer/blob/master/artifacts.md)
for more information.

**Almost as important:** The rest of this document is out of date.

# Getting Going

This will clean the source tree, compile the code, run the tests, package the
code into a jar file, and finally copy all the libraries to a location that
makes some modicum of sense. Then to run the mining scripts just run:
    ./github.sh

# Configuration
Project configuration is controlled through the `configuration.properties`
file in `src/main/resources`. Don't commit it with your github username
and apitoken, that would be bad.

Actually, right now it doesn't actually use those fields and it probably won't
anytime in the future. So don't worry so much about that.

## Additional important configuration parameters

Various elements of the miner can be turned on and off by changing the values
of their field to anything other than true. Those fields are:

* net.wagstrom.research.github.miner.issues
* net.wagstrom.research.github.miner.gists
* net.wagstrom.research.github.miner.repositories
* net.wagstrom.research.github.miner.organizations
* net.wagstrom.research.github.miner.users

# Explanation of fields
Every Vertex in the database should have the following:
* type: one of USER, REPOSITORY
* created_at: ISO 8601 formatted date of when the node was created

Every Edge in the database should have the followning fields:
* label: not really a field, but always present
* created_at: ISO 8601 formatted date of when the edge was created

## User Vertex Fields

## Repository Vertex Fields


[gh-api-limit]: http://developer.github.com/changes/2012-10-14-rate-limit-changes/
[license]: http://www.apache.org/licenses/LICENSE-2.0.html
