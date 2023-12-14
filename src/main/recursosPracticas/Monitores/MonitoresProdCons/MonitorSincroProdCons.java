package main.recursosPracticas.Monitores.MonitoresProdCons;

public class MonitorSincroProdCons<T> implements MonitorProdCons<T>{
	protected volatile Almacen<T> buffer;

	public MonitorSincroProdCons(int n) {
		buffer = new AlmacenN<T>(n);
	}
	
	@Override
	public synchronized void poner(T p) {
		while(!buffer.puedePoner()) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		buffer.almacenar(p);
		notifyAll();
		
	}

	@Override
	public synchronized T coger() {
		while(!buffer.puedeCoger()) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		T a = buffer.extraer();
		notifyAll();
		return a;
	}

	@Override
	public boolean hasOne() {
		return buffer.puedeCoger();
	}

}
