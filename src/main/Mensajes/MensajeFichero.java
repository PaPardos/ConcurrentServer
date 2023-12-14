package main.Mensajes;

import java.io.File;

import main.Usuario;

public class MensajeFichero extends Mensaje {

	File fich;
	
	public MensajeFichero(Usuario me, Usuario u, File fichero) {
		super(me, u);
		id = 10;
		fich = fichero;
	}

	public File getFich() {
		return fich;
	}

}
