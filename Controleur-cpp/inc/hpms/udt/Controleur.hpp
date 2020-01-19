#pragma once

#include <hpms/udt/ControleurComponent.hpp>

#include "Carte.hpp"
#include "Compte.hpp"

namespace hpms::udt {

   class Controleur : public hpms::udt::ControleurComponent {
   public:

      Controleur( void );

      virtual ~ Controleur( void ) {}

   public:

      virtual void maintenance( bool maintenance );

      virtual void rechargerLaCaisse( const double & montant );

      virtual void anomalie( bool anomalie );

      virtual void carteInseree( const char * id );

      virtual void informationsResponse( const hpms::dabtypes::Information & information );

      virtual void codeSaisi( const char * code );

      virtual void montantSaisi( const double & montant );

      virtual void carteRetiree( void );

      virtual void billetsRetires( void );

      virtual void annulationDemandeeParLeClient();

      virtual void arret( void );

   public:

      /**
       * Méthode appelée après réception et traitement d'un événement ou d'une requête.
       * L'état de l'automate à sans doute été mis à jour, il faut donc le publier.
       */
      virtual void afterDispatch( void );

   private:

      void confisquerLaCarte( void );

      void placerLesBilletsDansLaCorbeille( void );

   public:

      virtual void armerLeTimeoutDeSaisieDuCode       ( void );
      virtual void armerLeTimeoutDeSaisieDuMontant    ( void );
      virtual void armerLeTimeoutDeRetraitDeLaCarte   ( void );
      virtual void armerLeTimeoutDeRetraitDesBillets  ( void );
      virtual void annulerLeTimeoutDeRetraitDesBillets( void );

      virtual void saisieDuCodeElapsed     ( void );
      virtual void saisieDuMontantElapsed  ( void );
      virtual void retraitDeLaCarteElapsed ( void );
      virtual void retraitDesBilletsElapsed( void );

   private:

      Carte  _carte;
      Compte _compte;
      double _montantDeLatransactionEnCours;
   };
}
