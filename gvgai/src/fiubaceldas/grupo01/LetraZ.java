package fiubaceldas.grupo01;

public class LetraZ extends Simbolo{

	public LetraZ() {
		this.simbolo = new String("Z");
	}
	
	public boolean incluyeA(Simbolo otroSimbolo) {
		return this.esIgualA(otroSimbolo);
	}

}
