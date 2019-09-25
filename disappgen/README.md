Le sous-projet `disappgen` contient le générateur de code :
* Java
* C++
* C

Il prend en entrée un [fichier XML](../dab.xml) conforme [à ce schéma](../distributed-application.xsd).
Les modèles de code sont écrits en StringTemplate, basé sur un modèle JAXB enrichi de quelques java.util.Map<> quand le modèle ne présente pas les informations de façon adéquates pour StringTemplate.
