package main.Mensajes;

import java.io.Serializable;

import main.Usuario;

public abstract class Mensaje implements Serializable {
	
	/* Chuleta tipo de mensajes:
	 *  0 :Sin definir
	 *  1 :Conexión
	 *  2 :Confirmación de conexion
	 *  3 :ListaUsuario
	 *  4 :Confirmación llegada lista usuario (con lista)
	 *  5 :Pedir fichero
	 *  6 :Emitir fichero
	 *  7 :Preparado servidor-Cliente
	 *  8 :Preparado cliente-Servidor  
	 *  9 :Mensaje apagar Server
	 *  10 :Mensaje con fichero dentro
	 *  999: Mensaje de error con desc de error
	 * */
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected int id = 0;
	protected Usuario origen;
	protected Usuario destino;
	
	public Mensaje(Usuario origen){
		this.origen = origen;
	}
	public Mensaje(Usuario me, Usuario u) {
		this.origen = me;
		this.destino = u;
	}
	
	public int getTipo() {
		return id;
	}
	public String getOrigen() {
		return origen.toString();
	}
	public  String getDestino() {
		return destino.toString();
	}
}
