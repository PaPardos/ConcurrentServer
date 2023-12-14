package main.Almacenes;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AlmacenFuncionalLock<T> implements Almacen<T> {

    private final ReentrantLock lock = new ReentrantLock();
    private final T[] data;
    private final boolean[] used;
	private int nEscritores;
	private int nLectores;
	private Condition lectores = lock.newCondition();
	private Condition escritores = lock.newCondition();
	private int nEsperando;

    public AlmacenFuncionalLock(int size) {
    	this.nEscritores = 0;
    	this.nLectores = 0;
    	this.nEsperando = 0;
        data = (T[]) new Object[size];
		this.used = new boolean[size];
		for(int i = 0; i < used.length; i++) {
			used[i] = false;
		}
    }

    public T leer(int index) throws InterruptedException {
    	lock.lock();
        nLectores++;
        lock.unlock();
        try {
            while (nEscritores > 0) {
                lectores.await();
            }
            return data[index];
        } finally {
        	lock.lock();
        	nLectores--;
        	if (nLectores == 0 && nEsperando > 0) {
        		escritores.signal();
        	}
        	lock.unlock();
        }
    }

	@Override
	public void write(int index, Object value) throws InterruptedException {
		lock.lock();
	    	nEsperando++;
	        try {
	            while ( nLectores > 0 || nEscritores > 1) {
	                escritores.await();
	            }
	            nEscritores++;
	            nEsperando--;
	            if (!used[index]) {
	            	used[index] = true;
	            }
	            data[index] = (T) value;
	        } finally {
	        	nEscritores--;
	            if (nEscritores == 0) {
	            lectores.signalAll();
	            escritores.signal();
	            }
	            lock.unlock();
	        }
	}
}

