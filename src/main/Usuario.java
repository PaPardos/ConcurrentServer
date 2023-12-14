package main;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Usuario implements Comparable, Serializable{

	private int id;
	private String ip;
	private List<String> contenidos;
	private boolean connected = false;
	
	public Usuario(int id, String ip) {
		this.id = id;
		this.ip = ip;
		contenidos = new ArrayList<String>();
	}

	public String getIp() {
		return ip;
	}

	public int getId() {
		return id;
	}

	public static Usuario readUsuario(String data) {
		String[] datos = data.split(" ");
		Usuario user = new Usuario(Integer.parseInt(datos[0]), datos[1]);
		for(int i = 0;  i < Integer.parseInt(datos[2]) ; i++) {
				user.getContenidos().add(datos[3+i]);
		}
		return user;
	}
	
	@Override
	public String toString() {
		String s = id + " " + ip +" "+getContenidos().size();
		for(String i : getContenidos()) {
			s += " " + i;
		}
		return s;
	}
	
	public String toStringConnected() {
		String s = id + " " + ip +" "+getContenidos().size();
		for(String i : getContenidos()) {
			s += " " + i;
		}
		if(connected) {
			s+= " Online";
		}
		else {
			s+= " Offline";
		}
		return s;
	}

	public static String format(String origen) {
		String[] datos = origen.split(" ");
		Usuario user = new Usuario(Integer.parseInt(datos[0]), datos[1]);
		return "ID: " + user.id + " IP: " + user.ip ;
	}

	public List<String> getContenidos() {
		return contenidos;
	}

	@Override
	public int compareTo(Object o) {
		return this.id - ((Usuario)o).getId();
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}
	
}
