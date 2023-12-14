package main.Mensajes;

import main.Usuario;

public class MensajeConfirmacionCS extends Mensaje{

	private String s;

	public MensajeConfirmacionCS(Usuario me, Usuario u, String s) {
		super(me, u);
		id = 7;
		this.s = s;
	}

	public String getNombreFichero() {
		return s;
	}

}
