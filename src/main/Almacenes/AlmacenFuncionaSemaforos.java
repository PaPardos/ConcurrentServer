package main.Almacenes;

import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AlmacenFuncionaSemaforos<T> implements Almacen<T> {

    private final T[] data;
    private final boolean[] used;
	private int nEscritores;
	private int nLectores;
	private Semaphore lectores;
	private Semaphore escritores;
	private int nEsperando;

    public AlmacenFuncionaSemaforos(int size) {
    	lectores = new Semaphore(size);
    	escritores = new Semaphore(1);
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
        lectores.acquire();
        if(nLectores == 0) {
        	escritores.acquire();
        }
        nLectores++;
        try {
            return data[index];
        } finally {
        	nLectores--;
        	if (nLectores == 0 ) {
        		escritores.release();
        	}
        	lectores.release();
        }
   
    }

	@Override
	public void write(int index, Object value) throws InterruptedException {
	        try {
	            escritores.acquire();
	            if (!used[index]) {
	            	used[index] = true;
	            }
	            data[index] = (T) value;
	        } finally {
	            escritores.release();
	        }
	}
}

