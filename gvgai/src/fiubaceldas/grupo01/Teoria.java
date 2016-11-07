package fiubaceldas.grupo01;

import ontology.Types.ACTIONS;

public class Teoria {
	private String condicionInicial;
	private String accion;
	private String efectosPredichos;
	private double k;
	private double p;
	private double u;
	
	public Teoria() {
	}
	
	public Teoria(String condicionInicial, String accion, 
				  String efectosPredichos, double k, 
				  double p, double u) {
		this.setCondicionInicial(condicionInicial);
		this.setAccion(accion);
		this.setEfectosPredichos(efectosPredichos);
		this.setK(k);
		this.setP(p);
		this.setU(u);
	}

	public String getCondicionInicial() {
		return condicionInicial;
	}

	public void setCondicionInicial(String condicionInicial) {
		this.condicionInicial = condicionInicial;
	}

	public String getAccionComoString() {		
		return (this.accion);
	}
	
	public ontology.Types.ACTIONS getAccionComoAction() {
		if (this.accion.equals("arriba")){
			return (ACTIONS.ACTION_UP);
		}
		
		if (this.accion.equals("abajo")){
			return (ACTIONS.ACTION_DOWN);
		}
		
		if (this.accion.equals("izquierda")){
			return (ACTIONS.ACTION_LEFT);
		}
		
		if (this.accion.equals("derecha")){
			return (ACTIONS.ACTION_RIGHT);
		}
		
		return (ACTIONS.ACTION_NIL);
	}

	public void setAccion(String accion) {
		this.accion = accion;
	}
	
	public void setAccion(ontology.Types.ACTIONS accion) {
		if (accion.equals(ACTIONS.ACTION_UP)){
			this.accion = "arriba";
		}
		
		if (accion.equals(ACTIONS.ACTION_DOWN)){
			this.accion = "abajo";
		}
		
		if (accion.equals(ACTIONS.ACTION_LEFT)){
			this.accion = "izquierda";
		}
		
		if (accion.equals(ACTIONS.ACTION_RIGHT)){
			this.accion = "derecha";
		}
		
		if (accion.equals(ACTIONS.ACTION_NIL)){
			this.accion = "nada";
		}
	}

	public String getEfectosPredichos() {
		return efectosPredichos;
	}

	public void setEfectosPredichos(String efectosPredichos) {
		this.efectosPredichos = efectosPredichos;
	}

	public double getK() {
		return k;
	}

	public void setK(double k) {
		this.k = k;
	}

	public double getP() {
		return p;
	}

	public void setP(double p) {
		this.p = p;
	}

	public double getU() {
		return u;
	}

	public void setU(double u) {
		this.u = u;
	}
}
