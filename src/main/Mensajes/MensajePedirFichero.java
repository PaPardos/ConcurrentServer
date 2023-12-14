package main.Mensajes;

import main.Usuario;

public class MensajePedirFichero extends Mensaje {

	String nombre;
	public MensajePedirFichero(Usuario origen) {
		super(origen);
		id = 5;
	}
	
	public MensajePedirFichero(Usuario origen, String nombre) {
		super(origen);
		id = 5;
		this.nombre = nombre;
	}

	public String getNombreFichero() {
		return nombre;
	}

}
