#!/bin/bash

find . -name compile.log | xargs grep -iE 'error:|warning:|Stop.'
exit 0
