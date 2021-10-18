package it.manytomanyjpamaven.dao;

public class MyDAOFactory {

	// rendiamo questo factory SINGLETON
	private static UtenteDAO UTENTE_DAO_INSTANCE = null;
	private static RuoloDAO RUOLO_DAO_INSTANCE = null;

	public static UtenteDAO getUtenteDAOInstance() {
		if (UTENTE_DAO_INSTANCE == null)
			UTENTE_DAO_INSTANCE = new UtenteDAOImpl();
		return UTENTE_DAO_INSTANCE;
	}

	public static RuoloDAO getRuoloDAOInstance() {
		if (RUOLO_DAO_INSTANCE == null)
			RUOLO_DAO_INSTANCE = new RuoloDAOImpl();
		return RUOLO_DAO_INSTANCE;
	}

}
