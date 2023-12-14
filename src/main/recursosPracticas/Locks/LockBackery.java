package main.recursosPracticas.Locks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LockBackery implements Lock {

	private int m;
	private volatile List<Integer> turn;

	public LockBackery(int m){
		this.m = m;
		this.turn = new ArrayList<Integer>();
		for(int i = 0; i < m+1; i++){
			turn.add(0);
		}
	}
	
	@Override
	public void takeLock(int id) {
		turn.set(id, 1);
		turn.set(id, Collections.max(turn)+1);
		for(int i = 1; i < m+1; i++) {
			while(turn.get(i) != 0 
					&& (turn.get(id) > turn.get(i) ||  (turn.get(id) == turn.get(i) && id > i)));
		}
	}

	@Override
	public void unLock(int id) {
		turn.set(id, 0);
	}

}
