# Distributeur automatique de billets distribué - Support pédagogique

## Architecture et framework

L'application est décrite par des **composants**. Les relations entre composants sont décrites sous formes d'**interfaces** **offertes** ou **requises**. Les interfaces contiennent des **événements**, certains de types **[requêtes/réponses](https://en.wikipedia.org/wiki/Request%E2%80%93response)** et des **données partagées** selon le modèle **[publish/subscribe](https://fr.wikipedia.org/wiki/Publish-subscribe)**. La réception d'un événement active un traitement qui à son tour peut déclencher l'émission d'un ou plusieurs événements d'une interface. Les événements sont composés d'un ensemble de **données** de types **scalaire**, **énuméré** ou **agrégat**. De fonction

Le méta modèle de ce genre d'application est décrit par le schéma XML (xsd) : [distributed-application.xsd](distributed-application.xsd) alors que le modèle d'une application est décrit par un document XML : [dab.xml](dab.xml)

La régularité de l'architecture sous-tend la régularité du code, une grande partie de l'application est donc générée :
- Le code de communication, création, envoi et réception des messages UDP (événements)
- Le code d'activation des acteurs concerné par les événements reçus (routage)

Il a été développé quelques bibliothèques de classes (Java et C++) et de fonctions C afin de :
- Rendre homogène le traitement des messages et des événements quel que soit le langage cible.
- Faciliter le portage entre les OS GNU/Linux et MS-Windows
- Sauf pour Java, les librairies n'ont pas recourt à l'allocation dynamique de mémoire. C'est pour cette raison que la classe std::string a finalement été remplacée par des chaînes de longueurs fixes et pré allouées (le modèle spécifiée les longueurs), que les exceptions dérivées de std::exception ont été remplacées par des exceptions spécifiques dotées de fonctionnalités de localisation plus avancées.

Ainsi, la génération de code s'appuie sur un modèle commun et par trois modèles `StringTemplate` spécifiques des trois langages cibles.

## Les projets :

- **sc** pour **Site Central** : une application Java qui montre l'état des comptes bancaires et des cartes de crédit
- **udt** pour **Unité de Traitement** : l'application où est implémentée la logique du système, hors IHM
  - Une implantation en C, sans allocation dynamique de mémoire (aucun appel à `malloc`)
  - Une implantation en C++, assez proche de ce qu'on pourrait faire en Java
- **dab** pour **Distributeur Automatique de Billets** : une application Java qui permet l'interaction avec l'utilisateur final

## Logique applicative

La logique applicative dans **Unité de Traitement** est en grande partie décrite par un **[automate fini](https://fr.wikipedia.org/wiki/Automate_fini)**. Le support des événements temporisés ou *timeouts* est fourni par les bibliothèques. Les actions associées aux entrées dans un état mettent en oeuvre l'armement et l'annulation des *timeouts*.

## Construire et exécuter les projets :

**Pour les impatients :**
Se placer à la racine du projet et entrer :
- Pour construire les versions Java, C et C++ pour les cibles GNU/Linux et MinGW : `ant` 
- Pour exécuter seulement la version Java : `ant run-java` 
- Pour exécuter les versions Java (sc et dab) et C GNU/Linux (udt-c) et MinGW (udt-c-win32) : `ant run-c` 
- Seulement les versions Java (sc et dab) et C++ GNU/Linux (udt-cpp) et MinGW (udt-cpp-win32) : `ant run-cpp`

**Pour construire** le projet, il est nécessaire de disposer de :
- **Java**, dont la version doit être supérieure ou égale à 8, [OpenJDK](https://adoptopenjdk.net/) *latest* est vivement recommandé.
- Du compilateur **[JAXB](https://javaee.github.io/jaxb-v2/)** `xjc`, qu'on installe sous GNU/Linux Debian/Ubuntu/Mint par `sudo apt install xjc` 
- Du compilateur **[C11 et C++17](https://gcc.gnu.org/)** pour GNU/Linux et compilateur croisé, qu'on installe sous GNU/Linux Debian/Ubuntu/Mint par `sudo apt install gcc gcc-mingw-w64`
- De l'outil de production **[Apache Ant](https://ant.apache.org/)**, qu'on installe sous GNU/Linux Debian/Ubuntu/Mint par `sudo apt install ant`
- De l'outil de production **[GNU Make](https://www.gnu.org/software/make/)** qu'on installe sous GNU/Linux Debian/Ubuntu/Mint par `sudo apt install make`
- Les **bibliothèques JAXB** nécessaires, [qui ne sont plus fournies avec le JDK 11](https://www.jesperdj.com/2018/09/30/jaxb-on-java-9-10-11-and-beyond/) sont dans [lib](lib).

En pas-à-pas, pour comprendre :
1. Générer le code JAXB à partir du schéma [distributed-application.xsd](distributed-application.xsd) : `(cd disappgen && ant jaxb-gen)`
1. Compiler et packager le générateur de code : `(cd disappgen && ant jar)`
1. Générer le code de l'application à partir du document XML [dab.xml](dab.xml) : `ant generate-all-sources` 
1. Compiler dans l'ordre :
    * util-c    : `(cd util-c && make)`
    * udt-c     : `(cd udt-c && make)`
    * util-cpp  : `(cd util-cpp && make)`
    * udt-cpp   : `(cd udt-cpp && make)`
    * util-java : `(cd util-java && ant)`
    * sc        : `(cd sc && ant)`
    * dab       : `(cd dab && ant)`

**Pour exécuter** les projets, un environnement minimal doit suffire, aucune bibliothèque n'est utilisée.

## Reste à faire

1. Le code des événements temporisés est exécuté dans un autre thread que le thread principal. Actuellement, le partage des ressources liées à la communication (buffer, socket...) n'est pas géré. Plutôt que d'utiliser des exclusions mutuelles, il faudrait poster un message d'activation du traitement concerné pour retourner au cas général. Ainsi, **tous** les traitements applicatifs serait effectués par le même thread, sans concurrence.
1. Adopter un modèle d'exécution non plus asynchrone et temps-réel comme à présent mais par pas de temps discret, avec une méthode d'activation qui donne la main dans un ordre déterminé aux différents acteurs, chronomètres compris. Cela permettrait de pauser une exécution et de la reprendre puisque le temps serait simulé.
1. Automate : associer une action au franchissement d'une transition.
1. On sent qu'il serait possible de mener une campagne de tests exhaustive de chaque composant avec [JUnit](https://junit.org/junit5/), [CUnit](http://cunit.sourceforge.net/) ou [CppUnit](http://wiki.c2.com/?CppUnit).
