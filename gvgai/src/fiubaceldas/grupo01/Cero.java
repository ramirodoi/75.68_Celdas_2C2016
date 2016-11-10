package fiubaceldas.grupo01;

public class Cero extends Simbolo {

	public Cero() {
		this.simbolo = new String("0");
	}

	public boolean incluyeA(Simbolo otroSimbolo) {
		return this.esIgualA(otroSimbolo);
	}

}
