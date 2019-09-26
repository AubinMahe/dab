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

   public Server( int port, IRepository repository, SocketAddress ... lecteurDeCarteOffers ) throws IOException {
      _channel = DatagramChannel.open( StandardProtocolFamily.INET )
         .setOption( StandardSocketOptions.SO_REUSEADDR, true )
         .bind     ( new InetSocketAddress( port ));
      _repository     = repository;
      _lecteurDeCarte = new LecteurDeCarte( _channel, lecteurDeCarteOffers );
      _dispatcher     = new SiteCentralDispatcher( _channel, this );
      setDaemon( true );
      start();
   }

   @Override
   public void getInformations( String carteID ) throws IOException {
      System.err.printf( getClass().getName() + ".getInformations|carteID = '%s'\n", carteID );
      final ICarte carte   = _repository.getCarte ( carteID );
      if( carte != null ) {
         try { Thread.sleep( 3000 ); } catch( final InterruptedException x ) {/**/}
         final ICompte compte = _repository.getCompte( carte.getId());
         if( compte != null ) {
            final sc.Carte  netCarte  = new sc.Carte();
            final sc.Compte netCompte = new sc.Compte();
            carte .copyTo( netCarte );
            compte.copyTo( netCompte );
            _lecteurDeCarte.carteLue( netCarte, netCompte );
         }
      }
   }

   @Override
   public void incrNbEssais( String carteID ) {
      System.err.printf( getClass().getName() + ".incrNbEssais( '%s' )\n", carteID );
      final ICarte carte = _repository.getCarte( carteID );
      if( carte != null ) {
         carte.incrNbEssais();
      }
   }

   @Override
   public void retrait( String carteID, double montant ) {
      System.err.printf( getClass().getName() + ".retrait\n" );
      final ICompte compte = _repository.getCompte( carteID );
      if( compte != null ) {
         compte.retrait( montant );
      }
   }

   @Override
   public void shutdown() {
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
