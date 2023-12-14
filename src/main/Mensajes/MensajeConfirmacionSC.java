package main.Mensajes;

import main.Usuario;

public class MensajeConfirmacionSC extends Mensaje{

	private String s;

	public MensajeConfirmacionSC(Usuario me, Usuario u, String s) {
		super(me, u);
		id = 8;
		this.s = s;
	}

	public String getNombreFichero() {
		return s;
	}

}
