#!/bin/bash
# A small helper script for updating the archetype in the local maven repo,
# removing the old test module and generating a new one

# Change the variables below as necessary
NEW_MODULE_GROUP_ID="cz.cvut.spipes.modules"
NEW_MODULE_ARTIFACT_ID="test-own-artifact"
NEW_MODULE_NAME="foobar"

SCRIPT_PATH=$(dirname "$(readlink -f "$0")")

ARCHETYPE_DIR=$SCRIPT_PATH/..
SPIPES_MODULES_DIR=$ARCHETYPE_DIR/../../s-pipes-modules

cd $ARCHETYPE_DIR
ARCHETYPE_VERSION="$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)"
ARCHETYPE_GROUP_ID="$(mvn help:evaluate -Dexpression=project.groupId -q -DforceStdout)"
ARCHETYPE_ARTIFACT_ID="$(mvn help:evaluate -Dexpression=project.artifactId  -q -DforceStdout)"

mvn clean install archetype:update-local-catalog -q
echo "Updated archetype in local maven repository"

cd $SPIPES_MODULES_DIR
# Cleanup of old versions
rm -rf $NEW_MODULE_ARTIFACT_ID
sed -i "s/<module>$NEW_MODULE_ARTIFACT_ID<\/module>//" pom.xml
echo "Cleaned up the old version"

mvn archetype:generate \
  -DinteractiveMode=false \
  -DarchetypeGroupId=$ARCHETYPE_GROUP_ID \
  -DarchetypeArtifactId=$ARCHETYPE_ARTIFACT_ID \
  -DarchetypeVersion=$ARCHETYPE_VERSION \
  -DgroupId=$NEW_MODULE_GROUP_ID\
  -DartifactId=$NEW_MODULE_ARTIFACT_ID \
  -DmoduleName=$NEW_MODULE_NAME -q
echo "Regenerated the test module"
