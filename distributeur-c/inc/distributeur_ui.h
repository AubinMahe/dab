#pragma once

#include <dab/distributeur.h>

typedef struct business_logic_data_s {
   bool         etat_du_dab_published;
   const char * action;
   double       montant;
   bool         shutdown;
} business_logic_data;

util_error dab_distributeur_create_ui( dab_distributeur * This );
