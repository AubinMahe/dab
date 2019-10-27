#pragma once

namespace io {

   class Console {
   public:

      static const Console & getConsole();

   public:

      bool kbhit( void ) const;
      int  getch( void ) const;

   private:

      Console( void );

   private:

      Console( const Console & ) = delete;
      Console & operator = ( const Console & ) = delete;
   };
}

#define IO_CSI     "\033["
#define IO_ED      IO_CSI"2J"
#define IO_BOLD    IO_CSI"1m"
#define IO_SGR_OFF IO_CSI"0m"
#define IO_HOME    IO_CSI"1;1H"
