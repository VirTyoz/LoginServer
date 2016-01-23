package login;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import login.pocket.PocketId;
import login.service.LoginService;

public class ClientHandler extends SimpleChannelInboundHandler<byte[]> {
	private LoginService.Client clientService;
	private TDeserializer deserializer;
	public volatile PocketId pocketId;
	public volatile Client client;
	
	private static final Logger logger = LogManager.getLogger(ClientHandler.class);
	
	public ClientHandler(LoginService.Client clientService){
		pocketId = new PocketId();
		
		this.clientService=clientService;
		
		client = new Client(clientService);
		
		deserializer = new TDeserializer(new TBinaryProtocol.Factory());
	}
	
	public void channelActive(ChannelHandlerContext arg0) throws TException{
    	System.out.println(arg0.channel().remoteAddress()+"connacted");
    	//arg0.writeAndFlush(Unpooled.wrappedBuffer(client.sendConnectNewClient()));
    	arg0.writeAndFlush(client.sendConnectNewClient());
    	}
	
	

	@Override
	protected void channelRead0(ChannelHandlerContext arg0, byte[] arg1) throws Exception {
		deserializer.deserialize(pocketId, arg1);
		logger.info("PocketId Ok"+pocketId+arg1);
		
		switch(pocketId.id){
        case 5:
        	logger.info("RequestAuthLogin Ok");
        	arg0.writeAndFlush(client.authorizationClient(arg1));
        	break;
        case 6:
        	logger.info("RequestServerList Ok");
        	arg0.writeAndFlush(client.formationServerList(arg1));
        	break;
        case 7:
        	logger.info("RequestServerLogin Ok");
        	arg0.writeAndFlush(client.switchingGameServer(arg1));
        	break;
        default:
        	break;
        }
		
	  }
}
