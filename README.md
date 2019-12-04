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
- Rendre homogène le traitement des messages et des événements quel que soit le langage cible.
- Faciliter le portage entre les OS GNU/Linux et MS-Windows

Sauf pour Java, les librairies n'ont pas recourt à l'allocation dynamique de mémoire. C'est pour cette raison que, en C++ :
- la classe `std::string` a finalement été remplacée par des chaînes de longueurs fixes et pré allouées (le modèle spécifie les longueurs)
- les exceptions standard de `stdexcept` ont été remplacées par des exceptions spécifiques dérivées de `std::exception` dotées de fonctionnalités de localisation plus avancées notamment la gestion de la pile d'appel *à la Java*.

La génération de code, réalisée en Java, s'appuie sur un modèle commun et trois modèles `StringTemplate` spécifiques des trois langages cibles. La liste des sources générée est également produite pour faciliter l'écriture des *makefiles* au moyen d'un quatrième modèle `StringTemplate`.

## Les projets :

- **sc** pour **Site Central** : une application IHM en C, C++ ou Java qui montre l'état des comptes bancaires et des cartes de crédit
- **udt** pour **Unité de Traitement** : l'application où est implémentée la logique du système, hors IHM en C, C++ ou Java
- **dab** pour **Distributeur Automatique de Billets** : une application IHM en C, C++ ou Java qui permet d'en simuler l'usage par un utilisateur final.

## Construire et exécuter les projets :

**Pour construire** le projet, il est nécessaire de disposer de :
- **Java**, dont la version doit être supérieure ou égale à 8, [OpenJDK](https://adoptopenjdk.net/) *latest* est vivement recommandé.
- Du compilateur **[JAXB](https://javaee.github.io/jaxb-v2/)** `xjc`, qu'on installe sous GNU/Linux Debian/Ubuntu/Mint par `sudo apt install xjc`
- Du compilateur **[C11 et C++17](https://gcc.gnu.org/)** pour GNU/Linux et compilateur croisé, qu'on installe sous GNU/Linux Debian/Ubuntu/Mint par `sudo apt install gcc gcc-mingw-w64`
- Du [compilateur croisé Linux/Mac OS X](https://github.com/tpoechtrager/osxcross)
- De [WineHQ](https://www.winehq.org/) pour exécuter sous Linux des binaires MS-Windows et de son équivalent pour macOS X : [DarlingHQ](https://www.darlinghq.org/)
- De l'outil de production **[Apache Ant](https://ant.apache.org/)**, qu'on installe sous GNU/Linux Debian/Ubuntu/Mint par `sudo apt install ant`
- De l'outil de production **[GNU Make](https://www.gnu.org/software/make/)** qu'on installe sous GNU/Linux Debian/Ubuntu/Mint par `sudo apt install make`
- Les **bibliothèques JAXB** nécessaires, [qui ne sont plus fournies avec le JDK 11](https://www.jesperdj.com/2018/09/30/jaxb-on-java-9-10-11-and-beyond/) sont dans [lib](lib).

**Pour les impatients**, se placer à la racine du projet et entrer `ant` pour construire toutes les versions : Java, C et C++ pour les cibles GNU/Linux et MinGW/Windows.

**Les traces d'exécution** sont supporté par un jeu de macros qui les route vers la sortie standard d'erreur. Afin d'éviter de polluer la console, les scripts de lancement les redirigent vers /dev/pts/xx, les pseudo-terminaux Linux.
Pour lancer ces terminaux, taper `start-ttys`.

**Différents déploiements** exécutables :
- Pour exécuter 1 sc, 1 dab et 1 udt en java: `ant run-java`
- Pour exécuter 1 sc, 1 dab et 1 udt en C pour GNU/Linux : `ant run-c`
- Pour exécuter 1 sc, 1 dab et 1 udt en C pour MinGW/Windows : `ant run-c-win32`
- Pour exécuter 1 sc, 1 dab et 1 udt en C pour macOS X/Darling : `ant run-c-o64`
- Pour exécuter 1 sc, 1 dab et 1 udt en C++ pour GNU/Linux : `ant run-cpp`
- Pour exécuter 1 sc, 1 dab et 1 udt en C++ pour MinGW/Windows : `ant run-cpp-win32`
- Pour exécuter 1 sc, 1 dab et 1 udt en C++ pour macOS X/Darling : `ant run-cpp-o64`
- Pour exécuter 1 sc, 2 dab et 2 udt en java : `ant run-java-2`
- Pour exécuter 1 sc, 2 dab et 2 udt en C pour GNU/Linux : `ant run-c-2`
- Pour exécuter 1 sc, 2 dab et 2 udt en C pour MinGW/Windows : `ant run-c-win32-2`
- Pour exécuter 1 sc, 2 dab et 2 udt en C pour macOS X/Darling : `ant run-c-o64-2`
- Pour exécuter 1 sc, 2 dab et 2 udt en C++ pour GNU/Linux : `ant run-cpp-2`
- Pour exécuter 1 sc, 2 dab et 2 udt en C++ pour MinGW/Windows : `ant run-cpp-win32-2`
- Pour exécuter 1 sc, 2 dab et 2 udt en C++ pour macOS X/Darling : `ant run-cpp-o64-2`

Les déploiements à plusieurs dab et plusieurs udt permettent de vérifier le routage correct des réponses aux requêtes.
**Il est évidemment possible de panacher les langages** : un sc en Java, un udt en C, un dab en C++... Les combinaisons sont nombreuses !

**En pas-à-pas**, pour comprendre :
1. Générer le code JAXB à partir du schéma [distributed-application.xsd](distributed-application.xsd) : `(cd disappgen && ant jaxb-gen)`
1. Compiler et packager le générateur de code : `(cd disappgen && ant jar)`
1. Générer le code de l'application à partir du document XML [dab.xml](dab.xml) : `ant generate-all-sources`
1. Compiler dans l'ordre :
    1. util-c        : `(cd util-c && make)`, produit également `util-c-win32` et `util-c-o64`
    1. dabtypes-c    : `(cd dabtypes-c && make)`, produit également `dabtypes-c-win32` et `dabtypes-c-o64`
    1. udt-c         : `(cd udt-c && make)`, produit également `udt-c-win32` et `udt-c-o64`
    1. util-cpp      : `(cd util-cpp && make)`, produit également `util-cpp-win32` et `util-cpp-o64`
    1. dabtypes-cpp  : `(cd dabtypes-cpp && make)`, produit également `dabtypes-cpp-win32` et `dabtypes-cpp-o64`
    1. udt-cpp       : `(cd udt-cpp && make)`, produit également `udt-cpp-win32` et `udt-cpp-o64`
    1. util-java     : `(cd util-java && ant)`
    1. dabtypes-java : `(cd dabtypes-java && ant)`
    1. sc-java       : `(cd sc-java && ant)`
    1. dab           : `(cd distributeur-java && ant)`

**Pour exécuter** les projets, un environnement minimal doit suffire, aucune bibliothèque *runtime* n'est utilisée. Cependant, pour exécuter les productions pour MS-Windows et macOS, il faut les émulateurs Wine et Darling (ou les OS natifs).

## Reste à faire

1. Dans l'état actuel du modèle, il n'existe qu'un seul module de types, dont le nom (physique) est fourni par la balise `implémentation`. Il faudrait lui donner un nom logique, ce qui permettrait de déclarer plusieurs modules de types et de référencer un type dans le reste du module par son nom qualifié : `<module-name>.<type-name>`.

1. Il existe une balise `implementation` sous la balise `<types>`. C'est inutile puisque les implémentations nécessaires peuvent être déduites de leurs usages par le reste du modèle.

1. Même si le modèle actuel permet plusieurs composants par processus, l'implémentation ne le permet pas (sauf pour Java). En effet, comme le composant abstrait écoute le port du process, il ne peut y avoir plusieurs composants (*port already bound*). Une classe **ComponentFactory** responsable du cycle de vie des instances de composant et du routage des messages est à créer. C'est fait pour Java mais il faut l'étendre aux langages C et C++.

1. Certaines interactions n'ont pas été prévues :
    * données partagées avec plusieurs écrivains, plusieurs lecteurs
    * événements consommés par plusieurs composants

1. Production : générer tout ou partie des makefiles, build Apache/Ant et projets Eclipse (au moins en Java).
1. Wizard : même si les classes manuelle sont simples à coder, un wizard Eclipse de génération de classes serait bienvenu. A faire en Java, C et C++.
1. Automate : associer une action au franchissement d'une transition.

## Boite à idées, à débattre...

1. Adopter un modèle d'exécution non plus asynchrone et temps-réel comme à présent mais plutôt cyclique et par pas de temps discret, avec une méthode d'activation qui donne la main dans un ordre déterminé aux différents acteurs, chronomètres compris. Cela permettrait de rendre l'exécution plus contrôlable en environnement de test.
1. On sent qu'il serait possible de mener une campagne de tests exhaustive de chaque composant avec [JUnit](https://junit.org/junit5/), [CUnit](http://cunit.sourceforge.net/) ou [CppUnit](http://wiki.c2.com/?CppUnit). Une surcouche de ces frameworks de test en *boite noire*, c'est à dire au niveau *réseau UDP* est à développer pour en tirer le maximum de profit. A voir si ça ne revient pas à écrire totalement l'application... Pour un exemple simple comme le dab, l'application de test serait plus riche que l'application nominale...
