

import java.io.IOException;
import java.net.*;

public class MyHttpServer {
	
	@SuppressWarnings("resource")
	public static void main(String[] args) {
		
		if(args.length < 3) {
			System.out.println("Se tiene que introducir el host, el puerto y el número de conexiones simultáneas.");
			System.exit(1);
		}
		
		String host = args[0];
		int port = Integer.parseInt(args[1]);
		int portController = Integer.parseInt(args[2]);
		int hilos = Integer.parseInt(args[3]);

		try
		{
			ServerSocket skServidor = new ServerSocket(port);
		    System.out.println("Servidor en escucha " + port);
	
			for(;;)
			{				
				Socket skCliente = skServidor.accept();

		        System.out.println("Sirviendo cliente...");
		        
	        	Thread t = new HiloServidor(skCliente, host, portController, port);
	        	
	        	if(Thread.activeCount() < hilos) {
	        		t.start();
	        	}
			}
		}
		catch(Exception e)
		{
			System.out.println("Error: " + e.toString());
			
		}
	}
}

/*
	public static void main(String[] args) throws Exception {
		//int port = Integer.parseInt(args[0]);
		//int threads = Integer.parseInt(args[1]);
		
		HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/test", new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {

        	String response = "This is the response";
            
        	t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
*/