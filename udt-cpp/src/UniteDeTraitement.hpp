#include "Carte.hpp"
#include "Compte.hpp"

#include <dab/IIHM.hpp>
#include <dab/ISiteCentral.hpp>
#include <dab/IUniteDeTraitementDispatcher.hpp>

#include <util/Automaton.hpp>

namespace udt {

   class UniteDeTraitement : public dab::IUniteDeTraitement {
   private:

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

      typedef util::Automaton<dab::Etat, Event>::Arc      arc;
      typedef util::Automaton<dab::Etat, Event>::Shortcut shortcut;

   public:

      UniteDeTraitement(
         const char *   intrfc,
         unsigned short udtPort,
         const char *   scAddress,
         unsigned short scPort,
         const char *   dabAddress,
         unsigned short dabPort     )
       :
         _automaton(
            dab::Etat::AUCUN, {
               arc( dab::Etat::AUCUN          , Event::MAINTENANCE_ON          , dab::Etat::MAINTENANCE     ),
               arc( dab::Etat::MAINTENANCE    , Event::MAINTENANCE_OFF         , dab::Etat::EN_SERVICE      ),
               arc( dab::Etat::MAINTENANCE    , Event::SOLDE_CAISSE_INSUFFISANT, dab::Etat::MAINTENANCE     ),
               arc( dab::Etat::EN_SERVICE     , Event::MAINTENANCE_ON          , dab::Etat::MAINTENANCE     ),
               arc( dab::Etat::MAINTENANCE    , Event::ANOMALIE_ON             , dab::Etat::HORS_SERVICE    ),
               arc( dab::Etat::HORS_SERVICE   , Event::ANOMALIE_OFF            , dab::Etat::MAINTENANCE     ),
               arc( dab::Etat::HORS_SERVICE   , Event::MAINTENANCE_ON          , dab::Etat::MAINTENANCE     ),
               arc( dab::Etat::EN_SERVICE     , Event::SOLDE_CAISSE_INSUFFISANT, dab::Etat::HORS_SERVICE    ),
               arc( dab::Etat::EN_SERVICE     , Event::CARTE_INSEREE           , dab::Etat::LECTURE_CARTE   ),
               arc( dab::Etat::LECTURE_CARTE  , Event::CARTE_LUE_0             , dab::Etat::SAISIE_CODE_1   ),
               arc( dab::Etat::LECTURE_CARTE  , Event::CARTE_LUE_1             , dab::Etat::SAISIE_CODE_2   ),
               arc( dab::Etat::LECTURE_CARTE  , Event::CARTE_LUE_2             , dab::Etat::SAISIE_CODE_3   ),
               arc( dab::Etat::LECTURE_CARTE  , Event::CARTE_INVALIDE          , dab::Etat::EN_SERVICE      ),
               arc( dab::Etat::LECTURE_CARTE  , Event::CARTE_CONFISQUEE        , dab::Etat::EN_SERVICE      ),
               arc( dab::Etat::SAISIE_CODE_1  , Event::BON_CODE                , dab::Etat::SAISIE_MONTANT  ),
               arc( dab::Etat::SAISIE_CODE_1  , Event::MAUVAIS_CODE_1          , dab::Etat::SAISIE_CODE_2   ),
               arc( dab::Etat::SAISIE_CODE_2  , Event::BON_CODE                , dab::Etat::SAISIE_MONTANT  ),
               arc( dab::Etat::SAISIE_CODE_2  , Event::MAUVAIS_CODE_2          , dab::Etat::SAISIE_CODE_3   ),
               arc( dab::Etat::SAISIE_CODE_3  , Event::BON_CODE                , dab::Etat::SAISIE_MONTANT  ),
               arc( dab::Etat::SAISIE_CODE_3  , Event::MAUVAIS_CODE_3          , dab::Etat::EN_SERVICE      ),
               arc( dab::Etat::SAISIE_MONTANT , Event::MONTANT_OK              , dab::Etat::RETRAIT_CARTE   ),
               arc( dab::Etat::RETRAIT_CARTE  , Event::CARTE_RETIREE           , dab::Etat::RETRAIT_BILLETS ),
               arc( dab::Etat::RETRAIT_BILLETS, Event::BILLETS_RETIRES         , dab::Etat::EN_SERVICE      ),
            },
            {
               shortcut( Event::TERMINATE  , dab::Etat::HORS_SERVICE ),
               shortcut( Event::ANOMALIE_ON, dab::Etat::HORS_SERVICE ),
            }
         ),
         _valeurCaisse( 0.0 )
      {
         _socket.bind( intrfc, udtPort );
         sockaddr_in scTarget;
         sockaddr_in dabTarget;
         io::DatagramSocket::init( scAddress, scPort, scTarget );
         io::DatagramSocket::init( dabAddress, dabPort, dabTarget );
         _sc         = dab::newSiteCentral                ( _socket, {scTarget});
         _dab        = dab::newIHM                        ( _socket, {dabTarget});
         _dispatcher = dab::newUniteDeTraitementDispatcher( _socket, *this );
      }

   public:

      virtual void maintenance( bool maintenance ) {
         if( maintenance ) {
            _automaton.process( Event::MAINTENANCE_ON );
         }
         else {
            _automaton.process( Event::MAINTENANCE_OFF );
         }
      }

      virtual void rechargerLaCaisse( const double & montant ) {
         _valeurCaisse += montant;
         _dab->setSoldeCaisse( _valeurCaisse );
         if( _valeurCaisse < RETRAIT_MAX ) {
            _automaton.process( Event::SOLDE_CAISSE_INSUFFISANT );
         }
      }

      virtual void anomalie( bool anomalie ) {
         if( anomalie ) {
            _automaton.process( Event::ANOMALIE_ON );
         }
         else {
            _automaton.process( Event::ANOMALIE_OFF );
         }
      }

      virtual void carteInseree( const std::string & id ) {
         _carte .invalidate();
         _compte.invalidate();
         _sc->getInformations( id );
         _automaton.process( Event::CARTE_INSEREE );
      }

      virtual void carteLue( const dab::Carte & carte, const dab::Compte & compte ) {
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

      virtual void codeSaisi( const std::string & code ) {
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

      virtual void montantSaisi( const double & montant ) {
         _valeurCaisse -= montant;
         _dab->setSoldeCaisse( _valeurCaisse );
         _sc->retrait( _carte.getId(), montant );
         _automaton.process( Event::MONTANT_OK );
      }

      virtual void carteRetiree( void ) {
         _automaton.process( Event::CARTE_RETIREE );
      }
      virtual void billetsRetires( void ) {
         _automaton.process( Event::BILLETS_RETIRES );
      }

      virtual void shutdown( void ) {
         _dab->shutdown();
         _sc->shutdown();
         _automaton.process( Event::TERMINATE );
         _running = false;
      }

   public:

      const std::string & getCarteId() const {
         return _carte.getId();
      }

      double getRetraitMax( void ) const {
         return std::min( _compte.getSolde(), (double)RETRAIT_MAX );
      }

   public:

      void run() {
         _running = true;
         while( _running ) {
            if( _dispatcher->hasDispatched()) {
               _dab->setStatus( _automaton.getCurrentState());
            }
         }
      }

   private:

      static const unsigned RETRAIT_MAX = 1000;

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

   friend
      std::ostream & operator << ( std::ostream & stream, const Event & event ) {
         switch( event ) {
         case Event::MAINTENANCE_ON          : return stream << "MAINTENANCE_ON";           break;
         case Event::MAINTENANCE_OFF         : return stream << "MAINTENANCE_OFF";          break;
         case Event::SOLDE_CAISSE_INSUFFISANT: return stream << "SOLDE_CAISSE_INSUFFISANT"; break;
         case Event::ANOMALIE_ON             : return stream << "ANOMALIE_ON";              break;
         case Event::ANOMALIE_OFF            : return stream << "ANOMALIE_OFF";             break;
         case Event::CARTE_INSEREE           : return stream << "CARTE_INSEREE";            break;
         case Event::CARTE_LUE_0             : return stream << "CARTE_LUE_0";              break;
         case Event::CARTE_LUE_1             : return stream << "CARTE_LUE_1";              break;
         case Event::CARTE_LUE_2             : return stream << "CARTE_LUE_2";              break;
         case Event::CARTE_INVALIDE          : return stream << "CARTE_INVALIDE";           break;
         case Event::BON_CODE                : return stream << "BON_CODE";                 break;
         case Event::MAUVAIS_CODE_1          : return stream << "MAUVAIS_CODE_1";           break;
         case Event::MAUVAIS_CODE_2          : return stream << "MAUVAIS_CODE_2";           break;
         case Event::MAUVAIS_CODE_3          : return stream << "MAUVAIS_CODE_3";           break;
         case Event::CARTE_CONFISQUEE        : return stream << "CARTE_CONFISQUEE";         break;
         case Event::SOLDE_INSUFFISANT       : return stream << "SOLDE_INSUFFISANT";        break;
         case Event::MONTANT_OK              : return stream << "MONTANT_OK";               break;
         case Event::CARTE_RETIREE           : return stream << "CARTE_RETIREE";            break;
         case Event::BILLETS_RETIRES         : return stream << "BILLETS_RETIRES";          break;
         case Event::TERMINATE               : return stream << "TERMINATE";                break;
         case Event::LAST                    : return stream << "LAST (inutilisé)";         break;
         }
         return stream << "inconnu (" << event << ")";
      }
   };
}
