#pragma once

#include <DAB/distributeur.h>

typedef struct DAB_business_logic_data_s {
   bool         refresh;
   double       montant;
   bool         shutdown;
} DAB_business_logic_data;

util_error DAB_distributeur_create_ui( DAB_distributeur * This );
