#include <DAB/distributeur.h>
#include <DAB/distributeur_ui.h>

#include <DBT/evenement.h>

#include <util/log.h>
#include <os/errors.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

util_error DAB_distributeur_init( DAB_distributeur * This ) {
   UTIL_LOG_HERE();
   memset( This, 0, sizeof( DAB_distributeur ));
   DAB_business_logic_data * bl = (DAB_business_logic_data *)malloc( sizeof( DAB_business_logic_data ));
   memset( bl, 0, sizeof( DAB_business_logic_data ));
   This->user_context = bl;
   return UTIL_NO_ERROR;
}

util_error DAB_distributeur_etat_du_dab_published( DAB_distributeur * This ) {
   UTIL_LOG_HERE();
   return UTIL_NO_ERROR;
   (void)This;
}

util_error DAB_distributeur_ejecter_la_carte( DAB_distributeur * This ) {
   UTIL_LOG_HERE();
   return UTIL_NO_ERROR;
   (void)This;
}

util_error DAB_distributeur_ejecter_les_billets( DAB_distributeur * This, double montant ) {
   UTIL_LOG_ARGS( "montant = %7.2f", montant );
   DAB_business_logic_data * bl = (DAB_business_logic_data *)This->user_context;
   bl->montant = montant;
   return UTIL_NO_ERROR;
   (void)This;
}

util_error DAB_distributeur_confisquer_la_carte( DAB_distributeur * This ) {
   UTIL_LOG_HERE();
   return UTIL_NO_ERROR;
   (void)This;
}

util_error DAB_distributeur_placer_les_billets_dans_la_corbeille( DAB_distributeur * This ) {
   UTIL_LOG_HERE();
   return UTIL_NO_ERROR;
   (void)This;
}

util_error DAB_distributeur_shutdown( DAB_distributeur * This ) {
   UTIL_LOG_HERE();
   UTIL_ERROR_CHECK( DAB_distributeur_dispatcher_terminate( This->dispatcher ));
   free( This->user_context );
   UTIL_LOG_DONE();
   return UTIL_NO_ERROR;
}

util_error DAB_distributeur_before_dispatch( DAB_distributeur * This ) {
   UTIL_LOG_HERE();
   return UTIL_NO_ERROR;
   (void)This;
}

util_error DAB_distributeur_after_dispatch( DAB_distributeur * This, bool hasDispatched ) {
   UTIL_LOG_HERE();
   DAB_business_logic_data * bl = (DAB_business_logic_data *)This->user_context;
   bl->refresh = hasDispatched;
   return UTIL_NO_ERROR;
}
