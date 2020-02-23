import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

public class Server {
		
	    //private ArrayList<ConnectionToClient> clientList;
        //Maintaining list of multiple client connection objects
		private List<ConnectionToClient> clientList;
	    private ServerSocket serverSocket;

	    public Server(int port) {
	        //clientList = new ArrayList<ConnectionToClient>();
            //For handling concurrent operations from multiple clients
	    	clientList = new CopyOnWriteArrayList<ConnectionToClient>();

	        try {
				serverSocket = new ServerSocket(port);
			} catch (IOException e1) {
				e1.printStackTrace();
			}

	        Thread accept = new Thread() {
	            public void run(){
	                while(true){
	                    try{
	                        Socket s = serverSocket.accept();
	                        clientList.add(new ConnectionToClient(s));//ArrayList of connections-----------
	                        InetAddress clientAddress = s.getInetAddress();
	                        
	                        System.out.println("Added to Incoming! list: " + clientAddress.getHostName() + "[" + clientAddress.getHostAddress() + "]");
	                        System.out.println("In List:");
	                        int i=0;
	                        for(ConnectionToClient client : clientList) {
	                        	System.out.println(i+" IP: "+client.socket.getInetAddress().getHostAddress());
	                        }
	                    }
	                    catch(IOException e){ e.printStackTrace(); }
	                }
	            }
	        };
	        accept.start();
	    }    
	    
	    private class ConnectionToClient {
	        Socket socket;
	        PrintWriter out;

	        ConnectionToClient(Socket socket) throws IOException {
	            this.socket = socket;
	        }

	        public void write(String msg) {
	                //Send Message to clients---------------------
	        	    try {
						out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()),true);
						out.write(msg);
						out.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	 
	        }
	    }
	    
	    //Send to all clients except the one which made last change
	    public void sendToAll(String message,String ip){
	    	int i=0;
	    	try {
	    		for(ConnectionToClient client : clientList) {
	    			System.out.println(i+" IP: "+client.socket.getInetAddress().getHostAddress());
	    			if(!(client.socket.getInetAddress().getHostAddress().toString().equals(ip))) {
	    				client.write(message);//Send Message function call---------------------
			            System.out.println(i+" Broadcasted! to: "+client.socket.getInetAddress().getHostAddress());
			            clientList.remove(client);	
	    			}
	    			i++;
		        }
	    	}
	    	catch(Exception e) {System.out.println("In sendAll.\n"+e);}
	    	System.out.println("\n\n");
	    }

	}
