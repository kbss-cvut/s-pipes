#!/bin/bash

# Script provides various utilities to work with semantic pipelines. 

#
# resolve symlinks
#

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

java $DEBUG -Xmx1024m  -jar "$PROJECT_DIR"/"s-pipes-cli.jar"  "$@"
