# Distributeur automatique de billets distribué - Support pédagogique

## Architecture et framework

L'application est décrite par des **composants**. Les relations entre composants sont décrites sous formes d'**interfaces** **offertes** ou **requises**. Les interfaces contiennent des **événements**, certains de types **[requêtes/réponses](https://en.wikipedia.org/wiki/Request%E2%80%93response)** et des **données partagées** selon le modèle **[publish/subscribe](https://fr.wikipedia.org/wiki/Publish-subscribe)**. Les événements sont composés d'un ensemble de **données** de types **scalaire**, **énuméré** ou **agrégat**.

Les logiques applicatives peuvent en grande partie être décrites sous forme d'**[automates finis](https://fr.wikipedia.org/wiki/Automate_fini)**. Le support des événements temporisés ou *timeouts* est fourni par le modèle. Les actions associées à l'entrée dans un état peuvent armer ou annuler des *timeouts*. Le déclenchement de ces derniers donne lieu à l'émission d'événements de type *loopback*, reçus par le composant comme un événement réseau, dans le même thread.

Le méta modèle de ce genre d'application est décrit par le schéma XML (xsd) : [distributed-application.xsd](distributed-application.xsd) alors que le modèle d'une application est décrit par un document XML : [dab.xml](dab.xml)

La régularité de l'architecture sous-tend la régularité du code, une grande partie de l'application est donc générée :
- Le code de communication, création, envoi et réception des messages UDP (événements)
- Le code d'activation des acteurs concerné par les événements reçus du réseau et des *timeouts* (routage)

Il a été développé quelques bibliothèques de classes (Java et C++) et de fonctions C afin de :
- Rendre homogène le traitement des messages et des événements quel que soit le langage cible.
- Faciliter le portage entre les OS GNU/Linux et MS-Windows

Sauf pour Java, les librairies n'ont pas recourt à l'allocation dynamique de mémoire. C'est pour cette raison que, en C++ :
- la classe `std::string` a finalement été remplacée par des chaînes de longueurs fixes et pré allouées (le modèle spécifie les longueurs)
- les exceptions standard de `stdexcept` ont été remplacées par des exceptions spécifiques dérivées de `std::exception` dotées de fonctionnalités de localisation plus avancées notamment la gestion de la pile d'appel *à la Java*.

La génération de code, réalisée en Java, s'appuie sur un modèle commun et trois modèles `StringTemplate` spécifiques des trois langages cibles. La liste des sources générée est également produite pour faciliter l'écriture des *makefiles* au moyen d'un quatrième modèle `StringTemplate`.

## Les projets :

- **sc** pour **Site Central** : une application IHM en Java qui montre l'état des comptes bancaires et des cartes de crédit
- **udt** pour **Unité de Traitement** : l'application où est implémentée la logique du système, hors IHM
     - Une implantation en C
     - Une implantation en C++
     - Une implantation en Java
- **dab** pour **Distributeur Automatique de Billets** : une application IHM en Java qui permet d'en simuler l'usage par un utilisateur final.

## Construire et exécuter les projets :

**Pour construire** le projet, il est nécessaire de disposer de :
- **Java**, dont la version doit être supérieure ou égale à 8, [OpenJDK](https://adoptopenjdk.net/) *latest* est vivement recommandé.
- Du compilateur **[JAXB](https://javaee.github.io/jaxb-v2/)** `xjc`, qu'on installe sous GNU/Linux Debian/Ubuntu/Mint par `sudo apt install xjc` 
- Du compilateur **[C11 et C++17](https://gcc.gnu.org/)** pour GNU/Linux et compilateur croisé, qu'on installe sous GNU/Linux Debian/Ubuntu/Mint par `sudo apt install gcc gcc-mingw-w64`
- De l'outil de production **[Apache Ant](https://ant.apache.org/)**, qu'on installe sous GNU/Linux Debian/Ubuntu/Mint par `sudo apt install ant`
- De l'outil de production **[GNU Make](https://www.gnu.org/software/make/)** qu'on installe sous GNU/Linux Debian/Ubuntu/Mint par `sudo apt install make`
- Les **bibliothèques JAXB** nécessaires, [qui ne sont plus fournies avec le JDK 11](https://www.jesperdj.com/2018/09/30/jaxb-on-java-9-10-11-and-beyond/) sont dans [lib](lib).

**Pour les impatients :**
Se placer à la racine du projet et entrer `ant` pour construire toutes les versions : Java, C et C++ pour les cibles GNU/Linux et MinGW/Windows.

**Différents déploiements** exécutables :
- Pour exécuter 1 sc en java, 1 dab en java et 1 udt en java: `ant run-java`
- Pour exécuter 1 sc en java, 1 dab en java et 1 udt en C pour GNU/Linux : `ant run-c` 
- Pour exécuter 1 sc en java, 1 dab en java et 1 udt en C pour MinGW/Windows : `ant run-c-win32` 
- Pour exécuter 1 sc en java, 1 dab en java et 1 udt en C++ pour GNU/Linux : `ant run-cpp`
- Pour exécuter 1 sc en java, 1 dab en java et 1 udt en C++ pour MinGW/Windows : `ant run-cpp-win32`
- Pour exécuter 1 sc en java, 2 dab en java et 2 udt en java : `ant run-java-2`
- Pour exécuter 1 sc en java, 2 dab en java et 2 udt en C pour GNU/Linux : `ant run-c-2` 
- Pour exécuter 1 sc en java, 2 dab en java et 2 udt en C pour MinGW/Windows : `ant run-c-win32-2` 
- Pour exécuter 1 sc en java, 2 dab en java et 2 udt en C++ pour GNU/Linux : `ant run-cpp-2`
- Pour exécuter 1 sc en java, 2 dab en java et 2 udt en C++ pour MinGW/Windows : `ant run-cpp-win32-2`

Les déploiements à plusieurs dab et plusieurs udt permettent de vérifier le routage correct des réponses aux requêtes.

**En pas-à-pas**, pour comprendre :
1. Générer le code JAXB à partir du schéma [distributed-application.xsd](distributed-application.xsd) : `(cd disappgen && ant jaxb-gen)`
1. Compiler et packager le générateur de code : `(cd disappgen && ant jar)`
1. Générer le code de l'application à partir du document XML [dab.xml](dab.xml) : `ant generate-all-sources` 
1. Compiler dans l'ordre :
    * util-c    : `(cd util-c && make)`, produit également `util-c-win32`
    * udt-c     : `(cd udt-c && make)`, produit également `udt-c-win32`
    * util-cpp  : `(cd util-cpp && make)`, produit également `util-cpp-win32`
    * udt-cpp   : `(cd udt-cpp && make)`, produit également `udt-cpp-win32`
    * util-java : `(cd util-java && ant)`
    * sc        : `(cd sc && ant)`
    * dab       : `(cd dab && ant)`

**Pour exécuter** les projets, un environnement minimal doit suffire, aucune bibliothèque *runtime* n'est utilisée.

## Reste à faire
<ol>
<li>L'implémentation actuelle utilise des messages UDP pour connecter les composants, ce qui couvre les trois cas :<ul>
<li>composants distribués sur plusieurs machines</li>
<li>composants distribués sur plusieurs processus (éventuellement plusieurs langages)</li>
<li>composants regroupés au sein d'un même processus (mono-langage)<br />
Dans ce dernier cas, <b>des appels directs entre composants seraient plus performants</b>.</li> 
</ul>
<li>Automate : associer une action au franchissement d'une transition.</li>
</ol>

## Boite à idées, à débattre...

1. Adopter un modèle d'exécution non plus asynchrone et temps-réel comme à présent mais plutôt cyclique et par pas de temps discret, avec une méthode d'activation qui donne la main dans un ordre déterminé aux différents acteurs, chronomètres compris. Cela permettrait de rendre l'exécution plus contrôlable en environnement de test.
1. On sent qu'il serait possible de mener une campagne de tests exhaustive de chaque composant avec [JUnit](https://junit.org/junit5/), [CUnit](http://cunit.sourceforge.net/) ou [CppUnit](http://wiki.c2.com/?CppUnit). Une surcouche de ces frameworks de test en *boite noire*, c'est à dire au niveau *réseau UDP* est à développer pour en tirer le maximum de profit. A voir si ça ne revient pas à écrire totalement l'application... Pour un exemple simple comme le dab, l'application de test serait plus riche que l'application nominale...
