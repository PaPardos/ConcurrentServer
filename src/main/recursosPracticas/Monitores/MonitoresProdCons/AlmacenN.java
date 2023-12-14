package main.recursosPracticas.Monitores.MonitoresProdCons;

import java.util.ArrayList;

public class AlmacenN<T> implements Almacen<T> {

	private ArrayList<T> buffer;
	private int fin = 0;
	private int n;
	private int ini = 0;
	private boolean lleno = false;
	
	public AlmacenN(int N) {
		buffer = new ArrayList<T>(N);
		n = N;
	}
	
	@Override
	public void almacenar(T producto) {
		buffer.add(fin, producto);
		fin = (fin+1)%n;
		if (ini+1 == fin) {
			lleno = true;
		}
	}

	@Override
	public T extraer() {
		T result = buffer.get(ini);
		ini = (ini+1)%n;
		if (ini == fin) {
			lleno = false;
		}
		return result;
	}

	@Override
	public boolean puedeCoger() {
		return  lleno;
	}

	@Override
	public boolean puedePoner() {
		return ini != fin || !lleno;
	}

}
