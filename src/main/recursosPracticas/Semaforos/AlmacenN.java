package main.recursosPracticas.Semaforos;

import java.util.concurrent.Semaphore;

public class AlmacenN implements Almacen {

	private Producto buffer[];
	private Semaphore empty;
	private Semaphore full;
	private int fin = 0;
	private int n;
	private int ini = 0;
	
	public AlmacenN(int N) {
		empty = new Semaphore(N);
		full = new Semaphore(0);
		buffer = new Producto[N];
		n = N;
	}
	
	@Override
	public void almacenar(Producto producto) {
		try {
			empty.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		buffer[fin] = producto;
		System.out.println("Metido");
		fin = (fin+1)%n;
		full.release();
	}

	@Override
	public Producto extraer() {
		try {
			full.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Producto result = buffer[ini];
		System.out.println("sacado");
		ini = (ini+1)%n;
		empty.release();
		return result;
	}

}
