package fiubaceldas.grupo01;

import java.awt.List;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import core.game.StateObservationMulti;
import core.player.AbstractMultiPlayer;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Vector2d;
import graph.*;

public class Agent extends AbstractMultiPlayer {
	private Path path = FileSystems.getDefault().getPath(System.getProperty("user.dir") + "\\src\\fiubaceldas\\grupo01\\Teorias");
	private Perception medioManager;
	private Gson gsonManager;
	private int playerID;
	private ArrayList<Teoria> teorias;
	private ArrayList<Situacion> situacionesConocidas;
	private Situacion situacionAnterior = null;
	private Plan plan = new Plan();
	
	public Agent(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer, int playerID) {
		this.medioManager =  new Perception(stateObs);
		this.gsonManager = new GsonBuilder().setPrettyPrinting().create();
		this.playerID = playerID;
	}
	
	@Override
	public ACTIONS act(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer){	
		this.medioManager =  new Perception(stateObs);
		this.teorias = this.ObtenerTeorias();
		this.situacionesConocidas = this.obtenerSituacionesConocidas();
		Graph grafoTeoriasYSituaciones = this.obtenerGrafoTeoriasYSituaciones();
		
		ACTIONS ultimaAccion = stateObs.getAvatarLastAction();
		Situacion situacionActual = new Situacion(this.situacionesConocidas.size()+1,this.obtenerCasillerosSitActual());
		
		Teoria teoriaLocal = null;		
		if (situacionAnterior != null)
			teoriaLocal = new Teoria(teorias.size()+1, situacionAnterior, ultimaAccion,situacionActual, 1, 1, 
									calcularUtilidadTeoria(situacionAnterior, situacionActual));
		
		evaluarTeoria(teoriaLocal); 
//		GuardarTeorias(this.teorias);
		
		ACTIONS siguienteAccion = calcularAccionYActualizarPlan(stateObs, situacionActual, grafoTeoriasYSituaciones);
		
		situacionAnterior = situacionActual;
		
		return siguienteAccion;
		
	}
	
	
	private double calcularUtilidadTeoria(Situacion condicionInicial, Situacion efectosPredichos) {
		//TODO
		return 1;
	}
	
	private void evaluarTeoria(Teoria teoriaLocal) {
		//TODO
	}
	
	private ACTIONS calcularAccionYActualizarPlan(StateObservationMulti stateObs, Situacion situacionActual, 
												Graph grafoTeoriasYSituaciones) {
		if (plan.enEjecucion()) {
			if (plan.cumpleElPlan(situacionActual)) {
				if (!plan.seLlegoAlObjetivo()) {
					return plan.ejecutarSiguienteAccion();
				} else {
					if (plan.getUtilidadObjetivo() == 1) {
						//TODO: GANÓ
						return null;
						
					//Se puede haber llegado al obj del plan pero no ser el obj del juego	
					} else {
						plan.reiniciar();
						return this.RealizarMovimientoRandom(stateObs);
					}
				}
			//Si se estaba ejecutando un plan para llegar a un obj pero falla la última predicción	
			} else {
				Situacion objetivoActual = plan.obtenerSituacionObjetivo();
				plan.reiniciar();
				
				//Se calcula un nuevo camino a ver si se puede llegar al obj desde donde está ahora
				armarNuevoPlan(objetivoActual);
				
				//Si se encontró un nuevo camino lo ejecuta
				if (plan.enEjecucion())
					return plan.ejecutarSiguienteAccion();
				else
					return this.RealizarMovimientoRandom(stateObs);
			}
		} else {
			Teoria teoriaNuevoObjetivo = obtenerTeoriaConMayorUtilidad();
			if (teoriaNuevoObjetivo == null) {
				// Esto va a pasar al principio (todavía no hay teorías)
				return this.RealizarMovimientoRandom(stateObs); 
			}
			
			Situacion nuevoObjetivo = teoriaNuevoObjetivo.getSitEfectosPredichos();
			armarNuevoPlan(nuevoObjetivo);
			if (plan.enEjecucion())
				return plan.ejecutarSiguienteAccion();
			else
				return this.RealizarMovimientoRandom(stateObs);
		}
	}
	
	private Teoria obtenerTeoriaConMayorUtilidad() {
		//TODO
		return null;
	}
	
	private void armarNuevoPlan(Situacion situacionObjetivo) {
		//TODO (acá hay que usar el grafo)
	}
	
	private ArrayList<Situacion> obtenerSituacionesConocidas() {
		ArrayList<Situacion> situacionesConocidas = new ArrayList<Situacion>();
		for (Teoria teoria: teorias) {
			if (teoria != null) {
				Situacion condicionInicial = teoria.getSitCondicionInicial();
				Situacion efectosPredichos = teoria.getSitEfectosPredichos();
				
				boolean agregarCI = true;
				boolean agregarEP = true;
				
				for (Situacion situacion: situacionesConocidas) {
					
					if (situacion.getId() == condicionInicial.getId())
						agregarCI = false;
					
					if (situacion.getId() == efectosPredichos.getId())
						agregarEP = false;
				}
				
				if (agregarCI)
					situacionesConocidas.add(condicionInicial);
				
				if (agregarEP)
					situacionesConocidas.add(efectosPredichos);
			}
		}
		return situacionesConocidas;
	}

	//Realiza un movimiento random.
	private ontology.Types.ACTIONS RealizarMovimientoRandom(StateObservationMulti stateObs){
		ArrayList<ACTIONS> a = stateObs.getAvailableActions(this.playerID);
		
		return (ACTIONS.values()[new Random().nextInt(a.size())]);
	}
	
	//Obtiene todas las Teorias desde el archivo.
	private ArrayList<Teoria> ObtenerTeorias(){
		Type tipoArrayListTeoria = new TypeToken<ArrayList<Teoria>>(){}.getType();
		ArrayList<Teoria> teorias = gsonManager.fromJson(this.ObtenerPathDeTeorias(), tipoArrayListTeoria);
		
		return (teorias);
	}
	
	//Devuelve el path como String para poder leerlo como JSON.
	private String ObtenerPathDeTeorias(){
		try {
			return(new String(Files.readAllBytes(this.path)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return(null);
	}
	
	private char[][] obtenerCasillerosSitActual() {
		char[][] casillerosNivel = medioManager.getLevel();
		char[][] casillerosSituacion = new char[7][7];
		int posXPersonaje = medioManager.getPosicionPersonajeX();
		int posYPersonaje = medioManager.getPosicionPersonajeY();
		int anchoMapa = medioManager.getLevelWidth();
		int altoMapa = medioManager.getLevelHeight();
		int iSit = 0;
		for (int i = posXPersonaje - 3; i <= posXPersonaje + 3; i++) {
			int jSit = 0;
			for (int j = posYPersonaje - 3; j <= posYPersonaje + 3; j++){
				if (i >= 0 && i < anchoMapa && j >= 0 && j < altoMapa) {
					casillerosSituacion[iSit][jSit] = casillerosNivel[i][j];
				} else {
					casillerosSituacion[iSit][jSit] = '?';
				}
				jSit++;
			}
			iSit++;
		} 
		return casillerosSituacion;
	}
	
	private Graph obtenerGrafoTeoriasYSituaciones() {
		ArrayList<Vertex> verticesSituaciones = new ArrayList<Vertex>();
		HashMap<String, Vertex> verticesPorId = new HashMap<String, Vertex>();
		
		ArrayList<Edge> aristasTeorias = new ArrayList<Edge>();
		
		for (Situacion situacion: situacionesConocidas) {
			String idSituacion = Integer.toString(situacion.getId());
			Vertex verticeSituacion = new Vertex(idSituacion, idSituacion);
			verticesSituaciones.add(verticeSituacion);
			verticesPorId.put(idSituacion, verticeSituacion);
		}
		
		for (Teoria teoria: this.teorias) {
			if (teoria != null) {
				if (teoria.getU() != 0) {
					String idTeoria = Integer.toString(teoria.getId());
					String idSitOrigen = Integer.toString(teoria.getIdSitCondicionInicial());
					String idSitDestino = Integer.toString(teoria.getIdSitEfectosPredichos());
					Vertex verticeOrigen = verticesPorId.get(idSitOrigen);
					Vertex verticeDestino = verticesPorId.get(idSitDestino);
					int peso = (int)((teoria.getP()/teoria.getK())*100);
					Edge aristaTeoria = new Edge(idTeoria, verticeOrigen, verticeDestino, peso);
					aristasTeorias.add( aristaTeoria);
				}
			}
		}
		return new Graph(verticesSituaciones, aristasTeorias);
	}
	
	//Guarda una teoria en formato JSON.
	private void GuardarTeorias(ArrayList<Teoria> teorias){
		try {
			FileOutputStream out = new FileOutputStream(this.path.toString());
			out.write("[\n".getBytes());
			
			for (Teoria teoria : teorias) {
				out.write((gsonManager.toJson(teoria) + ",\n").getBytes());
			}
			
			out.write("]".getBytes());
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
