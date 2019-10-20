#pragma once

#ifdef __cplusplus
extern "C" {
#endif

void console_init( void );
int  console_kbhit( void );
int  console_getch( void );

#define IO_CSI     "\033["
#define IO_ED      IO_CSI"2J"
#define IO_BOLD    IO_CSI"1m"
#define IO_SGR_OFF IO_CSI"0m"
#define IO_HOME    IO_CSI"1;1H"

#ifdef __cplusplus
}
#endif
