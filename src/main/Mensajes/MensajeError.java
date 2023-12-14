package main.Mensajes;

import main.Usuario;

public class MensajeError extends Mensaje {

	private String desc;

	public MensajeError(Usuario origen, String desc) {
		super(origen);
		id = 999;
		this.desc = desc;
	}

	public String getDesc() {
		return desc;
	}

}
