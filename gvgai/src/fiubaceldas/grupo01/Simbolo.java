package fiubaceldas.grupo01;

public abstract class Simbolo {
	
	protected String simbolo;
	
	public String getSimbolo() {
		return this.simbolo;
	}
	
	public boolean esIgualA(Simbolo otroSimbolo) {
		return this.simbolo.equals(otroSimbolo.getSimbolo());
	}
	
	public abstract boolean incluyeA(Simbolo otroSimbolo);
}
