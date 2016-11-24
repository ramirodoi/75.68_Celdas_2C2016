package fiubaceldas.grupo01;

import ontology.Types.ACTIONS;
import fiubaceldas.grupo01.Situacion;

public class Teoria {
	private int id;
	private String condicionInicial;
	private int idSitCondicionInicial;
	private String accion;
	private String efectosPredichos;
	private int idSitEfectosPredichos;
	private double k;
	private double p;
	private double u;
	
	public Teoria() {
	}
	
	public Teoria(int id, String condicionInicial, int idSitCondicionInicial, String accion, 
				  String efectosPredichos, int idSitEfectosPredichos, double k, 
				  double p, double u) {
		this.setId(id);
		this.setCondicionInicial(condicionInicial);
		this.setIdSitCondicionInicial(idSitCondicionInicial);
		this.setAccion(accion);
		this.setEfectosPredichos(efectosPredichos);
		this.setIdSitEfectosPredichos(idSitEfectosPredichos);
		this.setK(k);
		this.setP(p);
		this.setU(u);
	}

	public Teoria(int id, Situacion condicionInicial, ACTIONS accion, 
				  Situacion efectosPredichos, double k, 
				  double p, double u) {
		this.setId(id);
		this.setCondicionInicial(condicionInicial);
		this.setIdSitCondicionInicial(condicionInicial.getId());
		this.setEfectosPredichos(efectosPredichos);
		this.setIdSitEfectosPredichos(efectosPredichos.getId());
		this.setAccion(accion);
		this.setK(k);
		this.setP(p);
		this.setU(u);
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getCondicionInicial() {
		return condicionInicial;
	}

	public void setCondicionInicial(String condicionInicial) {
		this.condicionInicial = condicionInicial;
	}
	
	public void setCondicionInicial(Situacion condicionInicial) {
		setCondicionInicial(obtenerSituacionComoString(condicionInicial));
	}
	
	public String getEfectosPredichos() {
		return efectosPredichos;
	}

	public void setEfectosPredichos(String efectosPredichos) {
		this.efectosPredichos = efectosPredichos;
	}
	
	public void setEfectosPredichos(Situacion efectosPredichos) {
		this.setEfectosPredichos(obtenerSituacionComoString(efectosPredichos));
	}
	
	private String obtenerSituacionComoString(Situacion situacion) {
		Simbolo[][] casillerosSituacion = situacion.getCasilleros();
		String situacionString = "";
		for (int fila = 0; fila < 7; fila++) {
			for (int col = 0; col < 7; col++) {
				situacionString += casillerosSituacion[fila][col].getSimbolo();
			}
			situacionString += "\n";
		}
		return situacionString;
	}
	
	public int getIdSitCondicionInicial() {
		return idSitCondicionInicial;
	}

	public void setIdSitCondicionInicial(int idSitCondicionInicial) {
		this.idSitCondicionInicial = idSitCondicionInicial;
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

	
	public int getIdSitEfectosPredichos() {
		return idSitEfectosPredichos;
	}

	public void setIdSitEfectosPredichos(int idSitEfectosPredichos) {
		this.idSitEfectosPredichos = idSitEfectosPredichos;
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
	
	public Situacion getSitCondicionInicial() {
		return new Situacion(this.idSitCondicionInicial, this.condicionInicial);
	}
	
	public Situacion getSitEfectosPredichos() {
		return new Situacion(this.idSitEfectosPredichos, this.efectosPredichos);
	}	
}
