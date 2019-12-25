#include <SC/banque.h>
#include <SC/repository.h>
#include <SC/banque_ui.h>

#include <DBT/evenement.h>

#include <util/log.h>
#include <os/sleep.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

util_error SC_banque_init( SC_banque * This ) {
   UTIL_LOG_HERE();
   memset( This, 0, sizeof( SC_banque ));
   SC_repository * repository = (SC_repository *)malloc( sizeof( SC_repository ));
   SC_repository_init( repository );
   This->user_context = repository;
   UTIL_LOG_DONE();
   return UTIL_NO_ERROR;
}

util_error SC_banque_informations( SC_banque * This, const char * carte_id, DBT_information * informations ) {
   UTIL_LOG_HERE();
   SC_repository * repository = (SC_repository *)This->user_context;
   DBT_carte * carte = NULL;
   UTIL_ERROR_CHECK( SC_repository_get_carte ( repository, carte_id, &carte  ));
   informations->carte = *carte;
   DBT_compte * compte = NULL;
   UTIL_ERROR_CHECK( SC_repository_get_compte( repository, carte_id, &compte ));
   informations->compte = *compte;
   os_sleep( 3000 );
   return UTIL_NO_ERROR;
}

util_error SC_banque_incr_nb_essais( SC_banque * This, const char * carte_id ) {
   UTIL_LOG_ARGS( "carte_id = %s", carte_id );
   SC_repository * repository = (SC_repository *)This->user_context;
   DBT_carte * carte = NULL;
   UTIL_ERROR_CHECK( SC_repository_get_carte ( repository, carte_id, &carte ));
   if( carte ) {
      ++(carte->nb_essais);
   }
   return UTIL_NO_ERROR;
}

util_error SC_banque_retrait( SC_banque * This, const char * carte_id, double montant ) {
   UTIL_LOG_ARGS( "carte_id = %s, montant = %7.2f", carte_id, montant );
   SC_repository * repository = (SC_repository *)This->user_context;
   DBT_compte * compte = NULL;
   UTIL_ERROR_CHECK( SC_repository_get_compte( repository, carte_id, &compte ));
   if( compte ) {
      compte->solde -= montant;
   }
   return UTIL_NO_ERROR;
}

util_error SC_banque_arret( SC_banque * This ) {
   UTIL_LOG_HERE();
   free( This->user_context );
   This->user_context = NULL;
   UTIL_ERROR_CHECK( SC_banque_dispatcher_terminate( This->dispatcher ));
   UTIL_LOG_DONE();
   return UTIL_NO_ERROR;
}

util_error SC_banque_before_dispatch( SC_banque * This ) {
   return UTIL_NO_ERROR;
   (void)This;
}

util_error SC_banque_after_dispatch( SC_banque * This, bool hasDispatched ) {
   SC_repository * repository = (SC_repository *)This->user_context;
   repository->has_changed = hasDispatched;
   return UTIL_NO_ERROR;
}
