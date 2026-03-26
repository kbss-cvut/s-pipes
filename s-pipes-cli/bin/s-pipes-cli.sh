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
MODULE_DIR="$PROJECT_DIR"/..
SRC_DIR="$MODULE_DIR"/src

CLI_JAR=$(find "$MODULE_DIR"/target -maxdepth 1 -name 's-pipes-cli-*.jar' ! -name '*-sources.jar' ! -name '*-javadoc.jar' 2>/dev/null | head -1)

if [ -z "$CLI_JAR" ]; then
    if [ ! -d "$SRC_DIR" ]; then
        echo "Error: s-pipes-cli JAR not found in s-pipes-cli/target/ and no src directory found to rebuild." >&2
        exit 1
    fi
    echo "JAR not found, building s-pipes-cli..." >&2
    mvn -f "$MODULE_DIR"/pom.xml package -DskipTests -q || exit 1
    CLI_JAR=$(find "$MODULE_DIR"/target -maxdepth 1 -name 's-pipes-cli-*.jar' ! -name '*-sources.jar' ! -name '*-javadoc.jar' 2>/dev/null | head -1)
elif [ -d "$SRC_DIR" ]; then
    NEWEST_SRC=$(find "$SRC_DIR" -type f -newer "$CLI_JAR" 2>/dev/null | head -1)
    if [ -n "$NEWEST_SRC" ]; then
        echo "Source files are newer than JAR, rebuilding s-pipes-cli..." >&2
        mvn -f "$MODULE_DIR"/pom.xml package -DskipTests -q || exit 1
        CLI_JAR=$(find "$MODULE_DIR"/target -maxdepth 1 -name 's-pipes-cli-*.jar' ! -name '*-sources.jar' ! -name '*-javadoc.jar' 2>/dev/null | head -1)
    fi
else
    echo "No src directory found, skipping rebuild check." >&2
fi

java $DEBUG -Xmx1024m -jar "$CLI_JAR" "$@"