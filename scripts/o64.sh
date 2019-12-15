#!/bin/bash

SCRIPT_HOME=$(dirname $0)

LD_LIBRARY_PATH=${LD_LIBRARY_PATH}:${SCRIPT_HOME}/../dab-lib darling shell ./run-$(EXEC_CPP)-o64.sh ${SCRIPT_HOME}/../dab-bin/$1
