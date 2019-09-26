#include "Carte.hpp"
#include "Compte.hpp"

#include <dab/Automaton.hpp>
#include <dab/IIHM.hpp>
#include <dab/ISiteCentral.hpp>
#include <dab/IUniteDeTraitementDispatcher.hpp>

namespace udt {

   class UniteDeTraitement : public dab::IUniteDeTraitement {
   public:

      UniteDeTraitement(
         const char *   intrfc,
         unsigned short udtPort,
         const char *   scAddress,
         unsigned short scPort,
         const char *   dabAddress,
         unsigned short dabPort     )
       :
         _dab         ( 0 ),
         _sc          ( 0 ),
         _dispatcher  ( 0 ),
         _running     ( false ),
         _valeurCaisse( 0.0 )
      {
         _socket.bind( intrfc, udtPort );
         sockaddr_in scTarget;
         sockaddr_in dabTarget;
         io::DatagramSocket::init( scAddress, scPort, scTarget );
         io::DatagramSocket::init( dabAddress, dabPort, dabTarget );
         _sc         = dab::newSiteCentral                ( _socket, { scTarget  });
         _dab        = dab::newIHM                        ( _socket, { dabTarget });
         _dispatcher = dab::newUniteDeTraitementDispatcher( _socket, *this );
      }

   public:

      virtual void maintenance( bool maintenance ) {
         if( maintenance ) {
            _automaton.process( dab::Evenement::MAINTENANCE_ON );
         }
         else {
            _automaton.process( dab::Evenement::MAINTENANCE_OFF );
         }
      }

      virtual void rechargerLaCaisse( const double & montant ) {
         _valeurCaisse += montant;
         _dab->setSoldeCaisse( _valeurCaisse );
         if( _valeurCaisse < RETRAIT_MAX ) {
            _automaton.process( dab::Evenement::SOLDE_CAISSE_INSUFFISANT );
         }
      }

      virtual void anomalie( bool anomalie ) {
         if( anomalie ) {
            _automaton.process( dab::Evenement::ANOMALIE_ON );
         }
         else {
            _automaton.process( dab::Evenement::ANOMALIE_OFF );
         }
      }

      virtual void carteInseree( const std::string & id ) {
         _carte .invalidate();
         _compte.invalidate();
         _sc->getInformations( id );
         _automaton.process( dab::Evenement::CARTE_INSEREE );
      }

      virtual void carteLue( const dab::Carte & carte, const dab::Compte & compte ) {
         _carte .set( carte.id, carte.code, carte.month, carte.year, carte.nbEssais );
         _compte.set( compte.id, compte.solde, compte.autorise );
         if( _carte.isValid() && _compte.isValid()) {
            if( _carte.getNbEssais() == 0 ) {
               _automaton.process( dab::Evenement::CARTE_LUE_0 );
            }
            else if( _carte.getNbEssais() == 1 ) {
               _automaton.process( dab::Evenement::CARTE_LUE_1 );
            }
            else if( _carte.getNbEssais() == 2 ) {
               _automaton.process( dab::Evenement::CARTE_LUE_2 );
            }
            else {
               _automaton.process( dab::Evenement::CARTE_CONFISQUEE );
               _dab->confisquerLaCarte();
            }
         }
         else {
            ::fprintf( stderr, "Carte et/ou compte invalide\n" );
            _automaton.process( dab::Evenement::CARTE_INVALIDE );
         }
      }

      virtual void codeSaisi( const std::string & code ) {
         if( _carte.compareCode( code )) {
            _automaton.process( dab::Evenement::BON_CODE );
         }
         else {
            _sc->incrNbEssais( _carte.getId());
            _carte.incrementeNbEssais();
            if( _carte.getNbEssais() == 1 ) {
               _automaton.process( dab::Evenement::MAUVAIS_CODE_1 );
            }
            else if( _carte.getNbEssais() == 2 ) {
               _automaton.process( dab::Evenement::MAUVAIS_CODE_2 );
            }
            else if( _carte.getNbEssais() == 3 ) {
               _automaton.process( dab::Evenement::MAUVAIS_CODE_3 );
               _dab->confisquerLaCarte();
            }
         }
      }

      virtual void montantSaisi( const double & montant ) {
         _valeurCaisse -= montant;
         _dab->setSoldeCaisse( _valeurCaisse );
         _sc->retrait( _carte.getId(), montant );
         _automaton.process( dab::Evenement::MONTANT_OK );
      }

      virtual void carteRetiree( void ) {
         _automaton.process( dab::Evenement::CARTE_RETIREE );
      }
      virtual void billetsRetires( void ) {
         _automaton.process( dab::Evenement::BILLETS_RETIRES );
      }

      virtual void shutdown( void ) {
         _dab->shutdown();
         _sc->shutdown();
         _automaton.process( dab::Evenement::TERMINATE );
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

      io::DatagramSocket                  _socket;
      dab::IIHM *                         _dab;
      dab::ISiteCentral *                 _sc;
      dab::IUniteDeTraitementDispatcher * _dispatcher;
      bool                                _running;
      dab::Automaton                      _automaton;
      Carte                               _carte;
      Compte                              _compte;
      double                              _valeurCaisse;

   private:
      UniteDeTraitement( const UniteDeTraitement & ) = delete;
      UniteDeTraitement & operator = ( const UniteDeTraitement & ) = delete;
   };
}
