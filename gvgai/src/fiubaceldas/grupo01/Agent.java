package fiubaceldas.grupo01;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
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
	private Path pathTeorias;
	private Path pathTeoriasPrecargadas = FileSystems.getDefault().getPath(System.getProperty("user.dir") + "\\src\\fiubaceldas\\grupo01\\TeoriasPrecargadas");
	private Perception medioManager;
	private Gson gsonManager;
	private int playerID;
	private ArrayList<Teoria> teorias;
	private ArrayList<Teoria> teoriasPrecargadas;
	private ArrayList<Teoria> teoriasConUtilidadNula;
	private ArrayList<Situacion> situacionesConocidas;
	private ArrayList<Vector2d> posicionesObjetivos;
	private int posPersonajeX;
	private int posPersonajeY;
	private Situacion situacionAnterior = null;
	private Plan plan = new Plan();
	private ArrayList<Integer> idSitObjetivosAlcanzados = new ArrayList<Integer>();
	
	public Agent(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer, int playerID) {
		this.medioManager =  new Perception(stateObs);
		this.gsonManager = new GsonBuilder().setPrettyPrinting().create();
		this.playerID = playerID;
		String stringPathTeorias = "\\src\\fiubaceldas\\grupo01\\Teorias" + Integer.toString(this.playerID);
		this.pathTeorias = FileSystems.getDefault().getPath(System.getProperty("user.dir") + stringPathTeorias);
		this.teorias = new ArrayList<Teoria>();
		this.teoriasPrecargadas = new ArrayList<Teoria>();
		this.teoriasConUtilidadNula = new ArrayList<Teoria>();
		this.posicionesObjetivos = medioManager.getPosicionesObjetivos();
		this.ObtenerTeoriasPrecargadas();
	}
	
	@Override
	public ACTIONS act(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer){
		
		this.medioManager =  new Perception(stateObs);

		this.posPersonajeX = (int)(stateObs.getAvatarPosition(this.playerID).x) / this.medioManager.getSpriteSizeWidthInPixels();
        this.posPersonajeY = (int)stateObs.getAvatarPosition(this.playerID).y / this.medioManager.getSpriteSizeHeightInPixels();
		
		this.ObtenerTeorias();
		this.situacionesConocidas = this.obtenerSituacionesConocidas();
		
		if (this.situacionAnterior != null) {
			agregarNuevaSituacion(situacionAnterior);
		}
		
		Situacion situacionActual = this.obtenerSituacionActual();
				
		ACTIONS ultimaAccion = stateObs.getAvatarLastAction(this.playerID);
		
		if (situacionAnterior != null){
			Teoria teoriaLocal = new Teoria(this.teorias.size()+this.teoriasPrecargadas.size()+ 1, this.situacionAnterior, ultimaAccion, situacionActual, 1, 1, 
									calcularUtilidadTeoria(this.situacionAnterior, ultimaAccion, situacionActual));
			evaluarTeoria(teoriaLocal);
		}
		
		ACTIONS siguienteAccion = calcularAccionYActualizarPlan(stateObs, situacionActual, this.obtenerGrafoTeoriasYSituaciones());
		this.situacionAnterior = situacionActual;
		
		this.GuardarTeorias();
		
		return siguienteAccion;
	}
	
	private double calcularUtilidadTeoria(Situacion condicionInicial, ACTIONS accion, Situacion efectosPredichos) { //

		if (this.esAccionPerdedora(condicionInicial, accion))
			return 0.0;
		
		HashMap<String, ArrayList<Vector2d>> posicionesCadaTipoDeElementoCI = condicionInicial.obtenerPosicionesCadaTipoDeElemento();
		HashMap<String, ArrayList<Vector2d>> posicionesCadaTipoDeElementoEP = efectosPredichos.obtenerPosicionesCadaTipoDeElemento();
		
		int cantidadCajasSueltasCI = 0;
		int cantidadCajasEnObjetivosCI = 0;
		
		if (posicionesCadaTipoDeElementoCI.containsKey("1"))
			cantidadCajasSueltasCI = posicionesCadaTipoDeElementoCI.get("1").size();
		
		if (posicionesCadaTipoDeElementoCI.containsKey("X"))
			cantidadCajasEnObjetivosCI = posicionesCadaTipoDeElementoCI.get("X").size();
				
		int cantidadCajasCI = cantidadCajasSueltasCI + cantidadCajasEnObjetivosCI;
		
		
		int cantidadCajasSueltasEP = 0;
		int cantidadCajasEnObjetivosEP = 0;
		
		if (posicionesCadaTipoDeElementoEP.containsKey("1"))
			cantidadCajasSueltasEP = posicionesCadaTipoDeElementoEP.get("1").size();
		
		if (posicionesCadaTipoDeElementoEP.containsKey("X"))
			cantidadCajasEnObjetivosEP = posicionesCadaTipoDeElementoEP.get("X").size();
				
		int cantidadCajasEP = cantidadCajasSueltasEP + cantidadCajasEnObjetivosEP;

		
		boolean habiaCajas = (cantidadCajasCI > 0);
		boolean hayCajas = (cantidadCajasEP > 0);
		boolean personajeSeMovio = (!(condicionInicial.mismaPosicionPersonaje(efectosPredichos)));
		
		if (!hayCajas) {
			if (habiaCajas) {
				return 0.01;
			} else {
				if (!personajeSeMovio)
					return 0.0625;
				else
					return 0.125;
			}
		} else {
			return this.calcularUtilidadSiHayCajas(posicionesCadaTipoDeElementoCI, posicionesCadaTipoDeElementoEP,
					cantidadCajasSueltasCI, cantidadCajasSueltasEP, cantidadCajasEnObjetivosCI, cantidadCajasEnObjetivosEP,
					condicionInicial, efectosPredichos);
		}
	}
	
	private double calcularUtilidadSiHayCajas(HashMap<String, ArrayList<Vector2d>> posicionesCadaTipoDeElementoCI,
								HashMap<String, ArrayList<Vector2d>> posicionesCadaTipoDeElementoEP,
								int cantidadCajasSueltasCI, int cantidadCajasSueltasEP,
								int cantidadCajasEnObjetivosCI, int cantidadCajasEnObjetivosEP,
								Situacion condicionInicial, Situacion efectosPredichos) { //
		
		int cantidadPersEnObjetivosEP = 0;
		int cantidadObtetivosLibresEP = 0;
		
		if (posicionesCadaTipoDeElementoEP.containsKey("Y"))
			cantidadPersEnObjetivosEP = posicionesCadaTipoDeElementoEP.get("Y").size();
		
		if (posicionesCadaTipoDeElementoEP.containsKey("Z"))
			cantidadPersEnObjetivosEP += posicionesCadaTipoDeElementoEP.get("Z").size();
		
		if (posicionesCadaTipoDeElementoEP.containsKey("0"))
			cantidadObtetivosLibresEP = posicionesCadaTipoDeElementoEP.get("0").size();
		
		int cantidadObjetivosEP = cantidadObtetivosLibresEP + cantidadCajasEnObjetivosEP + cantidadPersEnObjetivosEP;
		boolean hayObjetivos = (cantidadObjetivosEP > 0);
		
		if (!hayObjetivos)
			return calcularUtilidadSinObjetivos(posicionesCadaTipoDeElementoCI, posicionesCadaTipoDeElementoEP,
											cantidadCajasEnObjetivosCI, condicionInicial, efectosPredichos);
		else
			return calcularUtilidadConObjetivos(posicionesCadaTipoDeElementoCI, posicionesCadaTipoDeElementoEP,
					cantidadCajasSueltasEP, cantidadCajasEnObjetivosCI, cantidadCajasEnObjetivosEP, 
					condicionInicial, efectosPredichos);
	}


	private double calcularUtilidadSinObjetivos(HashMap<String, ArrayList<Vector2d>> posicionesCadaTipoDeElementoCI, 
			HashMap<String, ArrayList<Vector2d>> posicionesCadaTipoDeElementoEP, int cantidadCajasEnObjetivosCI,
			Situacion condicionInicial, Situacion efectosPredichos) { //
		
		int cantidadPersEnObjetivosCI = 0;
		int cantidadObtetivosLibresCI = 0;
		
		if (posicionesCadaTipoDeElementoCI.containsKey("Y"))
			cantidadPersEnObjetivosCI = posicionesCadaTipoDeElementoCI.get("Y").size();
		
		if (posicionesCadaTipoDeElementoCI.containsKey("Z"))
			cantidadPersEnObjetivosCI += posicionesCadaTipoDeElementoCI.get("Z").size();
		
		if (posicionesCadaTipoDeElementoCI.containsKey("0"))
			cantidadObtetivosLibresCI = posicionesCadaTipoDeElementoCI.get("0").size();
		
		int cantidadObjetivosCI = cantidadObtetivosLibresCI + cantidadCajasEnObjetivosCI + cantidadPersEnObjetivosCI;
		boolean habiaObjetivos = (cantidadObjetivosCI > 0);
		
		if (habiaObjetivos)
			return 0.1875;
		else
			return calcularUtilidadSinObjetivosCINiEP(posicionesCadaTipoDeElementoCI, posicionesCadaTipoDeElementoEP,
					condicionInicial, efectosPredichos);
	}
	
	private double calcularUtilidadSinObjetivosCINiEP(HashMap<String, ArrayList<Vector2d>> posicionesCadaTipoDeElementoCI,
			HashMap<String, ArrayList<Vector2d>> posicionesCadaTipoDeElementoEP,
			Situacion condicionInicial, Situacion efectosPredichos) { //
		
		
		ArrayList<Vector2d> posicionesCajasSueltasCI = null;
		if (posicionesCadaTipoDeElementoCI.containsKey("1"))
			posicionesCajasSueltasCI = posicionesCadaTipoDeElementoCI.get("1");
		
		ArrayList<Vector2d> posicionesCajasSueltasEP = posicionesCadaTipoDeElementoEP.get("1");
		
		boolean seMovioAlgunaCaja = false;
		if (posicionesCajasSueltasCI == null) {
			seMovioAlgunaCaja = true;
		} else {
			if (posicionesCajasSueltasCI.size() != posicionesCajasSueltasEP.size()) {
				seMovioAlgunaCaja = true;
			} else {
				for (Vector2d posicionCajaCI: posicionesCajasSueltasCI) {
					if (!(posicionesCajasSueltasEP.contains(posicionCajaCI))) {
						seMovioAlgunaCaja = true;
						break;
					}
				}
			}
		}
		
		if (!seMovioAlgunaCaja)
			return calcularUtilidadSiNoSeMovieronCajas(posicionesCadaTipoDeElementoCI, posicionesCadaTipoDeElementoEP,
					posicionesCajasSueltasCI, posicionesCajasSueltasEP,
					condicionInicial, efectosPredichos);
		else
			return 0.4375;
	}

	private double calcularUtilidadSiNoSeMovieronCajas(HashMap<String, ArrayList<Vector2d>> posicionesCadaTipoDeElementoCI,
			HashMap<String, ArrayList<Vector2d>> posicionesCadaTipoDeElementoEP,
			ArrayList<Vector2d> posicionesCajasSueltasCI, ArrayList<Vector2d> posicionesCajasSueltasEP,
			Situacion condicionInicial, Situacion efectosPredichos) { //
		
		Vector2d posicionPersonaje = new Vector2d(3,3);
		
		double distMinimaACajasCI = 100;
		for (Vector2d posicionCajaCI: posicionesCajasSueltasCI) {
			double distanciaACaja = posicionPersonaje.dist(posicionCajaCI);
			if (distanciaACaja < distMinimaACajasCI)
				distMinimaACajasCI = distanciaACaja;
		}
		
		double distMinimaACajasEP = 100;
		for (Vector2d posicionCajaEP: posicionesCajasSueltasEP) {
			double distanciaACaja = posicionPersonaje.dist(posicionCajaEP);
			if (distanciaACaja < distMinimaACajasEP)
				distMinimaACajasEP = distanciaACaja;
		}
		
		boolean seAlejoDeLasCajas = (distMinimaACajasCI < distMinimaACajasEP);
		if (seAlejoDeLasCajas) {
			return 0.25;
		} else {
			boolean seMovioElPersonaje = (!(condicionInicial.mismaPosicionPersonaje(efectosPredichos)));
			if (!seMovioElPersonaje)
				return 0.3125;
			else
				return (0.375 + 0.0625 / distMinimaACajasEP);
		}
	}

	private double calcularUtilidadConObjetivos(HashMap<String, ArrayList<Vector2d>> posicionesCadaTipoDeElementoCI,
			HashMap<String, ArrayList<Vector2d>> posicionesCadaTipoDeElementoEP,
			int cantidadCajasSueltasEP, int cantidadCajasEnObjetivosCI,
			int cantidadCajasEnObjetivosEP, Situacion condicionInicial,
			Situacion efectosPredichos) { //

		
		ArrayList<Vector2d> posicionesCajasSueltasEP = null;
		if (posicionesCadaTipoDeElementoEP.containsKey("1"))
			posicionesCajasSueltasEP = posicionesCadaTipoDeElementoEP.get("1");
		
		ArrayList<Vector2d> posicionesObjetivosSinCajasEP = null;
		if (posicionesCadaTipoDeElementoEP.containsKey("0"))
			posicionesObjetivosSinCajasEP = posicionesCadaTipoDeElementoEP.get("0");
		if (posicionesCadaTipoDeElementoEP.containsKey("Y")) {
			if (posicionesObjetivosSinCajasEP == null)
				posicionesObjetivosSinCajasEP = posicionesCadaTipoDeElementoEP.get("Y");
			else
				posicionesObjetivosSinCajasEP.addAll(posicionesCadaTipoDeElementoEP.get("Y"));
		}
		if (posicionesCadaTipoDeElementoEP.containsKey("Z")) {
			if (posicionesObjetivosSinCajasEP == null)
				posicionesObjetivosSinCajasEP = posicionesCadaTipoDeElementoEP.get("Z");
			else
				posicionesObjetivosSinCajasEP.addAll(posicionesCadaTipoDeElementoEP.get("Z"));
		}
		
		boolean hayCajasEnObjetivos = (cantidadCajasEnObjetivosEP > 0);
		if (!hayCajasEnObjetivos)
			return calcularUtilidadSinCajasEnObjetivos(posicionesCadaTipoDeElementoCI, posicionesCadaTipoDeElementoEP,
					cantidadCajasEnObjetivosCI, condicionInicial, efectosPredichos ,
					posicionesCajasSueltasEP, posicionesObjetivosSinCajasEP);
		else
			return calcularUtilidadConCajasEnObjetivos(posicionesCadaTipoDeElementoCI, posicionesCadaTipoDeElementoEP,
					cantidadCajasSueltasEP, cantidadCajasEnObjetivosEP,
					posicionesCajasSueltasEP, posicionesObjetivosSinCajasEP);
	}	
	

	private double calcularUtilidadSinCajasEnObjetivos(HashMap<String, ArrayList<Vector2d>> posicionesCadaTipoDeElementoCI,
			HashMap<String, ArrayList<Vector2d>> posicionesCadaTipoDeElementoEP,
			int cantidadCajasEnObjetivosCI, Situacion condicionInicial,
			Situacion efectosPredichos,
			ArrayList<Vector2d> posicionesCajasSueltasEP, ArrayList<Vector2d> posicionesObjetivosSinCajasEP) { //
		
		boolean habiaCajasEnObjetivos = (cantidadCajasEnObjetivosCI > 0);
		if (habiaCajasEnObjetivos)
			return 0.5;
		else
			return calcularUtilidadSinCajasEnObjetivosCINiEP(posicionesCadaTipoDeElementoCI, posicionesCadaTipoDeElementoEP,
					condicionInicial, efectosPredichos,
					posicionesCajasSueltasEP, posicionesObjetivosSinCajasEP);
			
	}

	
	private double calcularUtilidadSinCajasEnObjetivosCINiEP(HashMap<String, ArrayList<Vector2d>> posicionesCadaTipoDeElementoCI,
			HashMap<String, ArrayList<Vector2d>> posicionesCadaTipoDeElementoEP,
			Situacion condicionInicial, Situacion efectosPredichos,
			ArrayList<Vector2d> posicionesCajasSueltasEP, ArrayList<Vector2d> posicionesObjetivosSinCajasEP) { //
			
		boolean aumentoDistanciaCajasObjetivos = false;		
		
		double distMinimaCajasObjetivosEP = 100;		
		for (Vector2d posicionCajaEP: posicionesCajasSueltasEP){
			for (Vector2d posicionObjetivoEP: posicionesObjetivosSinCajasEP) {
				double distanciaCajaObjetivo = posicionCajaEP.dist(posicionObjetivoEP);
				if (distanciaCajaObjetivo < distMinimaCajasObjetivosEP)
					distMinimaCajasObjetivosEP = distanciaCajaObjetivo;
			}
		}
		
		
		ArrayList<Vector2d> posicionesCajasSueltasCI = null;
		if (posicionesCadaTipoDeElementoCI.containsKey("1"))
			posicionesCajasSueltasCI = posicionesCadaTipoDeElementoCI.get("1");
		
		ArrayList<Vector2d> posicionesObjetivosSinCajasCI = null;
		if (posicionesCadaTipoDeElementoCI.containsKey("0"))
			posicionesObjetivosSinCajasCI = posicionesCadaTipoDeElementoCI.get("0");
		if (posicionesCadaTipoDeElementoCI.containsKey("Y")) {
			if (posicionesObjetivosSinCajasCI == null)
				posicionesObjetivosSinCajasCI = posicionesCadaTipoDeElementoCI.get("Y");
			else
				posicionesObjetivosSinCajasCI.addAll(posicionesCadaTipoDeElementoCI.get("Y"));
		}
		if (posicionesCadaTipoDeElementoCI.containsKey("Z")) {
			if (posicionesObjetivosSinCajasCI == null)
				posicionesObjetivosSinCajasCI = posicionesCadaTipoDeElementoCI.get("Z");
			else
				posicionesObjetivosSinCajasCI.addAll(posicionesCadaTipoDeElementoCI.get("Z"));
		}
		
		
		double distMinimaCajasObjetivosCI = 100;
		if (!(posicionesCajasSueltasCI == null || posicionesObjetivosSinCajasCI == null)) {
			
			for (Vector2d posicionCajaCI: posicionesCajasSueltasCI){
				for (Vector2d posicionObjetivoCI: posicionesObjetivosSinCajasCI) {
					double distanciaCajaObjetivo = posicionCajaCI.dist(posicionObjetivoCI);
					if (distanciaCajaObjetivo < distMinimaCajasObjetivosCI)
						distMinimaCajasObjetivosCI = distanciaCajaObjetivo;
				}
			}			
			
			aumentoDistanciaCajasObjetivos = (distMinimaCajasObjetivosEP > distMinimaCajasObjetivosCI);
		}
		
		if (aumentoDistanciaCajasObjetivos) {
			return 0.5625;
		} else {
			boolean seMovioPersonaje = (!(condicionInicial.mismaPosicionPersonaje(efectosPredichos)));
			if (!seMovioPersonaje)
				return 0.625;
			else
				return 0.6825 + 0.0625 / distMinimaCajasObjetivosEP;
		}
		
		
	}
	
	private double calcularUtilidadConCajasEnObjetivos(HashMap<String, ArrayList<Vector2d>> posicionesCadaTipoDeElementoCI,
			HashMap<String, ArrayList<Vector2d>> posicionesCadaTipoDeElementoEP,
			int cantidadCajasSueltasEP, int cantidadCajasEnObjetivosEP,
			ArrayList<Vector2d> posicionesCajasSueltasEP, ArrayList<Vector2d> posicionesObjetivosSinCajasEP) { //
		
		if (cantidadCajasEnObjetivosEP != 3){
			if (cantidadCajasEnObjetivosEP != 2)
				return calcularUtilidadConUnaCajaEnObjetivos(posicionesCadaTipoDeElementoCI, posicionesCadaTipoDeElementoEP, 
						posicionesCajasSueltasEP, posicionesObjetivosSinCajasEP, cantidadCajasSueltasEP);
			else
				return calcularUtilidadConDosCajasEnObjetivos(posicionesCadaTipoDeElementoCI, posicionesCadaTipoDeElementoEP,
						posicionesCajasSueltasEP, posicionesObjetivosSinCajasEP, cantidadCajasSueltasEP);
		} else {
			return 1;
		}
	}

	
	private double calcularUtilidadConUnaCajaEnObjetivos(HashMap<String, ArrayList<Vector2d>> posicionesCadaTipoDeElementoCI,
			HashMap<String, ArrayList<Vector2d>> posicionesCadaTipoDeElementoEP, ArrayList<Vector2d> posicionesCajasSueltasEP,
			ArrayList<Vector2d> posicionesObjetivosSinCajasEP, int cantidadCajasSueltasEP) {
		
		boolean hayCajasSueltas = (cantidadCajasSueltasEP > 0);
		if (!hayCajasSueltas)
			return 0.75;
		else {
			double distMinimaCajasObjetivosEP = 100;
			for (Vector2d posicionCajaEP: posicionesCajasSueltasEP){
				for (Vector2d posicionObjetivoEP: posicionesObjetivosSinCajasEP) {
					double distanciaCajaObjetivo = posicionCajaEP.dist(posicionObjetivoEP);
					if (distanciaCajaObjetivo < distMinimaCajasObjetivosEP)
						distMinimaCajasObjetivosEP = distanciaCajaObjetivo;
				}
			}
			return 0.8125 + 0.0625 / distMinimaCajasObjetivosEP;
		}
	}
	
	private double calcularUtilidadConDosCajasEnObjetivos(HashMap<String, ArrayList<Vector2d>> posicionesCadaTipoDeElementoCI,
			HashMap<String, ArrayList<Vector2d>> posicionesCadaTipoDeElementoEP, ArrayList<Vector2d> posicionesCajasSueltasEP,
			ArrayList<Vector2d> posicionesObjetivosSinCajasEP, int cantidadCajasSueltasEP) {
		
		boolean hayCajasSueltas = (cantidadCajasSueltasEP > 0);
		if (!hayCajasSueltas)
			return 0.875;
		else {
			double distMinimaCajasObjetivosEP = 100;
			for (Vector2d posicionCajaEP: posicionesCajasSueltasEP){
				for (Vector2d posicionObjetivoEP: posicionesObjetivosSinCajasEP) {
					double distanciaCajaObjetivo = posicionCajaEP.dist(posicionObjetivoEP);
					if (distanciaCajaObjetivo < distMinimaCajasObjetivosEP)
						distMinimaCajasObjetivosEP = distanciaCajaObjetivo;
				}
			}
			return 0.9375 + 0.0625 / distMinimaCajasObjetivosEP;
		}
	}

	private void evaluarTeoria(Teoria teoriaLocal) {
		
		if (teoriaLocal != null) {					
			
			ArrayList<Teoria> teoriasSimilares = this.buscarTeoriasSimilares(teoriaLocal, this.teorias);
			ArrayList<Teoria> teoriasSimilaresPrecargadas = this.buscarTeoriasSimilares(teoriaLocal, this.teoriasPrecargadas);
			ArrayList<Teoria> todasLasTeoriasSimilares = new ArrayList<Teoria>();
			todasLasTeoriasSimilares.addAll(teoriasSimilares);
			todasLasTeoriasSimilares.addAll(teoriasSimilaresPrecargadas);
			
			boolean hayTeoriasSimilares = (todasLasTeoriasSimilares.size() > 0);			
			if (hayTeoriasSimilares) {
				Teoria teoriaIgualALocal = this.buscarTeoriaIgual(teoriaLocal, todasLasTeoriasSimilares);
				if (teoriaIgualALocal != null) {
					teoriaIgualALocal.setP(teoriaIgualALocal.getP() + 1);
					for (Teoria teoriaSimilar: todasLasTeoriasSimilares) {
						teoriaSimilar.setK(teoriaSimilar.getK() + 1);
					}
				} else {
					
					this.teorias.add(teoriaLocal);	
					
					Situacion CITeoriaLocal = teoriaLocal.getSitCondicionInicial();					
					Situacion EPTeoriaLocal = teoriaLocal.getSitEfectosPredichos();
					
					Teoria teoriaSimilar = todasLasTeoriasSimilares.get(0);
					Situacion EPTeoriaSimilar = teoriaSimilar.getSitEfectosPredichos();
					
					Situacion EPTeoriaMutante = EPTeoriaLocal.generalizacionCon(EPTeoriaSimilar, EPTeoriaLocal.getId()+1);
															
					for (int i = 1; i < todasLasTeoriasSimilares.size() && EPTeoriaMutante != null; i++) {
						EPTeoriaSimilar = todasLasTeoriasSimilares.get(i).getSitEfectosPredichos();
						EPTeoriaMutante = EPTeoriaMutante.generalizacionCon(EPTeoriaSimilar, teoriaLocal.getId()+1);
					}
					
					double KTeoriasSimilares = teoriaSimilar.getK();
					double nuevoKTeoriasSimilares;
					
					if (EPTeoriaMutante != null) {
					
						double utilidadTeoriaMutante;
						if (teoriaLocal.getU() == 0.0)
							utilidadTeoriaMutante = 0.0;
						else
							utilidadTeoriaMutante = calcularUtilidadTeoria(CITeoriaLocal, teoriaLocal.getAccionComoAction(), EPTeoriaMutante);
						
						Teoria teoriaMutante = new Teoria(teoriaLocal.getId() + 1, 
												CITeoriaLocal, teoriaLocal.getAccionComoAction(), EPTeoriaMutante, 
												KTeoriasSimilares + 2, 1, utilidadTeoriaMutante);
						
						Teoria teoriaIgualAMutante = this.buscarTeoriaIgual(teoriaMutante, todasLasTeoriasSimilares);
						
						if (teoriaIgualAMutante != null)
							nuevoKTeoriasSimilares = KTeoriasSimilares + 1;
						else
							nuevoKTeoriasSimilares = KTeoriasSimilares + 2;
						
						
						if (teoriaIgualAMutante != null) {
							teoriaIgualAMutante.setP(teoriaIgualAMutante.getP() + 1);
						} else {
							this.teorias.add(teoriaMutante);
						}
						
					} else {
						nuevoKTeoriasSimilares = KTeoriasSimilares + 1;
					}
					
					for (Teoria teoria: todasLasTeoriasSimilares) {
						teoria.setK(nuevoKTeoriasSimilares);
					}
					
					teoriaLocal.setK(nuevoKTeoriasSimilares);					
									
				}
			} else {
				this.teorias.add(teoriaLocal);
				if (teoriaLocal.getU() == 0) {
					this.teoriasConUtilidadNula.add(teoriaLocal);
				}
			}
		}
	}	
	
	
	private ArrayList<Teoria> buscarTeoriasSimilares(Teoria teoriaLocal, ArrayList<Teoria> listaDeTeorias) {
		
		ArrayList<Teoria> teoriasSimilares = new ArrayList<Teoria>();
		Situacion condicionInicialTeoriaLocal = teoriaLocal.getSitCondicionInicial();
		ACTIONS accionTeoriaLocal = teoriaLocal.getAccionComoAction();
		
		for (Teoria teoria : listaDeTeorias) {
			if (teoria != null) {
				
				Situacion condicionInicialTeoria = teoria.getSitCondicionInicial();
				ACTIONS accionTeoria = teoria.getAccionComoAction();									
				
				if (condicionInicialTeoria.incluyeA(condicionInicialTeoriaLocal)
						&& accionTeoria == accionTeoriaLocal){
					
					teoriasSimilares.add(teoria);
				}
			}
		}	
		return teoriasSimilares;
	}

	private Teoria buscarTeoriaIgual(Teoria teoriaLocal, ArrayList<Teoria> listaDeTeoriasSimilares) {

		Situacion efectosPredichosTeoriaLocal = teoriaLocal.getSitEfectosPredichos();

		for (Teoria teoria : listaDeTeoriasSimilares) {
			if (teoria != null) {				
				
				Situacion efectosPredichosTeoria = teoria.getSitEfectosPredichos();
				
				if (efectosPredichosTeoria.incluyeA(efectosPredichosTeoriaLocal)){
					return teoria;
				}
			}
		}	
		return null;
	}


	
	private ACTIONS calcularAccionYActualizarPlan(StateObservationMulti stateObs, Situacion situacionActual, 
												Graph grafoTeoriasYSituaciones) {		
		if (plan.enEjecucion()) {
			if (plan.cumpleElPlan(situacionActual)) {
				if (!plan.seLlegoAlObjetivo()) {
					ACTIONS siguienteAccion = plan.ejecutarSiguienteAccion();
					if (!(this.esAccionPerdedora(situacionActual, siguienteAccion))) {
						return siguienteAccion;
					} else {
						plan.reiniciar();
						return this.RealizarMovimientoRandom(stateObs, situacionActual);
					}
				} else {
					if (plan.getUtilidadObjetivo() == 1) {
						//TODO: GANÓ
						return null;
						
					//Se puede haber llegado al obj del plan pero no ser el obj del juego	
					} else {
						this.idSitObjetivosAlcanzados.add(plan.obtenerSituacionObjetivo().getId());
						plan.reiniciar();
						return this.RealizarMovimientoRandom(stateObs, situacionActual);
					}
				}
			//Si se estaba ejecutando un plan para llegar a un obj pero falla la última predicción	
			} else {
				plan.reiniciar();				
				return this.RealizarMovimientoRandom(stateObs, situacionActual);
			}
		} else {
			Teoria teoriaNuevoObjetivo = obtenerTeoriaConMayorUtilidad();
			if (teoriaNuevoObjetivo == null) {
				// Esto va a pasar al principio (todavía no hay teorías)
				return this.RealizarMovimientoRandom(stateObs, situacionActual); 
			}
			
			Situacion nuevoObjetivo = teoriaNuevoObjetivo.getSitEfectosPredichos();
			if (!(this.idSitObjetivosAlcanzados.contains(nuevoObjetivo.getId())))
				armarNuevoPlan(situacionActual, nuevoObjetivo, grafoTeoriasYSituaciones);
			
			if (plan.enEjecucion()) {
				ACTIONS siguienteAccion = plan.ejecutarSiguienteAccion();
				if (!(this.esAccionPerdedora(situacionActual, siguienteAccion))) {
					return siguienteAccion;
				} else {
					plan.reiniciar();
					return this.RealizarMovimientoRandom(stateObs, situacionActual);
				}
			} else {
				return this.RealizarMovimientoRandom(stateObs, situacionActual);
			}
		}
	}
	
	private void armarNuevoPlan(Situacion situacionActual, Situacion situacionObjetivo, Graph grafoTeoriasYSituaciones) {
		
		DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(grafoTeoriasYSituaciones);
		Vertex nodoOrigen = grafoTeoriasYSituaciones.getNode(situacionActual.getId());
		Vertex nodoDestino = grafoTeoriasYSituaciones.getNode(situacionObjetivo.getId());
		dijkstra.execute(nodoOrigen);
		
		LinkedList<Vertex> caminoNodos = dijkstra.getPath(nodoDestino);
						
		if (caminoNodos != null)
			if (caminoNodos.size() >= 2) {
				
				ArrayList<Situacion> caminoSituaciones = new ArrayList<Situacion>();
				for (Vertex nodoSituacion: caminoNodos){
					for (Situacion situacion: this.situacionesConocidas) {
							
						if (situacion != null){
							int idNodo = Integer.parseInt(nodoSituacion.getId());
								
							if (idNodo == situacion.getId()) {
								caminoSituaciones.add(situacion);
								break;
							}
						}
					}
				}
				
				ArrayList<Teoria> caminoTeoriasAcumplir = this.obtenerCaminoTeoriasACumplir(caminoSituaciones);
				
				caminoSituaciones.remove(0); //quitamos la primera situacion del plan porque es la actual
				
				ArrayList<ACTIONS> accionesARealizar = new ArrayList<ACTIONS>();
				for (Teoria teoria: caminoTeoriasAcumplir)
					accionesARealizar.add(teoria.getAccionComoAction());
				
				Teoria ultimaTeoriaACumplir = caminoTeoriasAcumplir.get(caminoTeoriasAcumplir.size() - 1);
				double utilidadObjetivo = ultimaTeoriaACumplir.getU();
				
				this.plan.setSituacionesPlan(caminoSituaciones);
				this.plan.setAccionesPlan(accionesARealizar);
				this.plan.setUtilidadObjetivo(utilidadObjetivo);			
				
			}
		 	
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
		double utilidadMax = 0;
		Teoria teoriaConUtilidadMax = null;
		
		for (Teoria teoria : this.teorias) {
			if (teoria.getU() >= utilidadMax){
				utilidadMax = teoria.getU();
			}
		}
		
		if (utilidadMax == 0)
			return null;
		
		ArrayList<Teoria> teoriasConUtilidadMax = new ArrayList<Teoria>();
		for (Teoria teoria : this.teorias) {
			if (teoria.getU() == utilidadMax){
				teoriasConUtilidadMax.add(teoria);
			}
		}
		
		double maxPorcentajeExitos = 0;
		for (Teoria teoria : teoriasConUtilidadMax) {
			double porcentajeExitos = teoria.getP() / teoria.getK();
			if (porcentajeExitos >= maxPorcentajeExitos){
				maxPorcentajeExitos = porcentajeExitos;
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
	private ontology.Types.ACTIONS RealizarMovimientoRandom(StateObservationMulti stateObs, Situacion situacionActual){
		ArrayList<ACTIONS> accionesPosibles = stateObs.getAvailableActions(this.playerID);
		ArrayList<ACTIONS> accionesNoPosibles = new ArrayList<>();
		
		while (accionesNoPosibles.size() != 4) {
			ACTIONS accionRandom = ACTIONS.values()[new Random().nextInt(accionesPosibles.size())];
			
			while (accionesNoPosibles.contains(accionRandom)){
				accionRandom = ACTIONS.values()[new Random().nextInt(accionesPosibles.size())];
			}
			
			if (situacionActual == null){
				return accionRandom;
			}
			
			if (this.esAccionPerdedora(situacionActual, accionRandom)){
				accionesNoPosibles.add(accionRandom);
			} else {
				return accionRandom;
			}
		}
		
		return ACTIONS.ACTION_NIL;
	}
	
	boolean esAccionPerdedora(Situacion condicionInicial, ACTIONS accion) {
		for (Teoria teoriaConUtilidadNula: this.teoriasConUtilidadNula) {
			ACTIONS accionTeoriaUtilidadNula = teoriaConUtilidadNula.getAccionComoAction();
			
			if (accion.equals(accionTeoriaUtilidadNula)) {
				Situacion CITeoriaUtilidadNula = teoriaConUtilidadNula.getSitCondicionInicial();
				
				if (CITeoriaUtilidadNula.incluyeA(condicionInicial)) {
					return true;
				}
			}
		}
		return false;
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
			for (Teoria teoriaPrecargada: teoriasPrecargadas) {
				this.teoriasPrecargadas.add(teoriaPrecargada);
				if (teoriaPrecargada.getU() == 0) {
					this.teoriasConUtilidadNula.add(teoriaPrecargada);
				}
			}
		}
	}
	
	//Devuelve el path como String para poder leerlo como JSON.
	private String ObtenerPathDeTeorias(){
		try {
			return(new String(Files.readAllBytes(this.pathTeorias)));
		} catch (IOException e) {
		}
		
		return(null);
	}
	
	//Devuelve el path como String para poder leerlo como JSON.
	private String ObtenerPathDeTeoriasPrecargadas(){
		try {
			return(new String(Files.readAllBytes(this.pathTeoriasPrecargadas)));
		} catch (IOException e) {
		}
		
		return(null);
	}
	
	private char[][] obtenerCasillerosSitActual() {
		char[][] casillerosNivel = medioManager.getLevel();
		char[][] casillerosSituacion = new char[7][7];
		int colPersonaje = (int) (this.posPersonajeX);
		int filaPersonaje = (int)(this.posPersonajeY);
		int anchoMapa = medioManager.getLevelWidth();
		int altoMapa = medioManager.getLevelHeight();
		int filaSit = 0;
		for (int fila = filaPersonaje - 3; fila <= filaPersonaje + 3; fila++) {
			int colSit = 0;
			for (int col = colPersonaje - 3; col <= colPersonaje + 3; col++){
				Vector2d posicionEnMapa = new Vector2d(col,fila);				
				if (fila >= 0 && fila < altoMapa && col >= 0 && col < anchoMapa) {
					char simboloVisible = casillerosNivel[fila][col];
					if (simboloVisible == '1' && this.posicionesObjetivos.contains(posicionEnMapa)) {
						casillerosSituacion[filaSit][colSit] = 'X';
					} else {
						if (simboloVisible == 'A' && this.posicionesObjetivos.contains(posicionEnMapa)) {
							casillerosSituacion[filaSit][colSit] = 'Y';
						} else {
							if (simboloVisible == 'B' && this.posicionesObjetivos.contains(posicionEnMapa)) {
								casillerosSituacion[filaSit][colSit] = 'Z';
							} else {
								casillerosSituacion[filaSit][colSit] = simboloVisible;
							}							
						}
					}
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
