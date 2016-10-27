import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.net.*;
import java.io.*;

public class Registrador extends UnicastRemoteObject implements InterfazRegistro {  
	
	private int puerto; 
	
    protected Registrador(int puerto) throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
		this.puerto = puerto;
	}

	public static void main (String args[])     
    {            
        //Cada URLRegistro será un sensor.
		if(args.length < 1) {
			System.out.println("Tiene que indicar el puerto en que se ejecutará el Registro");
			System.exit(1);
		}
		
		int puerto = Integer.parseInt(args[0]);
		
    	String URLRegistro;
        try           
        {   
        	System.setSecurityManager(new RMISecurityManager());
        	
        	Registrador registrador = new Registrador(puerto);
        	
        	
    		Registry reg = LocateRegistry.getRegistry(puerto);
    		reg.rebind("/registrador", registrador);
            
            System.out.println("Servidor de objeto preparado.");
        }            
        catch (Exception ex)            
        {                  
            System.out.println(ex);            
        }     
    }
    
	public void registrarSonda(InterfazRemoto s) throws RemoteException{
		System.setSecurityManager(new RMISecurityManager());
		
		Registry reg = LocateRegistry.getRegistry(puerto);
		reg.rebind("/sonda" + s.getNumSensor(), s);
		
		System.out.println("Sonda registrada");
	}   
}

