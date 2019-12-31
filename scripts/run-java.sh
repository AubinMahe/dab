#!/bin/bash

# usage run-java.sh isolated-sc-java

SCRIPT_HOME=$(dirname $0)
JAR_NAME=$1
if [ "${JAR_NAME}" == "isolated-ihm1-scripted" ]
then
   sleep 5
   java\
    --module-path=/usr/lib/jvm/openjfx-sdk-13/lib\
    --add-modules=javafx.controls,javafx.fxml,javafx.graphics\
    -jar ${SCRIPT_HOME}/../dab-bin/${JAR_NAME}.jar $2 $3 $4 2>/dev/null
else
   if [ $# -eq 2 ]
   then
      java\
       --module-path=/usr/lib/jvm/openjfx-sdk-13/lib\
       --add-modules=javafx.controls,javafx.fxml,javafx.graphics\
       -jar ${SCRIPT_HOME}/../dab-bin/${JAR_NAME}.jar 2>${SCRIPT_HOME}/../dab-bin/${JAR_NAME}-java.log
    else
      java\
       --module-path=/usr/lib/jvm/openjfx-sdk-13/lib\
       --add-modules=javafx.controls,javafx.fxml,javafx.graphics\
       -jar ${SCRIPT_HOME}/../dab-bin/${JAR_NAME}.jar
    fi
fi
