package loginserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;

import login.service.LoginService;
import loginserver.pocket.Init;
import loginserver.pocket.LoginFail;
import loginserver.pocket.LoginOk;
import loginserver.pocket.PlayOk;
import loginserver.pocket.PocketId;
import loginserver.pocket.RequestAuthLogin;
import loginserver.pocket.RequestServerList;
import loginserver.pocket.RequestServerLogin;
import loginserver.pocket.ServerList;

public class Client{
	private LoginService.Client clientService;
	private Socket connectionClient;
	private InputStream inputStream;
	private OutputStream outputStream;
	private Connection loginServerDB;
	private Statement loginServerStmt;
	private ResultSet loginServerRS;
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
	public Client(Socket connectionClient, Connection loginServerDB, LoginService.Client clientService) throws IOException, SQLException{
		
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
		this.loginServerDB=loginServerDB;
		this.clientService=clientService;
		
		inputStream = connectionClient.getInputStream();
		outputStream = connectionClient.getOutputStream();
		
		serializer = new TSerializer(new TBinaryProtocol.Factory());
		deserializer = new TDeserializer(new TBinaryProtocol.Factory());

		loginServerStmt = loginServerDB.createStatement();
		
		new ReadThread().start();
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
	
	
	/*public void authorizationClient(){
		try {
			
			loginServerRS = loginServerStmt.executeQuery("SELECT * FROM accounts WHERE login = '"+requestAuthLogin.login+"'AND password = '"+requestAuthLogin.password+"'");
			
			
			
			if(loginServerRS.next()){
				
				if(loginServerRS.getInt("accessLevel")!=-1){
				loginOk.id = 3;
				
				logger.info("LoginOk Ok");
				sendToClient(serializer.serialize(loginOk));
				}else{
					loginFail.id = 2;
					loginFail.error = -1;
					
					logger.info("LoginFail Ok ban");
					sendToClient(serializer.serialize(loginFail));
				}
			}else{
				loginFail.id = 2;
				loginFail.error = 1;
				
				logger.info("LoginFail Ok log. pas.");
				sendToClient(serializer.serialize(loginFail));
			}
			}catch (Exception e) {
				logger.catching(e);
		}
	}*/
	
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void formationServerList(){
		try {
			loginServerRS = loginServerStmt.executeQuery("SELECT * FROM gameservers");
			
			while(loginServerRS.next()){
				serverList.id = 8;
				serverList.serverIp = loginServerRS.getString("host");
				serverList.serverPort = loginServerRS.getShort("port");
				serverList.serverAgeLimit = loginServerRS.getShort("age_limit");
				serverList.serverType = loginServerRS.getShort("type");
				serverList.serverOnlineLimit = loginServerRS.getShort("online_limit");
				
				logger.info("ServerList Ok");
				sendToClient(serializer.serialize(serverList));
			}
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
	
	
	private class ReadThread extends Thread{
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


