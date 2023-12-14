package main.Mensajes;

import main.Usuario;

public class MensajeConfirmacionConexion extends Mensaje {

	public MensajeConfirmacionConexion(Usuario origen) {
		super(origen);
		id = 2;
	}
	public MensajeConfirmacionConexion(Usuario origen, Usuario destino) {
		super(origen, destino);
		id = 2;
	}

	@Override
	public int getTipo() {
		return id;
	}

}
