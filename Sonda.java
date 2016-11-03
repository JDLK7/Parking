import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Sonda extends UnicastRemoteObject implements InterfazRemoto, Serializable {
	private int numSensor;
	private int volumen;
	private String fecha;
	private String ultimaFecha;
	private int led;
	
	public Sonda() throws RemoteException {
		super();
		
		numSensor = -1;
		volumen = 0;
		fecha = null;
		led = 0;
	}
	
	public Sonda(int volumen, String fecha, String ultimaFecha, int led) throws RemoteException {
		super();
		
		numSensor = -1;
		this.volumen = volumen;
		this.fecha = fecha;
		this.ultimaFecha = ultimaFecha;
		this.led = led;
	}
	
	public int getNumSensor() {
		return numSensor;
	}

	public void setNumSensor(int numSensor) {
		this.numSensor = numSensor;
	}

	public int getVolumen() {
		return volumen;
	}

	public void setVolumen(int volumen) {
		this.volumen = volumen;
	}

	public String getFecha() {
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date f = new Date();
		
		return dateFormat.format(f);
	}
	
	public String getUltimaFecha() {
		return ultimaFecha;
	}

	public void setUltimaFecha(String ultimaFecha) {
		this.ultimaFecha = ultimaFecha;
	}

	public int getLed() {
		return led;
	}

	public void setLed(int led) {
		this.led = led;
	}
	
	//La excepcion se captura pero casi mejor si se lanza.
	public void comprobarSensor() {
		Scanner sc = null;
		try {
			sc = new Scanner(new File("Sensor" + numSensor));
		} catch (FileNotFoundException e) {
			//Si hay un problema con el fichero el sensor muestra un mensaje y se apaga.
			System.out.println("El nombre del fichero no es válido: " + numSensor);
			e.printStackTrace();
			System.exit(1);
		}
		
		String line = sc.next();
		line = line.substring(line.indexOf('=')+1);
		volumen = Integer.parseInt(line);
		
		ultimaFecha = sc.next();
		ultimaFecha = ultimaFecha.substring(ultimaFecha.indexOf('=')+1);
		
		ultimaFecha += " " + sc.next();
		
		line = sc.next();
		line = line.substring(line.indexOf('=')+1);
		led = Integer.parseInt(line);
	}
	
	public Sonda getSensor() {
		return this;
	}
	
	public String estadoSensor() {
		return "Volumen=" + getVolumen() + "\nFecha="+ getFecha().toString() + "\nUltimaFecha=" + getUltimaFecha() + "\nLed=" + getLed() + "\n";
	}
	
	public static void main(String[] args) throws RemoteException {
		Sonda sonda = new Sonda();
		
		String host;
		int puerto;
		
		if(args.length < 3) {
			System.out.println("Tiene que introducir el número del sensor, la ip del registro remoto y su puerto.");
			System.exit(1);
		}
		
		sonda.setNumSensor(Integer.parseInt(args[0]));
		host = args[1];
		puerto = Integer.parseInt(args[2]);
		
		sonda.comprobarSensor();
		
		System.out.println(sonda.estadoSensor());
		
		System.setSecurityManager(new RMISecurityManager());
		
		Registry reg = LocateRegistry.getRegistry(host,puerto);
		try {
			InterfazRegistro ir = (InterfazRegistro) reg.lookup("/registrador");
			ir.registrarSonda(sonda);
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
	}
}
