package main.Almacenes;

public interface Almacen<T> {

	public T leer(int index) throws InterruptedException;
	public void write(int index, T value) throws InterruptedException;
}

