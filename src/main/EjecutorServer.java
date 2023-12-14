package main;

public class EjecutorServer {

	public static void main(String[] args) {
		Servidor s = new Servidor();
		s.run();
		try {
			s.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
