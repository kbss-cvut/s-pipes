#!/bin/bash
BASEDIR="/Users/qwerty/Documents/documents/university/masters/semester-4/semestral-project/personal-fork/s-pipes-modulegen/s-pipes-modules-utils/"
SPIPESDIR="/Users/qwerty/Documents/documents/university/masters/semester-4/semestral-project/personal-fork/s-pipes-modulegen/s-pipes-modules/"

cd $BASEDIR/s-pipes-module-archetype
mvn clean install
echo "Updated archetype in local maven repository"

cd $SPIPESDIR
# Cleanup of old versions
rm -rf test-own-artifact
sed -i '' 's/<module>test-own-artifact<\/module>//' pom.xml

mvn archetype:generate \
  -DinteractiveMode=false \
  -DarchetypeGroupId=cz.cvut.kbss \
  -DarchetypeArtifactId=s-pipes-module-archetype \
  -DarchetypeVersion=1.0-SNAPSHOT \
  -DgroupId=cz.cvut.spipes.modules \
  -DartifactId=test-own-artifact \
  -DmoduleName=foobar
cd $BASEDIR
echo "Regenerated the test module"
