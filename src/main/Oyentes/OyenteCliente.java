package main.Oyentes;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;

import main.Usuario;
import main.Mensajes.Mensaje;
import main.Mensajes.MensajeConexion;
import main.Mensajes.MensajeConfirmacionConexion;
import main.Mensajes.MensajeConfirmacionListaUsuario;
import main.Mensajes.MensajeError;
import main.Mensajes.MensajeListaUsuario;
import main.NuevasUtilidades.Fin;
import main.recursosPracticas.Monitores.MonitoresProdCons.MonitorProdCons;

public class OyenteCliente extends Thread {
	
	public static final int PUERTO_SERVER = 2000;

	private Usuario me;
	private Fin finished;
	private MonitorProdCons<List<Usuario>> listaUsuarios;
	private MonitorProdCons<Mensaje> enviarDestino;
	private MonitorProdCons<Mensaje> entregarAOtrosOyentes;

	private Socket s;

	private MonitorProdCons<Mensaje> monitArchivos;

	private boolean entrada;

	public OyenteCliente(Usuario me, MonitorProdCons<List<Usuario>> ListaUsuarios, MonitorProdCons<Mensaje> enviar, MonitorProdCons<Mensaje> entregar, MonitorProdCons<Mensaje> monitArchivos,  Fin finished, boolean entrada) {
		this.me = me;
		this.listaUsuarios = ListaUsuarios;
		this.enviarDestino = enviar;
		this.entregarAOtrosOyentes = entregar;
		this.monitArchivos = monitArchivos;
		this.entrada = entrada;
		this.finished = finished;
	}

	public OyenteCliente(Socket s, Usuario me, MonitorProdCons<List<Usuario>> ListaUsuarios,
			MonitorProdCons<Mensaje> enviar, MonitorProdCons<Mensaje> entregar, MonitorProdCons<Mensaje> monitArchivos, Fin finished, boolean entrada) {
		this.s = s;
		this.entrada = entrada;
		this.me = me;
		this.listaUsuarios = ListaUsuarios;
		this.enviarDestino = enviar;
		this.entregarAOtrosOyentes = entregar;
		this.monitArchivos = monitArchivos;
		this.finished = finished;
	}

	@Override
	public void run() {
		try {
			//Si no se proporciona socket se presupone que es el server con quien se conecta (Puerto 2000)
			if (s == null)
				s = new Socket("localhost",PUERTO_SERVER);
			OutputStream outputStream = s.getOutputStream();
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
			InputStream inputStream = s.getInputStream();
			ObjectInputStream mensajes = new ObjectInputStream(inputStream);
			objectOutputStream.writeObject(new MensajeConexion(me, !entrada));
			try {
	        while(!finished.check()) {
	        	if (!entrada) {
	        		while(enviarDestino.hasOne()) {
	        			Mensaje m = enviarDestino.coger();
	        			objectOutputStream.writeObject(m);
	        		}
	        	}
	        	else {
	        		//Si el oyente que tiene es de entrada espera a recibir información
	        		Mensaje i = (Mensaje) mensajes.readObject();

	        		switch(i.getTipo()) {
	        		case 1:
	        			System.out.println("Mensaje de conexion recibido por cliente " + Usuario.format(i.getOrigen()));
	        			break;
	        		case 2:
	        			//Si es un oyente del handler le pasa al Handler la info de la conexion
	        			if (s.getPort() != PUERTO_SERVER)
	        				entregarAOtrosOyentes.poner(i);
	        			else {
	        				//Si no es un oyente del handler informa al usuario
	        				System.out.println("Mensaje de confirmación de conexion recibido por servidor " + Usuario.format(i.getOrigen()));
	        			}
	        			break;
	        		case 4:
	        			//Le pasa la lista al usuario mediante un monitor.
	        			System.out.println("Mensaje de confirmación de lista de usuarios recibido por cliente " + Usuario.format(i.getOrigen()));
	        			listaUsuarios.poner(((MensajeConfirmacionListaUsuario)i).getLista());
	        			break;
	        		case 6:
	        			//Le pasa el mensaje al handler que ya lo gestionará.
	        			System.out.println("Mensaje de emision de archivo recibido de parte de " + Usuario.format(i.getOrigen()));
	        			entregarAOtrosOyentes.poner(i);
	        			break;
	        		case 8:
	        			//Le pasa el mensaje al handler que ya lo gestionará.
	        			System.out.println("Mensaje de confirmación de conexion SC recibido de " + Usuario.format(i.getOrigen()));
	        			entregarAOtrosOyentes.poner(i);
	        			break;
	        		case 9:
	        			//Le llega mensaje de que el otro se apaga
	        			System.out.println("Mensaje de confirmación de conexion SC recibido de " + Usuario.format(i.getOrigen()));
	        			entregarAOtrosOyentes.poner(i);
	        			break;
	        		case 10:
	        			//Recibe un archivo y se lo da al cliente.
	        			System.out.println("Mensaje de fichero recibido por parte de : " + Usuario.format(i.getOrigen()));
	        			monitArchivos.poner(i);
	        			break;
	        		case 999:
	        			System.out.println("Mensaje de error recibido por cliente " + Usuario.format(i.getOrigen()) + " con descripción:\n " +((MensajeError)i).getDesc());
	        			break;
	        		default:
	        			System.out.println("Problema al decodificar mensaje, desconocido o no procesable");
	        			break;
	        		}
	        	}
			}
			}
			catch(EOFException e){
				
			}
			finally{
				close();
			}
		} catch (IOException e) {
			System.out.println("El Socket" + me.getId() +"se ha cerrado");
		} catch (ClassNotFoundException e) {
			System.out.println("Problemas al ejecutar el cliente");
			e.printStackTrace();
		}
		try {
			s.close();
		} catch (IOException e) {
			System.out.println("Problemas al cerrar el cliente");
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
