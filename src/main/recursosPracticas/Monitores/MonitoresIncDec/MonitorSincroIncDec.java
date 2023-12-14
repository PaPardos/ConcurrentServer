package main.recursosPracticas.Monitores.MonitoresIncDec;

public class MonitorSincroIncDec implements MonitorIncDec {

	int valor;
	boolean puesto;
	
	public MonitorSincroIncDec(){
		valor = 0;
		puesto = false;
	}

	@Override
	public synchronized void incrementar() {
		while(puesto) {
			try {
				System.out.println("A esperar inc");
				wait();
				System.out.println("Desp Inc");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("A incrementar");
		valor++;
		notify();
	}

	@Override
	public synchronized void decrementar() {
		while(!puesto) {
			try {
				System.out.println("A esperar dec");
				wait();
				System.out.println("Desp Dec");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("A decrementar");
		valor--;
		notify();
		
	}

	@Override
	public synchronized void alt() {
		System.out.println("Cambio");
		puesto = !puesto;
		notifyAll();
	}
	
	public int get() {
		return valor;
	}

}
