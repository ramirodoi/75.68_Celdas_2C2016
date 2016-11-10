package fiubaceldas.grupo01;

public class Punto extends Simbolo {

	public Punto() {
		this.simbolo = new String(".");
	}

	public boolean incluyeA(Simbolo otroSimbolo) {
		return this.esIgualA(otroSimbolo);
	}

}
