package util;

public class Log {

   public static void printf() {
      final StackTraceElement stackTraceElt = Thread.currentThread().getStackTrace()[2];
      System.err.printf( "%d:%s.%s\n",
         System.currentTimeMillis(), stackTraceElt.getClassName(), stackTraceElt.getMethodName());
   }

   public static void printf( String format, Object ... args ) {
      final StackTraceElement stackTraceElt = Thread.currentThread().getStackTrace()[2];
      final String            msg           = String.format( format, args );
      System.err.printf( "%d:%s.%s|%s\n",
         System.currentTimeMillis(), stackTraceElt.getClassName(), stackTraceElt.getMethodName(), msg );
   }
}
