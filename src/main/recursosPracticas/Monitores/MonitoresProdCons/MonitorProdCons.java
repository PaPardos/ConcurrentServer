package main.recursosPracticas.Monitores.MonitoresProdCons;

public interface MonitorProdCons<T> {
	void poner(T p);
	T coger();
	boolean hasOne();
}
