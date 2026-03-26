#!/bin/bash

# Script provides various utilities to migrate semantic pipelines.

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

MIGRATION_JAR=$(find "$PROJECT_DIR"/../target -maxdepth 1 -name 's-pipes-migration-*.jar' ! -name '*-sources.jar' ! -name '*-javadoc.jar' 2>/dev/null | head -1)

if [ -z "$MIGRATION_JAR" ]; then
    echo "Error: s-pipes-migration JAR not found in s-pipes-migration/target/. Run 'mvn package' first." >&2
    exit 1
fi

java $DEBUG -Xmx1024m -jar "$MIGRATION_JAR" "$@"
