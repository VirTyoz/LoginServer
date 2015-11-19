package login;

import java.net.ServerSocket;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import login.service.LoginService;




public class LoginServer {
	
    private static final Logger logger = LogManager.getLogger(LoginServer.class);
	public static void main(String[] args){
		int loginServerPort = 3132;
		int databasePort = 3306;
		int databaseServicePort = 6060;
		String databaseServoceIP="127.0.0.1";
		String databaseIP="127.0.0.1";
	 
		try{
			 ServerSocket loginServerSosket = new ServerSocket(loginServerPort);
			 
			 //rpc
			 TTransport transport = new TSocket(databaseServoceIP, databaseServicePort);
			 transport.open();
			 TProtocol protocol = new TBinaryProtocol(transport);
			 LoginService.Client clientService = new LoginService.Client(protocol);
			
			 logger.info("LoginServer is running "+loginServerSosket);
			 
			 // Game Server connect
			 new GameServerThread().start();
			 
			 for(;;){
			 Socket socketLogin = loginServerSosket.accept();
			 logger.info("Client has connect "+socketLogin);
			 new Client(socketLogin, clientService); //new client
			}
			 } catch(Exception e){
			 logger.catching(e);
			 }
	}
}