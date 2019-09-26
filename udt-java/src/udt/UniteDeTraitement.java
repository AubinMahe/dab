package udt;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.channels.DatagramChannel;

import dab.Automaton;
import dab.Evenement;
import dab.IIHM;
import dab.ISiteCentral;
import dab.IUniteDeTraitement;
import dab.net.IHM;
import dab.net.SiteCentral;
import dab.net.UniteDeTraitementDispatcher;

public final class UniteDeTraitement implements IUniteDeTraitement {

   private static final double RETRAIT_MAX = 1000.0;

   private final DatagramChannel             _channel;
   private final IIHM                        _ui;
   private final ISiteCentral                _sc;
   private final UniteDeTraitementDispatcher _dispatcher;
   private final Automaton                   _automaton = new Automaton();
   private final Carte                       _carte     = new Carte();
   private final Compte                      _compte    = new Compte();
   private /* */ boolean                     _running;
   private /* */ double                      _valeurCaisse;

   public UniteDeTraitement( String intrfc, int udtPort, String scAddress, int scPort, String dabAddress, int dabPort ) throws IOException {
      _channel      = DatagramChannel
         .open( StandardProtocolFamily.INET )
         .setOption( StandardSocketOptions.SO_REUSEADDR, true )
         .bind     ( new InetSocketAddress( intrfc, udtPort ));
      _sc           = new SiteCentral                ( _channel, new InetSocketAddress( scAddress , scPort  ));
      _ui           = new IHM                        ( _channel, new InetSocketAddress( dabAddress, dabPort ));
      _dispatcher   = new UniteDeTraitementDispatcher( _channel, this );
      _valeurCaisse = 0.0;
   }

   /**
    * E4 : La mise en service d'un DAB est faite manuellement par l'opérateur
    */
   @Override
   public void maintenance( boolean maintenance ) {
      if( maintenance ) {
         _automaton.process( Evenement.MAINTENANCE_ON );
      }
      else {
         _automaton.process( Evenement.MAINTENANCE_OFF );
      }
   }

   /**
    * E1 : Le DAB peut déclencher sa mise hors service, lorsqu'il détecte que sa caisse comporte un solde inférieur à celui du montant
    * maximum autorisé pour un retrait (1 000 €)
    * E5 : l'opérateur est chargé du rechargement de la caisse du DAB.
    * @throws IOException
    */
   @Override
   public void rechargerLaCaisse( double  montant ) throws IOException {
      _valeurCaisse += montant;
      _ui.setSoldeCaisse( _valeurCaisse );
      if( _valeurCaisse < RETRAIT_MAX ) {
         _automaton.process( Evenement.SOLDE_CAISSE_INSUFFISANT );
      }
   }

   @Override
   public void anomalie( boolean anomalie ) {
      if( anomalie ) {
         _automaton.process( Evenement.ANOMALIE_ON );
      }
      else {
         _automaton.process( Evenement.ANOMALIE_OFF );
      }
   }

   /**
    * E20 : Le calcul du montant maximum du retrait auquel le client a droit, en fonction du solde du compte et de la somme maximum à
    * laquelle tous les clients ont droit (montant fixé à 1000 €) : la somme maximum que le client peut retirer correspond au minimum
    * de ces deux valeurs.
    */
   double getRetraitMax() {
      return Math.min( _compte.getSolde(), RETRAIT_MAX );
   }

   @Override
   public void carteInseree( String id ) throws IOException {
      _carte .invalidate();
      _compte.invalidate();
      _sc.getInformations( id );
      _automaton.process( Evenement.CARTE_INSEREE );
   }

   @SuppressWarnings("exports")
   @Override
   public void carteLue( dab.Carte  carte, dab.Compte  compte ) throws IOException {
      _carte .set( carte.id, carte.code, carte.month, carte.year, carte.nbEssais );
      _compte.set( compte.id, compte.solde, compte.autorise );
      if( _carte.isValid() && _compte.isValid()) {
         if( _carte.getNbEssais() == 0 ) {
            _automaton.process( Evenement.CARTE_LUE_0 );
         }
         else if( _carte.getNbEssais() == 1 ) {
            _automaton.process( Evenement.CARTE_LUE_1 );
         }
         else if( _carte.getNbEssais() == 2 ) {
            _automaton.process( Evenement.CARTE_LUE_2 );
         }
         else {
            _automaton.process( Evenement.CARTE_CONFISQUEE );
            _ui.confisquerLaCarte();
         }
      }
      else {
         System.err.printf( "Carte et/ou compte invalide\n" );
         _automaton.process( Evenement.CARTE_INVALIDE );
      }
   }

   @Override
   public void codeSaisi( String code ) throws IOException {
      if( _carte.compareCode( code )) {
         _automaton.process( Evenement.BON_CODE );
      }
      else {
         _sc.incrNbEssais( _carte.getId());
         _carte.incrementeNbEssais();
         if( _carte.getNbEssais() == 1 ) {
            _automaton.process( Evenement.MAUVAIS_CODE_1 );
         }
         else if( _carte.getNbEssais() == 2 ) {
            _automaton.process( Evenement.MAUVAIS_CODE_2 );
         }
         else if( _carte.getNbEssais() == 3 ) {
            _automaton.process( Evenement.MAUVAIS_CODE_3 );
            _ui.confisquerLaCarte();
         }
      }
   }

   @Override
   public void montantSaisi( double montant ) throws IOException {
      if( montant > getRetraitMax()) {
         _automaton.process( Evenement.SOLDE_INSUFFISANT );
         _ui.ejecterLaCarte();
      }
      else {
         _valeurCaisse -= montant;
         _ui.setSoldeCaisse( _valeurCaisse );
         _sc.retrait( _carte.getId(), montant );
         _automaton.process( Evenement.MONTANT_OK );
      }
   }

   @Override
   public void carteRetiree() {
      _automaton.process( Evenement.CARTE_RETIREE );
   }

   @Override
   public void billetsRetires() {
      _automaton.process( Evenement.BILLETS_RETIRES );
   }

   @Override
   public void shutdown() throws IOException {
      _ui.shutdown();
      _sc.shutdown();
      _automaton.process( Evenement.TERMINATE );
      _running = false;
   }

   public void run() throws IOException {
      _running = true;
      while( _running ) {
         if( _dispatcher.hasDispatched()) {
            _ui.setStatus( _automaton.getCurrentState());
         }
      }
   }
}
