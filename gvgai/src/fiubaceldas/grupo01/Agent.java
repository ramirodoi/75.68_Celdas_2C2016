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
	private Path pathTeorias = FileSystems.getDefault().getPath(System.getProperty("user.dir") + "\\src\\fiubaceldas\\grupo01\\Teorias");
	private Path pathTeoriasPrecargadas = FileSystems.getDefault().getPath(System.getProperty("user.dir") + "\\src\\fiubaceldas\\grupo01\\TeoriasPrecargadas");
	private Perception medioManager;
	private Gson gsonManager;
	private int playerID;
	private ArrayList<Teoria> teorias;
	private ArrayList<Teoria> teoriasPrecargadas;
	private ArrayList<Situacion> situacionesConocidas;
	private Situacion situacionAnterior = null;
	private Plan plan = new Plan();
	
	public Agent(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer, int playerID) {
		this.medioManager =  new Perception(stateObs);
		this.gsonManager = new GsonBuilder().setPrettyPrinting().create();
		this.playerID = playerID;
		this.teorias = new ArrayList<Teoria>();
		this.teoriasPrecargadas = new ArrayList<Teoria>();
	}
	
	@Override
	public ACTIONS act(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer){
		this.medioManager =  new Perception(stateObs);
		this.ObtenerTodasLasTeorias();
		this.situacionesConocidas = this.obtenerSituacionesConocidas();
		
		if (this.situacionAnterior != null) {
			agregarNuevaSituacion(situacionAnterior);
		}
		
		Situacion situacionActual = this.obtenerSituacionActual();
				
		if (situacionAnterior != null){
			Teoria teoriaLocal = new Teoria(this.teorias.size()+this.teoriasPrecargadas.size()+ 1, this.situacionAnterior, stateObs.getAvatarLastAction(), situacionActual, 1, 1, 
									calcularUtilidadTeoria(this.situacionAnterior, situacionActual));
			evaluarTeoria(teoriaLocal);
		}
		
		ACTIONS siguienteAccion = calcularAccionYActualizarPlan(stateObs, situacionActual, this.obtenerGrafoTeoriasYSituaciones());
		this.situacionAnterior = situacionActual;
		
		this.GuardarTeorias();
		
		return siguienteAccion;
	}
	
	
	private double calcularUtilidadTeoria(Situacion condicionInicial, Situacion efectosPredichos) {
		int cantidadDePuntitosDeLlegada = efectosPredichos.obtenerCantidadDeElementos("0");
		int cantidadDeCajas = efectosPredichos.obtenerCantidadDeElementos("1");
		int cantidadTotal = cantidadDePuntitosDeLlegada + cantidadDeCajas;
		
		return (1 - (1.0/cantidadTotal));
	}
	
	private void evaluarTeoria(Teoria teoriaLocal) {
		if (teoriaLocal != null) {
			boolean encontroTeoria = false;
			
			for (Teoria teoria : this.teorias) {
				if (teoria != null) {
					if (teoria.getSitCondicionInicial().incluyeA(teoriaLocal.getSitCondicionInicial())){
						encontroTeoria = true;
						teoria.setK(teoria.getK() + 1);
						
						if (teoria.getSitEfectosPredichos().incluyeA(teoriaLocal.getSitEfectosPredichos())){
							teoria.setP(teoria.getP() + 1);
						}
						
						break;
					}
				}
			}
			
			if (!encontroTeoria){
				this.teorias.add(teoriaLocal);
			}
		}
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
				armarNuevoPlan(objetivoActual, grafoTeoriasYSituaciones);
				
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
			armarNuevoPlan(nuevoObjetivo, grafoTeoriasYSituaciones);
			
			if (plan.enEjecucion())
				return plan.ejecutarSiguienteAccion();
			else
				return this.RealizarMovimientoRandom(stateObs);
		}
	}
	
	private void armarNuevoPlan(Situacion situacionObjetivo, Graph grafoTeoriasYSituaciones) {
		//TODO (acá hay que usar el grafo)
		this.obtenerTeoriaConMayorUtilidad();
	}
	
	private Teoria obtenerTeoriaConMayorUtilidad() {
		double utilidadMAX = 0;
		Teoria teoriaConUtilidadMax = null;
		
		for (Teoria teoria : this.teorias) {
			if (teoria.getU() >= utilidadMAX){
				utilidadMAX = teoria.getU();
				teoriaConUtilidadMax = teoria;
			}
		}
		
		return (teoriaConUtilidadMax);
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
		for (Teoria teoriaPrecargada: this.teoriasPrecargadas) {
			if (teoriaPrecargada != null) {
				Situacion condicionInicial = teoriaPrecargada.getSitCondicionInicial();
				Situacion efectosPredichos = teoriaPrecargada.getSitEfectosPredichos();
				
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
	
	// Agrega la situación si no hay una igual registrada
	public void agregarNuevaSituacion(Situacion situacion) {
		boolean agregar = true;
		for (Situacion s: this.situacionesConocidas) {
			if (s.esIgualA(situacion)) {
				agregar = false;
				break;
			}
		}
		if (agregar)
			this.situacionesConocidas.add(situacion);
	}

	//Realiza un movimiento random.
	private ontology.Types.ACTIONS RealizarMovimientoRandom(StateObservationMulti stateObs){
		ArrayList<ACTIONS> a = stateObs.getAvailableActions(this.playerID);
		
		return (ACTIONS.values()[new Random().nextInt(a.size())]);
	}
	
	private void ObtenerTodasLasTeorias(){
		this.ObtenerTeorias();
		this.ObtenerTeoriasPrecargadas();
	}
	
	//Obtiene todas las Teorias desde el archivo.
	private void ObtenerTeorias(){
		Type tipoArrayListTeoria = new TypeToken<ArrayList<Teoria>>(){}.getType();
		ArrayList<Teoria> teorias = gsonManager.fromJson(this.ObtenerPathDeTeorias(), tipoArrayListTeoria);
		
		if (teorias != null && teorias.size() > 0){
			this.teorias.clear();
			this.teorias.addAll(teorias);
		}
	}
	
	//Obtiene todas las Teorias desde el archivo.
	private void ObtenerTeoriasPrecargadas(){
		Type tipoArrayListTeoria = new TypeToken<ArrayList<Teoria>>(){}.getType();
		ArrayList<Teoria> teoriasPrecargadas = gsonManager.fromJson(this.ObtenerPathDeTeoriasPrecargadas(), tipoArrayListTeoria);
		
		if (teoriasPrecargadas != null && teoriasPrecargadas.size() > 0){
			this.teoriasPrecargadas.clear();
			this.teoriasPrecargadas.addAll(teoriasPrecargadas);
		}
	}
	
	//Devuelve el path como String para poder leerlo como JSON.
	private String ObtenerPathDeTeorias(){
		try {
			return(new String(Files.readAllBytes(this.pathTeorias)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return(null);
	}
	
	//Devuelve el path como String para poder leerlo como JSON.
	private String ObtenerPathDeTeoriasPrecargadas(){
		try {
			return(new String(Files.readAllBytes(this.pathTeoriasPrecargadas)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return(null);
	}
	
	private char[][] obtenerCasillerosSitActual() {
		char[][] casillerosNivel = medioManager.getLevel();
		char[][] casillerosSituacion = new char[7][7];
		int colPersonaje = medioManager.getPosicionPersonajeX();
		int filaPersonaje = medioManager.getPosicionPersonajeY();
		int anchoMapa = medioManager.getLevelWidth();
		int altoMapa = medioManager.getLevelHeight();
		int filaSit = 0;
		for (int fila = filaPersonaje - 3; fila <= filaPersonaje + 3; fila++) {
			int colSit = 0;
			for (int col = colPersonaje - 3; col <= colPersonaje + 3; col++){
				if (fila >= 0 && fila < altoMapa && col >= 0 && col < anchoMapa) {
					casillerosSituacion[filaSit][colSit] = casillerosNivel[fila][col];
				} else {
					casillerosSituacion[filaSit][colSit] = '?';
				}
				colSit++;
			}
			filaSit++;
		} 
		return casillerosSituacion;
	}
	
	private Situacion obtenerSituacionActual() {
		Situacion situacionActual = new Situacion(this.situacionesConocidas.size()+1,this.obtenerCasillerosSitActual());
		for (Situacion s: this.situacionesConocidas) {
			if (s.esIgualA(situacionActual)) {
				return s;
			}
		}
		
		this.situacionesConocidas.add(situacionActual);
		return situacionActual;
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
					Double peso = (teoria.getK()-teoria.getP())/teoria.getK();
					Edge aristaTeoria = new Edge(idTeoria, verticeOrigen, verticeDestino, peso);
					aristasTeorias.add( aristaTeoria);
				}
			}
		}
		return new Graph(verticesSituaciones, aristasTeorias);
	}
	
	//Guarda una teoria en formato JSON.
	private void GuardarTeorias(){
		try {
			FileOutputStream out = new FileOutputStream(this.pathTeorias.toString());
			out.write("[\n".getBytes());
			
			for (int i = 0; i < this.teorias.size(); i++) {
				out.write(gsonManager.toJson(this.teorias.get(i)).getBytes());
				
				if (i != this.teorias.size()-1) {
					out.write((",\n").getBytes());	
				}
			}
			
			out.write("\n]".getBytes());
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
