#include <dab/IIHM.hpp>
#include <dab/ISiteCentral.hpp>
#include <dab/IUniteDeTraitementDispatcher.hpp>

#include <util/Automaton.hpp>

namespace dab::udt {

   class Date {
   public:

      Date( unsigned char month = 0, unsigned short year = 0 ) :
         _month( month ),
         _year ( year  )
      {}

      void set( byte month, ushort year ) {
         _month = month;
         _year  = year;
      }

      inline bool isValid() const {
         return( 0 < _month )&&( _month < 13 )&&( _year > 2018 );
      }

      byte           _month;
      unsigned short _year;

   private:
      Date( const Date & ) = delete;
      Date & operator = ( const Date & ) = delete;
   };

   class Carte {
   public:

      Carte() :
         _isValid( false )
      {}

      void set(
         const std::string & carteID,
         const std::string & code,
         byte                month,
         ushort              year,
         byte                nbEssais )
      {
         _id       = carteID;
         _code     = code;
         _nbEssais = nbEssais;
         _isValid  = true;
         _peremption.set( month, year );
      }

      inline void incrementeNbEssais( void ) { ++_nbEssais; }

      inline void invalidate( void ) { _isValid = false; }

      inline bool isValid( void ) const { return _isValid; }

      inline const std::string & getId( void ) const { return _id; }

      inline bool compareCode( const std::string & code ) const { return _code == code; }

      inline byte getNbEssais( void ) const { return _nbEssais; }

   private:

      bool        _isValid;
      std::string _id;
      std::string _code;
      std::string _compte;
      Date        _peremption;
      byte        _nbEssais;
   };

   class Compte {
   public:

      Compte() :
         _isValid ( false ),
         _id      ( ""    ),
         _solde   ( 0.0   ),
         _autorise( false )
      {}

      void set( const std::string id, const double & solde, bool autorise ) {
         _id       = id;
         _solde    = solde;
         _autorise = autorise;
         _isValid  = true;
      }

      void invalidate() {
         fprintf( stderr, "Compte::invalidate\n" );
         _isValid = false;
      }

      bool isValid( void ) const { return _isValid; }

      const std::string & getId( void ) const { return _id; }

      const double & getSolde( void ) const { return _solde; }

   private:

      bool        _isValid;
      std::string _id;
      double      _solde;
      bool        _autorise;
   };

   enum class Event : unsigned char {
      FIRST,

      MAINTENANCE_ON = FIRST,
      MAINTENANCE_OFF,
      SOLDE_CAISSE_INSUFFISANT,
      ANOMALIE_ON,
      ANOMALIE_OFF,
      CARTE_INSEREE,
      CARTE_LUE_0,
      CARTE_LUE_1,
      CARTE_LUE_2,
      CARTE_INVALIDE,
      BON_CODE,
      MAUVAIS_CODE_1,
      MAUVAIS_CODE_2,
      MAUVAIS_CODE_3,
      CARTE_CONFISQUEE,
      SOLDE_INSUFFISANT,
      MONTANT_OK,
      CARTE_RETIREE,
      BILLETS_RETIRES,
      TERMINATE,

      LAST,
   };

   class UniteDeTraitement : public dab::IUniteDeTraitement {
   public:

      UniteDeTraitement(
         const char *   intrfc,
         unsigned short udtPort,
         const char *   scAddress,
         unsigned short scPort,
         const char *   dabAddress,
         unsigned short dabPort     );

   public:

      virtual void maintenance( bool maintenance );
      virtual void rechargerLaCaisse( const double & montant );
      virtual void anomalie( bool anomalie );
      virtual void carteInseree( const std::string & id );
      virtual void carteLue( const dab::Carte & carte, const dab::Compte & compte );
      virtual void codeSaisi( const std::string & code );
      virtual void montantSaisi( const double & code );
      virtual void carteRetiree( void );
      virtual void billetsRetires( void );
      virtual void shutdown( void );

   public:

      const std::string & getCarteId() const { return _carte.getId(); }
      double getRetraitMax( void ) const;

   public:

      void run( void );

   private:

      static const double RETRAIT_MAX;

      typedef util::Automaton<dab::Etat, Event> automaton_t;

      io::DatagramSocket                  _socket;
      dab::IIHM *                         _dab;
      dab::ISiteCentral *                 _sc;
      dab::IUniteDeTraitementDispatcher * _dispatcher;
      bool                                _running;
      automaton_t                         _automaton;
      Carte                               _carte;
      Compte                              _compte;
      double                              _valeurCaisse;

   private:
      UniteDeTraitement( const UniteDeTraitement & ) = delete;
      UniteDeTraitement & operator = ( const UniteDeTraitement & ) = delete;
   };

   ::dab::IUniteDeTraitement * newUniteDeTraitement(
      const char *   intrfc,
      unsigned short udtPort,
      const char *   scAddress,
      unsigned short scPort,
      const char *   dabAddress,
      unsigned short dabPort     )
   {
      return new UniteDeTraitement( intrfc, udtPort, scAddress, scPort, dabAddress, dabPort );
   }
}

std::ostream & operator << ( std::ostream & stream, const dab::udt::Event & event ) {
   switch( event ) {
   case dab::udt::Event::MAINTENANCE_ON          : return stream << "MAINTENANCE_ON";           break;
   case dab::udt::Event::MAINTENANCE_OFF         : return stream << "MAINTENANCE_OFF";          break;
   case dab::udt::Event::SOLDE_CAISSE_INSUFFISANT: return stream << "SOLDE_CAISSE_INSUFFISANT"; break;
   case dab::udt::Event::ANOMALIE_ON             : return stream << "ANOMALIE_ON";              break;
   case dab::udt::Event::ANOMALIE_OFF            : return stream << "ANOMALIE_OFF";             break;
   case dab::udt::Event::CARTE_INSEREE           : return stream << "CARTE_INSEREE";            break;
   case dab::udt::Event::CARTE_LUE_0             : return stream << "CARTE_LUE_0";              break;
   case dab::udt::Event::CARTE_LUE_1             : return stream << "CARTE_LUE_1";              break;
   case dab::udt::Event::CARTE_LUE_2             : return stream << "CARTE_LUE_2";              break;
   case dab::udt::Event::CARTE_INVALIDE          : return stream << "CARTE_INVALIDE";           break;
   case dab::udt::Event::BON_CODE                : return stream << "BON_CODE";                 break;
   case dab::udt::Event::MAUVAIS_CODE_1          : return stream << "MAUVAIS_CODE_1";           break;
   case dab::udt::Event::MAUVAIS_CODE_2          : return stream << "MAUVAIS_CODE_2";           break;
   case dab::udt::Event::MAUVAIS_CODE_3          : return stream << "MAUVAIS_CODE_3";           break;
   case dab::udt::Event::CARTE_CONFISQUEE        : return stream << "CARTE_CONFISQUEE";         break;
   case dab::udt::Event::SOLDE_INSUFFISANT       : return stream << "SOLDE_INSUFFISANT";        break;
   case dab::udt::Event::MONTANT_OK              : return stream << "MONTANT_OK";               break;
   case dab::udt::Event::CARTE_RETIREE           : return stream << "CARTE_RETIREE";            break;
   case dab::udt::Event::BILLETS_RETIRES         : return stream << "BILLETS_RETIRES";          break;
   case dab::udt::Event::TERMINATE               : return stream << "TERMINATE";                break;
   case dab::udt::Event::LAST                    : return stream << "LAST (inutilisé)";         break;
   }
   return stream << "inconnu (" << event << ")";
}

using namespace dab;
using namespace dab::udt;

const double UniteDeTraitement::RETRAIT_MAX = 1000.0;

typedef util::Automaton<Etat, Event>::Arc      arc;
typedef util::Automaton<Etat, Event>::Shortcut shortcut;

UniteDeTraitement::UniteDeTraitement(
   const char *   intrfc,
   unsigned short udtPort,
   const char *   scAddress,
   unsigned short scPort,
   const char *   dabAddress,
   unsigned short dabPort     )
 :
   _automaton(
      Etat::AUCUN, {
         arc( Etat::AUCUN          , Event::MAINTENANCE_ON          , Etat::MAINTENANCE     ),
         arc( Etat::MAINTENANCE    , Event::MAINTENANCE_OFF         , Etat::EN_SERVICE      ),
         arc( Etat::MAINTENANCE    , Event::SOLDE_CAISSE_INSUFFISANT, Etat::MAINTENANCE     ),
         arc( Etat::EN_SERVICE     , Event::MAINTENANCE_ON          , Etat::MAINTENANCE     ),
         arc( Etat::MAINTENANCE    , Event::ANOMALIE_ON             , Etat::HORS_SERVICE    ),
         arc( Etat::HORS_SERVICE   , Event::ANOMALIE_OFF            , Etat::MAINTENANCE     ),
         arc( Etat::HORS_SERVICE   , Event::MAINTENANCE_ON          , Etat::MAINTENANCE     ),
         arc( Etat::EN_SERVICE     , Event::SOLDE_CAISSE_INSUFFISANT, Etat::HORS_SERVICE    ),
         arc( Etat::EN_SERVICE     , Event::CARTE_INSEREE           , Etat::LECTURE_CARTE   ),
         arc( Etat::LECTURE_CARTE  , Event::CARTE_LUE_0             , Etat::SAISIE_CODE_1   ),
         arc( Etat::LECTURE_CARTE  , Event::CARTE_LUE_1             , Etat::SAISIE_CODE_2   ),
         arc( Etat::LECTURE_CARTE  , Event::CARTE_LUE_2             , Etat::SAISIE_CODE_3   ),
         arc( Etat::LECTURE_CARTE  , Event::CARTE_INVALIDE          , Etat::EN_SERVICE      ),
         arc( Etat::LECTURE_CARTE  , Event::CARTE_CONFISQUEE        , Etat::EN_SERVICE      ),
         arc( Etat::SAISIE_CODE_1  , Event::BON_CODE                , Etat::SAISIE_MONTANT  ),
         arc( Etat::SAISIE_CODE_1  , Event::MAUVAIS_CODE_1          , Etat::SAISIE_CODE_2   ),
         arc( Etat::SAISIE_CODE_2  , Event::BON_CODE                , Etat::SAISIE_MONTANT  ),
         arc( Etat::SAISIE_CODE_2  , Event::MAUVAIS_CODE_2          , Etat::SAISIE_CODE_3   ),
         arc( Etat::SAISIE_CODE_3  , Event::BON_CODE                , Etat::SAISIE_MONTANT  ),
         arc( Etat::SAISIE_CODE_3  , Event::MAUVAIS_CODE_3          , Etat::EN_SERVICE      ),
         arc( Etat::SAISIE_MONTANT , Event::MONTANT_OK              , Etat::RETRAIT_CARTE   ),
         arc( Etat::RETRAIT_CARTE  , Event::CARTE_RETIREE           , Etat::RETRAIT_BILLETS ),
         arc( Etat::RETRAIT_BILLETS, Event::BILLETS_RETIRES         , Etat::EN_SERVICE      ),
      },
      {
         shortcut( Event::TERMINATE  , Etat::HORS_SERVICE ),
         shortcut( Event::ANOMALIE_ON, Etat::HORS_SERVICE ),
      }
   ),
   _valeurCaisse( 0.0 )
{
   _socket.bind( intrfc, udtPort );
   sockaddr_in scTarget;
   sockaddr_in dabTarget;
   io::DatagramSocket::init( scAddress, scPort, scTarget );
   io::DatagramSocket::init( dabAddress, dabPort, dabTarget );
   _sc         = newSiteCentral                ( _socket, {scTarget});
   _dab        = newIHM                        ( _socket, {dabTarget});
   _dispatcher = newUniteDeTraitementDispatcher( _socket, *this );
}

/**
 * E4 : La mise en service d'un DAB est faite manuellement par l'opérateur
 */
void UniteDeTraitement::maintenance( bool maintenance ) {
   if( maintenance ) {
      _automaton.process( Event::MAINTENANCE_ON );
   }
   else {
      _automaton.process( Event::MAINTENANCE_OFF );
   }
}

/**
 * E1 : Le DAB peut déclencher sa mise hors service, lorsqu'il détecte que sa caisse comporte un solde inférieur à celui du montant
 * maximum autorisé pour un retrait (1 000 €)
 * E5 : l'opérateur est chargé du rechargement de la caisse du DAB.
 */
void UniteDeTraitement::rechargerLaCaisse( const double & montant ) {
   _valeurCaisse += montant;
   _dab->setSoldeCaisse( _valeurCaisse );
   if( _valeurCaisse < RETRAIT_MAX ) {
      _automaton.process( Event::SOLDE_CAISSE_INSUFFISANT );
   }
}

void UniteDeTraitement::anomalie( bool anomalie ) {
   if( anomalie ) {
      _automaton.process( Event::ANOMALIE_ON );
   }
   else {
      _automaton.process( Event::ANOMALIE_OFF );
   }
}

/**
 * E20 : Le calcul du montant maximum du retrait auquel le client a droit, en fonction du solde du compte et de la somme maximum à
 * laquelle tous les clients ont droit (montant fixé à 1000 €) : la somme maximum que le client peut retirer correspond au minimum
 * de ces deux valeurs.
 */
double UniteDeTraitement::getRetraitMax( void ) const {
   return std::min( _compte.getSolde(), RETRAIT_MAX );
}

void UniteDeTraitement::carteInseree( const std::string & id ) {
   _carte .invalidate();
   _compte.invalidate();
   _sc->getInformations( id );
   _automaton.process( Event::CARTE_INSEREE );
}

void UniteDeTraitement::carteLue( const dab::Carte & carte, const dab::Compte & compte ) {
   _carte .set( carte.id, carte.code, carte.month, carte.year, carte.nbEssais );
   _compte.set( compte.id, compte.solde, compte.autorise );
   if( _carte.isValid() && _compte.isValid()) {
      if( _carte.getNbEssais() == 0 ) {
         _automaton.process( Event::CARTE_LUE_0 );
      }
      else if( _carte.getNbEssais() == 1 ) {
         _automaton.process( Event::CARTE_LUE_1 );
      }
      else if( _carte.getNbEssais() == 2 ) {
         _automaton.process( Event::CARTE_LUE_2 );
      }
      else {
         _automaton.process( Event::CARTE_CONFISQUEE );
         _dab->confisquerLaCarte();
      }
   }
   else {
      ::fprintf( stderr, "Carte et/ou compte invalide\n" );
      _automaton.process( Event::CARTE_INVALIDE );
   }
}

void UniteDeTraitement::codeSaisi( const std::string & code ) {
   if( _carte.compareCode( code )) {
      _automaton.process( Event::BON_CODE );
   }
   else {
      _sc->incrNbEssais( _carte.getId());
      _carte.incrementeNbEssais();
      if( _carte.getNbEssais() == 1 ) {
         _automaton.process( Event::MAUVAIS_CODE_1 );
      }
      else if( _carte.getNbEssais() == 2 ) {
         _automaton.process( Event::MAUVAIS_CODE_2 );
      }
      else if( _carte.getNbEssais() == 3 ) {
         _automaton.process( Event::MAUVAIS_CODE_3 );
         _dab->confisquerLaCarte();
      }
   }
}

void UniteDeTraitement::montantSaisi( const double & montant ) {
   _valeurCaisse -= montant;
   _dab->setSoldeCaisse( _valeurCaisse );
   _sc->retrait( _carte.getId(), montant );
   _automaton.process( Event::MONTANT_OK );
}

void UniteDeTraitement::carteRetiree( void ) {
   _automaton.process( Event::CARTE_RETIREE );
}

void UniteDeTraitement::billetsRetires( void ) {
   _automaton.process( Event::BILLETS_RETIRES );
}

void UniteDeTraitement::shutdown( void ) {
   _dab->shutdown();
   _sc->shutdown();
   _automaton.process( Event::TERMINATE );
   _running = false;
}

void UniteDeTraitement::run() {
   _running = true;
   while( _running ) {
      if( _dispatcher->hasDispatched()) {
         _dab->setStatus( _automaton.getCurrentState());
      }
   }
}
