package fiubaceldas.grupo01;

import java.awt.List;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.LineNumberInputStream;
import java.lang.reflect.Type;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.Vector;

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
		int cantidadDePuntitosDeLlegadaInicial = condicionInicial.obtenerCantidadDeElementos("0");
		int cantidadDeCajasInicial = condicionInicial.obtenerCantidadDeElementos("1");
		int cantidadTotalInicial = cantidadDePuntitosDeLlegadaInicial + cantidadDeCajasInicial;
		
		int cantidadDePuntitosDeLlegadaFinal = efectosPredichos.obtenerCantidadDeElementos("0");
		int cantidadDeCajasFinal = efectosPredichos.obtenerCantidadDeElementos("1");
		int cantidadTotalFinal = cantidadDePuntitosDeLlegadaFinal + cantidadDeCajasFinal;
		
		if (condicionInicial.esIgualA(efectosPredichos)){
			return (0.05);
		}
		
		if (cantidadTotalInicial != 0 && cantidadTotalFinal != 0) {
			if (cantidadTotalInicial == cantidadTotalFinal) {
				Vector2d agente = null;
				ArrayList<Vector2d> listaDeCajas = new ArrayList<Vector2d>();
				ArrayList<Vector2d> listaDePuntos = new ArrayList<Vector2d>();
				
				Simbolo[][] simbolos = efectosPredichos.getCasilleros();
				
				for (int i = 0; i < 7; i++){
					for (int j = 0; j < 7; j++){
						if (simbolos[i][j].getSimbolo().equals("A")){
							agente = new Vector2d(j,i);
						}
						
						if (simbolos[i][j].getSimbolo().equals("1")){
							Vector2d caja = new Vector2d(j,i);
							listaDeCajas.add(caja);
						}
						
						if (simbolos[i][j].getSimbolo().equals("0")){
							Vector2d punto = new Vector2d(j,i);
							listaDePuntos.add(punto);
						}
					}
				}
				
				double menorDistanciaCajas = 9999;
				
				for (Vector2d caja : listaDeCajas) {
					double distancia = agente.dist(caja);
					
					if (distancia < menorDistanciaCajas){
						menorDistanciaCajas = distancia;
					}
				}
				
				double menorDistanciaPuntos = 9999;
				
				for (Vector2d caja : listaDeCajas) {
					double distancia = agente.dist(caja);
					
					if (distancia < menorDistanciaPuntos){
						menorDistanciaPuntos = distancia;
					}
				}
				
				return Math.abs(1 - (1.0/(cantidadTotalFinal * (1 / menorDistanciaCajas) * (1 / menorDistanciaPuntos))));
			}
		}
		
		if (cantidadTotalFinal != 0){
			return (1 - (1.0/cantidadTotalFinal));
		}
		
		return (0.1);
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
				armarNuevoPlan(situacionActual, objetivoActual, grafoTeoriasYSituaciones);
				
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
			armarNuevoPlan(situacionActual, nuevoObjetivo, grafoTeoriasYSituaciones);
			
			if (plan.enEjecucion())
				return plan.ejecutarSiguienteAccion();
			else
				return this.RealizarMovimientoRandom(stateObs);
		}
	}
	
	private void armarNuevoPlan(Situacion situacionActual, Situacion situacionObjetivo, Graph grafoTeoriasYSituaciones) {
		/*
		DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(grafoTeoriasYSituaciones);
		Vertex nodoOrigen = grafoTeoriasYSituaciones.getNode(situacionActual.getId());
		Vertex nodoDestino = grafoTeoriasYSituaciones.getNode(situacionObjetivo.getId());
		dijkstra.execute(nodoOrigen);
		
		LinkedList<Vertex> caminoNodos = dijkstra.getPath(nodoDestino);
						
		if (caminoNodos != null)
			if (caminoNodos.size() >= 2) {
				
				ArrayList<Situacion> caminoSituaciones = new ArrayList<Situacion>();
				for (Vertex nodoSituacion: caminoNodos){
					if (nodoSituacion != null){
						for (Situacion situacion: this.situacionesConocidas) {
							
							if (situacion != null){
								int idNodo = Integer.getInteger(nodoSituacion.getId());
								
								if (idNodo == situacion.getId()) {
									caminoSituaciones.add(situacion);
									break;
								}
							}
						}
					}
				}
				
				ArrayList<Teoria> caminoTeoriasAcumplir = this.obtenerCaminoTeoriasACumplir(caminoSituaciones);
				
				ArrayList<ACTIONS> accionesARealizar = new ArrayList<ACTIONS>();
				for (Teoria teoria: caminoTeoriasAcumplir)
					accionesARealizar.add(teoria.getAccionComoAction());
				
				Teoria ultimaTeoriaACumplir = caminoTeoriasAcumplir.get(caminoTeoriasAcumplir.size() - 1);
				double utilidadObjetivo = ultimaTeoriaACumplir.getU();
				
				this.plan.setSituacionesPlan(caminoSituaciones);
				this.plan.setAccionesPlan(accionesARealizar);
				this.plan.setUtilidadObjetivo(utilidadObjetivo);			
				
			}
		 	*/
	}
	
	private ArrayList<Teoria> obtenerCaminoTeoriasACumplir(ArrayList<Situacion> caminoSituaciones) {
		ArrayList<Teoria> caminoTeorias = new ArrayList<Teoria>();
		for (int nSitOrigen = 0; nSitOrigen < caminoSituaciones.size() - 1; nSitOrigen++) {
			int idSitOrigen = caminoSituaciones.get(nSitOrigen).getId();
			int idSitDestino = caminoSituaciones.get(nSitOrigen + 1).getId();
			
			boolean encontroTeoria = false;
			for (Teoria teoria: this.teorias) {
				if (teoria.getIdSitCondicionInicial() == idSitOrigen
						&& teoria.getIdSitEfectosPredichos() == idSitDestino) {
					caminoTeorias.add(teoria);
					encontroTeoria = true;
					break;
				}
			}
			
			if (!encontroTeoria) {
				for (Teoria teoria: this.teoriasPrecargadas) {
					if (teoria.getIdSitCondicionInicial() == idSitOrigen
							&& teoria.getIdSitEfectosPredichos() == idSitDestino) {
						caminoTeorias.add(teoria);
						break;
					}
				}
			}
		}
		return caminoTeorias;
		
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
		HashMap<Integer,Situacion> situacionesIds = new HashMap<Integer,Situacion>();
		for (Teoria teoria: teorias) {
			if (teoria != null) {
				
					Situacion condicionInicial = teoria.getSitCondicionInicial();
					situacionesIds.put(condicionInicial.getId(), condicionInicial);

					Situacion efectosPredichos = teoria.getSitEfectosPredichos();
					situacionesIds.put(efectosPredichos.getId(), efectosPredichos);
			}
		}
		for (Teoria teoriaPrecargada: this.teoriasPrecargadas) {
			if (teoriaPrecargada != null) {
								
				Situacion condicionInicial = teoriaPrecargada.getSitCondicionInicial();
				situacionesIds.put(condicionInicial.getId(), condicionInicial);

				Situacion efectosPredichos = teoriaPrecargada.getSitEfectosPredichos();
				situacionesIds.put(efectosPredichos.getId(), efectosPredichos);
			}
		}
		
		for (Situacion situacion: situacionesIds.values())
			situacionesConocidas.add(situacion);
		
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
