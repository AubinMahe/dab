package udt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import dab.Controleur;
import dabtypes.Carte;
import dabtypes.Compte;
import dabtypes.Etat;

@SuppressWarnings("static-method")
@TestMethodOrder(OrderAnnotation.class)
class APITests {

   private static final Carte      carte        = new Carte();
   private static final Compte     compte       = new Compte();
   private static /* */ double     soldeCaisse = 10_000.0;
   private static /* */ Controleur ctrl;

   @BeforeAll
   static void initAll() throws IOException {
      carte.id       = "A123";
      carte.code     = "1230";
      carte.month    = 6;
      carte.year     = 2020;
      carte.nbEssais = 0;

      compte.id       = "A123";
      compte.autorise = true;
      compte.solde    = 3_257.23;

      ctrl = new Controleur( "udt1" );
      assertEquals( ctrl.getEtat(), Etat.MAINTENANCE );
      ctrl.rechargerLaCaisse( soldeCaisse );
      ctrl.maintenance( false );
      assertEquals( ctrl.getEtat(), Etat.EN_SERVICE );
      assertEquals( ctrl.getSoldeCaisse(), soldeCaisse );
   }

   @Test
   @Order(1)
   void nominal() throws IOException {
      final double RETRAIT = 120.00;
      assertEquals( ctrl.getEtat(), Etat.EN_SERVICE );
      ctrl.carteInseree( "A123" );
      assertEquals( ctrl.getEtat(), Etat.LECTURE_CARTE );
      ctrl.getInformations( carte, compte );
      assertEquals( ctrl.getEtat(), Etat.SAISIE_CODE_1 );
      ctrl.codeSaisi( "0000" );
      assertEquals( ctrl.getEtat(), Etat.SAISIE_CODE_2 );
      ctrl.codeSaisi( "0000" );
      assertEquals( ctrl.getEtat(), Etat.SAISIE_CODE_3 );
      ctrl.codeSaisi( carte.code );
      assertEquals( ctrl.getEtat(), Etat.SAISIE_MONTANT );
      ctrl.montantSaisi( RETRAIT );
      assertEquals( ctrl.getEtat(), Etat.RETRAIT_CARTE_BILLETS );
      ctrl.carteRetiree();
      assertEquals( ctrl.getEtat(), Etat.RETRAIT_BILLETS );
      ctrl.billetsRetires();
      assertEquals( ctrl.getEtat(), Etat.EN_SERVICE );
      soldeCaisse -= RETRAIT;
      assertEquals( ctrl.getSoldeCaisse(), soldeCaisse );
   }

   @Test
   @Order(2)
   void soldeInsuffisant() throws IOException {
      assertEquals( ctrl.getEtat(), Etat.EN_SERVICE );
      ctrl.carteInseree( "A123" );
      assertEquals( ctrl.getEtat(), Etat.LECTURE_CARTE );
      ctrl.getInformations( carte, compte );
      assertEquals( ctrl.getEtat(), Etat.SAISIE_CODE_1 );
      ctrl.codeSaisi( carte.code );
      assertEquals( ctrl.getEtat(), Etat.SAISIE_MONTANT );
      ctrl.montantSaisi( 5000.00 );
      assertEquals( ctrl.getEtat(), Etat.RETRAIT_CARTE_SOLDE_COMPTE );
      ctrl.carteRetiree();
      assertEquals( ctrl.getEtat(), Etat.EN_SERVICE );
      assertEquals( ctrl.getSoldeCaisse(), soldeCaisse );
   }

   @Test
   @Order(3)
   void annulation() throws IOException {
      assertEquals( ctrl.getEtat(), Etat.EN_SERVICE );
      ctrl.carteInseree( "A123" );
      assertEquals( ctrl.getEtat(), Etat.LECTURE_CARTE );
      ctrl.getInformations( carte, compte );
      assertEquals( ctrl.getEtat(), Etat.SAISIE_CODE_1 );
      ctrl.codeSaisi( carte.code );
      assertEquals( ctrl.getEtat(), Etat.SAISIE_MONTANT );
      ctrl.montantSaisi( 200.00 );
      assertEquals( ctrl.getEtat(), Etat.RETRAIT_CARTE_BILLETS );
      ctrl.annulationDemandeeParLeClient();
      assertEquals( ctrl.getEtat(), Etat.EN_SERVICE );
      assertEquals( ctrl.getSoldeCaisse(), soldeCaisse );
   }
}