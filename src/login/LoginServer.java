package login;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import login.service.LoginService;




public class LoginServer {
	
    private static final Logger logger = LogManager.getLogger(LoginServer.class);
    
    public static void main(String[] args) throws Exception {
    	int loginServerPort = 3132;
    	int gameServerConnectionPort = 3190;
		int databaseServicePort = 6060;
		String databaseServiceIP="127.0.0.1";
		
		//Rpc
    	 TTransport transport = new TSocket(databaseServiceIP, databaseServicePort);
		 transport.open();
		 TProtocol protocol = new TBinaryProtocol(transport);
		 LoginService.Client clientService = new LoginService.Client(protocol);
		 
		//Netty
		 EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
	     EventLoopGroup workerGroup = new NioEventLoopGroup();

	        
	     	try {
	     		//Client
	            ServerBootstrap l = new ServerBootstrap() // (2)
	            .group(bossGroup, workerGroup)
	            .handler(new LoggingHandler(LogLevel.INFO))
	            .channel(NioServerSocketChannel.class) // (3)
	            .childHandler(new LoginServerInitializer(clientService));
	            
	            //Game Server
	            ServerBootstrap g = new ServerBootstrap() // (2)
	    	            .group(bossGroup, workerGroup)
	    	            .handler(new LoggingHandler(LogLevel.INFO))
	    	            .channel(NioServerSocketChannel.class) // (3)
	    	            .childHandler(new GameServerInitializer());

	            // Bind and start to accept incoming connections.
	            
	            ChannelFuture lf = l.bind(loginServerPort).sync(); // (7)
	            ChannelFuture gf = g.bind(gameServerConnectionPort).sync(); // (7)

	            // Wait until the server socket is closed.
	            // In this example, this does not happen, but you can do that to gracefully
	            // shut down your server.
	            lf.channel().closeFuture().sync();
	            gf.channel().closeFuture().sync();
	        } finally {
	            workerGroup.shutdownGracefully();
	            bossGroup.shutdownGracefully();
	        }
	    }
    }