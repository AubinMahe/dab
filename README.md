# Framework de systèmes distribués en C, C++ et Java

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

Sauf pour Java, les librairies n'ont pas recours à l'allocation dynamique de mémoire. C'est pour cette raison que, en C++ :
- la classe `std::string` a finalement été remplacée par des chaînes de longueurs fixes et pré-allouées (le modèle spécifie les longueurs)
- les exceptions standard de `stdexcept` ont été remplacées par des exceptions spécifiques dérivées de `std::exception` dotées de fonctionnalités de localisation plus avancées notamment la gestion de la pile d'appel *à la Java*.

La génération de code, réalisée en Java, s'appuie sur un modèle commun et trois modèles `StringTemplate` spécifiques des trois langages cibles. La liste des sources générée est également produite pour faciliter l'écriture des *makefiles* au moyen d'un quatrième modèle `StringTemplate`.

## Modèle de génération ##

Le modèle logique de composants est dans [dab.xml](dab.xml) alors que le modèle physique de génération d'artefact - avec des variations suivants les langages C, C++ ou Java - et de paramètrage des processus (adresses, port) est dans [dab-gen.xml](dab-gen.xml). Il est lui-même décrit par le schéma [distributed-application-generation.xsd](distributed-application-generation.xsd).
Chaque composant donne lieu à la génération d'une librairie dynamique alors que les *factories* sont des exécutables. Ce sont ces dernières qui assument le paramétrage des processus.

## Les projets :

Le cas mis en œuvre est un distributeur automatique de billets distribué naïf, qui déploie les composants suivants :

- **Banque** : une application IHM en C, C++ ou Java qui montre l'état des comptes bancaires et des cartes de crédit
- **Contrôleur** : l'application où est implémentée la logique du système, hors IHM en C, C++ ou Java
- **Distributeur** : une application IHM en C, C++ ou Java qui permet de simuler l'usage d'un distributeur automatique de billet par un utilisateur final.

## Les déploiements :

- **isolated** est un projet où chaque composant est dans son propre processus, il permet :
    - Des communications et des partages de données entre hôtes distants
    - Un panachage à volonté des langages utilisés
   
- **mixed** isole le composant banque et déploie deux processus regroupant une unité de traitement et une IHM, croisés

- **allin** déploie tous les composants au sein d'un seul processus, mono-langage.

## Tests de non régression #
Les tests de non-régression pour les 3 langages et les 3 déploiements sont réalisés à l'aide d'une implémentation du composant Distributeur qui exécute un scénario cadencé par le temps et les événements. La validation du comportement des composants `Controleur` et `Banque` est effectuée par analyse automatique des logs d'exécution par le testeur `Distributeur`. Le testeur est en Java mais permet de tester les trois implémentations C, C++ et Java.

## Construire et exécuter les projets :

**Pour construire** les projets, il est nécessaire de disposer de :
- **Java**, dont la version doit être supérieure ou égale à 8, [OpenJDK](https://adoptopenjdk.net/) *latest* est vivement recommandé.
- Du compilateur **[JAXB](https://javaee.github.io/jaxb-v2/)** `xjc`, qu'on installe sous GNU/Linux Debian/Ubuntu/Mint par `sudo apt install xjc`
- Les **bibliothèques JAXB** nécessaires, [qui ne sont plus fournies avec le JDK 11](https://www.jesperdj.com/2018/09/30/jaxb-on-java-9-10-11-and-beyond/) sont dans [lib](lib).
- De l'outil de production **[Apache Ant](https://ant.apache.org/)**, qu'on installe sous GNU/Linux Debian/Ubuntu/Mint par `sudo apt install ant`
- De l'outil de production **[GNU Make](https://www.gnu.org/software/make/)** qu'on installe sous GNU/Linux Debian/Ubuntu/Mint par `sudo apt install make`
- Des compilateurs C11 et C++17 **[gcc](https://gcc.gnu.org/)**, qu'on installe sous GNU/Linux Debian/Ubuntu/Mint par `sudo apt install gcc`
- Des compilateurs croisés C11 et C++17 pour Windows **[gcc-mingw32](https://gcc.gnu.org/)**, qu'on installe sous GNU/Linux Debian/Ubuntu/Mint par `sudo apt install gcc-mingw-w64`
- Des compilateurs croisés C11 et C++17 pour macOS **[clang-o64](https://github.com/tpoechtrager/osxcross)**, issu de [clang](https://clang.llvm.org/) 

**Pour exécuter** les projets, les outils suivants sont nécessaires :
- [WineHQ](https://www.winehq.org/) pour exécuter sous Linux des binaires Windows
- [DarlingHQ](https://www.darlinghq.org/) pour exécuter sous macOS 

On peut aussi exécuter les binaires cross-compilés directement sous l'OS cible, si on en dispose.

**Pour les impatients**, se placer à la racine du projet et entrer `ant` pour construire toutes les versions : Java, C et C++ pour les cibles GNU/Linux, MinGW/Windows et macOS.

**Les traces d'exécution** sont supporté par un jeu de macros qui les route vers la sortie standard d'erreur. Afin d'éviter de polluer la console, les scripts de lancement les redirigent vers /dev/pts/xx, les pseudo-terminaux Linux.
Pour lancer ces terminaux, taper `start-ttys`.

**Différents déploiements** exécutables :
- Pour exécuter le process `xxx` du déploiement `isolated` implémenté avec le langage `yyy`, taper : `ant runiso-xxx-yyy`
- Pour exécuter le process `xxx` du déploiement `mixed`    implémenté avec le langage `yyy`, taper : `ant runmix-xxx-java`
- Pour exécuter le process `one` du déploiement `allin`    implémenté avec le langage `java` (le seul qui est vraiment fonctionnel), taper : `ant runallin-one-java`
- Pour exécuter les tests de non régression du langage `yyy`, taper : `ant run-functional-tests-yyy`
- **Rappel** :
    - Les instances du modèles [dab.xml](dab.xml) sont : `sc`, `udt1`, `udt2`, `ihm1`, `ihm2`, `dab1`, `dab2`
    - Les langages supportés sont nommés `java`, `c` et `cpp`

Les déploiements à plusieurs `Distributeur` et plusieurs `Controleur` permettent de vérifier le routage correct des réponses aux requêtes.
**Il est évidemment possible de panacher les langages et les OS** : une Banque en Java, un Contrôleur en C sous Microsoft Windows, un Distributeur en C++ sous macOS... Les combinaisons sont nombreuses avec 3 composants, 3 langages, 3 OS : 27 cas. Il est également possible de créer des déploiements ou les processus hébergent plus d'un composant, par exemple, les cinq composants en Java sous macOS ou un Distributeur et un Contrôleur dans le même processus sous GNU/Linux connectés à une instance de Banque sous Microsoft Windows plus un Distributeur et un Contrôleur dans le même processus sous macOS, toujours connectés à la même instance de Banque.

- Les librairies util-xxx et dabtypes-xxx ainsi que les trois composants `Distributeur`, `Banque` et `Controleur` produisent des librairies dynamiques (.so, .dll, .dylib).
- Les exécutables qui correspondent au `process` du modèle hébergent les factories générées, ils sont nommés &lt;deploiement>-&lt;processus>-&lt;langage>. Ce sont eux qui réalisent les instanciations, conformément au déploiement.

1. Générer le code JAXB à partir du schéma [distributed-application.xsd](distributed-application.xsd) : `(cd disappgen && ant jaxb-gen)`
1. Compiler et packager le générateur de code : `(cd disappgen && ant jar)`
1. Générer le code de l'application à partir du document XML [dab.xml](dab.xml) : `ant generate-all-sources`
1. Compiler dans l'ordre :
    1. util-c                         : `(cd util-c && make)`
    1. util-cpp                       : `(cd util-cpp && make)`
    1. dabtypes-c                     : `(cd dabtypes-c && make)`
    1. dabtypes-cpp                   : `(cd dabtypes-cpp && make)`
    1. daenums-c                      : `(cd daenums-c && make)`
    1. daenums-cpp                    : `(cd daenums-cpp && make)`
    1. Pour chaque composant C ou C++ : `(cd <comp>-<lge> && make)`
    1. Pour chaque processus C ou C++ : `(cd <dep>-<process>-<lge> && make)`
    1. util-java                      : `(cd util-java && ant)`
    1. dabtypes-java                  : `(cd dabtypes-java && ant)`
    1. daenums-java                   : `(cd daenums-java && ant)`
    1. Pour chaque composant Java     : `(cd <comp>-java && ant)`
    1. Pour chaque processus Java     : `(cd <dep>-<process>-java && ant)`

**Pour exécuter** les projets, un environnement minimal doit suffire, aucune bibliothèque *runtime* n'est utilisée. Cependant, pour exécuter les productions pour MS-Windows et macOS, il faut les émulateurs Wine et Darling (ou les OS natifs).

## Reste à faire

1. Les messages UDP entrants sont activants : dès qu'ils sont reçus, le code métier associé est invoqué : les données sont rafraîchies, les événements et requêtes exécutées. Mettre en place une file d'attente de messages puis activer les traitements uniquement pour les messages spécifiés *activant* dans le modèle fournira un modèle d'exécution plus riche. Pour une exécution cyclique par exemple, un composant n'offrira qu'une seule méthode activante `execute`, déclenchée par un réveil périodique. **La version LATEST offre ces fonctionnalités, mais uniquement en C++ et partiellement en JAVA**, C est désactivé, le temps de le mettre à niveau.

1. Si on sait gérer une ou plusieurs files d'attente ou pourrait revoir le threading :
    - La version actuelle reçoit les messages, les ventile et exécute le code métier dans le même thread, ce qui n'est pas satisfaisant en cas d'attente applicative bloquante (c'est le cas pendant 3 secondes dans le composant `Banque` pour simuler le temps de recherche et de communication sur réseaux WAN sécurisés).
    - On doit envisager au moins deux threads : l'un reçoit les messages et les ventile dans la bonne file d'attente, l'autre exécute le code applicatif.
    - On pourrait modéliser le threading de façon à permettre un thread par requête, un thread par composant ou un thread par processus.
    - **La version LATEST offre ces fonctionnalités, mais uniquement en C++ et partiellement en JAVA**, C est désactivé, le temps de le mettre à niveau.

1. Certaines intégrités référentielles gagneraient à être exprimées dans le schéma et au moyen d'un checker exécuté en aval de la génération

1. Même si le code manuel est simple à coder, un **wizard Eclipse** de génération de composant serait bienvenu. À faire en Java pour C, C++ et Java.

1. La génération des **makefile** semble possible. Envisager un mode où si le fichier existe, on ne l'écrase pas, afin de préserver les réglages personnels.

1. Automate : associer une action au franchissement d'une transition.

1. Ajouter un nouveau langage : JavaScript dans le navigateur, pour développer les IHM en HTML5/CSS3. Ajouter un adaptateur d'interface UDP vers web-socket.

1. Ajouter un nouveau langage : Python.

## Boite à idées, à débattre...

1. Adopter un modèle d'exécution non plus asynchrone et temps-réel comme à présent mais plutôt cyclique et par pas de temps discret, avec une méthode d'activation qui donne la main dans un ordre déterminé aux différents acteurs, chronomètres compris. Cela permettrait de rendre l'exécution plus contrôlable en environnement de test.

1. Il serait possible d'offrir plusieurs files d'attente et de ventiler les messages reçus en fonction de leur priorité. Les messages seraient alors *activant* pour une file ou pour toutes. Pas facile de trouver un bon exemple pour valider le concept...

1. On sent qu'il serait possible de mener une campagne de tests exhaustive de chaque composant avec [JUnit](https://junit.org/junit5/), [CUnit](http://cunit.sourceforge.net/) ou [CppUnit](http://wiki.c2.com/?CppUnit). Une surcouche de ces frameworks de test en *boite noire*, au niveau *réseau UDP* est à développer pour en tirer le maximum de profit. A voir si ça ne revient pas à écrire totalement l'application... Pour un exemple simple comme le dab, l'application de test serait plus riche que l'application nominale...
