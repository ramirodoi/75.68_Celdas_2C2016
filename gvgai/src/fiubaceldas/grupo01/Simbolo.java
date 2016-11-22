package fiubaceldas.grupo01;

public abstract class Simbolo {
	
	protected String simbolo;
	
	public String getSimbolo() {
		return this.simbolo;
	}
	
	public boolean esIgualA(Simbolo otroSimbolo) {
		if ((otroSimbolo.getSimbolo().equals("?")) || (otroSimbolo.getSimbolo().equals(simbolo))){
			return true;
		}
			
		return false;
	}
	
	public abstract boolean incluyeA(Simbolo otroSimbolo);
}
