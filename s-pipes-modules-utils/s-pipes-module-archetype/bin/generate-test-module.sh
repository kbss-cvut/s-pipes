#!/bin/bash
# A small helper script for updating the archetype in the local maven repo,
# removing the old test module and generating a new one

# Change the variables below as necessary
NEW_MODULE_GROUP_ID="cz.cvut.spipes.modules"
NEW_MODULE_ARTIFACT_ID="test-own-artifact"
NEW_MODULE_NAME="foobar"

echo "-------------------------------------------------------------------------"
echo "Step 1: Getting the directory paths"

SCRIPT_PATH=$(dirname $(dirname "$(readlink -f "$0")"))
ARCHETYPE_DIR=$SCRIPT_PATH/..
SPIPES_MODULES_DIR=$ARCHETYPE_DIR/../s-pipes-modules

echo "SCRIPT_PATH = $SCRIPT_PATH"
echo "ARCHETYPE_DIR = $ARCHETYPE_DIR"
echo "SPIPES_MODULES_DIR = $SPIPES_MODULES_DIR"


echo "-------------------------------------------------------------------------"
echo "Step 2: Gathering info..."
cd "$ARCHETYPE_DIR" || exit 1

ARCHETYPE_VERSION="$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)"
echo "ARCHETYPE_VERSION = $ARCHETYPE_VERSION"

ARCHETYPE_GROUP_ID="$(mvn help:evaluate -Dexpression=project.groupId -q -DforceStdout)"
echo "ARCHETYPE_GROUP_ID = $ARCHETYPE_GROUP_ID"

ARCHETYPE_ARTIFACT_ID="$(mvn help:evaluate -Dexpression=project.artifactId  -q -DforceStdout)"
echo "ARCHETYPE_ARTIFACT_ID = $ARCHETYPE_ARTIFACT_ID"

cd "$SPIPES_MODULES_DIR" || exit 2
MODULE_PARENT_VERSION="$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)"
echo "MODULE_PARENT_VERSION = $MODULE_PARENT_VERSION"


echo "-------------------------------------------------------------------------"
echo "Step 3: Updating local maven archetype catalog"
cd "$ARCHETYPE_DIR" || exit 3
mvn clean install archetype:update-local-catalog
echo "Updated local maven archetype catalog"


echo "-------------------------------------------------------------------------"
# Convenience step — makes it easy to repeatedly re-create a module.
echo "Step 4: Cleaning up in pom in $SPIPES_MODULES_DIR"
cd "$SPIPES_MODULES_DIR" || exit 4
rm -rf $NEW_MODULE_ARTIFACT_ID
sed -i "/\<module\>$NEW_MODULE_ARTIFACT_ID\<\/module\>/d" pom.xml
echo "Cleaned up any existing old versions"


echo "-------------------------------------------------------------------------"
echo "Step 5: Generating a new module from archetype $ARCHETYPE_GROUP_ID:$ARCHETYPE_ARTIFACT_ID:$ARCHETYPE_VERSION"
mvn archetype:generate \
  -DinteractiveMode=false \
  -DarchetypeGroupId=$ARCHETYPE_GROUP_ID \
  -DarchetypeArtifactId=$ARCHETYPE_ARTIFACT_ID \
  -DarchetypeVersion=$ARCHETYPE_VERSION \
  -DgroupId=$NEW_MODULE_GROUP_ID\
  -DartifactId=$NEW_MODULE_ARTIFACT_ID \
  -DmoduleName=$NEW_MODULE_NAME \
  -DmoduleParentVersion="$MODULE_PARENT_VERSION"
echo "Generating a new module"

echo "-------------------------------------------------------------------------"
# Additional whitespaces in s-pipes-modules/pom.xml is an unresolved bug related to Java 9:
# https://issues.apache.org/jira/browse/ARCHETYPE-584
echo "Step 6: Removing redundant whitespaces from s-pipes-modules/pom.xml"
cd "$SPIPES_MODULES_DIR" || exit 5
sed -i '' '/^[[:space:]]*$/d' pom.xml
