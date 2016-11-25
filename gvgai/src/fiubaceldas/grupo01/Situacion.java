package fiubaceldas.grupo01;

public class Situacion {
	private int id;
	private Simbolo[][] casilleros = null;
	private String situacionComoString;
	
	public Situacion(int id, char[][] casilleros) {
		this.id = id;
		this.setSimbolosCasilleros(casilleros);
	}
	
	public Situacion(int id, String situacionComoString) {
		this.id = id;
		this.situacionComoString = situacionComoString;
	}
	
	public int getId() {
		return id;
	}
	
	public Simbolo[][] getCasilleros() {
		if (this.casilleros == null) {
			this.inicializarCasillerosDesdeString();
		}
		return this.casilleros;
	}
	
	public String getSituacionComoString() {
		return this.situacionComoString;
	}
	
	public void inicializarCasillerosDesdeString() {
		String[] filas = this.situacionComoString.split("\\n");
		char[][] casillerosSit = new char[7][7];
		for (int i = 0; i < 7; i++) {
			for (int j = 0; j < 7; j++) {
				casillerosSit[j][i] = filas[j].charAt(i);
			}
		}
		this.setSimbolosCasilleros(casillerosSit);
	}
	
	public void setSimbolosCasilleros(char[][] casilleros) {
		this.casilleros = new Simbolo[7][7];
		StringBuilder sb = new StringBuilder("");
		for(int i = 0; i < 7; i++) {
			for(int j = 0; j < 7; j++) {
				char caracterCasillero = casilleros[i][j];
				sb.append(caracterCasillero);
				if(caracterCasillero == 'A')
					this.casilleros[i][j] = new LetraA();
				else if(caracterCasillero == 'B')
					this.casilleros[i][j] = new LetraB();
				else if(caracterCasillero == '.')
					this.casilleros[i][j] = new Punto();
				else if(caracterCasillero == 'w')
					this.casilleros[i][j] = new LetraW();
				else if(caracterCasillero == '0')
					this.casilleros[i][j] = new Cero();
				else if(caracterCasillero == '1')
					this.casilleros[i][j] = new Uno();
				else if(caracterCasillero == 'X')
					this.casilleros[i][j] = new LetraX();
				else if(caracterCasillero == 'Y')
					this.casilleros[i][j] = new LetraY();
				else if(caracterCasillero == '?')
					this.casilleros[i][j] = new SignoDePregunta();
			}
			sb.append("\n");
		}
		this.situacionComoString = sb.toString();
	}
	
	public boolean esIgualA(Situacion otraSituacion) {
		return (this.situacionComoString.equals(otraSituacion.getSituacionComoString()));
	}
	
	public boolean incluyeA(Situacion otraSituacion) {
		Simbolo[][] casillerosOtraSituacion = otraSituacion.getCasilleros();
		Simbolo[][] casilleros = this.getCasilleros();
		for (int i = 0; i < 7; i++)
			for (int j = 0; j < 7; j++)
				if (!(casilleros[i][j].incluyeA(casillerosOtraSituacion[i][j])))
					return false;
		return true;
	}
	
	public String toString() {
		return this.situacionComoString;
	}
	
	// Devuelve la cantidad de elemntos en el cuadrado de 7x7.
	public int obtenerCantidadDeElementos(String elementosAContar){
		Simbolo[][] casilleros = this.getCasilleros();
		int cantidadDeElementos = 0;
		
		for (int fila = 0; fila < 7; fila++){
			for (int col = 0; col < 7; col++){
				if (casilleros[fila][col].getSimbolo().equals(elementosAContar)){
					cantidadDeElementos++;
				}
			}
		}
		
		return (cantidadDeElementos);
	}
}
