package main.Mensajes;

import java.util.ArrayList;

import main.Usuario;

public class MensajeConfirmacionListaUsuario extends Mensaje {

	private ArrayList<Usuario> lista;

	public MensajeConfirmacionListaUsuario(Usuario origen) {
		super(origen);
		id = 4;
	}

	public MensajeConfirmacionListaUsuario(Usuario me, Usuario u, ArrayList<Usuario> lista) {
		super(me,u);
		id = 4;
		this.lista = lista;
	}

	public ArrayList<Usuario> getLista() {
		return lista;
	}

}
