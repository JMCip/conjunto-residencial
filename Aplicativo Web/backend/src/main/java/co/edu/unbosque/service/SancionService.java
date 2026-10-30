package co.edu.unbosque.service;

import java.sql.SQLException;
import java.util.List;

import co.edu.unbosque.dao.SancionDAO;
import co.edu.unbosque.model.Sancion;

public class SancionService {
	
	private final SancionDAO sancionDao = new SancionDAO();
	
	public List<Sancion> listar() throws SQLException {
		return sancionDao.listar();
	}
}
