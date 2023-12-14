package main;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import main.Mensajes.Mensaje;
import main.Mensajes.MensajeConfirmacionCS;
import main.Mensajes.MensajeConfirmacionSC;
import main.Mensajes.MensajeEmitirFichero;
import main.Mensajes.MensajeError;
import main.Mensajes.MensajeFichero;
import main.Mensajes.MensajePedirFichero;
import main.Mensajes.MensajeShutDown;
import main.NuevasUtilidades.Fin;
import main.Oyentes.OyenteCliente;
import main.recursosPracticas.Monitores.MonitoresProdCons.MonitorProdCons;
import main.recursosPracticas.Monitores.MonitoresProdCons.MonitorSincroProdCons;

public class ClientPeer2PeerHandler extends Thread {


	private static final int OFFSET = 1000;
	private ServerSocket ss;
	private Usuario me;
	private HashMap<Integer,MonitorProdCons<Mensaje>> oyentesIn;
	private MonitorProdCons<Mensaje> paraCliente;
	private MonitorProdCons<Mensaje> monitMensajesServidorIn;
	private MonitorProdCons<List<Usuario>> listaUsuarios;
	private MonitorProdCons<Mensaje> monitArchivos;
	private Fin finished;
	private HashMap<Integer, OyenteCliente> oyentesEntrada;
	private HashMap<Integer, OyenteCliente> oyentesSalida;

	public ClientPeer2PeerHandler(Usuario me, MonitorProdCons<Mensaje> monitMensajesServidorIn, MonitorProdCons<Mensaje> monitMensajesServidorOut, MonitorProdCons<Mensaje> monitArchivos, Fin finished) {
		this.me = me;
		paraCliente = monitMensajesServidorOut;
		this.monitMensajesServidorIn = monitMensajesServidorIn;
		this.monitArchivos = monitArchivos;
		//Guarda los canales de entrada y salida.
		oyentesEntrada = new HashMap<Integer,OyenteCliente>();
		oyentesSalida = new HashMap<Integer,OyenteCliente>();
		oyentesIn = new HashMap<Integer, MonitorProdCons<Mensaje>>();
		this.finished = finished;
	}

	public void run() {
		try {
			ss = new ServerSocket(me.getId()+OFFSET);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		while(!finished.check()) {
			//Espera a que le pasen mensajes de Emitir archivo o de confirmación de SC (Significa que ha pedido un archivo) 
			Mensaje m = paraCliente.coger();
			switch (m.getTipo()) {
			case 6: 
				//yo actuo como host
				if (me.getContenidos().contains(((MensajeEmitirFichero)m).getNombreFichero())) {
					int uId = Usuario.readUsuario(m.getOrigen()).getId();
					if (!oyentesSalida.containsKey(uId)) {
						try {
							//Si no se ha conectado antes con este le manda confirmación para que se conecte y pone a funcionar el oyente una vez recibe confirmación de conexión, así como manda el archivo
							monitMensajesServidorIn.poner(new MensajeConfirmacionCS(me, Usuario.readUsuario(m.getOrigen()), ((MensajeEmitirFichero)m).getNombreFichero()));
							Socket s = ss.accept();
							MonitorSincroProdCons<Mensaje> oyIn = new MonitorSincroProdCons<Mensaje>(10);
							OyenteCliente oyCl = new OyenteCliente(s,me, listaUsuarios, oyIn , paraCliente, monitArchivos, finished, false);
							oyCl.start();
							oyentesSalida.put(Usuario.readUsuario(m.getOrigen()).getId(), oyCl);
							oyentesIn.put(Usuario.readUsuario(m.getOrigen()).getId(), oyIn);
							//lee el archivo
							File f = leerArchivo(((MensajeEmitirFichero)m).getNombreFichero());
							//Finalmente manda el archivo.
							oyIn.poner(new MensajeFichero(me, Usuario.readUsuario(m.getOrigen()), f));;
						}
						catch (IOException e) {
							monitMensajesServidorIn.poner(new MensajeError(me, "Problema al realizar la conexion con el usuario solicitado"));
						}
					}
					else {
						//Si ya lo tiene simplemente lee el archivo y se lo pasa al oyente en forma de mensaje para que lo entregue.
						File f = leerArchivo(((MensajeEmitirFichero)m).getNombreFichero());
						oyentesIn.get(uId).poner(new MensajeFichero(me, Usuario.readUsuario(m.getOrigen()), f));;
					}
				}
				else {
					monitMensajesServidorIn.poner(new MensajeError(me, "El usuario no tiene el archivo solicitado"));
				}
				break;
			case 8:
				try {
					//Significa que quiere un archivo y que necesita un oyente que lo reciba
					//Si fuera un sistema real se utilizaría la ip contenida en el usuario en vez de localhost, en un puerto fijo en vez de con el offset + id
					Socket s = new Socket("localhost", OFFSET + Usuario.readUsuario(m.getOrigen()).getId());
					OyenteCliente oyCl = new OyenteCliente(s,me, listaUsuarios, null , paraCliente, monitArchivos, finished, true);
					oyCl.start();
					Mensaje cConex = paraCliente.coger();
					oyentesEntrada.put(Usuario.readUsuario(cConex.getOrigen()).getId(), oyCl);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case 9:
				finished.incrementar();
				break;
			default:
				System.out.println("Problema al decodificar mensaje, desconocido o no procesable");
				break;
			}
		}
		close();
	}

	public void close() {
		//Espera a todos los oyentes antes de cerrarse.
		for(Entry<Integer, OyenteCliente> o :oyentesSalida.entrySet()) {
			try {
				oyentesIn.get(o.getKey()).poner(new MensajeShutDown(me));
				o.getValue().join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		//Como estos están esperando hay que interrumpirlos para que paren de esperar
		for(Entry<Integer, OyenteCliente> o :oyentesEntrada.entrySet()) {
			o.getValue().interrupt();
			if (o.getValue().isInterrupted()) {
				o.getValue().close();
			}
		}
		try {
			ss.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private File leerArchivo(String nombreFichero) {
		// Debería leer un archivo y dolvolver sus datos, aunque para el objetivo de la práctica se ve irrelevante.
		return null;
	}
}