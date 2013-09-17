#!/bin/bash
pomFiles=`find artifacts/ -name *.pom`
jarFiles=`find artifacts/ -name *.jar`
ascFiles=`find artifacts/ -name *.asc`
if [ -d "bundleTemp" ]; then
  rm -rf bundleTemp
fi
mkdir bundleTemp
for filename in $pomFiles
do
    cp $filename bundleTemp
done
for filename in $jarFiles
do
    cp $filename bundleTemp
done
for filename in $ascFiles
do
    cp $filename bundleTemp
done
cd bundleTemp
jar -cvf bundle.jar *.jar *.pom *.asc
