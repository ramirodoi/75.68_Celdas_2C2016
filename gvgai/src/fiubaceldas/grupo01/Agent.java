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
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import core.game.StateObservationMulti;
import core.player.AbstractMultiPlayer;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

public class Agent extends AbstractMultiPlayer {
	private Path path = FileSystems.getDefault().getPath(System.getProperty("user.dir") + "\\src\\fiubaceldas\\grupo01\\Teorias");
	private Perception medioManager;
	private Gson gsonManager;
	private int playerID;
	
	public Agent(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer, int playerID) {
		this.medioManager =  new Perception(stateObs);
		this.gsonManager = new GsonBuilder().setPrettyPrinting().create();
		this.playerID = playerID;
	}
	
	@Override
	public ACTIONS act(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer){	
		this.medioManager =  new Perception(stateObs);
		ArrayList<Teoria> teorias = this.ObtenerTeorias();
		
		if (teorias != null){
			for (Teoria teoria : teorias) {
				
				if (teoria != null){
					//TODO: calcular la utilidad para que siempre agarre la mejor y no la primera de la lista.
					if (teoria.getU() > 0){
						if (teoria.getCondicionInicial().equals(this.medioManager.toString())){
							//TODO: ver cómo se hace para comparar los efectos predichos.
							return (teoria.getAccionComoAction());
						}
					}
				} else {
					return (this.RealizarMovimientoRandom(stateObs));
				}
			}
		} else {			
			return (this.RealizarMovimientoRandom(stateObs));
		}
		
		//TODO: Guardar las teorías aprendidas de esta forma...
		/*
		ArrayList<Teoria> teorias2 = new ArrayList<>();
		teorias2.add(new Teoria("inicio", "derecha", "final", 123, 123, 123));
		teorias2.add(new Teoria("inicio", "izquierda", "final", 321, 321, 321));
		this.GuardarTeorias(teorias2);
	 	*/
		
		return (ACTIONS.ACTION_NIL);
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
