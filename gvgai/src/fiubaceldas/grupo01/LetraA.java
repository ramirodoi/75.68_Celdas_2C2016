package fiubaceldas.grupo01;

public class LetraA extends Simbolo {

	public LetraA() {
		this.simbolo = new String("A");
	}
	
	public boolean incluyeA(Simbolo otroSimbolo) {
		return this.esIgualA(otroSimbolo);
	}

}
