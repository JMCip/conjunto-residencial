package co.edu.unbosque.service;

import java.sql.SQLException;
import java.util.List;

import co.edu.unbosque.dao.ConceptoCobroDAO;
import co.edu.unbosque.model.ConceptoCobro;

public class ConceptoCobroService {
	
	private final ConceptoCobroDAO conceptoCobroDao = new ConceptoCobroDAO();
	
	public List<ConceptoCobro> listar() throws SQLException {
		return conceptoCobroDao.listar();
	}
}
