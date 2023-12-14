package main.Oyentes;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import main.Usuario;
import main.Almacenes.Almacen;
import main.Almacenes.AlmacenFuncionalLock;
import main.Mensajes.Mensaje;
import main.Mensajes.MensajeConexion;
import main.Mensajes.MensajeConfirmacionCS;
import main.Mensajes.MensajeConfirmacionConexion;
import main.Mensajes.MensajeConfirmacionListaUsuario;
import main.Mensajes.MensajeConfirmacionSC;
import main.Mensajes.MensajeEmitirFichero;
import main.Mensajes.MensajeError;
import main.Mensajes.MensajePedirFichero;
import main.NuevasUtilidades.Fin;
import main.recursosPracticas.Locks.Lock;
import main.recursosPracticas.Locks.TicketLock;
import main.recursosPracticas.Monitores.MonitoresProdCons.MonitorProdCons;

public class OyenteServidor extends Thread {

	private static final int MAX_CONEXIONES = 100;
	private Socket socket;
	private Almacen<Usuario> listaInfoUsuario;
	private int myId = 0;
	private TicketLock lockOyentesServer;
	private Fin fin;
	private Usuario me;
	private List<Mensaje> mensajesOtros;
	private ObjectOutputStream objectOutputStream;
	private ObjectInputStream mensajes;
	private boolean entrada;
	private MonitorProdCons<Mensaje> inOut;
	private OyenteGestionOtrosServer oyenteOtros;
	public OyenteServidor(Lock lockOyentes,MonitorProdCons<Mensaje> inOut, Almacen<Usuario> infoUsuariosConocidos,Socket serverSocket, Fin fin, Usuario userMaquina, List<Mensaje> mensajesOtros){
		this.socket = serverSocket;
		this.listaInfoUsuario = infoUsuariosConocidos;
		this.lockOyentesServer = (TicketLock) lockOyentes;
		this.inOut = inOut;
		this.entrada = true;
		this.fin = fin;
		this.me = userMaquina;
		this.mensajesOtros = mensajesOtros;
	}
	
	@Override
	public void run() {
		try {
			OutputStream outputStream = socket.getOutputStream();
	        objectOutputStream = new ObjectOutputStream(outputStream);
			InputStream inputStream = socket.getInputStream();
			mensajes = new ObjectInputStream(inputStream);
			//Primero reciben el mensaje de conexión y luego ya se dividen en entrada y salida.
	        while(!fin.check() ) {
	        	if(!entrada) {
	        		checkMensajesEnviar();
	        	}
	        	else {
	        		checkMensajeSocket();
	        	}
			}
	        
		} catch (IOException e) {
			System.out.println("Se cierra la conexión con el cliente " + myId);
			if(!socket.isClosed()) {
				try {
					socket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		} catch (ClassNotFoundException e) {
			System.out.println("Problema en uno de los oyentes");
			e.printStackTrace();
		} 
		finally{
			try {
				close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	private void checkMensajesEnviar() {
		
		if (oyenteOtros == null) {
			//Se crea un oyente para gestionar los mensajes que vienen de otros oyentes y que deben mandarse a este
			oyenteOtros = new OyenteGestionOtrosServer(lockOyentesServer, mensajesOtros,inOut, fin, myId);
			oyenteOtros.start();
		}
		//Mediante el monitor va cogiendo los mensajes y los manda.
		Mensaje e = inOut.coger();
		try {
			objectOutputStream.writeObject(e);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private void checkMensajeSocket() throws IOException, ClassNotFoundException {
		Mensaje i = (Mensaje) mensajes.readObject();

    	switch(i.getTipo()) {
    	case 1:
    		System.out.println("Mensaje de conexion recibido por cliente " + Usuario.format(i.getOrigen()));
    		Usuario u = Usuario.readUsuario(i.getOrigen());
    		myId = u.getId() + 100;
    		entrada = ((MensajeConexion)i).isEntrada();
    		if(!entrada) {
    			System.out.println("Mensaje de conexion de socket salida recibido por cliente " + Usuario.format(i.getOrigen()));
    			myId = u.getId(); 
    			return;
    		}
    		else
    		{
    			System.out.println("Mensaje de conexion de socket entrada recibido por cliente " + Usuario.format(i.getOrigen()));
    		}
    		boolean here = false;
    		try {
    			u.setConnected(true);
				listaInfoUsuario.write(u.getId(), u);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
    		inOut.poner(new MensajeConfirmacionConexion(me,u));
    		break;
    	case 3:
    		System.out.println("Mensaje de petición de lista de usuarios recibido por cliente " + Usuario.format(i.getOrigen()));
    		Usuario user = Usuario.readUsuario(i.getOrigen());
    		ArrayList<Usuario> lista = new ArrayList<Usuario>();
    		for(int j = 0 ; j < MAX_CONEXIONES; j++) {
				Usuario info = null;
				try {
					info = listaInfoUsuario.leer(j);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (info !=null) {
					lista.add(info);
				}
			}
    		inOut.poner(new MensajeConfirmacionListaUsuario(me,user, lista));
    		break;
    	case 5:
    		String fich = ((MensajePedirFichero)i).getNombreFichero();
			System.out.println("Mensaje de petición de fichero recibido por cliente " + Usuario.format(i.getOrigen()) + " del fichero " + fich );
			boolean contenido = false;
			int ticket = lockOyentesServer.acquireTicket();
			lockOyentesServer.takeLock(ticket);
			for(int j = 0 ; j < MAX_CONEXIONES; j++) {
				Usuario info = null;
				try {
					info = listaInfoUsuario.leer(j);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (info !=null) {
					if (!contenido && info.getContenidos().contains(fich) && info.isConnected()) {
						contenido = true;
						mandarAOtro(new MensajeEmitirFichero(Usuario.readUsuario(i.getOrigen()),info, fich));
					}
				}
			}
			lockOyentesServer.unLock(ticket);
			if(!contenido) {
	    		inOut.poner(new MensajeError(me, "Ningún usuario tiene el archivo o no están conectados."));
			}
    		break;
    	case 7:
    		System.out.println("Mensaje de confirmación de conexion pasado por parte de usuario: " + Usuario.format(i.getOrigen()) + " a usuario: " + Usuario.format(i.getDestino()) );
    		int ticket1 = lockOyentesServer.acquireTicket();
			lockOyentesServer.takeLock(ticket1);
    		mandarAOtro(new MensajeConfirmacionSC(Usuario.readUsuario(i.getOrigen()), Usuario.readUsuario(i.getDestino()), ((MensajeConfirmacionCS)i).getNombreFichero()));
    		lockOyentesServer.unLock(ticket1);
    		break;
    	case 9:
    		System.out.println("Mensaje de Shut down recibido por cliente " + Usuario.format(i.getOrigen()) + " con descripción:\n ");
    		inOut.poner(i);
    		int ticket11 = lockOyentesServer.acquireTicket();
			lockOyentesServer.takeLock(ticket11);
    		Usuario userOut = Usuario.readUsuario(i.getOrigen());
    		userOut.setConnected(false);
    		try {
				listaInfoUsuario.write(userOut.getId(), userOut);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    		lockOyentesServer.unLock(ticket11);;
    		fin.incrementar();
    		break;
    	case 999:
    		System.out.println("Mensaje de error recibido por cliente " + Usuario.format(i.getOrigen()) + " con descripción:\n " +((MensajeError)i).getDesc());
    		break;
    	default:
    		System.out.println("Problema al decodificar mensaje, desconocido o no procesable");
    		break;
    	}
	}

	private void mandarAOtro(Mensaje mensaje) {
			mensajesOtros.add(mensaje);
	}

	public void close() throws IOException {
		if(!entrada) {
			if (oyenteOtros != null) {
				try {
					oyenteOtros.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		socket.close();
	}

}
