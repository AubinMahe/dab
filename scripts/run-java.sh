#/bin/bash

# usage run-java.sh isolated.sc

SCRIPT_HOME=$(dirname $0)
JAR_NAME=$1

java\
 --module-path=/usr/lib/jvm/openjfx-sdk-13/lib\
 --add-modules=javafx.controls,javafx.fxml,javafx.graphics\
 -jar ${SCRIPT_HOME}/../dab-bin/${JAR_NAME}.jar
