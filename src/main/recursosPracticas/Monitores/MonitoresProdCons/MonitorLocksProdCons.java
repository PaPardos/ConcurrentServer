package main.recursosPracticas.Monitores.MonitoresProdCons;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import main.Mensajes.Mensaje;
import main.recursosPracticas.Semaforos.Producto;

public class MonitorLocksProdCons<T> implements MonitorProdCons<T> {

	private Producto valor;
	private final ReentrantLock lock = new ReentrantLock();
	private final Condition read = lock.newCondition();
	private final Condition write = lock.newCondition();
	private int nr;
	private int nw;
	
	public MonitorLocksProdCons(){
		valor = null;
		nr = 0;
		nw = 0;
	}

	@Override
	public T coger() {
		
		return null;
	}

	@Override
	public void poner(T p) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean hasOne() {
		// TODO Auto-generated method stub
		return false;
	}

}
