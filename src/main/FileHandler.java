package main;

import java.io.File;

import main.Mensajes.Mensaje;
import main.Mensajes.MensajeError;
import main.Mensajes.MensajeFichero;
import main.NuevasUtilidades.Fin;
import main.recursosPracticas.Monitores.MonitoresProdCons.MonitorProdCons;

public class FileHandler extends Thread {

	private MonitorProdCons<Mensaje> monitArchivos;
	private Fin fin;

	public FileHandler(MonitorProdCons<Mensaje> monitArchivos, Fin finished) {
		this.monitArchivos = monitArchivos;
		this.fin = finished;
	}

	@Override
	public void run() {
		while (!fin.check()) {
			Mensaje m = monitArchivos.coger();
			switch(m.getTipo()) {
			case 10:
				System.out.println("Archivo recibido con éxito.");
				guardarArchivo(((MensajeFichero)m).getFich());
				break;
			case 9:
				fin.incrementar();
				break;
			case 999:
				System.out.println(((MensajeError)m).getDesc());
				break;
			default:
				System.out.println("Problema al decodificar mensaje, desconocido o no procesable");
				break;
			}
			break;
		}
	}

	private void guardarArchivo(File fich) {
		//Lo guardaría en alguna parte, pero para los objetivos de la práctica se ve irrelevante esa implementación.
		System.out.println("Archivo guardado con éxito.");
	}
}
