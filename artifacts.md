Roles in a Networked Software Development Ecosystem: A Case Study in GitHub
===========================================================================

* Patrick Wagstrom
* Anita Sarma
* Corey Jergensen

We are pleased to provide both the dataset and the source code to our tool
for those interested in it.

Our data is stored as a Neo4j embedded database and can be downloaded from:

http://bit.ly/wbZbHZ

The source code to our tools can be obtained by visiting:

https://github.com/pridkett/gitminer

If the paper is accepted we will make a virtual machine image available which
will greatly simplify this process.

Please read this entire file as it contains detailed instructions of both
the hard way to get at our data (downloading and compiling our code then
running the scraper yourself) and the easy way (downloading a copy of the
pre-processed graph database).

System Prerequisites
====================

Our system requires that the host environment has a preconfigured installation of
a Java virtual machine and also maven2 installed. If you are running a Mac maven
is easily installable through Homebrew. On Linux it can be found in your package
manager. If you are running Windows you can probably figure it out or borrow a
friends Mac or Linux system. In addition you will need git to obtain the project
source code.

The software has a single dependency that MUST be installed to run the script -
Gremlin, a path traversal language for graph databases. Gremlin also requires
maven and Java to install and compile, but that is it. As of March 2012 the following
instructions should work for installing Gremlin. Here we assume that you keep source
code in `~/src`:

    cd ~/src
    git clone https://github.com/tinkerpop/gremlin.git
    cd gremlin
    mvn clean compile package

Congratulations, you now have a working installation of Gremlin. You can test it
by running the included `gremlin.sh` script.

Downloading GitMiner
====================

GitMiner is the name of the tool that we developed to download code from GitHub. It
is released under the Apache License. Again, assuming that you have already installed
Gremlin to `~/src/gremlin`, the following commands will work for downloading and 
installing GitMiner:

    cd ~/src
    git clone https://pridkett@github.com/pridkett/gitminer.git
    cd gitminer
    git checkout fse2012
    mvn clean compile package

At this point you have a compiled installation of GitMiner and given a proper
configuration file can start to download code to a grph database. Or, you can simply
use the code that we have already provided.

Accessing our Data
==================

There are two pieces of data that you may want to access. The first is the
`configuration.properties` file we used to run GitMiner. Assuming that GitMiner
has been compiled in `~/src/gitminer` you can use the following commands to download
our configuration file and mine the code yourself:

    cd src/gitminer
    wget http://patrick.wagstrom.net/misc/fse2012/configuration.properties
    ./gitminer.sh -c configuration.properties

At this point you will see a lot of debugging output as the system begins to fetch
data at a throttled rate from the GitHub servers. However, this is only part of the
story, after that completes you will also need to load the project source code into
the database. That can be done with these commands:

    cd src/gitminer
    ./repository-loader.sh -c configuration.properties

When these commands complete you will have approximately 2.1GiB of data in
`~/src/gitminer/graph.20120210.db`. Of course this is the really hard way to get the
data. It also takes a VERY long time.

Accessing our Data: The Easy Way
================================

Visit http://bit.ly/wbZbHZ in your web browser. This is a Google docs page for
`graph.db.20120210.tar.gz`, which is an archive our dataset. It is approximately
550MiB. Select `file`->`save` from the menu underneath where is says
`graph.db.20120210.tar.gz` and save the file to `~/src/gitminer`. Now, you can
run the following commands to see our data analysis script in action:

    cd src/gitminer
    tar -zxvf graph.db.20120210.tar.gz
    ./gremlin.sh -e src/main/gremlin/roles.grm

If all goes as planned you should see the complete output of our data as it scrolls
by on the screen. If you filter through the thousands of names and just look at the
numbers and the matrices, you will see that all of our results are there. If you
would rather see the output of our data without running that command it can be
found at http://patrick.wagstrom.net/misc/fse2012/full.txt

Exploring the Data
==================

You can also explore the data using any tool that can explore Neo4j databases,
including the neo4j rest server or neoclipse. Another way to access the data is
to use gremlin from the command line. Here is a quick primer of commands you
can use to access and explore the data using Gremlin:

    cd src/gitminer
    ./gremlin.sh

At this point you are now in a Gremlin shell and should see a prompt that looks like
`gremlin>`. The following commands will do some fun things:

    import net.wagstrom.research.github.IndexNames
    import net.wagstrom.research.github.IdCols
    import net.wagstrom.research.github.EdgeType
    import net.wagstrom.research.github.VertexType
    import net.wagstrom.research.github.PropertyName
    g = new Neo4jGraph("graph.20120210.db")
    rails = g.idx(IndexNames.REPOSITORY).get(IdCols.REPOSITORY, "rails/rails").next()
    m = [:]; rails.bothE.label.groupCount(m).iterate(); m

That will print out a summary of all the edges incident upon the rails project in
the database. 

    rails.in(EdgeType.REPOWATCHED).out(EdgeType.REPOWATCHED).has(PropertyName.FULLNAME, "rack/rack").count()

This gives a count of all of the users who watch both Rails and Rack. These commands
might be a little slow as neo4j slowly memory maps files it needs to access for the
graph traversals.

These graph traversals can be very powerful. For example, lets say we want to look
at all of the users who watch rails, rack, and sinatra and have also filed issues on
rails:

    rails.in(EdgeType.REPOWATCHED).as('user'). \
          out(EdgeType.REPOWATCHED).has(PropertyName.FULLNAME, "rack/rack"). \
          back('user'). \
          out(EdgeType.REPOWATCHED).has(PropertyName.FULLNAME, "sinatra/sinatra"). \
          back('user'). \
          out(EdgeType.ISSUEOWNER).in(EdgeType.ISSUE). \
          has(PropertyName.FULLNAME, "rails/rails"). \
          back('user').dedup().login

Anyway, you get the idea of how cool this data set is. If you would like to see
some summary data about the dataset including the links and properties available
for each node you can visit http://patrick.wagstrom.net/misc/fse2012/overview.txt
That overview can also be generated by running:

    ./gremlin.sh -e src/main/gremlin/overview.grm

Usage Restrictions
==================

The code the project is open source. If you use the project to download any
data from github or you can do whatever you want, we cannot stop you. However,
we ask that you provide a cite to this paper once it is published.

If you use the data set provided for your own research you must, at the very
least, provide a citation to this work that created the work. We prefer that
you make contact with us and engage in building a community around this awesome
technology.

Who knows, it could prove fruitful to both of us.

Also, merely shooting us an email about the data will not give away your identity
as a reviewer. We have made this data and code available to several other groups
of researchers too.
