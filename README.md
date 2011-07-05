# Github Mining Tools

# Getting Going
This project uses Apache Maven to manage all dependencies and versioning. The
simplest way to get going is to run the following command:
    mvn clean compile assembly:single

This will clean the source tream, compile the code, and finally copy all the
libraries to a location that makes some modicum of sense. Then to run the
mining scripts just run:
    ./github.sh

# Configuration
Project configuration is controlled through the `configuration.properties`
file in `src/main/resources`. Don't commit it with your github username
and apitoken, that would be bad.
