# Distributeur automatique de billets distribué - Support pédagogique

## Architecture et framework

L'application est décrite par des **composants**. Les relations entre composants sont décrites sous formes d'**interfaces** **offertes** ou **requises**. Les interfaces contiennent des **événements**, certains de types **[requêtes/réponses](https://en.wikipedia.org/wiki/Request%E2%80%93response)** et des **données partagées** selon le modèle **[publish/subscribe](https://fr.wikipedia.org/wiki/Publish-subscribe)**. Les événements sont composés d'un ensemble de **données** de types **scalaire**, **énuméré** ou **agrégat**.

Les logiques applicatives peuvent en grande partie être décrites sous forme d'**[automates finis](https://fr.wikipedia.org/wiki/Automate_fini)**. Le support des événements temporisés ou *timeouts* est fourni par le modèle. Les actions associées à l'entrée dans un état peuvent armer ou annuler des *timeouts*. Le déclenchement de ces derniers donne lieu à l'émission d'événements de type *loopback*, reçus par le composant comme un événement réseau, dans le même thread. Un [plugin Eclipse](org.hpms.automaton/org.hpms.automaton.usite/target/org.hpms.automaton.usite-1.0.0.zip) a été développé pour éditer au mieux les automates : voici l'exemple appliqué à [l'unité de traitement](org.hpms.automaton/snapshot.png). Il est basé sur un [méta-modèle](distributed-application-automaton.xsd) déposé.

Le méta modèle de ce genre d'application est décrit par le schéma XML (xsd) : [distributed-application.xsd](distributed-application.xsd) alors que le modèle d'une application est décrit par un document XML : [dab.xml](dab.xml) mais un langage spécifique a été créé, le *DAL* au moyen du [framework Xtext](https://www.eclipse.org/Xtext/). Le modèle *DAL* est disponible [ici](dab.dal). Il génère, au moyen d'[Xtend](https://www.eclipse.org/xtend/), le fichier XML [dab.xml](dab.xml).

La régularité de l'architecture sous-tend la régularité du code, une grande partie de l'application est donc générée :
- Le code de communication, création, envoi et réception des messages UDP (événements)
- Le code d'activation des acteurs concerné par les événements reçus du réseau et des *timeouts* (routage)

Dans le diagramme de classe UML du composant 'Distributeur' (IHM) ci-dessous, seul la classe `Distributeur` est manuelle :

![UML class diagram](dab-class-diagram.png "Diagramme de classe UML du composant 'Distributeur' (IHM)")
 
Il a été développé quelques bibliothèques de classes (Java et C++) et de fonctions C afin de :
- Rendre homogène le traitement des messages et des événements quel que soit le langage cible : <a href="https://fr.wikipedia.org/wiki/C_(langage)">C</a>, <a href="https://fr.wikipedia.org/wiki/C%2B%2B">C++</a> et <a href="https://fr.wikipedia.org/wiki/Java_(langage)">Java</a>.
- Faciliter le portage entre les OS <a href="https://fr.wikipedia.org/wiki/Linux">GNU/Linux</a>, <a href="https://fr.wikipedia.org/wiki/Microsoft_Windows">Microsoft Windows</a> et <a href="https://fr.wikipedia.org/wiki/MacOS">macOS</a>

Sauf pour Java, les librairies n'ont pas recourt à l'allocation dynamique de mémoire. C'est pour cette raison que, en C++ :
- la classe `std::string` a finalement été remplacée par des chaînes de longueurs fixes et pré allouées (le modèle spécifie les longueurs)
- les exceptions standard de `stdexcept` ont été remplacées par des exceptions spécifiques dérivées de `std::exception` dotées de fonctionnalités de localisation plus avancées notamment la gestion de la pile d'appel *à la Java*.

La génération de code, réalisée en Java, s'appuie sur un modèle commun et trois modèles `StringTemplate` spécifiques des trois langages cibles. La liste des sources générée est également produite pour faciliter l'écriture des *makefiles* au moyen d'un quatrième modèle `StringTemplate`.

## Les projets :

- **Banque** : une application IHM en C, C++ ou Java qui montre l'état des comptes bancaires et des cartes de crédit
- **Contrôleur** : l'application où est implémentée la logique du système, hors IHM en C, C++ ou Java
- **Distributeur** : une application IHM en C, C++ ou Java qui permet de simuler l'usage d'un distributeur automatique de billet par un utilisateur final.

## Construire et exécuter les projets :

**Pour construire** le projet, il est nécessaire de disposer de :
- **Java**, dont la version doit être supérieure ou égale à 8, [OpenJDK](https://adoptopenjdk.net/) *latest* est vivement recommandé.
- Du compilateur **[JAXB](https://javaee.github.io/jaxb-v2/)** `xjc`, qu'on installe sous GNU/Linux Debian/Ubuntu/Mint par `sudo apt install xjc`
- Du compilateur **[C11 et C++17](https://gcc.gnu.org/)** pour GNU/Linux et compilateur croisé, qu'on installe sous GNU/Linux Debian/Ubuntu/Mint par `sudo apt install gcc gcc-mingw-w64`
- Du [compilateur croisé Linux/macOS](https://github.com/tpoechtrager/osxcross)
- De [WineHQ](https://www.winehq.org/) pour exécuter sous Linux des binaires MS-Windows et de son équivalent pour macOS : [DarlingHQ](https://www.darlinghq.org/)
- De l'outil de production **[Apache Ant](https://ant.apache.org/)**, qu'on installe sous GNU/Linux Debian/Ubuntu/Mint par `sudo apt install ant`
- De l'outil de production **[GNU Make](https://www.gnu.org/software/make/)** qu'on installe sous GNU/Linux Debian/Ubuntu/Mint par `sudo apt install make`
- Les **bibliothèques JAXB** nécessaires, [qui ne sont plus fournies avec le JDK 11](https://www.jesperdj.com/2018/09/30/jaxb-on-java-9-10-11-and-beyond/) sont dans [lib](lib).

**Pour les impatients**, se placer à la racine du projet et entrer `ant` pour construire toutes les versions : Java, C et C++ pour les cibles GNU/Linux, MinGW/Windows et macOS.

**Les traces d'exécution** sont supporté par un jeu de macros qui les route vers la sortie standard d'erreur. Afin d'éviter de polluer la console, les scripts de lancement les redirigent vers /dev/pts/xx, les pseudo-terminaux Linux.
Pour lancer ces terminaux, taper `start-ttys`.

**Différents déploiements** exécutables :
- Pour exécuter 1 Banque, 1 Distributeur et 1 Contrôleur en java: `ant run-java`
- Pour exécuter 1 Banque, 1 Distributeur et 1 Contrôleur en C pour GNU/Linux : `ant run-c`
- Pour exécuter 1 Banque, 1 Distributeur et 1 Contrôleur en C pour MinGW/Windows : `ant run-c-win32`
- Pour exécuter 1 Banque, 1 Distributeur et 1 Contrôleur en C pour macOS/Darling : `ant run-c-o64`
- Pour exécuter 1 Banque, 1 Distributeur et 1 Contrôleur en C++ pour GNU/Linux : `ant run-cpp`
- Pour exécuter 1 Banque, 1 Distributeur et 1 Contrôleur en C++ pour MinGW/Windows : `ant run-cpp-win32`
- Pour exécuter 1 Banque, 1 Distributeur et 1 Contrôleur en C++ pour macOS/Darling : `ant run-cpp-o64`
- Pour exécuter 1 Banque, 2 Distributeur et 2 Contrôleur en java : `ant run-java-2`
- Pour exécuter 1 Banque, 2 Distributeur et 2 Contrôleur en C pour GNU/Linux : `ant run-c-2`
- Pour exécuter 1 Banque, 2 Distributeur et 2 Contrôleur en C pour MinGW/Windows : `ant run-c-win32-2`
- Pour exécuter 1 Banque, 2 Distributeur et 2 Contrôleur en C pour macOS/Darling : `ant run-c-o64-2`
- Pour exécuter 1 Banque, 2 Distributeur et 2 Contrôleur en C++ pour GNU/Linux : `ant run-cpp-2`
- Pour exécuter 1 Banque, 2 Distributeur et 2 Contrôleur en C++ pour MinGW/Windows : `ant run-cpp-win32-2`
- Pour exécuter 1 Banque, 2 Distributeur et 2 Contrôleur en C++ pour macOS/Darling : `ant run-cpp-o64-2`

Les déploiements à plusieurs Distributeur et plusieurs Contrôleur permettent de vérifier le routage correct des réponses aux requêtes.
**Il est évidemment possible de panacher les langages et les OS** : une Banque en Java, un Contrôleur en C sous Microsoft Windows, un Distributeur en C++ sous macOS... Les combinaisons sont nombreuses avec 3 composants, 3 langages, 3 OS : 27 cas. Il est également possible de créer des déploiements ou les processus hébergent plus d'un composant, par exemple, les cinq composants en Java sous macOS ou un Distributeur et un Contrôleur dans le même processus sous GNU/Linux connectés à une instance de Banque sous Microsoft Windows plus un Distributeur et un Contrôleur dans le même processus sous macOS, toujours connectés à la même instance de Banque.

**En pas-à-pas**, pour comprendre :

- Les librairies util-xxx et dabtypes-xxx ainsi que les trois composants `Distributeur`, `Banque` et `Controleur` produisent des librairies dynamiques (.so, .dll, .dylib).
- Les exécutables qui correspondent au `process` du modèle hébergent les factories générées, ils sont nommés &lt;deploiement>-&lt;processus>-&lt;langage>. Ce sont eux qui réalisent les instantiation, conformément au déploiement.

1. Générer le code JAXB à partir du schéma [distributed-application.xsd](distributed-application.xsd) : `(cd disappgen && ant jaxb-gen)`
1. Compiler et packager le générateur de code : `(cd disappgen && ant jar)`
1. Générer le code de l'application à partir du document XML [dab.xml](dab.xml) : `ant generate-all-sources`
1. Compiler dans l'ordre :
    1. util-c            : `(cd util-c && make)`
    1. dabtypes-c        : `(cd dabtypes-c && make)`
    1. Controleur-c      : `(cd Controleur-c && make)`
    1. Banque-c          : `(cd Banque-c && make)`
    1. Distributeur-c    : `(cd Distributeur-c && make)`
    1. isolated-udt1-c   : `(cd isolated-udt1-c && make)`
    1. isolated-udt2-c   : `(cd isolated-udt2-c && make)`
    1. isolated-ihm1-c   : `(cd isolated-ihm1-c && make)`
    1. isolated-ihm2-c   : `(cd isolated-ihm2-c && make)`
    1. isolated-sc-c     : `(cd isolated-sc-c && make)`
    1. util-cpp          : `(cd util-cpp && make)`
    1. dabtypes-cpp      : `(cd dabtypes-cpp && make)`
    1. Controleur-cpp    : `(cd Controleur-cpp && make)`
    1. Banque-cpp        : `(cd Banque-cpp && make)`
    1. Distributeur-cpp  : `(cd Distributeur-cpp && make)`
    1. isolated-udt1-cpp : `(cd isolated-udt1-cpp  && make)`
    1. isolated-udt2-cpp : `(cd isolated-udt2-cpp  && make)`
    1. isolated-ihm1-cpp : `(cd isolated-ihm1-cpp  && make)`
    1. isolated-ihm2-cpp : `(cd isolated-ihm2-cpp  && make)`
    1. isolated-sc-cpp   : `(cd isolated-sc-cpp  && make)`
    1. util-java         : `(cd util-java && ant)`
    1. dabtypes-java     : `(cd dabtypes-java && ant)`
    1. Controleur-java   : `(cd Controleur-java && ant)`
    1. Banque-java       : `(cd Banque-java && ant)`
    1. Distributeur-java : `(cd Distributeur-java && ant)`
    1. isolated-udt1-java: `(cd isolated-udt1-java && ant)`
    1. isolated-udt2-java: `(cd isolated-udt2-java && ant)`
    1. isolated-ihm1-java: `(cd isolated-ihm1-java && ant)`
    1. isolated-ihm2-java: `(cd isolated-ihm2-java && ant)`
    1. isolated-sc-java  : `(cd isolated-sc-java && ant)`

**Pour exécuter** les projets, un environnement minimal doit suffire, aucune bibliothèque *runtime* n'est utilisée. Cependant, pour exécuter les productions pour MS-Windows et macOS, il faut les émulateurs Wine et Darling (ou les OS natifs).

## Reste à faire

1. Certaines interactions n'ont pas été prévues :
    * données partagées avec plusieurs écrivains, plusieurs lecteurs
    * événements consommés par plusieurs composants

1. Wizard : même si le code manuel est simple à coder, un wizard Eclipse de génération de classes **et de makefile** serait bienvenu. A faire en Java pour Java, C et C++.

1. Automate : associer une action au franchissement d'une transition.

## Boite à idées, à débattre...

1. Adopter un modèle d'exécution non plus asynchrone et temps-réel comme à présent mais plutôt cyclique et par pas de temps discret, avec une méthode d'activation qui donne la main dans un ordre déterminé aux différents acteurs, chronomètres compris. Cela permettrait de rendre l'exécution plus contrôlable en environnement de test.

1. On sent qu'il serait possible de mener une campagne de tests exhaustive de chaque composant avec [JUnit](https://junit.org/junit5/), [CUnit](http://cunit.sourceforge.net/) ou [CppUnit](http://wiki.c2.com/?CppUnit). Une surcouche de ces frameworks de test en *boite noire*, c'est à dire au niveau *réseau UDP* est à développer pour en tirer le maximum de profit. A voir si ça ne revient pas à écrire totalement l'application... Pour un exemple simple comme le dab, l'application de test serait plus riche que l'application nominale...

