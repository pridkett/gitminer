#!/bin/bash

GREMLIN_PATH="$(dirname $0)/../gremlin/gremlin.sh"

# gremlin wrapper script that adds the appropriate libraries to the command
# line this allows us to use the various enums provided through our code within
# gremlin scripts
CLASSPATH=$(find $(dirname $0)/target -name "*.jar" | egrep -v "(slf4j)|(logback)" | tr "\n" : | sed -e 's/:$//'):$(dirname $0)/src/main/gremlin $GREMLIN_PATH $@
