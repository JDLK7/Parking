

import java.lang.Exception;
import java.net.Socket;
import java.util.Scanner;
import java.io.*;

public class HiloServidor extends Thread {

	private Socket skNavegador;
	private Socket skController;
	private String host;
	private int portController;
	private int port;
	
	public HiloServidor(Socket p_cliente, String host, int portController, int port)
	{
		this.skNavegador = p_cliente;
		this.skController = null;
		this.host = host;
		this.portController = portController;
		this.port = port;
	}
	
	/*
	* Lee datos del socket. Supone que se le pasa un buffer con hueco 
	*	suficiente para los datos. Devuelve el numero de bytes leidos o
	* 0 si se cierra fichero o -1 si hay error.
	*/
	public void leeSocketNavegador ()
	{
		try
		{
			InputStreamReader isr = new InputStreamReader(skNavegador.getInputStream());
			Scanner sc = new Scanner(isr);
			
			//Si es una peticion GET se acepta o no según el recurso que se esté pidiendo.
			if(sc.next().equals("GET")) {
				String s = "";
				
				if(sc.hasNext()) {
					s = sc.next();
				}
				
				if(s.equals("/index.html") || s.equals("/")) {
					escribeSocketNavegador(skNavegador, 200, "");
				}
				
				//Si la pagina se le pide al controlador.
				else if(s.startsWith("/controladorSD")) {
					//Si se pide el indice de la pagina de los sensores.
					if(s.equals("/controladorSD/index.html") || s.equals("/controladorSD")) {
						skController = new Socket(host, portController);
						escribirSocketController("/index.html");
						
						String html = leerSocketController();
						escribeSocketNavegador(skNavegador, 200, html);
					}
					
					//Si se pide otra cosa se le pasa al controlador.
					else {
						skController = new Socket(host, portController);
						escribirSocketController(s);
						
						String html = leerSocketController();
						escribeSocketNavegador(skNavegador, 200, html);
					}
				}
				
				//En otro caso la respuesta será error 404.
				else {
					escribeSocketNavegador(skNavegador,404,"");
				}
			}
			
			//Como otro método diferente no está permitido, se envía un código de error
			else {
				escribeSocketNavegador(skNavegador, 405, "");
			}
		}
		catch (Exception e)
		{
			System.out.println("Error: " + e.toString());
		}
	}
	
	public String leerSocketController() {
		InputStream is;
		try {
			is = skController.getInputStream();
			DataInputStream dis = new DataInputStream(is);
			
			return dis.readUTF();
		} catch (IOException e) {
			System.out.println("Error al leer el socket del controlador.");
			e.printStackTrace();
		}
		
		return null;
	}
	
	//Escribe en el socket los datos HTML de la página de respuesta a la petición.
	public void escribeSocketNavegador (Socket skNavegador, int codigoRespuesta, String html)
	{
		try
		{
			OutputStream aux = skNavegador.getOutputStream();
			DataOutputStream flujo= new DataOutputStream( aux );
			
			//String datos = "HTTP/1.1 200 OK\n";
			String datos = "HTTP/1.1 ";
			
			if(codigoRespuesta == 200) {
				datos += 200;
			}
			
			else {
				String pagHtml = "";
				html = "";
				
				switch(codigoRespuesta) {
					case 200: datos += 200;
						pagHtml = "index.html";
						break;
					case 404: datos += 404;
						pagHtml = "error404.html";
						break;
					case 405: datos += 405;
						pagHtml = "error405.html";
						break;
					case 409: datos += 409;
						pagHtml = "error409.html";
						break;
				}
				
				File f = new File(pagHtml);
				Scanner sc = new Scanner(f);
				
				while(sc.hasNextLine()) {
					html += sc.nextLine();
				}
			}
			
			datos += "Connection: close\n";
			datos += "Content-Length: " + html.length() +"\n";
			datos += "Content-Type: text/html\n";
			datos += "Server: Servidor HTTP\n\n";
			datos += html;
			
			flujo.writeUTF(datos);      
		}
		catch (Exception e)
		{
			System.out.println("Error: " + e.toString());
		}
		return;
	}
	
	public void escribirSocketController(String url) {
		try {
			OutputStream aux = skController.getOutputStream();
			DataOutputStream flujo= new DataOutputStream(aux);
			
			flujo.writeUTF(url.split("/")[1]);
			
			System.out.println("Conectando con el controlador.");
			
			//SE CIERRA EL SOCKET DEL CONTROLLER CUANDO ESTE RESPONDA
		}
		catch(IOException ex) {
			ex.printStackTrace();
		}
		
	}
	
    public void run() {		
        try {
			leeSocketNavegador();
			
			skNavegador.close();
        }
        catch (Exception e) {
          System.out.println("Error: " + e.toString());
        }
    }
}
