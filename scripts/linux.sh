#!/bin/bash

SCRIPT_HOME=$(dirname $0)

export LD_LIBRARY_PATH=${LD_LIBRARY_PATH}:${SCRIPT_HOME}/../dab-lib
case $1 in
   isolated-ihm1-c)   ${SCRIPT_HOME}/../dab-bin/$1 2>/dev/pts/1 ;;
   isolated-ihm1-cpp) ${SCRIPT_HOME}/../dab-bin/$1 2>/dev/pts/1 ;;
   isolated-sc-c)     ${SCRIPT_HOME}/../dab-bin/$1 2>/dev/pts/2 ;;
   isolated-sc-cpp)   ${SCRIPT_HOME}/../dab-bin/$1 2>/dev/pts/2 ;;
   *)                 ${SCRIPT_HOME}/../dab-bin/$1 ;;
esac
