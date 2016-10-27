import java.rmi.Remote;
import java.rmi.RemoteException;

public interface InterfazRemoto extends Remote {
	public Sonda getSensor() throws RemoteException;
	
	public int getVolumen() throws RemoteException;
	public String getFecha() throws RemoteException;
	public String getHora() throws RemoteException;
	public int getLed() throws RemoteException;
	public int getNumSensor() throws RemoteException;
	
	public void comprobarSensor() throws RemoteException;
}
