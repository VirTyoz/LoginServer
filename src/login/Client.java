package login;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;

import login.pocket.Init;
import login.pocket.LoginFail;
import login.pocket.LoginOk;
import login.pocket.PlayOk;
import login.pocket.PocketId;
import login.pocket.RequestAuthLogin;
import login.pocket.RequestServerList;
import login.pocket.RequestServerLogin;
import login.pocket.ServerList;
import login.service.LoginService;

public class Client{
	private LoginService.Client clientService;
	private Socket connectionClient;
	private InputStream inputStream;
	private OutputStream outputStream;
	private TSerializer serializer;
	private TDeserializer deserializer;
	public volatile Init init;
	public volatile RequestAuthLogin requestAuthLogin;
	public volatile PlayOk playOk;
	public volatile LoginOk loginOk;
	public volatile LoginFail loginFail;
	public volatile PocketId pocketId;
	public volatile RequestServerList requestServerList;
	public volatile ServerList serverList;
	public volatile RequestServerLogin requestServerLogin;
	
	private static final Logger logger = LogManager.getLogger(Client.class);
	public Client(Socket connectionClient, LoginService.Client clientService){
		
		init = new Init();
		requestAuthLogin = new RequestAuthLogin();
		playOk = new PlayOk();
		requestServerList = new RequestServerList();
		serverList =  new ServerList();
		loginOk = new LoginOk();
		loginFail = new LoginFail();
		pocketId = new PocketId();
		requestServerLogin = new RequestServerLogin();
		
		this.connectionClient = connectionClient;
		this.clientService=clientService;
		
		try {
			inputStream = connectionClient.getInputStream();
			outputStream = connectionClient.getOutputStream();
		} catch (IOException e) {
			logger.info(e);;
		}
		
		serializer = new TSerializer(new TBinaryProtocol.Factory());
		deserializer = new TDeserializer(new TBinaryProtocol.Factory());
		
		new ReadThreadClient().start();
		sendConnectNewClient();
	}
	
	
	
	public void sendConnectNewClient(){
		try{
			init.id = 1;
			init.connectionId=5;
			init.sessionId=3;
			
			logger.info("Init Ok");
			sendToClient(serializer.serialize(init));
			
		}catch(Exception e){
			logger.catching(e);
		}
	}
	
	
	public void authorizationClient(){
		try {
			int status = clientService.authorization(requestAuthLogin.login,requestAuthLogin.password);
			switch(status){
			case -1:
				loginFail.id = 2;
				loginFail.error = -1;
				
				logger.info("LoginFail Ok ban");
				sendToClient(serializer.serialize(loginFail));
				break;
				
			case 1:
				loginFail.id = 2;
				loginFail.error = 1;
				
				logger.info("LoginFail Ok log. pas.");
				sendToClient(serializer.serialize(loginFail));
				break;
			
			case 2:
				loginOk.id = 3;
				
				logger.info("LoginOk Ok");
				sendToClient(serializer.serialize(loginOk));
				break;
				
			default:
				break;
			}
		} catch (TException e) {

			logger.info(e);
		}
	}
	
	
	public void formationServerList(){
		try {
			serverList = clientService.serverList();
				
				logger.info("ServerList Ok"+serverList.serverPort);
				sendToClient(serializer.serialize(serverList));
			
		} catch (Exception e) {
			logger.catching(e);
		}
	}
	
	
	public void switchingGameServer(){
		
		try {
			playOk.id = 4;
			
			logger.info("PlayOk Ok");
			sendToClient(serializer.serialize(playOk));
		} catch (Exception e) {
			logger.catching(e);
		}
		
	}
	
	
	
	public void sendToClient(byte[] _buffers){
		try{
			outputStream.write(_buffers);
			outputStream.flush();
			logger.info("Sent data to client Ok");
		}catch(Exception e){
			logger.catching(e);
		}
	}
	
	
	private class ReadThreadClient extends Thread{
		@Override
		public void run(){
			super.run();
			byte buffers[] = new byte[1024];
			
			while(connectionClient.isConnected()){
				try{
					int data = inputStream.read(buffers);
					
					if(data != -1){	
					deserializer.deserialize(pocketId, buffers);
					logger.info("PocketId Ok"+pocketId);
					
					switch(pocketId.id){
			        case 5:
			        	deserializer.deserialize(requestAuthLogin, buffers);
			        	//Arrays.fill( buffers, (byte)0 );
			        	logger.info("RequestAuthLogin Ok");
			        	authorizationClient();
			        	break;
			        case 6:
			        	deserializer.deserialize(requestServerList, buffers);
			        	//Arrays.fill( buffers, (byte)0 );
			        	logger.info("RequestServerList Ok");
			        	formationServerList();
			        	break;
			        case 7:
			        	deserializer.deserialize(requestServerLogin, buffers);
			        	//Arrays.fill( buffers, (byte)0 );
			        	logger.info("RequestServerLogin Ok");
			        	switchingGameServer();
			        	break;
			        default:
			        	break;
			        }
					
				  }
					
				}catch(Exception e){
					logger.info("ReadThread Error");
					logger.catching(e);
					return;
				}
			}
		}
	}
}


