package main.recursosPracticas.Semaforos;

import java.util.concurrent.Semaphore;

public class AlmacenUno implements Almacen {
	
	private Producto buffer;
	private Semaphore empty;
	private Semaphore full; 
	
	public AlmacenUno() {
		empty = new Semaphore(1);
		full = new Semaphore(0);
		buffer = null;
	}
	
	@Override
	public void almacenar(Producto producto) {
		
		try {
			empty.acquire();
			buffer = producto;
			System.out.println("Metido");
			full.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	@Override
	public Producto extraer() {
		try {
			full.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("sacado");
		Producto buffer2 = buffer;
		buffer = null;
		empty.release();
		return buffer2;
	}

}
