package main.recursosPracticas.Monitores.MonitoresIncDec;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MonitorLocksIncDec implements MonitorIncDec {

	volatile int valor;
	
	boolean puesto;
	
	private final ReentrantLock lock = new ReentrantLock();
	
	public MonitorLocksIncDec(){
		valor = 0;
	}
	
	@Override
	public void incrementar() {
		lock.lock();
		valor++;
		lock.unlock();

	}

	@Override
	public void decrementar() {
		lock.lock();
		valor--;
		lock.unlock();

	}

	@Override
	public void alt() {

	}

	@Override
	public int get() {
		return valor;
	}

}
