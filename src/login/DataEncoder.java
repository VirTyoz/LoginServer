package login;

import java.util.List;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

public class DataEncoder extends MessageToMessageEncoder<byte[]> {

	@Override
	protected void encode(ChannelHandlerContext arg0, byte[] arg1, List<Object> arg2){
		System.out.println("Encoder");
		arg2.add(Unpooled.wrappedBuffer(arg1));
	}
}
