package udt;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.channels.DatagramChannel;

import dab.Etat;
import dab.IIHM;
import dab.ISiteCentral;
import dab.IUniteDeTraitement;
import dab.net.IHM;
import dab.net.SiteCentral;
import dab.net.UniteDeTraitementDispatcher;
import util.Automaton;
import util.Automaton.Arc;
import util.Automaton.Shortcut;

class Date {

   Date( byte month, short year ) {
      _month = month;
      _year  = year;
   }

   void set( byte month, short year ) {
      _month = month;
      _year  = year;
   }

   boolean isValid() {
      return( 0 < _month )&&( _month < 13 )&&( _year > 2018 );
   }

   byte  _month;
   short _year;
}

class Carte {

   Carte() {
      _isValid = false;
   }

   void set( String carteID, String code, byte month, short year, byte nbEssais ) {
      _id       = carteID;
      _code     = code;
      _nbEssais = nbEssais;
      _isValid  = true;
      _peremption.set( month, year );
   }

   void incrementeNbEssais() { ++_nbEssais; }

   void invalidate() { _isValid = false; }

   boolean isValid() { return _isValid; }

   String getId() { return _id; }

   boolean compareCode( String code ) { return _code == code; }

   byte getNbEssais() { return _nbEssais; }

   boolean _isValid;
   String  _id;
   String  _code;
   String  _compte;
   Date    _peremption;
   byte    _nbEssais;
}

class Compte {

   Compte() {
      _isValid  = false;
      _id       = "";
      _solde    = 0.0;
      _autorise = false;
   }

   void set( String id, double  solde, boolean autorise ) {
      _id       = id;
      _solde    = solde;
      _autorise = autorise;
      _isValid  = true;
   }

   void invalidate() {
      System.err.print( "Compte.invalidate\n" );
      _isValid = false;
   }

   boolean isValid() { return _isValid; }

   String getId() { return _id; }

   double  getSolde() { return _solde; }

   boolean _isValid;
   String  _id;
   double  _solde;
   boolean _autorise;
}

enum Event {

   MAINTENANCE_ON,
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
   ;
}

public final class UniteDeTraitement implements IUniteDeTraitement {

   static final double RETRAIT_MAX = 1000.0;

   private final DatagramChannel             _channel;
   private final IIHM                        _ui;
   private final ISiteCentral                _sc;
   private final UniteDeTraitementDispatcher _dispatcher;
   private final Automaton<Etat, Event>      _automaton;
   private final Carte                       _carte  = new Carte();
   private final Compte                      _compte = new Compte();
   private /* */ boolean                     _running;
   private /* */ double                      _valeurCaisse;

   @SuppressWarnings({ "unchecked", "rawtypes" })
   public UniteDeTraitement( String intrfc, int udtPort, String scAddress, int scPort, String dabAddress, int dabPort ) throws IOException {
      _channel = DatagramChannel.open( StandardProtocolFamily.INET )
         .setOption( StandardSocketOptions.SO_REUSEADDR, true )
         .bind     ( new InetSocketAddress( intrfc, udtPort ));
      _sc         = new SiteCentral                ( _channel, new InetSocketAddress( scAddress , scPort  ));
      _ui         = new IHM                        ( _channel, new InetSocketAddress( dabAddress, dabPort ));
      _dispatcher = new UniteDeTraitementDispatcher( _channel, this );
      _automaton = new Automaton<Etat, Event>(
         Etat.AUCUN, new Automaton.Arc[] {
            new Arc( Etat.AUCUN          , Event.MAINTENANCE_ON          , Etat.MAINTENANCE     ),
            new Arc( Etat.MAINTENANCE    , Event.MAINTENANCE_OFF         , Etat.EN_SERVICE      ),
            new Arc( Etat.MAINTENANCE    , Event.SOLDE_CAISSE_INSUFFISANT, Etat.MAINTENANCE     ),
            new Arc( Etat.EN_SERVICE     , Event.MAINTENANCE_ON          , Etat.MAINTENANCE     ),
            new Arc( Etat.MAINTENANCE    , Event.ANOMALIE_ON             , Etat.HORS_SERVICE    ),
            new Arc( Etat.HORS_SERVICE   , Event.ANOMALIE_OFF            , Etat.MAINTENANCE     ),
            new Arc( Etat.HORS_SERVICE   , Event.MAINTENANCE_ON          , Etat.MAINTENANCE     ),
            new Arc( Etat.EN_SERVICE     , Event.SOLDE_CAISSE_INSUFFISANT, Etat.HORS_SERVICE    ),
            new Arc( Etat.EN_SERVICE     , Event.CARTE_INSEREE           , Etat.LECTURE_CARTE   ),
            new Arc( Etat.LECTURE_CARTE  , Event.CARTE_LUE_0             , Etat.SAISIE_CODE_1   ),
            new Arc( Etat.LECTURE_CARTE  , Event.CARTE_LUE_1             , Etat.SAISIE_CODE_2   ),
            new Arc( Etat.LECTURE_CARTE  , Event.CARTE_LUE_2             , Etat.SAISIE_CODE_3   ),
            new Arc( Etat.LECTURE_CARTE  , Event.CARTE_INVALIDE          , Etat.EN_SERVICE      ),
            new Arc( Etat.LECTURE_CARTE  , Event.CARTE_CONFISQUEE        , Etat.EN_SERVICE      ),
            new Arc( Etat.SAISIE_CODE_1  , Event.BON_CODE                , Etat.SAISIE_MONTANT  ),
            new Arc( Etat.SAISIE_CODE_1  , Event.MAUVAIS_CODE_1          , Etat.SAISIE_CODE_2   ),
            new Arc( Etat.SAISIE_CODE_2  , Event.BON_CODE                , Etat.SAISIE_MONTANT  ),
            new Arc( Etat.SAISIE_CODE_2  , Event.MAUVAIS_CODE_2          , Etat.SAISIE_CODE_3   ),
            new Arc( Etat.SAISIE_CODE_3  , Event.BON_CODE                , Etat.SAISIE_MONTANT  ),
            new Arc( Etat.SAISIE_CODE_3  , Event.MAUVAIS_CODE_3          , Etat.EN_SERVICE      ),
            new Arc( Etat.SAISIE_MONTANT , Event.MONTANT_OK              , Etat.RETRAIT_CARTE   ),
            new Arc( Etat.RETRAIT_CARTE  , Event.CARTE_RETIREE           , Etat.RETRAIT_BILLETS ),
            new Arc( Etat.RETRAIT_BILLETS, Event.BILLETS_RETIRES         , Etat.EN_SERVICE      ),
         },
         new Automaton.Shortcut[] {
            new Shortcut( Event.TERMINATE  , Etat.HORS_SERVICE ),
            new Shortcut( Event.ANOMALIE_ON, Etat.HORS_SERVICE ),
         }
      );
      _valeurCaisse = 0.0;
   }

   /**
    * E4 : La mise en service d'un DAB est faite manuellement par l'opérateur
    */
   @Override
   public void maintenance( boolean maintenance ) {
      if( maintenance ) {
         _automaton.process( Event.MAINTENANCE_ON );
      }
      else {
         _automaton.process( Event.MAINTENANCE_OFF );
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
         _automaton.process( Event.SOLDE_CAISSE_INSUFFISANT );
      }
   }

   @Override
   public void anomalie( boolean anomalie ) {
      if( anomalie ) {
         _automaton.process( Event.ANOMALIE_ON );
      }
      else {
         _automaton.process( Event.ANOMALIE_OFF );
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
      _automaton.process( Event.CARTE_INSEREE );
   }

   @SuppressWarnings("exports")
   @Override
   public void carteLue( dab.Carte  carte, dab.Compte  compte ) throws IOException {
      _carte .set( carte.id, carte.code, carte.month, carte.year, carte.nbEssais );
      _compte.set( compte.id, compte.solde, compte.autorise );
      if( _carte.isValid() && _compte.isValid()) {
         if( _carte.getNbEssais() == 0 ) {
            _automaton.process( Event.CARTE_LUE_0 );
         }
         else if( _carte.getNbEssais() == 1 ) {
            _automaton.process( Event.CARTE_LUE_1 );
         }
         else if( _carte.getNbEssais() == 2 ) {
            _automaton.process( Event.CARTE_LUE_2 );
         }
         else {
            _automaton.process( Event.CARTE_CONFISQUEE );
            _ui.confisquerLaCarte();
         }
      }
      else {
         System.err.printf( "Carte et/ou compte invalide\n" );
         _automaton.process( Event.CARTE_INVALIDE );
      }
   }

   @Override
   public void codeSaisi( String code ) throws IOException {
      if( _carte.compareCode( code )) {
         _automaton.process( Event.BON_CODE );
      }
      else {
         _sc.incrNbEssais( _carte.getId());
         _carte.incrementeNbEssais();
         if( _carte.getNbEssais() == 1 ) {
            _automaton.process( Event.MAUVAIS_CODE_1 );
         }
         else if( _carte.getNbEssais() == 2 ) {
            _automaton.process( Event.MAUVAIS_CODE_2 );
         }
         else if( _carte.getNbEssais() == 3 ) {
            _automaton.process( Event.MAUVAIS_CODE_3 );
            _ui.confisquerLaCarte();
         }
      }
   }

   @Override
   public void montantSaisi( double  montant ) throws IOException {
      _valeurCaisse -= montant;
      _ui.setSoldeCaisse( _valeurCaisse );
      _sc.retrait( _carte.getId(), montant );
      _automaton.process( Event.MONTANT_OK );
   }

   @Override
   public void carteRetiree() {
      _automaton.process( Event.CARTE_RETIREE );
   }

   @Override
   public void billetsRetires() {
      _automaton.process( Event.BILLETS_RETIRES );
   }

   @Override
   public void shutdown() throws IOException {
      _ui.shutdown();
      _sc.shutdown();
      _automaton.process( Event.TERMINATE );
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
