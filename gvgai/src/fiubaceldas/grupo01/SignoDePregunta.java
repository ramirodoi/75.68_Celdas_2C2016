package fiubaceldas.grupo01;

public class SignoDePregunta extends Simbolo {

	public SignoDePregunta() {
		this.simbolo = new String("?");
	}

	public boolean incluyeA(Simbolo otroSimbolo) {
		return true;
	}

}
