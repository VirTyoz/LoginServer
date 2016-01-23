package login;

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
import login.pocket.RequestAuthLogin;
import login.pocket.RequestServerList;
import login.pocket.RequestServerLogin;
import login.pocket.ServerList;
import login.service.LoginService;

public class Client{
	private LoginService.Client clientService;
	private TSerializer serializer;
	private TDeserializer deserializer;
	public volatile Init init;
	public volatile RequestAuthLogin requestAuthLogin;
	public volatile PlayOk playOk;
	public volatile LoginOk loginOk;
	public volatile LoginFail loginFail;
	public volatile RequestServerList requestServerList;
	public volatile ServerList serverList;
	public volatile RequestServerLogin requestServerLogin;
	
	private static final Logger logger = LogManager.getLogger(Client.class);
	public Client(LoginService.Client clientService){
		
		init = new Init();
		requestAuthLogin = new RequestAuthLogin();
		playOk = new PlayOk();
		requestServerList = new RequestServerList();
		serverList =  new ServerList();
		loginOk = new LoginOk();
		loginFail = new LoginFail();
		requestServerLogin = new RequestServerLogin();
		
		this.clientService=clientService;
		
		serializer = new TSerializer(new TBinaryProtocol.Factory());
		deserializer = new TDeserializer(new TBinaryProtocol.Factory());
		
	}
	
	
	
	public byte[] sendConnectNewClient(){
		try{
			init.id = 1;
			init.connectionId=5;
			init.sessionId=3;
			
			logger.info("Init Ok");
			return serializer.serialize(init);
			
		}catch(Exception e){
			logger.catching(e);
		}
		return null;
	}
	
	
	public byte[] authorizationClient(byte[] bytes){
		try {
			deserializer.deserialize(requestAuthLogin, bytes);
			
			int status = clientService.authorization(requestAuthLogin.login,requestAuthLogin.password);
			
			switch(status){
			case -1:
				loginFail.id = 2;
				loginFail.error = -1;
				
				logger.info("LoginFail Ok ban"+loginFail.error);
				return serializer.serialize(loginFail);
				
			case 1:
				loginFail.id = 2;
				loginFail.error = 1;
				
				logger.info("LoginFail Ok log. pas.");
				return serializer.serialize(loginFail);
			
			case 2:
				loginOk.id = 3;
				
				logger.info("LoginOk Ok");
				return serializer.serialize(loginOk);
				
			default:
				break;
			}
		} catch (TException e) {

			logger.info("AuthorizationClient error"+e);
		}
		return null;
	}
	
	
	public byte[] formationServerList(byte[] bytes){
		try {
			deserializer.deserialize(requestServerList, bytes);
			serverList = clientService.serverList();
				serverList.id = 8;
				logger.info("ServerList Ok"+serverList.serverPort);
				return serializer.serialize(serverList);
			
		} catch (Exception e) {
			logger.catching(e);
		}
		return null;
	}
	
	
	public byte[] switchingGameServer(byte[] bytes){
		
		try {
			deserializer.deserialize(requestServerLogin, bytes);
			playOk.id = 4;
			
			logger.info("PlayOk Ok");
			return serializer.serialize(playOk);
		} catch (Exception e) {
			logger.catching(e);
		}
		return null;
		
	}
}


