#!/usr/bin/env bash

NAME=service-validator
JAR=$NAME.jar
VERSION=$(cat VERSION)
KEY=$HOME/.ssh/lappsgrid-shared-key.pem
TGZ=$NAME-$VERSION.tgz
LATEST=$NAME-latest.tgz

if [ ! -e target/$JAR ] ; then
	echo "JAR file is missing.  Please build the project first."
	exit 1
fi

cp LICENSE.txt target/
cp README.md target/
cp service-validator target/
cd target
tar czf $TGZ $JAR $NAME LICENSE.txt README.md

if [ -e $KEY ] ; then
	scp -i $KEY $TGZ root@downloads.lappsgrid.org:/var/lib/downloads
	if [ "$1" = "--latest" ] ; then
	    cp $TGZ $LATEST
	    scp -i $KEY $LATEST root@downloads.lappsgrid.org:/var/lib/downloads
	fi
fi
