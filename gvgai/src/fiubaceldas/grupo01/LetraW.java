package fiubaceldas.grupo01;

public class LetraW extends Simbolo {

	public LetraW() {
		this.simbolo = new String("w");
	}

	public boolean incluyeA(Simbolo otroSimbolo) {
		return this.esIgualA(otroSimbolo);
	}

}
