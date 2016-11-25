package fiubaceldas.grupo01;

public class LetraY extends Simbolo {
	public LetraY() {
		this.simbolo = new String("Y");
	}
	
	public boolean incluyeA(Simbolo otroSimbolo) {
		return this.esIgualA(otroSimbolo);
	}	
}
