package fiubaceldas.grupo01;

public class GameController {
	private static GameController instance = null;
	private String sitInicialPlayerUno = null;
	private String sitInicialPlayerDos = null;
	private String efPredichosPlayerUno = null;
	private String efPredichosPlayerDos = null;
	
	protected GameController() {
	}
	
	public static GameController getInstance() {
		if(instance == null) {
			instance = new GameController();
		}
		
		return instance;
	}

	public String getSitInicialPlayerUno() {
		return sitInicialPlayerUno;
	}

	public void setSitInicialPlayerUno(String sitInicialPlayerUno) {
		this.sitInicialPlayerUno = sitInicialPlayerUno;
	}

	public String getSitInicialPlayerDos() {
		return sitInicialPlayerDos;
	}

	public void setSitInicialPlayerDos(String sitInicialPlayerDos) {
		this.sitInicialPlayerDos = sitInicialPlayerDos;
	}

	public String getEfPredichosPlayerUno() {
		return efPredichosPlayerUno;
	}

	public void setEfPredichosPlayerUno(String efPredichosPlayerUno) {
		this.efPredichosPlayerUno = efPredichosPlayerUno;
	}

	public String getEfPredichosPlayerDos() {
		return efPredichosPlayerDos;
	}

	public void setEfPredichosPlayerDos(String efPredichosPlayerDos) {
		this.efPredichosPlayerDos = efPredichosPlayerDos;
	}
}
