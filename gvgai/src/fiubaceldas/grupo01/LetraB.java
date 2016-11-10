package fiubaceldas.grupo01;

public class LetraB extends Simbolo {

	public LetraB() {
		this.simbolo = new String("B");
	}

	public boolean incluyeA(Simbolo otroSimbolo) {
		return this.esIgualA(otroSimbolo);
	}

}
