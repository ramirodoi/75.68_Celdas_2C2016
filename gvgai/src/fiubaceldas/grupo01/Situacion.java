package fiubaceldas.grupo01;

public class Situacion {
	private int id;
	private Simbolo[][] casilleros;
	
	public Situacion(int id, char[][] casilleros) {
		this.id = id;
		this.setSimbolosCasilleros(casilleros);
	}
	
	public int getId() {
		return id;
	}
	
	public Simbolo[][] getCasilleros() {
		return casilleros;
	}
	
	public void setSimbolosCasilleros(char[][] casilleros) {
		this.casilleros = new Simbolo[7][7];
		for(int i = 0; i < 7; i++) {
			for(int j = 0; j < 7; j++) {
				if(casilleros[i][j] == 'A')
					this.casilleros[i][j] = new LetraA();
				else if(casilleros[i][j] == 'B')
					this.casilleros[i][j] = new LetraB();
				else if(casilleros[i][j] == '.')
					this.casilleros[i][j] = new Punto();
				else if(casilleros[i][j] == 'w')
					this.casilleros[i][j] = new LetraW();
				else if(casilleros[i][j] == '0')
					this.casilleros[i][j] = new Cero();
				else if(casilleros[i][j] == '1')
					this.casilleros[i][j] = new Uno();
				else if(casilleros[i][j] == '?')
					this.casilleros[i][j] = new SignoDePregunta();
			}
		}
	}
	
	public boolean esIgualA(Situacion otraSituacion) {
		Simbolo[][] casillerosOtraSituacion = otraSituacion.getCasilleros();
		for (int i = 0; i < 7; i++)
			for (int j = 0; j < 7; j++)
				if (!casilleros[i][j].esIgualA(casillerosOtraSituacion[i][j]))
					return false;
		return true;
	}
	
	public boolean incluyeA(Situacion otraSituacion) {
		Simbolo[][] casillerosOtraSituacion = otraSituacion.getCasilleros();
		for (int i = 0; i < 7; i++)
			for (int j = 0; j < 7; j++)
				if (!casilleros[i][j].incluyeA(casillerosOtraSituacion[i][j]))
					return false;
		return true;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder("");
		for(int i = 0; i < 7; i++){
		        for(int j = 0; j < 7; j++){
		        	sb.append(casilleros[i][j].getSimbolo().charAt(0));
		        }
		        sb.append("\n");
		}		
		return sb.toString();
	}
	
}
