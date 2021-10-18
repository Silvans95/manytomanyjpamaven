package it.manytomanyjpamaven.dao;

import it.manytomanyjpamaven.model.Ruolo;

public interface RuoloDAO extends IBaseDAO<Ruolo> {
	
	public Ruolo findByDescrizioneAndCodice(String descrizione, String codice) throws Exception;

}
