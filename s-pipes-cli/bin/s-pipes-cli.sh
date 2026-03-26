#!/bin/bash

# Script provides various utilities to work with semantic pipelines. 

PRG=$0
#DEBUG=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005
DEBUG=

while [ -h "$PRG" ]; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '^.*-> \(.*\)$' 2>/dev/null`
    if expr "$link" : '^/' 2> /dev/null >/dev/null; then
	PRG="$link"
    else
	PRG="`dirname "$PRG"`/$link"
    fi
done

PROJECT_DIR=`dirname "$PRG"`

CLI_JAR=$(find "$PROJECT_DIR"/../target -maxdepth 1 -name 's-pipes-cli-*.jar' ! -name '*-sources.jar' ! -name '*-javadoc.jar' 2>/dev/null | head -1)

if [ -z "$CLI_JAR" ]; then
    echo "Error: s-pipes-cli JAR not found in s-pipes-cli/target/. Run 'mvn package' first." >&2
    exit 1
fi

java $DEBUG -Xmx1024m -jar "$CLI_JAR" "$@"