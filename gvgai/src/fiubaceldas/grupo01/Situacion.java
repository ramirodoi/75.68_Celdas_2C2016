package fiubaceldas.grupo01;

public class Situacion {
	private int id;
	private char[][] casilleros;
	
	public Situacion(int id, char[][] casilleros) {
		this.setId(id);
		this.setCasilleros(casilleros);
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public char[][] getCasilleros() {
		return casilleros;
	}
	
	public void setCasilleros(char[][] casilleros) {
		this.casilleros = casilleros;
	}
	
}
