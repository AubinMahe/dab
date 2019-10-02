package util;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Timeout {

   public static final int TIMEOUT_MAX_COUNT = 20;

   private static final ScheduledExecutorService _Executor = Executors.newScheduledThreadPool( TIMEOUT_MAX_COUNT );

   public static ScheduledFuture<?> start( Duration duration, Runnable actionWhenElapsed ) {
      return _Executor.schedule( actionWhenElapsed, duration.toNanos(), TimeUnit.NANOSECONDS );
   }
}
