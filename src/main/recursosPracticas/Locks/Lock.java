package main.recursosPracticas.Locks;

public interface Lock {
	public void takeLock(int id);
	public void unLock(int id);
}
