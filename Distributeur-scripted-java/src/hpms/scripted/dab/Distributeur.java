package hpms.scripted.dab;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.function.BooleanSupplier;

import da.IMainLoop;
import da.InstanceID;
import hpms.dabtypes.Etat;

public class Distributeur extends hpms.scripted.dab.DistributeurComponent {

   private static final long SAISIE_DU_CODE_DURATION      = 30 * 1000L;
   private static final long SAISIE_DU_MONTANT_DURATION   = 30 * 1000L;
   private static final long RETRAIT_DE_LA_CARTE_DURATION = 10 * 1000L;
   private static final long RETRAIT_DES_BILLETS_DURATION = 10 * 1000L;
   private static final long BANQUE_DELAI_DE_TRAITEMENT   =  3 * 1000L;

   private long           _tempsDeReponse = 20L;
   private long           _atStart;
   private BufferedReader _scLog;
   @SuppressWarnings("unused")
   private BufferedReader _udtLog;
   private double         _soldeCompteBefore;
   private double         _soldeCompteAfter;

   private boolean startTimeout( long milliseconds, BooleanSupplier predicate ) {
      final long atStart = System.currentTimeMillis();
      milliseconds += _tempsDeReponse;
      while( System.currentTimeMillis() - atStart < milliseconds ) {
         if( predicate.getAsBoolean()) {
            return true;
         }
         try {
            Thread.sleep( 10L );
         }
         catch( final InterruptedException e ) {
            e.printStackTrace();
         }
      }
      return predicate.getAsBoolean();
   }

   public Distributeur( InstanceID instanceID, IMainLoop mainLoop ) {
      super( instanceID, mainLoop );
      _etatDuDab.etat = Etat.MAINTENANCE;
   }

   @Override
   public void etatDuDabPublished() throws IOException {
      System.out.printf( "OK    : %6d : %s|etat = %s, solde = %7.2f\n",
         System.currentTimeMillis() - _atStart,
         Thread.currentThread().getStackTrace()[1].getMethodName(),
         _etatDuDab.etat, _etatDuDab.soldeCaisse );
   }

   @Override
   public void confisquerLaCarte() throws IOException {
      System.out.printf( "OK    : %6d : %s|etat = %s, solde = %7.2f\n",
         System.currentTimeMillis() - _atStart,
         Thread.currentThread().getStackTrace()[1].getMethodName(),
         _etatDuDab.etat, _etatDuDab.soldeCaisse );
   }

   @Override
   public void placerLesBilletsDansLaCorbeille() throws IOException {
      System.out.printf( "OK    : %6d : %s|etat = %s, solde = %7.2f\n",
         System.currentTimeMillis() - _atStart,
         Thread.currentThread().getStackTrace()[1].getMethodName(),
         _etatDuDab.etat, _etatDuDab.soldeCaisse );
   }

   @Override
   public void ejecterLaCarte() throws IOException {
      System.out.printf( "OK    : %6d : %s|etat = %s, solde = %7.2f\n",
         System.currentTimeMillis() - _atStart,
         Thread.currentThread().getStackTrace()[1].getMethodName(),
         _etatDuDab.etat, _etatDuDab.soldeCaisse );
   }

   @Override
   public void ejecterLesBillets( double montant ) throws IOException {
      System.out.printf( "OK    : %6d : %s|etat = %s, solde = %7.2f, montant = %7.2f\n",
         System.currentTimeMillis() - _atStart,
         Thread.currentThread().getStackTrace()[1].getMethodName(),
         _etatDuDab.etat, _etatDuDab.soldeCaisse, montant );
   }

   @Override
   public void arret() throws IOException {
      _mainLoop.terminate();
   }

   private double getSolde() throws NumberFormatException, IOException {
      double solde = 0.0;
      String line;
      while(( line = _scLog.readLine()) != null ) {
         if( line.startsWith( "TEST|solde    = " )) {
            final String amount = line.substring( "TEST|solde    = ".length()).strip().replaceAll( ",", "." );
            solde = Double.parseDouble( amount );
         }
      }
      return solde;
   }

   private boolean nominal() throws IOException, InterruptedException {
      boolean retVal = false;
      _uniteDeTraitement.rechargerLaCaisse( 1000 );
      System.out.printf( "OK    : %6d : rechargerLaCaisse\n", System.currentTimeMillis() - _atStart );
      if( startTimeout( 0, () -> _etatDuDab.etat == Etat.MAINTENANCE )) {
         getMaintenable().maintenance( false );
         System.out.printf( "OK    : %6d : maintenance off\n", System.currentTimeMillis() - _atStart );
         if( startTimeout( 0, () -> _etatDuDab.etat == Etat.EN_SERVICE )) {
            getUniteDeTraitement().carteInseree( "A123" );
            System.out.printf( "OK    : %6d : carteInseree\n", System.currentTimeMillis() - _atStart );
            if( startTimeout( 0, () -> _etatDuDab.etat == Etat.LECTURE_CARTE )) {
               System.out.printf( "OK    : %6d : lecture en cours\n", System.currentTimeMillis() - _atStart );
               if( startTimeout( BANQUE_DELAI_DE_TRAITEMENT, () -> _etatDuDab.etat == Etat.SAISIE_CODE_1 )) {
                  _soldeCompteBefore = getSolde();
                  System.out.printf( "OK    : %6d : solde compte = %7.2f\n", System.currentTimeMillis() - _atStart, _soldeCompteBefore );
                  _uniteDeTraitement.codeSaisi( "1230" );
                  System.out.printf( "OK    : %6d : codeSaisi\n", System.currentTimeMillis() - _atStart );
                  if( startTimeout( 0, () -> _etatDuDab.etat == Etat.SAISIE_MONTANT )) {
                     _uniteDeTraitement.montantSaisi( 200.0 );
                     System.out.printf( "OK    : %6d : montantSaisi\n", System.currentTimeMillis() - _atStart );
                     if( startTimeout( 0, () -> _etatDuDab.etat == Etat.RETRAIT_CARTE_BILLETS )) {
                        _uniteDeTraitement.carteRetiree();
                        System.out.printf( "OK    : %6d : carteRetiree\n", System.currentTimeMillis() - _atStart );
                        if( startTimeout( 0, () -> _etatDuDab.etat == Etat.RETRAIT_CARTE_BILLETS )) {
                           _uniteDeTraitement.billetsRetires();
                           System.out.printf( "OK    : %6d : billetsRetires\n", System.currentTimeMillis() - _atStart );
                           if( startTimeout( 0, () -> _etatDuDab.etat == Etat.EN_SERVICE )) {
                              if( _etatDuDab.soldeCaisse == 1_000 - 200 ) {
                                 System.out.printf( "OK    : %6d : soldeCaisse est bon\n", System.currentTimeMillis() - _atStart );
                                 Thread.sleep( 500L );
                                 _soldeCompteAfter = getSolde();
                                 if( _soldeCompteAfter == _soldeCompteBefore - 200 ) {
                                    System.out.printf( "OK    : %6d : soldeCompte est bon\n", System.currentTimeMillis() - _atStart );
                                    _soldeCompteBefore = _soldeCompteAfter;
                                    retVal = true;
                                 }
                                 else {
                                    System.out.printf( "échec : %6d : solde compte est faux (%7.2f)\n",
                                       System.currentTimeMillis() - _atStart, _soldeCompteAfter );
                                 }
                              }
                              else {
                                 System.out.printf( "échec : %6d : solde caisse est faux (%7.2f)\n",
                                    System.currentTimeMillis() - _atStart, _etatDuDab.soldeCaisse );
                              }
                           }
                           else {
                              System.out.printf( "échec : %6d : timeout sur 'En service' en fin de transaction\n",
                                 System.currentTimeMillis() - _atStart );
                           }
                        }
                        else {
                           System.out.printf( "échec : %6d : timeout sur 'Retrait des billets'\n",
                              System.currentTimeMillis() - _atStart );
                        }
                     }
                     else {
                        System.out.printf( "échec : %6d : timeout sur 'Retrait de la carte'\n",
                           System.currentTimeMillis() - _atStart );
                     }
                  }
                  else {
                     System.out.printf( "échec : %6d : timeout sur 'Saisie du montant'\n", System.currentTimeMillis() - _atStart );
                  }
               }
               else {
                  System.out.printf( "échec : %6d : timeout sur 'Réponse de la banque'\n", System.currentTimeMillis() - _atStart );
               }
            }
            else {
               System.out.printf( "échec : %6d : timeout sur 'Carte insérée'\n", System.currentTimeMillis() - _atStart );
            }
         }
         else {
            System.out.printf( "échec : %6d : timeout sur 'En service' initial\n", System.currentTimeMillis() - _atStart );
         }
      }
      else {
         System.out.printf( "échec : %6d : timeout sur 'Recharger la caisse'\n", System.currentTimeMillis() - _atStart );
      }
      return retVal;
   }

   private boolean timeoutSaisieDuCode() throws IOException {
      boolean retVal = false;
      getUniteDeTraitement().carteInseree( "A123" );
      if( startTimeout( 0, () -> _etatDuDab.etat == Etat.LECTURE_CARTE )) {
         if( startTimeout( BANQUE_DELAI_DE_TRAITEMENT, () -> _etatDuDab.etat == Etat.SAISIE_CODE_1 )) {
            if( startTimeout( SAISIE_DU_CODE_DURATION, () -> _etatDuDab.etat == Etat.EN_SERVICE )) {
               if( _etatDuDab.soldeCaisse == 1_000 - 200 ) {
                  System.out.printf( "OK    : %6d : timeout de saisie du code vérifié\n", System.currentTimeMillis() - _atStart );
                  final double soldeCompte = getSolde();
                  if( soldeCompte == _soldeCompteBefore ) {
                     System.out.printf( "OK    : %6d : solde compte est inchangé\n",
                        System.currentTimeMillis() - _atStart, _soldeCompteAfter );
                     retVal = true;
                  }
                  else {
                     System.out.printf( "échec : %6d : solde compte est faux (%7.2f)\n",
                        System.currentTimeMillis() - _atStart, soldeCompte );
                  }
               }
               else {
                  System.out.printf( "échec : %6d : solde caisse est faux (7.2f)\n",
                     System.currentTimeMillis() - _atStart, _etatDuDab.soldeCaisse );
               }
            }
            else {
               System.out.printf( "échec : %6d : le timeout de saisie du code n'a pas expiré\n",
                  System.currentTimeMillis() - _atStart );
            }
         }
         else {
            System.out.printf( "échec : %6d : timeout sur 'Réponse de la banque'\n", System.currentTimeMillis() - _atStart );
         }
      }
      else {
         System.out.printf( "échec : %6d : timeout sur 'Carte insérée'\n", System.currentTimeMillis() - _atStart );
      }
      return retVal;
   }

   private boolean timeoutSaisieDuMontant() throws IOException {
      boolean retVal = false;
      getUniteDeTraitement().carteInseree( "A123" );
      if( startTimeout( 0, () -> _etatDuDab.etat == Etat.LECTURE_CARTE )) {
         if( startTimeout( BANQUE_DELAI_DE_TRAITEMENT, () -> _etatDuDab.etat == Etat.SAISIE_CODE_1 )) {
            _uniteDeTraitement.codeSaisi( "1230" );
            if( startTimeout( 0, () -> _etatDuDab.etat == Etat.SAISIE_MONTANT )) {
               if( startTimeout( SAISIE_DU_MONTANT_DURATION, () -> _etatDuDab.etat == Etat.EN_SERVICE )) {
                  System.out.printf( "OK    : %6d : timeout de saisie du montant vérifié\n", System.currentTimeMillis() - _atStart );
                  final double soldeCompte = getSolde();
                  if( soldeCompte == _soldeCompteBefore ) {
                     System.out.printf( "OK    : %6d : solde compte est inchangé\n",
                        System.currentTimeMillis() - _atStart, _soldeCompteAfter );
                     retVal = true;
                  }
                  else {
                     System.out.printf( "échec : %6d : solde compte est faux (%7.2f)\n",
                        System.currentTimeMillis() - _atStart, soldeCompte );
                  }
               }
               else {
                  System.out.printf( "échec : %6d : le timeout de saisie du montant n'a pas expiré\n",
                     System.currentTimeMillis() - _atStart );
               }
            }
            else {
               System.out.printf( "échec : %6d : timeout sur 'Saisie du montant'\n", System.currentTimeMillis() - _atStart );
            }
         }
         else {
            System.out.printf( "échec : %6d : timeout sur 'Réponse de la banque'\n", System.currentTimeMillis() - _atStart );
         }
      }
      else {
         System.out.printf( "échec : %6d : timeout sur 'Carte insérée'\n", System.currentTimeMillis() - _atStart );
      }
      return retVal;
   }

   private boolean timeoutRetirerLaCarte() throws IOException {
      boolean retVal = false;
      getUniteDeTraitement().carteInseree( "A123" );
      if( startTimeout( 0, () -> _etatDuDab.etat == Etat.LECTURE_CARTE )) {
         if( startTimeout( BANQUE_DELAI_DE_TRAITEMENT, () -> _etatDuDab.etat == Etat.SAISIE_CODE_1 )) {
            _uniteDeTraitement.codeSaisi( "1230" );
            if( startTimeout( 0, () -> _etatDuDab.etat == Etat.SAISIE_MONTANT )) {
               _uniteDeTraitement.montantSaisi( 200.0 );
               if( startTimeout( 0, () -> _etatDuDab.etat == Etat.RETRAIT_CARTE_BILLETS )) {
                  if( startTimeout( RETRAIT_DE_LA_CARTE_DURATION, () -> _etatDuDab.etat == Etat.EN_SERVICE )) {
                     System.out.printf( "OK    : %6d : timeout de retrait de la carte vérifié\n",
                        System.currentTimeMillis() - _atStart );
                     final double soldeCompte = getSolde();
                     if( soldeCompte == _soldeCompteBefore ) {
                        System.out.printf( "OK    : %6d : solde compte est inchangé\n",
                           System.currentTimeMillis() - _atStart, _soldeCompteAfter );
                        retVal = true;
                     }
                     else {
                        System.out.printf( "échec : %6d : solde compte est faux (%7.2f)\n",
                           System.currentTimeMillis() - _atStart, soldeCompte );
                     }
                  }
                  else {
                     System.out.printf( "échec : %6d : le timeout de retrait de la carte n'a pas expiré\n",
                        System.currentTimeMillis() - _atStart );
                  }
               }
               else {
                  System.out.printf( "échec : %6d : timeout sur 'Retrait de la carte'\n", System.currentTimeMillis() - _atStart );
               }
            }
            else {
               System.out.printf( "échec : %6d : timeout sur 'Saisie du montant'\n", System.currentTimeMillis() - _atStart );
            }
         }
         else {
            System.out.printf( "échec : %6d : timeout sur 'Réponse de la banque'\n", System.currentTimeMillis() - _atStart );
         }
      }
      else {
         System.out.printf( "échec : %6d : timeout sur 'Carte insérée'\n", System.currentTimeMillis() - _atStart );
      }
      return retVal;
   }

   private boolean timeoutRetirerLesBillets() throws IOException, InterruptedException {
      boolean retVal = false;
      getUniteDeTraitement().carteInseree( "A123" );
      if( startTimeout( 0, () -> _etatDuDab.etat == Etat.LECTURE_CARTE )) {
         if( startTimeout( BANQUE_DELAI_DE_TRAITEMENT, () -> _etatDuDab.etat == Etat.SAISIE_CODE_1 )) {
            _uniteDeTraitement.codeSaisi( "1230" );
            if( startTimeout( 0, () -> _etatDuDab.etat == Etat.SAISIE_MONTANT )) {
               _uniteDeTraitement.montantSaisi( 200.0 );
               if( startTimeout( 0, () -> _etatDuDab.etat == Etat.RETRAIT_CARTE_BILLETS )) {
                  _uniteDeTraitement.carteRetiree();
                  if( startTimeout( 0, () -> _etatDuDab.etat == Etat.RETRAIT_CARTE_BILLETS )) {
                     if( startTimeout( RETRAIT_DES_BILLETS_DURATION, () -> _etatDuDab.etat == Etat.EN_SERVICE )) {
                        System.out.printf( "OK    : %6d : timeout de retrait des billets vérifié\n",
                           System.currentTimeMillis() - _atStart );
                        Thread.sleep( 500L );
                        _soldeCompteAfter = getSolde();
                        if( _soldeCompteBefore - _soldeCompteAfter == 200 ) {
                           System.out.printf( "OK    : %6d : solde compte est correct\n",
                              System.currentTimeMillis() - _atStart, _soldeCompteAfter );
                           retVal = true;
                        }
                        else {
                           System.out.printf( "échec : %6d : solde compte est faux (%7.2f)\n",
                              System.currentTimeMillis() - _atStart, _soldeCompteAfter );
                        }
                     }
                     else {
                        System.out.printf( "échec : %6d : le timeout de retrait de la carte n'a pas expiré\n",
                           System.currentTimeMillis() - _atStart );
                     }
                  }
                  else {
                     System.out.printf( "échec : %6d : timeout sur 'Retrait des billets'\n",
                        System.currentTimeMillis() - _atStart );
                  }
               }
               else {
                  System.out.printf( "échec : %6d : timeout sur 'Retrait de la carte'\n", System.currentTimeMillis() - _atStart );
               }
            }
            else {
               System.out.printf( "échec : %6d : timeout sur 'Saisie du montant'\n", System.currentTimeMillis() - _atStart );
            }
         }
         else {
            System.out.printf( "échec : %6d : timeout sur 'Réponse de la banque'\n", System.currentTimeMillis() - _atStart );
         }
      }
      else {
         System.out.printf( "échec : %6d : timeout sur 'Carte insérée'\n", System.currentTimeMillis() - _atStart );
      }
      return retVal;
   }

   public void run( int delay, File scLogFile, File udtLogFile ) throws IOException {
      try(
         final BufferedReader scLog  = new BufferedReader( new FileReader( scLogFile  ));
         final BufferedReader udtLog = new BufferedReader( new FileReader( udtLogFile )) )
      {
         _scLog          = scLog;
         _udtLog         = udtLog;
         _tempsDeReponse = delay;
         _atStart        = System.currentTimeMillis();
         if(   nominal()
            && timeoutSaisieDuCode()
            && timeoutSaisieDuMontant()
            && timeoutRetirerLaCarte()
            && timeoutRetirerLesBillets())
         {
            System.out.printf( "OK    : %6d : all tests done with success.\n", System.currentTimeMillis() - _atStart );
         }
      }
      catch( final Throwable t ) {
         t.printStackTrace();
      }
      _scLog  = null;
      _udtLog = null;
      _uniteDeTraitement.arret();
      _mainLoop.terminate();
   }
}
