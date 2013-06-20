GitMiner
========

Copyright (c) 2011-2012 by IBM and the University of Nebraska-Lincoln

By Patrick Wagstrom &lt;<patrick@wagstrom.net>&gt; and Corey Jergenson &lt;<corey.jergensen@gmail.com>&gt;

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

Alternatively, you may authenticate by using an OAuth token to be
configured as follows in lieu of giving login and password.

    net.wagstrom.research.github.token=YOUROAUTHTOKEN

See http://developer.github.com/v3/oauth/ for more information.

Execution
---------
Execution of GitMiner is a two step process that consists of first using the
GitHub API to download project data and then later using git directly to process
project source code commits. The configuration file created in the last step
has all the settings you'll need for stages.

To begin, run GitMiner so it downloads data from using the GitHub API:

    ./gitminer.sh -c configuration.properties

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
  
* **name:** `net.wagstrom.research.github.token`<br>
  **default:** no default<br>
  **description:** this can be set instead of login and password to
  authenticate with GitHub using the given OAuth token as documented on
  http://developer.github.com/v3/oauth/.
  
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

* **name:** `net.wagstrom.research.github.apiThrottle.maxCalls.v3`<br>
  **default** `4980`<br>
  **description:** The maximum number of calls via the GitHub v3 API in a given
  time period. Use this to rate limit under what the API says. I typically set
  this value to `4980` or something like that to avoid problems when I hit API
  limits. If the value is `0` then this is ignored.
  
* **name:** `net.wagstrom.research.github.apiThrottle.maxCallsInterval.v3`<br>
  **default:** `3600`<br>
  **description:** Time period (in seconds) to make the maximum number of calls
  using the v3 GitHub API. Previously
  some APIs allowed 60calls/min and others 5000/hr, but the API didn't set this.
  Now it seems to always be 5000/hr, so this is generally set to `3600`.

* **name:** `net.wagstrom.research.github.miner.repositories`<br>
  **default:** `true`<br>
  **description:** a `true`/`false` parameter on whether or not to download
  data for the projects specified in `net.wagstrom.research.github.users` property.
  
* **name:** `net.wagstrom.research.github.miner.repositories.collaborators`<br>
  **default:** `true`<br>
  **description:** a `true`/`false` parameter on whether or not to downlaod
  data for the collaborators listed for each project.
  
* **name:** `net.wagstrom.research.github.miner.repositories.contributors`<br>
  **default:** `true`<br>
  **description:** a `true`/`false` parameter on whether or not to download
  data for the contributors listed for each project.
  
* **name:** `net.wagstrom.research.github.miner.repositories.watchers`<br>
  **default:** `true`<br>
  **description:** a `true`/`false` parameter for whether or not to download
  data for the watchers listed for each project.
  
* **name:** `net.wagstrom.research.github.miner.repositories.forks`<br>
  **default:** `true`<br>
  **description:** a `true`/`false` parameter for whether or not to download
  data about forks for each project.
  
* **name:** `net.wagstrom.research.github.miner.repositories.issues`<br>
  **default:** `true`<br>
  **description:** a `true`/`false` parameter for whether or not to download
  data about issues for each project.
  
* **name:** `net.wagstrom.research.github.miner.repositories.pullrequests`<br>
  **default:** `true`<br>
  **description:** a `true`/`false` parameter for whether or not to download
  data about pull requests for each project.

* **name:** `net.wagstrom.research.github.miner.repositories.users`<br>
  **default:** `true`<br>
  **description:** a `true`/`false` parameter for whether or not to download
  all the data about all the users for each project. **FIXME:** I'm not certain
  off the top of my head about what interplay this has with other settings above.
  
* **name:** `net.wagstrom.research.github.miner.users.events`<br>
  **default:** `true`<br>
  **description:** a `true`/`false` parameter for whether or not to download the
  public events stream for each user mined.
  
* **name:** `net.wagstrom.research.github.miner.users.gists`<br>
  **default:** `true`<br>
  **description:** a `true`/`false` parameter for whether or not to download the
  set of gists for each user mined.

* **name:** `net.wagstrom.research.github.miner.users`<br>
  **default:** `true`<br>
  **description:** a `true`/`false` parameter for whether or not to download
  any information for the users listed in `net.wagstrom.research.github.users`.
  
* **name:** `net.wagstrom.research.github.miner.organizations`<br>
  **default:** `true`<br>
  **description:** a `true`/`false` parameter for whether or not to download
  any information for the organizations listed in `net.wagstrom.research.github.organizations`.
  
* **name:** `net.wagstrom.research.github.miner.gists`<br>
  **default:** `true`<br>
  **description:** a `true`/`false` parameter for whether or not to download
  any gists at all.
  
* **name:** `net.wagstrom.research.github.dbengine`<br>
  **default:** `neo4j`<br>
  **description:** the name of the [Blueprints][blueprints] database backend
  to use. Right now this has only been tested on `neo4j`, `orientdb`, and
  `tinkergraph`. This feature is dependent on the features present in
  [govscigraph][govscigraph].
  
* **name:** `net.wagstrom.research.github.dburl`<br>
  **default:** `github.db`<br>
  **description:** the URL of the database to save to. For neo4j this is
  simply the directory where the database exists.

sample data
-----------

If you'd like to see the output of gitminer without having to execute it, we
have made two full datasets available on github.

* [gitminer-data-rails][gitminer-data-rails]: a scrape of the rails ecosystem from
the month of May 2012
* [gitminer-data-tinkerpop][gitminer-data-tinkerpop]: a scrape of the projects
in the tinkerpop stack from the month of May 2012

[gh-api-limit]: http://developer.github.com/changes/2012-10-14-rate-limit-changes/
[license]: http://www.apache.org/licenses/LICENSE-2.0.html
[blueprints]: https://github.com/tinkerpop/blueprints
[govscigraph]: https://github.com/pridkett/govscigraph
[gitminer-data-rails]: https://github.com/pridkett/gitminer-data-rails
[gitminer-data-tinkerpop]: https://github.com/pridkett/gitminer-data-tinkerpop
