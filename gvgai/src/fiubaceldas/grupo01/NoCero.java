package fiubaceldas.grupo01;

public class NoCero extends Simbolo {

	public NoCero() {
		this.simbolo = "Q";
	}
	
	@Override
	public boolean incluyeA(Simbolo otroSimbolo) {
		return (!(otroSimbolo.getSimbolo().equals("0")));
	}

}
