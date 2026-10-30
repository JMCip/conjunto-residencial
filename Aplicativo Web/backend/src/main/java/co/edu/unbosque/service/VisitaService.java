package co.edu.unbosque.service;

import java.sql.SQLException;
import java.util.List;

import co.edu.unbosque.dao.VisitaDAO;
import co.edu.unbosque.model.Visita;

public class VisitaService {
	
	private final VisitaDAO visitaDao = new VisitaDAO();
	
	public List<Visita> listar() throws SQLException {
		return visitaDao.listar();
	}
}
