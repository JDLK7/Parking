import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class HiloController extends Thread {
	
	private Socket skHttpServer;
	private int portRMI;
	
	public HiloController(Socket p_cliente, int portRMI)
	{
		this.skHttpServer = p_cliente;
		this.portRMI = portRMI;
	}
	
	public String leerSocket() {
		try {
			InputStream isr;
			isr = skHttpServer.getInputStream();
			DataInputStream br = new DataInputStream(isr);
			
			return br.readUTF();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return "";
	}
	
	public String procesarPeticion(String peticion) throws RemoteException, NotBoundException {
		String html = "";
		
		html += "<html>\n";
		html += "<head>\n";
		html += "<title>Parking ultragenerasión</title>\n";
		html += "</head>\n";
		
		html += "<body>\n";
		
		if(peticion.contains("?sonda=")) {
			int numSonda = Integer.parseInt(peticion.substring(peticion.indexOf("=")+1));
			
			Registry registroRemoto = obtenerRegistroRemoto();
			InterfazRemoto sonda = (InterfazRemoto) registroRemoto.lookup("/sonda" + numSonda);
			
			if(peticion.startsWith("volumen")) {
				html += "<h1>Volumen: " + sonda.getVolumen() + "</h1>";
			}
			else if(peticion.startsWith("fecha")) {
				html += "<h1>Fecha: " + sonda.getFecha() + " " + sonda.getHora() + "</h1>";
			}
			else if(peticion.startsWith("ultimafecha")) {
				//html += "<h1>Última fecha: " + sonda.getFecha() + "</h1>";
			}
			else if(peticion.startsWith("led")) {
				html += "<h1>Led: " + sonda.getLed() + "</h1>";
			}
			else {
				html += "<h1>Error: propiedad no encontrada</h1>";
			}
		}
		else {
			html += "<h1>Error: recurso no encontrado</h1>";
		}
		
		html += "</body>\n";
		html += "</html>\n";
		
		return html;	
	}
	
	public Registry obtenerRegistroRemoto() {
		try{
			System.setSecurityManager(new RMISecurityManager());
			return LocateRegistry.getRegistry(portRMI);
		}
		catch(RemoteException ex) {
			System.out.println("Error al instanciar el objeto remoto " + ex);
            System.exit(0);
		}
		
		return null;
	}
	
	public String generarIndex() throws FileNotFoundException {
		String html = "";
		
		html += "<html>\n";
		html += "<head>\n";
		html += "<title>Parking ultragenerasión</title>\n";
		
		Scanner sc = new Scanner(new File("script"));
		while(sc.hasNextLine()) {
			html += sc.nextLine() + "\n";
		}
		
		html += "</head>\n";
		
		html += "<body>\n";
		
		html += "<input type=\"radio\" name=\"propiedad\" value=\"volumen\"/>Volumen</br>\n";
		html += "<input type=\"radio\" name=\"propiedad\" value=\"fecha\"/>Fecha</br>\n";
		html += "<input type=\"radio\" name=\"propiedad\" value=\"ultimadecha\"/>Última fecha</br>\n";
		html += "<input type=\"radio\" name=\"propiedad\" value=\"led\"/>Led</input>\n";
		html += "<input type=\"text\" id=\"valorLed\"/></br></br>\n";
		
		try{
			String[] objetosRemotos = obtenerRegistroRemoto().list();
			
			for(int i=0; i<objetosRemotos.length; i++) {
				if(objetosRemotos[i].contains("/sonda")) {
					html += "<input type=\"button\" id=\"sonda=" + objetosRemotos[i].substring(6) + "\" value=\"Sonda " + objetosRemotos[i].substring(6) + "\" onclick=\"generarUrl(this.id);\"></br>\n";
				}
			}
		}
		catch(Exception ex) {
			System.out.println("Error al listar sondas");
			ex.printStackTrace();
		}
				
		html += "</body>\n";
		html += "</html>\n";
		
		return html;
	}
	
	public void escribeSocket(String html) throws IOException {
		OutputStream aux = skHttpServer.getOutputStream();
		DataOutputStream flujo= new DataOutputStream( aux );
		
		flujo.writeUTF(html);
	}
	
	public void run() {        
        try {
        	String[] peticion = leerSocket().split("/");
        	
        	if(peticion[1].equals("") || peticion[1].equals("/index.html")) {
        		escribeSocket(generarIndex());
        	}
        	else {
        		escribeSocket(procesarPeticion(peticion[1]));
        	}
			
			skHttpServer.close();
        }
        catch (Exception e) {
          System.out.println("Error: " + e.toString());
        }
	}
}
