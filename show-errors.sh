#!/bin/bash

find . -name compile.log | xargs grep -iE 'error:|erreur|attention|warning:|Stop.|Arrêt.'
exit 0
