package fiubaceldas.grupo01;

public class LetraAoB extends Simbolo {

	public LetraAoB() {
		this.simbolo = "P";
	}
	public boolean incluyeA(Simbolo otroSimbolo) {
		return ((otroSimbolo.getSimbolo().equals("A")) || (otroSimbolo.getSimbolo().equals("B"))
				|| (otroSimbolo.getSimbolo().equals("Y")) || (otroSimbolo.getSimbolo().equals("Z")) );
	}

}
