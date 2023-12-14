package main.Mensajes;

import main.Usuario;

public class MensajeConexion extends Mensaje {

	private boolean entrada;
	public MensajeConexion(Usuario origen, boolean entrada) {
		super(origen);
		id = 1;
		this.entrada = entrada;
	}
	@Override
	public int getTipo() {
		return id;
	}
	public boolean isEntrada() {
		return entrada;
	}

}
