package main.recursosPracticas.Monitores.MonitoresProdCons;

public class AlmacenUno<T> implements Almacen<T> {
	
	private volatile T buffer;
	
	public AlmacenUno() {
		buffer = null;
	}
	
	@Override
	public void almacenar(T producto) {
			buffer = producto;

	}

	@Override
	public T extraer() {
		T buffer2 = buffer;
		buffer = null;
		return buffer2;
	}

	@Override
	public boolean puedeCoger() {
		return buffer != null;
	}

	@Override
	public boolean puedePoner() {
		return buffer == null;
	}

}
