package sc.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.channels.DatagramChannel;

import sc.ICarte;
import sc.ICompte;
import sc.ILecteurDeCarte;
import sc.IRepository;
import sc.ISiteCentral;

public final class Server extends Thread implements ISiteCentral {

   private final DatagramChannel       _channel;
   private final IRepository           _repository;
   private final ILecteurDeCarte       _lecteurDeCarte;
   private final SiteCentralDispatcher _dispatcher;

   public Server( int port, IRepository repository ) throws IOException {
      _channel = DatagramChannel.open( StandardProtocolFamily.INET )
         .setOption( StandardSocketOptions.SO_REUSEADDR, true )
         .bind     ( new InetSocketAddress( port ));
      _repository     = repository;
      _lecteurDeCarte = new LecteurDeCarte( _channel );
      _dispatcher     = new SiteCentralDispatcher( _channel, this );
      setDaemon( true );
      start();
   }

   @Override
   public void getInformations( SocketAddress from, String carteID ) throws IOException {
      System.err.printf( getClass().getName() + ".getInformations|carteID = '%s'\n", carteID );
      final ICarte carte   = _repository.getCarte ( carteID );
      if( carte != null ) {
         try { Thread.sleep( 3000 ); } catch( final InterruptedException x ) {/**/}
         final ICompte compte = _repository.getCompte( carte.getId());
         if( compte != null ) {
            _lecteurDeCarte.carteLue( from,
               carte.getId(),
               carte.getCode(),
               carte.getExpirationMonth(),
               carte.getExpirationYear(),
               carte.getNbEssais(),
               compte.getId(),
               compte.getSolde(),
               compte.getAutorise());
         }
      }
   }

   @Override
   public void incrNbEssais( SocketAddress from, String carteID ) {
      System.err.printf( getClass().getName() + ".incrNbEssais( '%s' )\n", carteID );
      final ICarte carte = _repository.getCarte( carteID );
      if( carte != null ) {
         carte.incrNbEssais();
      }
   }

   @Override
   public void retrait( SocketAddress from, String carteID, double montant ) {
      System.err.printf( getClass().getName() + ".retrait\n" );
      final ICompte compte = _repository.getCompte( carteID );
      if( compte != null ) {
         compte.retrait( montant );
      }
   }

   @Override
   public void shutdown( SocketAddress from ) {
      _repository.close();
   }

   @Override
   public void run() {
      try {
         for(;;) {
            _dispatcher.hasDispatched();
         }
      }
      catch( final Throwable t ) {
         t.printStackTrace();
      }
   }
}
