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
	private String hostRMI;
	private int portRMI;
	
	public HiloController(Socket p_cliente, String hostRMI, int portRMI)
	{
		this.skHttpServer = p_cliente;
		this.hostRMI = hostRMI;
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
	
	public String procesarPeticion(String peticion) throws RemoteException {
		String html = "";
		
		html += "<html>\n";
		html += "<head>\n";
		html += "<title>Parking ultragenerasión</title>\n";
		html += "</head>\n";
		
		html += "<body style=\"background-color: #ECF0F1;\">\n";

		String style = " style=\"color: #323232; text-align: center;\"";
		
		if(peticion.contains("?sonda=")) {
			int numSonda;
			
			if(peticion.startsWith("set")) {
				numSonda = Integer.parseInt(peticion.substring(peticion.indexOf("=")+1, peticion.indexOf("%")));
			}
			else {
				numSonda = Integer.parseInt(peticion.substring(peticion.indexOf("=")+1));
			}
			
			Registry registroRemoto = obtenerRegistroRemoto();
			InterfazRemoto sonda;
			
			try {
				sonda = (InterfazRemoto) registroRemoto.lookup("/sonda" + numSonda);
			
				if(peticion.startsWith("volumen")) {
					int volumen = sonda.getVolumen();
					String color = ""; 

					html += "<h1" + style + ">Volumen: " + volumen + "</h1>";
					
					if(volumen == 0) {
						color = "green";
					}
					else if(volumen > 0 && volumen < 30) {
						color = "#FFBF00";
					}
					else {
						color = "red";
					}
					
					html += "<div style=\"width: 50px; height:50px; -webkit-border-radius: 25px; -moz-border-radius: 25px; border-radius: 25px; background: " + color + "; margin-left: 50%;\"></div>";
				}
				else if(peticion.startsWith("fecha")) {
					html += "<h1" + style + ">Fecha: " + sonda.getFecha() + "</h1>";
				}
				else if(peticion.startsWith("ultimafecha")) {
					html += "<h1" + style + ">Última fecha: " + sonda.getUltimaFecha() + "</h1>";
				}
				else if(peticion.startsWith("led")) {
					html += "<h1" + style + ">Led: " + sonda.getLed() + "</h1>";
				}
				else if(peticion.startsWith("set")) {
					sonda.setLed(Integer.parseInt(peticion.substring(peticion.indexOf("%")+1)));
					sonda.setUltimaFecha(sonda.getFecha());
					
					html += "<h1" + style +">Led modificado correctamente</h1>";
				}
				else {
					html += "<h1" + style + ">Error: variable no válida</h1>";
				}
			}
			catch (NotBoundException e) {
				html += "<h1" + style + ">Error: el sensor no existe</h1>";
			}
		}
		else {
			html += "<h1" + style + ">Error: recurso no encontrado</h1>";
		}
		
		html += "</body>\n";
		html += "</html>\n";
		
		return html;	
	}
	
	public Registry obtenerRegistroRemoto() {
		try{
			System.setSecurityManager(new RMISecurityManager());
			return LocateRegistry.getRegistry(hostRMI, portRMI);
		}
		catch(RemoteException ex) {
			System.out.println("Error al instanciar el objeto remoto " + ex);
            System.exit(0);
		}
		
		return null;
	}
	
	public String generarIndex() throws FileNotFoundException {
		String html = "";

		Scanner sc = new Scanner(new File("controllerIndex.html"));

		while(sc.hasNextLine()) {
			String s = sc.nextLine() + "\n";

			if(s.contains("<!---->")) {
				break;
			}
			else {
				html += s;
			}
		}

		try{
			String[] objetosRemotos = obtenerRegistroRemoto().list();
			
			for(int i=0; i<objetosRemotos.length; i++) {
				if(objetosRemotos[i].contains("/sonda")) {
					html += "<input type=\"button\" id=\"sonda=" + objetosRemotos[i].substring(6) + "\" value=\"Sonda " + objetosRemotos[i].substring(6) + "\" onclick=\"generarUrl(this.id);\">\n";
				}
			}
		}
		catch(Exception ex) {
			System.out.println("Error al listar sondas");
			ex.printStackTrace();
		}

		html += "</div>\n";	
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
        	
        	if(peticion.length == 1 || peticion[1].equals("index.html")) {
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
