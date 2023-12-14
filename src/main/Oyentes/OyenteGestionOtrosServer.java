package main.Oyentes;

import java.util.ArrayList;
import java.util.List;

import main.Usuario;
import main.Mensajes.Mensaje;
import main.Mensajes.MensajeShutDown;
import main.NuevasUtilidades.Fin;
import main.recursosPracticas.Locks.Lock;
import main.recursosPracticas.Locks.TicketLock;
import main.recursosPracticas.Monitores.MonitoresProdCons.MonitorProdCons;

public class OyenteGestionOtrosServer extends Thread {

	private Fin fin;
	private MonitorProdCons<Mensaje> inOut;
	private List<Mensaje> mensajesOtros;
	private TicketLock lockOyentesServer;
	private int myId;
	private Usuario me;

	public OyenteGestionOtrosServer(Lock lockOyentesServer, List<Mensaje> mensajesOtros, MonitorProdCons<Mensaje> inOut,
			Fin fin, int myId) {
		this.lockOyentesServer = (TicketLock) lockOyentesServer;
		this.mensajesOtros = mensajesOtros;
		this.inOut = inOut;
		this.myId = myId;
		this.fin = fin;
		me = new Usuario(0, "localhost");
	}

	@Override
	public void run(){
		while(!fin.check()) {
			int ticket = lockOyentesServer.acquireTicket();
			lockOyentesServer.takeLock(ticket);
			if (!mensajesOtros.isEmpty()) {
				List<Mensaje> listBorrar = new ArrayList<Mensaje>();
				for(Mensaje e : mensajesOtros) {
					if (Usuario.readUsuario(e.getDestino()).getId() == myId) {
						inOut.poner(e);
						listBorrar.add(e);
					}
				}
				for(Mensaje e : listBorrar) {
					mensajesOtros.remove(e);
				}
			}
			lockOyentesServer.unLock(ticket);
		}
		inOut.poner(new MensajeShutDown(me));
	}
}
