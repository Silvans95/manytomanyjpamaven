package it.manytomanyjpamaven.test;

import java.text.SimpleDateFormat;
import java.util.Date;

import it.manytomanyjpamaven.dao.EntityManagerUtil;
import it.manytomanyjpamaven.model.Ruolo;
import it.manytomanyjpamaven.model.StatoUtente;
import it.manytomanyjpamaven.model.Utente;
import it.manytomanyjpamaven.service.MyServiceFactory;
import it.manytomanyjpamaven.service.RuoloService;
import it.manytomanyjpamaven.service.UtenteService;

public class ManyToManyTest {

	public static final StatoUtente DISABILITATO = null;

	public static void main(String[] args) {
		UtenteService utenteServiceInstance = MyServiceFactory.getUtenteServiceInstance();
		RuoloService ruoloServiceInstance = MyServiceFactory.getRuoloServiceInstance();

		// ora passo alle operazioni CRUD
		try {

			// inizializzo i ruoli sul db
			initRuoli(ruoloServiceInstance);

			System.out.println("In tabella Utente ci sono " + utenteServiceInstance.listAll().size() + " elementi.");

			testInserisciNuovoUtente(utenteServiceInstance);
			System.out.println("In tabella Utente ci sono " + utenteServiceInstance.listAll().size() + " elementi.");

			testCollegaUtenteARuoloEsistente(ruoloServiceInstance, utenteServiceInstance);
			System.out.println("In tabella Utente ci sono " + utenteServiceInstance.listAll().size() + " elementi.");

			testModificaStatoUtente(utenteServiceInstance);
			System.out.println("In tabella Utente ci sono " + utenteServiceInstance.listAll().size() + " elementi.");

			System.out.println(ruoloServiceInstance.listAll());

			testRimuoviRuoloDaUtente(ruoloServiceInstance, utenteServiceInstance);
			System.out.println("In tabella Utente ci sono " + utenteServiceInstance.listAll().size() + " elementi.");

			testRimozioneRuolo(ruoloServiceInstance, utenteServiceInstance);

			testCercaPerData(utenteServiceInstance);
		
			testCercaUtentiConPasswordMinore(utenteServiceInstance);

			testAdminDisabilitati(ruoloServiceInstance, utenteServiceInstance);

			testContaUtentiAdmin(ruoloServiceInstance, utenteServiceInstance);
			
			testDescrizioneDistintaRuoliUtenti(utenteServiceInstance);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// questa è necessaria per chiudere tutte le connessioni quindi rilasciare il
			// main
			EntityManagerUtil.shutdown();
		}

	}

	public static void initRuoli(RuoloService ruoloServiceInstance) throws Exception {
		if (ruoloServiceInstance.cercaPerDescrizioneECodice("Administrator", "ROLE_ADMIN") == null) {
			ruoloServiceInstance.inserisciNuovo(new Ruolo("Administrator", "ROLE_ADMIN"));
		}

		if (ruoloServiceInstance.cercaPerDescrizioneECodice("Classic User", "ROLE_CLASSIC_USER") == null) {
			ruoloServiceInstance.inserisciNuovo(new Ruolo("Classic User", "ROLE_CLASSIC_USER"));
		}
	}

	public static void testInserisciNuovoUtente(UtenteService utenteServiceInstance) throws Exception {
		System.out.println(".......testInserisciNuovoUtente inizio.............");

		Utente utenteNuovo = new Utente("pippo.rossi", "xxx", "pippo", "rossi", new Date());
		utenteServiceInstance.inserisciNuovo(utenteNuovo);
		if (utenteNuovo.getId() == null)
			throw new RuntimeException("testInserisciNuovoUtente fallito ");

		System.out.println(".......testInserisciNuovoUtente fine: PASSED.............");
	}

	public static void testCollegaUtenteARuoloEsistente(RuoloService ruoloServiceInstance,
			UtenteService utenteServiceInstance) throws Exception {
		System.out.println(".......testCollegaUtenteARuoloEsistente inizio.............");

		Ruolo ruoloEsistenteSuDb = ruoloServiceInstance.caricaSingoloElemento(1L);
		if (ruoloEsistenteSuDb == null)
			throw new RuntimeException("testCollegaUtenteARuoloEsistente fallito: ruolo inesistente ");

		// mi creo un utente inserendolo direttamente su db
		Utente utenteNuovo = new Utente("mario.bianchi", "JJJ", "mario", "bianchi", new Date());
		utenteServiceInstance.inserisciNuovo(utenteNuovo);
		if (utenteNuovo.getId() == null)
			throw new RuntimeException("testInserisciNuovoUtente fallito: utente non inserito ");

		utenteServiceInstance.aggiungiRuolo(utenteNuovo, ruoloEsistenteSuDb);
		// per fare il test ricarico interamente l'oggetto e la relazione
		Utente utenteReloaded = utenteServiceInstance.caricaUtenteSingoloConRuoli(utenteNuovo.getId());
		if (utenteReloaded.getRuoli().size() != 1)
			throw new RuntimeException("testInserisciNuovoUtente fallito: ruoli non aggiunti ");

		System.out.println(".......testCollegaUtenteARuoloEsistente fine: PASSED.............");
	}

	public static void testModificaStatoUtente(UtenteService utenteServiceInstance) throws Exception {
		System.out.println(".......testModificaStatoUtente inizio.............");

		// mi creo un utente inserendolo direttamente su db
		Utente utenteNuovo = new Utente("mario1.bianchi1", "JJJ", "mario1", "bianchi1", new Date());
		utenteServiceInstance.inserisciNuovo(utenteNuovo);
		if (utenteNuovo.getId() == null)
			throw new RuntimeException("testModificaStatoUtente fallito: utente non inserito ");

		// proviamo a passarlo nello stato ATTIVO ma salviamoci il vecchio stato
		StatoUtente vecchioStato = utenteNuovo.getStato();
		utenteNuovo.setStato(StatoUtente.ATTIVO);
		utenteServiceInstance.aggiorna(utenteNuovo);

		if (utenteNuovo.getStato().equals(vecchioStato))
			throw new RuntimeException("testModificaStatoUtente fallito: modifica non avvenuta correttamente ");

		System.out.println(".......testModificaStatoUtente fine: PASSED.............");
	}

// ########################################################################################################

	public static void testRimuoviRuoloDaUtente(RuoloService ruoloServiceInstance, UtenteService utenteServiceInstance)
			throws Exception {
		System.out.println(".......testRimuoviRuoloDaUtente inizio.............");

		// carico un ruolo e lo associo ad un nuovo utente
		Ruolo ruoloEsistenteSuDb = ruoloServiceInstance.cercaPerDescrizioneECodice("Administrator", "ROLE_ADMIN");
		if (ruoloEsistenteSuDb == null)
			throw new RuntimeException("testRimuoviRuoloDaUtente fallito: ruolo inesistente ");

		// mi creo un utente inserendolo direttamente su db
		Utente utenteNuovo = new Utente("aldo.manuzzi", "pwd@2", "aldo", "manuzzi", new Date());
		utenteServiceInstance.inserisciNuovo(utenteNuovo);
		if (utenteNuovo.getId() == null)
			throw new RuntimeException("testRimuoviRuoloDaUtente fallito: utente non inserito ");
		utenteServiceInstance.aggiungiRuolo(utenteNuovo, ruoloEsistenteSuDb);

		// ora ricarico il record e provo a disassociare il ruolo
		Utente utenteReloaded = utenteServiceInstance.caricaUtenteSingoloConRuoli(utenteNuovo.getId());
		boolean confermoRuoloPresente = false;
		for (Ruolo ruoloItem : utenteReloaded.getRuoli()) {
			if (ruoloItem.getCodice().equals(ruoloEsistenteSuDb.getCodice())) {
				confermoRuoloPresente = true;
				break;
			}
		}

		if (!confermoRuoloPresente)
			throw new RuntimeException("testRimuoviRuoloDaUtente fallito: utente e ruolo non associati ");

		// ora provo la rimozione vera e propria ma poi forzo il caricamento per fare un
		// confronto 'pulito'
		utenteServiceInstance.rimuoviRuoloDaUtente(utenteReloaded, ruoloEsistenteSuDb);
		utenteReloaded = utenteServiceInstance.caricaUtenteSingoloConRuoli(utenteNuovo.getId());
		if (!utenteReloaded.getRuoli().isEmpty())
			throw new RuntimeException("testRimuoviRuoloDaUtente fallito: ruolo ancora associato ");

		System.out.println(".......testRimuoviRuoloDaUtente fine: PASSED.............");
	}

//	###############################################################################################################
	public static void testRimozioneRuolo(RuoloService ruoloServiceInstance, UtenteService utenteServiceInstance)
			throws Exception {
		System.out.println(".......testRimozione inizio.............");

		Utente utenteInutile = new Utente("silvans95", "SPQR");
		utenteServiceInstance.inserisciNuovo(utenteInutile);
		if (utenteInutile.getId() == null)
			throw new RuntimeException("testInserisciNuovoUtente fallito: utente non inserito ");

		Ruolo nuovoRuolo = new Ruolo("ruolo user inutile", "ROLE_USELESS_USER");

		ruoloServiceInstance.inserisciNuovo(nuovoRuolo);

		utenteServiceInstance.aggiungiRuolo(utenteInutile, nuovoRuolo);
		// per fare il test ricarico interamente l'oggetto e la relazione
		Utente utenteReloaded = utenteServiceInstance.caricaUtenteSingoloConRuoli(utenteInutile.getId());
		if (utenteReloaded.getRuoli().size() != 1)
			throw new RuntimeException("testInserisciNuovoUtente fallito: ruoli non aggiunti ");

		utenteServiceInstance.rimuoviRuoloDaUtente(utenteInutile, nuovoRuolo);

		Long idUtenteInserito = utenteInutile.getId();
		utenteServiceInstance.rimuovi(utenteServiceInstance.caricaSingoloElemento(idUtenteInserito));
		// proviamo a vedere se è stato rimosso
		if (ruoloServiceInstance.caricaSingoloElemento(idUtenteInserito) != null)
			throw new RuntimeException("testRimozioneUtente fallito: record utente non cancellato ");

		Long idRuoloInserito = nuovoRuolo.getId();
		ruoloServiceInstance.rimuovi(ruoloServiceInstance.caricaSingoloElemento(idRuoloInserito));
		// proviamo a vedere se è stato rimosso
		if (ruoloServiceInstance.caricaSingoloElemento(idRuoloInserito) != null)
			throw new RuntimeException("testRimozioneInserito fallito: record ruolo non cancellato ");
		System.out.println(".......testRimozione fine: PASSED.............");
	}

//	##########################################################################################################################
//	##########################################################################################################################

	public static void testCercaPerData(UtenteService utenteServiceInstance) throws Exception {
		System.out.println(".......testCercaPerData inizio.............");

		String dataDaControllare = "2021-06-02";

		Date dataCreatedNum = new SimpleDateFormat("yyyyy-MM-dd").parse(dataDaControllare);

		Utente utenteInutile = new Utente("leon", "goa", "leonardo", "iappelli", dataCreatedNum);

		utenteServiceInstance.cercaTuttiUtentiData();

		utenteServiceInstance.inserisciNuovo(utenteInutile);

		if (utenteInutile.getId() == null)
			throw new RuntimeException("testInserisciNuovoUtente fallito: utente non inserito ");

		utenteServiceInstance.rimuovi(utenteInutile);

		System.out.println(utenteServiceInstance.cercaTuttiUtentiData().size());
		System.out.println(".......testCercaPerData fine: PASSED.............");
	}

//	##########################################################################################################################
//	##########################################################################################################################

	public static void testContaUtentiAdmin(RuoloService ruoloServiceInstance, UtenteService utenteServiceInstance)
			throws Exception {
		System.out.println(".......testContaAdmin inizio.............");

		Ruolo ruoloEsistenteSuDb = ruoloServiceInstance.cercaPerDescrizioneECodice("Administrator", "ROLE_ADMIN");
		if (ruoloEsistenteSuDb == null)
			throw new RuntimeException("testRimuoviRuoloDaUtente fallito: ruolo inesistente ");

		Utente utenteNuovo = new Utente("s.p", "adas@2", "gas", "iabf", new Date());

		utenteServiceInstance.inserisciNuovo(utenteNuovo);

		utenteServiceInstance.aggiungiRuolo(utenteNuovo, ruoloEsistenteSuDb);

		System.out.println(utenteServiceInstance.contaUtentiAdmin());

		System.out.println(".......testContaAdmin fine: PASSED.............");
	}

//	##########################################################################################################################
//	##########################################################################################################################

	public static void testCercaUtentiConPasswordMinore(UtenteService utenteServiceInstance) throws Exception {
		System.out.println(".......testCercaUtentiConPasswordMinore inizio.............");

		Utente utenteInutile = new Utente("leonardo", "iappelli");

		utenteServiceInstance.inserisciNuovo(utenteInutile);

		utenteServiceInstance.cercaUtentiConPasswordMinore();

		if (utenteInutile.getId() == null)
			throw new RuntimeException("testInserisciNuovoUtente fallito: utente non inserito ");

		utenteServiceInstance.rimuovi(utenteInutile);

		System.out.println(utenteServiceInstance.cercaUtentiConPasswordMinore().size());
		System.out.println(".......testCercaUtentiConPasswordMinore fine: PASSED.............");

	}

	// ##########################################################################################################################
	// ##########################################################################################################################

	public static void testAdminDisabilitati(RuoloService ruoloServiceInstance, UtenteService utenteServiceInstance)
			throws Exception {
		System.out.println(".......testAdminDisabilitati inizio.............");

		Ruolo ruoloEsistenteSuDb = ruoloServiceInstance.cercaPerDescrizioneECodice("Administrator", "ROLE_ADMIN");
		if (ruoloEsistenteSuDb == null)
			throw new RuntimeException("testRimuoviRuoloDaUtente fallito: ruolo inesistente ");

		Utente utenteNuovo = new Utente("s.p", "adas@2", "gas", "iabf", new Date());

		utenteNuovo.setStato(StatoUtente.DISABILITATO);

		utenteServiceInstance.inserisciNuovo(utenteNuovo);

		utenteServiceInstance.aggiungiRuolo(utenteNuovo, ruoloEsistenteSuDb);

		System.out.println(utenteServiceInstance.adminDisabilitati());

		System.out.println(".......testAdminDisabilitati fine: PASSED.............");

	}

	// ##########################################################################################################################
	// ##########################################################################################################################

	public static void testDescrizioneDistintaRuoliUtenti(UtenteService utenteServiceInstance) throws Exception {
		System.out.println(".......testDescrizioneDistintaRuoliUtenti inizio.............");

		System.out.println(utenteServiceInstance.cercaUtentiConDescrizioneRuoli().size());

		System.out.println(".......testDescrizioneDistintaRuoliUtenti finito.............");
	}
	
}
