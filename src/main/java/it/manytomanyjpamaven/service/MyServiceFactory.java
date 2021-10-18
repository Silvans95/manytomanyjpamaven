package it.manytomanyjpamaven.service;

import it.manytomanyjpamaven.dao.MyDAOFactory;

public class MyServiceFactory {

	// rendiamo questo factory SINGLETON
	private static UtenteService UTENTE_SERVICE_INSTANCE;
	private static RuoloService RUOLO_SERVICE_INSTANCE;

	public static UtenteService getUtenteServiceInstance() {
		if (UTENTE_SERVICE_INSTANCE == null)
			UTENTE_SERVICE_INSTANCE = new UtenteServiceImpl();

		UTENTE_SERVICE_INSTANCE.setUtenteDAO(MyDAOFactory.getUtenteDAOInstance());
		return UTENTE_SERVICE_INSTANCE;
	}

	public static RuoloService getRuoloServiceInstance() {
		if (RUOLO_SERVICE_INSTANCE == null)
			RUOLO_SERVICE_INSTANCE = new RuoloServiceImpl();



		RUOLO_SERVICE_INSTANCE.setRuoloDAO(MyDAOFactory.getRuoloDAOInstance());
		RUOLO_SERVICE_INSTANCE.setUtenteDAO(MyDAOFactory.getUtenteDAOInstance());
		return RUOLO_SERVICE_INSTANCE;
	}

}
