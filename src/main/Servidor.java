package main;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import main.Almacenes.Almacen;
import main.Almacenes.AlmacenFuncionaSemaforos;
import main.Almacenes.AlmacenFuncionalLock;
import main.Mensajes.Mensaje;
import main.NuevasUtilidades.Fin;
import main.Oyentes.OyenteServidor;
import main.recursosPracticas.Locks.Lock;
import main.recursosPracticas.Locks.TicketLock;
import main.recursosPracticas.Monitores.MonitoresProdCons.MonitorProdCons;
import main.recursosPracticas.Monitores.MonitoresProdCons.MonitorSincroProdCons; 

public class Servidor extends Thread {
	
	public static final int MAX_CONEXIONES = 100;
	public static final int PUERTO_SERVER = 2000;
	
	
	private volatile Almacen<Usuario> infoUsuariosConocidos;
	private List<OyenteServidor> oyentes;
	private Lock lockOyentes;
	private Fin fin;
	private ServerSocket sSocket;
	private Usuario me;
	private volatile List<Mensaje> mensajesIntraOyentes;
	
	public Servidor(){
		init();
		loadInfo();
		System.out.println("Inicialización de servidor con éxito.");
	}

	public void run() {
		System.out.println("Esperando conexiones.");
		while(!fin.check()) {
			try {
				Socket s = sSocket.accept();
				Socket s1 = sSocket.accept();
				MonitorProdCons<Mensaje> inOut = new MonitorSincroProdCons<Mensaje>(10);
				Fin finished = new Fin();
				OyenteServidor o = new OyenteServidor(lockOyentes, inOut,infoUsuariosConocidos, s, finished, me, mensajesIntraOyentes);
				OyenteServidor o2 = new OyenteServidor(lockOyentes, inOut,infoUsuariosConocidos, s1, finished, me, mensajesIntraOyentes);
				o.start();
				o2.start();
				oyentes.add(o);
				oyentes.add(o2);
				Thread.sleep(300);
				saveList();
				
			} catch (IOException | InterruptedException e) {
				System.out.println("Problema al hacer una conexión");
				e.printStackTrace();
			}
		}
		try {
			close();
		} catch (IOException e) {
			System.out.println("Algo ha ido mal al cerrarse.");
			e.printStackTrace();
		}
	}
	private void saveList() {
		String listaFinal = "";
		for (int i = 0; i < MAX_CONEXIONES; i++) {
			Usuario u;
			try {
				u = infoUsuariosConocidos.leer(i);
				if(u != null)
					listaFinal += u.toString() + "\n";
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		File myFoo = new File("usuariosConocidos.txt");
		FileWriter fooWriter;
		try {
			fooWriter = new FileWriter(myFoo, false);
			fooWriter.write(listaFinal);
			fooWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void close() throws IOException {
		for(OyenteServidor o : oyentes) {
			try {
				o.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		sSocket.close();
	}

	private void init() {
		me = new Usuario(0, "localhost");
		infoUsuariosConocidos = new AlmacenFuncionaSemaforos<Usuario>(MAX_CONEXIONES);
		oyentes = new ArrayList<OyenteServidor>();
		lockOyentes = new TicketLock();
		fin = new Fin();
		mensajesIntraOyentes = new ArrayList<Mensaje>();
		try {
			sSocket = new ServerSocket(PUERTO_SERVER);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadInfo() {
		try {
		      File myObj = new File("usuariosConocidos.txt");
		      Scanner myReader = new Scanner(myObj);
		      while (myReader.hasNextLine()) {
		        String data = myReader.nextLine();
		        Usuario nuevoUser = Usuario.readUsuario(data);
		        try {
					infoUsuariosConocidos.write(nuevoUser.getId(), nuevoUser);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		      }
		      myReader.close();
		      System.out.println("Información de usuarios leída con éxito.");
		    } catch (FileNotFoundException e) {
		      System.out.println("No existe usuariosConocidos.");
		      e.printStackTrace();
		    }
	}
	
}
