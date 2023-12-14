package main.NuevasUtilidades;

import java.util.concurrent.Semaphore;

public class Fin {
	public int n = 0;
	Semaphore s = new Semaphore(1);

	public void incrementar() {
		try {
			s.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		n++;
		s.release();
	}
	public Boolean check(){
		//Única operación de lectura => al ser atómica no genera problemas de varios accesos
		return n != 0;
	}
}
