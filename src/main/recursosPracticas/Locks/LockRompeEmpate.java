package main.recursosPracticas.Locks;

import java.util.ArrayList;

public class LockRompeEmpate implements Lock {

	volatile ArrayList<Integer> listaRompe;
	volatile ArrayList<Integer> listaUltimo;
	private int m;
	public LockRompeEmpate(int m){
		this.m = m;
		listaRompe = new ArrayList<Integer>();
		for(int i = 0; i < m+1; i++) {
			listaRompe.add(0);
		}
		listaUltimo = new ArrayList<Integer>();
		for(int i = 0; i < m+1; i++) {
			listaUltimo.add(0);
		}
	}
	public void takeLock(int id) {
		for(int j = 1; j <= m; j++) {
			listaRompe.set(id, j);
			listaUltimo.set(j, id);
			for(int k = 1; k <= m; k++) {
				if(k != id)
					while((listaRompe.get(k) >= listaRompe.get(id)) && (listaUltimo.get(j) == id));
			}
		}
	}

	@Override
	public void unLock(int id) {
		listaRompe.set(id, 0);
	}
}