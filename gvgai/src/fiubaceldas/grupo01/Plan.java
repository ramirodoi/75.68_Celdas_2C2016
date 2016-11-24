package fiubaceldas.grupo01;

import java.util.ArrayList;

import ontology.Types.ACTIONS;

public class Plan {
	ArrayList<Situacion> situacionesPlan = new ArrayList<Situacion>();
	ArrayList<ACTIONS> accionesPlan = new ArrayList<ACTIONS>();
	int nroUltimaAccionEjecutada = -1;
	double utilidadObjetivo = -1;
	/*
	public Plan (ArrayList<Teoria> listaTeorias) {
	}
	*/
	
	public ACTIONS ejecutarSiguienteAccion() {
		ACTIONS accionAEjecutar;
		if (nroUltimaAccionEjecutada < accionesPlan.size() - 1) {
			accionAEjecutar = accionesPlan.get(nroUltimaAccionEjecutada + 1);
			nroUltimaAccionEjecutada ++;
		} else {
			accionAEjecutar = ACTIONS.ACTION_NIL;			
		}
		return accionAEjecutar;
	}
	
	public double getUtilidadObjetivo() {
		return this.utilidadObjetivo;
	}
	
	public boolean enEjecucion() {
		return (utilidadObjetivo > 0);
	}
	
	boolean seLlegoAlObjetivo() {
		return nroUltimaAccionEjecutada == accionesPlan.size() - 1;
	}
	
	public boolean cumpleElPlan(Situacion situacionActual) {
		if (nroUltimaAccionEjecutada >= 0)
			return (situacionesPlan.get(nroUltimaAccionEjecutada).esIgualA(situacionActual));
		return false;
	}
	
	public Situacion obtenerSituacionObjetivo() {
		if (situacionesPlan.size() > 0)
			return situacionesPlan.get(situacionesPlan.size() - 1);
		return null;
	}
	
	public void inicializarNuevoPlan(ArrayList<Situacion> situacionesPlan, ArrayList<ACTIONS> accionesPlan, 
										double utilidadObjetivo) {
		this.reiniciar();
		
		for (Situacion situcionPlan: situacionesPlan)
			this.situacionesPlan.add(situcionPlan);
		
		for (ACTIONS accionPlan: accionesPlan)
			this.accionesPlan.add(accionPlan);
		
		this.utilidadObjetivo = utilidadObjetivo;
		
	}
	
	public void reiniciar() {
		situacionesPlan.clear();
		accionesPlan.clear();
		nroUltimaAccionEjecutada = -1;
		utilidadObjetivo = -1;
	}

	public void setSituacionesPlan(ArrayList<Situacion> caminoSituaciones) {
		this.situacionesPlan = caminoSituaciones;
		
	}

	public void setAccionesPlan(ArrayList<ACTIONS> accionesARealizar) {
		this.accionesPlan = accionesARealizar;
		
	}

	public void setUtilidadObjetivo(double utilidadObjetivo) {
		this.utilidadObjetivo = utilidadObjetivo;
		
	}
}
