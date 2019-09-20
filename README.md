# Distributeur automatique de billets distribué

## Support pédagogique.

## Application composée de deux IHM en Java et une unité de traitement en C++ ou en C.

- Librairies simplissimes de classes C++ (à la Java) et de fonctions C (sans malloc) pour faciliter le portage entre les OS GNU/Linux et MS-Windows
- API java.nio partiellement portée en C++ et C pour faciliter l'usage des sockets
- Génération automatique du code réseau à partir de la paire de fichier :
  - [distributed-application.xsd](distributed-application.xsd), le schéma (ou méta modèle)
  - [dab.xml](dab.xml), les définition de :
    * types échangés limités aux types scalaires, énumérés et structures
    * interfaces
    * composants qui offrent et requièrent des interfaces et qui sont implémentés dans un seul langage

