#!/bin/bash

SCRIPT_HOME=$(dirname $0)

WINEPATH=/usr/lib/gcc/i686-w64-mingw32/7.3-win32\
 LD_LIBRARY_PATH=${LD_LIBRARY_PATH}:${SCRIPT_HOME}/../dab-lib\
 wine ${SCRIPT_HOME}/../dab-bin/$1
