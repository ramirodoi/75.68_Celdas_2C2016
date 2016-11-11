package fiubaceldas.grupo01;

import java.util.ArrayList;

import ontology.Types.ACTIONS;

public class Plan {
	ArrayList<Situacion> situacionesPlan = new ArrayList<Situacion>();
	ArrayList<ACTIONS> accionesPlan = new ArrayList<ACTIONS>();
	int nroUltimaAccionEjecutada = -1;
	double utilidadObjetivo = -1;
	
	public ACTIONS ejecutarSiguienteAccion() {
		ACTIONS accionAEjecutar;
		if (nroUltimaAccionEjecutada < accionesPlan.size() - 1  && nroUltimaAccionEjecutada >= 0) {
			accionAEjecutar = accionesPlan.get(nroUltimaAccionEjecutada + 1);
			nroUltimaAccionEjecutada ++;
		} else {
			accionAEjecutar = ACTIONS.ACTION_NIL;			
		}
		return accionAEjecutar;
	}
	
	public boolean cumpleElPlan(Situacion situacionActual) {
		return (situacionesPlan.get(nroUltimaAccionEjecutada).esIgualA(situacionActual));
	}
	
	public void reiniciar() {
		situacionesPlan = new ArrayList<Situacion>();
		accionesPlan = new ArrayList<ACTIONS>();
		nroUltimaAccionEjecutada = -1;
		utilidadObjetivo = -1;
	}
}
