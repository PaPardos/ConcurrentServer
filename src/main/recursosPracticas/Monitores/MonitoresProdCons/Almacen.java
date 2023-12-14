package main.recursosPracticas.Monitores.MonitoresProdCons;


public interface Almacen<T> {
/**
* Almacena (como ultimo) un producto en el almac�en. Si no hay
* hueco el proceso que ejecute el m�etodo bloquear�a hasta que lo
* haya.
*/
public void almacenar(T p);
/**
* Extrae el primer producto disponible. Si no hay productos el
* proceso que ejecute el m�etodo bloquear�a hasta que se almacene un
* dato.
*/
public T extraer();
public boolean puedeCoger();
public boolean puedePoner();
}

