# Github Mining Tools

**Important (April 13, 2012):** Small changes have been made to the packaging of the code
submitted along with the FSE2012 paper. Please see [artifacts.txt](https://github.com/pridkett/gitminer/blob/master/artifacts.md)
for more information.

**Almost as important:** The rest of this document is out of date.

# Getting Going
This project uses Apache Maven to manage all dependencies and versioning. The
simplest way to get going is to run the following command:
    mvn clean compile package assembly:single

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



