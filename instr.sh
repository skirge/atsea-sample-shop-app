#!/bin/sh
#
# $Id$
#

if [ ! -r $INFILTRATOR_JAR ]; then
	echo "Please put Burp Infiltrator in this directory"
	exit 1
fi

APPSERVER=atsea-sample-shop-app_appserver_1

TARGET_JAR=/app/AtSea-0.0.1-SNAPSHOT.jar
TMP_JAR=/tmp/AtSea-0.0.1-SNAPSHOT.jar

rm -f $TMP_JAR

docker cp $APPSERVER:$TARGET_JAR $TMP_JAR

INFILTRATOR_JAR=`pwd`/burp_infiltrator_java.jar

if [ ! -r ${TMP_JAR} ]; then
	echo "Please build application jar file first!"
	exit 1
fi

grep "portswigger" $TMP_JAR && { echo "Jar seems to be already instrumented!"; exit 1}

WORK_DIR=/tmp/instr-jar

rm -rf $WORK_DIR
mkdir -p $WORK_DIR

unzip -d $WORK_DIR $TMP_JAR

java -jar $INFILTRATOR_JAR --use-http=true --non-interactive --target-paths=$WORK_DIR

pushd $WORK_DIR
zip -0r $TMP_JAR .
popd

docker cp $TMP_JAR $APPSERVER:$TARGET_JAR
docker restart $APPSERVER

