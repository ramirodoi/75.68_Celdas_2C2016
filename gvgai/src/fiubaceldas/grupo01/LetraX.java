package fiubaceldas.grupo01;

public class LetraX extends Simbolo{

	public LetraX() {
		this.simbolo = new String("X");
	}
	
	public boolean incluyeA(Simbolo otroSimbolo) {
		return this.esIgualA(otroSimbolo);
	}
	
}
