package fiubaceldas.grupo01;

public class Uno extends Simbolo {

	public Uno() {
		this.simbolo = new String("1");
	}

	public boolean incluyeA(Simbolo otroSimbolo) {
		return this.esIgualA(otroSimbolo);
	}

}
