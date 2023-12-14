package main.recursosPracticas.Locks;

import java.util.concurrent.atomic.AtomicInteger;

public class TicketLock implements Lock{
	
    private volatile AtomicInteger  siguiente = new AtomicInteger(0);
    private volatile AtomicInteger turno = new AtomicInteger(0);

    public int acquireTicket() {
        int myTicket = siguiente.getAndAdd(1);
        siguiente.compareAndExchange(Integer.MAX_VALUE, 0);
        return myTicket;
    }

	@Override
	public void takeLock(int myTicket) {
		int a = turno.get();
		while (myTicket != turno.get());
	}

	@Override
	public void unLock(int id) {
		int fin = turno.getAndAdd(1);
		turno.compareAndExchange(Integer.MAX_VALUE, 0);
	}
}
