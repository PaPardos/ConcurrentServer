package main.Mensajes;

import main.Usuario;

public class MensajeEmitirFichero extends MensajePedirFichero {

	public MensajeEmitirFichero(Usuario origen, Usuario info, String nombre) {
		super(origen, nombre);
		destino = info;
		id = 6;
	}

}
