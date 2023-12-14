package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import main.Mensajes.Mensaje;
import main.Mensajes.MensajeConfirmacionCS;
import main.Mensajes.MensajeEmitirFichero;
import main.Mensajes.MensajeError;
import main.Mensajes.MensajeListaUsuario;
import main.Mensajes.MensajePedirFichero;
import main.Mensajes.MensajeShutDown;
import main.NuevasUtilidades.Fin;
import main.Oyentes.OyenteCliente;
import main.recursosPracticas.Monitores.MonitoresProdCons.MonitorProdCons;
import main.recursosPracticas.Monitores.MonitoresProdCons.MonitorSincroProdCons;
import main.recursosPracticas.Monitores.MonitoresProdCons.MonitorSincroProdCons1;

public class Cliente {
	
	
	
	private Usuario me;
	private BufferedReader reader;
    private final Fin finished = new Fin();
	private ClientPeer2PeerHandler handler;
	private OyenteCliente oyenteServidorIn;
	private OyenteCliente oyenteServidorOut;
	private FileHandler fileHandler;
	
	public void run(){
		String name = null;
		//Pide información hasta que esta sea en el formato correcto.
		while(name == null) {
			System.out.println("Inicio de cliente, proporcione datos de usuario (formato: id(1-100) ip numeroElemsdisponibles <Elem1> <Elem2> ...):");
			reader = new BufferedReader(new InputStreamReader(System.in));
			try {
				name = reader.readLine();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			me = Usuario.readUsuario(name);
		}
		//Monitores que serán utilizados para comunicación entre Threads Oyentes, Cliente y Handler
		MonitorProdCons<List<Usuario>> listaUsuarios = new MonitorSincroProdCons1<List<Usuario>>();
		MonitorProdCons<Mensaje> monitMensajesServidorIn = new MonitorSincroProdCons<Mensaje>(10);
		MonitorProdCons<Mensaje> monitMensajesServidorOut = new MonitorSincroProdCons<Mensaje>(10);
		MonitorProdCons<Mensaje> monitArchivos = new MonitorSincroProdCons<Mensaje>(10);
		fileHandler = new FileHandler(monitArchivos, finished);
		//Al dejar el socket a null se conecta automaticamente con el server
		//Se utilizan dos uno para entrada otro para salida, como escriben y leen de los mismos monitores y usando el mismo lock son seguros.
		oyenteServidorIn = new OyenteCliente( me, listaUsuarios, monitMensajesServidorIn, monitMensajesServidorOut, monitArchivos, finished, true);
		oyenteServidorOut = new OyenteCliente( me, listaUsuarios, monitMensajesServidorIn, monitMensajesServidorOut, monitArchivos, finished, false);
		oyenteServidorIn.start();
		oyenteServidorOut.start();
		fileHandler.start();
		//Handler creado para poder gestionar los mensajes enviados y recibidos por y para oyentes que no sean el del principal, además de con el cliente.
		handler = new ClientPeer2PeerHandler(me, monitMensajesServidorIn, monitMensajesServidorOut, monitArchivos, finished);
		handler.start();
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Sistema inicializado con éxito.");
		while(!finished.check()) {
			//Sistema de opciones
			System.out.println(" Qué quieres hacer? \n 1: Pedir lista de información de usuarios y mostrarla. \n 2: Pedir un archivo al servidor \n 3: Apagar  conexion y salir");
			try {
				String option = reader.readLine();
				switch(Integer.parseInt(option)) {
				case 1:
					monitMensajesServidorIn.poner(new MensajeListaUsuario(me));
					//Espera a que pongan la lista y la lean después de ordenarle a la entrada del socket que mande el mensaje.
					List<Usuario> lista = listaUsuarios.coger();
					for(Usuario i : lista) {
						System.out.println(i.toStringConnected());
					}
					break;
				case 2:
					System.out.println("Indique el nombre del archivo.");
					String nombre = reader.readLine();
					//Ordena que pida el mensaje al servidor, esperando que algún oyente de algún canal (ya creado o que cree el handler después) se lo devuelva en forma de mensaje.
					//El mensaje confirmará que le ha llegado el FileHandler
					monitMensajesServidorIn.poner(new MensajePedirFichero(me, nombre));
					break;
				case 3:
					monitMensajesServidorIn.poner(new MensajeShutDown(me));
					monitArchivos.poner(new MensajeShutDown(me));
					finished.incrementar();
					break;
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		close();
	}

	private void close() {
		try {
			oyenteServidorIn.join();
			oyenteServidorOut.join();
			handler.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
