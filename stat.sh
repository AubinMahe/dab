#!/bin/bash

compte() {
   nom=$1
   liste=$nom
   printf '%-8s : ' $nom
   cat /tmp/$liste | while read file
   do
      cat $file
   done | wc -l
}

find . -type f -not -path '*/\.git/*' | sort > /tmp/tous
find . -name '*.java' \
   -or -name '*.css' \
   -or -name '*.fxml' \
   -or -name '*.properties'\
   -or -name '*.stg'       >  /tmp/java
echo ./disappgen/README.md >> /tmp/java
find . -name '*.hpp' -or -name '*.cpp'> /tmp/cpp
find . -name '*.h'   -or -name '*.c'  > /tmp/c
find . -name '*.project' -or -name '*.cproject' -or -name '*.classpath' -or -name language.settings.xml > /tmp/projets
find . -name build.xml -or -name Makefile >  /tmp/builders
echo ./stat.sh                            >> /tmp/builders
find . -type f | grep ./lib/ > /tmp/libs
echo ./dab.xml                     >  /tmp/model
echo ./distributed-application.xsd >> /tmp/model
echo ./README.md                   >> /tmp/model
echo ./LICENSE                     >> /tmp/model
find . -name '.gitignore' > /tmp/git
sort /tmp/java /tmp/cpp /tmp/c /tmp/projets /tmp/builders /tmp/libs /tmp/model /tmp/git > /tmp/tous2
comm -3 /tmp/tous /tmp/tous2 > /tmp/a_ventiler
count=$(cat /tmp/a_ventiler | wc -l)
if [ $count -gt 0 ]
then
   echo Les fichiers suivants doivent être ventilés dans les catégorie appropriées :
   cat /tmp/a_ventiler
else
   echo Nombre de lignes de texte par catégorie :
   compte git
   compte builders
   compte model
   compte c
   compte cpp
   compte projets
   compte java
fi

