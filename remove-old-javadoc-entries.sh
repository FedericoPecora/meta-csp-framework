#!/bin/bash
files=`find javadoc/ -name [A-Z]*.html`
for filename in $files
do
    srcName=${filename/javadoc/trunk\/src\/main\/java}
    srcName=${srcName%.*}".java"
    if [ ! -f $srcName ]; then
	num=`echo "$srcName" | grep -o "\." | wc -l`
	if [ $num == 1 ]; then
	    echo $srcName" not found, removing javadoc entry."
	    rm $filename
	fi
    fi
	
done
