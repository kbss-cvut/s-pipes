#!/bin/bash
# A small helper script for updating the archetype in the local maven repo,
# removing the old test module and generating a new one

# Change the variables below as necessary
ARCHETYPE_GROUP_ID="cz.cvut.kbss"
ARCHETYPE_ARTIFACT_ID="s-pipes-module-archetype"
ARCHETYPE_VERSION="0.4.0"

NEW_MODULE_GROUP_ID="cz.cvut.spipes.modules"
NEW_MODULE_ARTIFACT_ID="test-own-artifact"
NEW_MODULE_NAME="foobar"

ARCHETYPE_DIR=../
SPIPES_MODULES_DIR=$ARCHETYPE_DIR/../s-pipes-modules


cd $ARCHETYPE_DIR
mvn clean install archetype:update-local-catalog -q
echo "Updated archetype in local maven repository"

cd $SPIPES_MODULES_DIR
# Cleanup of old versions
rm -rf $NEW_MODULE_ARTIFACT_ID
sed -i '' "s/<module>$NEW_MODULE_ARTIFACT_ID<\/module>//" pom.xml
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
