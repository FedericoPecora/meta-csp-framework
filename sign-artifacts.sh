#!/bin/bash
pomFiles=`find artifacts/ -name *.pom`
jarFiles=`find artifacts/ -name *.jar`
for filename in $pomFiles
do
    gpg -ab $filename
done
for filename in $jarFiles
do
    gpg -ab $filename
done
