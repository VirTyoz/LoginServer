package login;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import login.service.LoginService;

public class LoginServerInitializer extends ChannelInitializer<SocketChannel> {
	private LoginService.Client clientService;
	
	public LoginServerInitializer(LoginService.Client clientService){
		
		this.clientService=clientService;
	}

	@Override
	protected void initChannel(SocketChannel arg0) throws Exception {
		
		ChannelPipeline pipeline = arg0.pipeline();
		
		pipeline.addLast(new LoginConnectionHandler());
		pipeline.addLast(new DataDecoder());
		pipeline.addLast(new DataEncoder());
		
		pipeline.addLast(new ClientHandler(clientService));
		
	}

}
