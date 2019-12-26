Le sous-projet `disappgen` contient le générateur de code :
* Java
* C++
* C

Il prend en entrée un [fichier XML](../dab.xml) conforme [à ce schéma](../distributed-application.xsd).

Les modèles de code sont écrits en StringTemplate, exploitant un modèle ad'hoc qui tire profit des classes générés par JAXB.
