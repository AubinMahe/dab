#pragma once

#include <sc/repository.h>

typedef struct business_logic_data_s {
   sc_repository repository;
   bool          refresh_needed;
   bool          shutdown;
} business_logic_data;

util_error sc_banque_create_ui( sc_banque * This );
