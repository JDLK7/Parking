import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.*;

public class Controller {
	public static void main(String[] args) {
		if(args.length < 1) {
			System.out.println("Se tiene que introducir el puerto de escucha.");
			System.exit(1);
		}
		
		int puerto = Integer.parseInt(args[0]);
		int puertoRMI = Integer.parseInt(args[1]);
		
		try {
			ServerSocket skServidor = new ServerSocket(puerto);
			System.out.println("Controlador en escucha " + puerto);
			
			for(;;) {
				Socket skCliente = skServidor.accept();
				
				System.out.println("Conectado con el servidor...");
				
				Thread t = new HiloController(skCliente, puertoRMI);
				t.start();
			}
		}
		catch(Exception ex) {
			System.out.println("Error: " + ex.toString());
		}
	}
}
