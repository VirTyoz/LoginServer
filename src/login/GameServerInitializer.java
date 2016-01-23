package login;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

public class GameServerInitializer extends ChannelInitializer<SocketChannel>{

	@Override
	protected void initChannel(SocketChannel arg0) throws Exception {

		ChannelPipeline pipeline = arg0.pipeline();
		
		pipeline.addLast(new GameConnectionHandler());
		pipeline.addLast(new DataDecoder());
		
	}

}
