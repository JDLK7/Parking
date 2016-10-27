import java.rmi.Remote;
import java.rmi.RemoteException;

public interface InterfazRegistro extends Remote {
	public void registrarSonda(InterfazRemoto s) throws RemoteException;
}
