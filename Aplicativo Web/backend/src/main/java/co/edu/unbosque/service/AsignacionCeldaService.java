package co.edu.unbosque.service;

import java.sql.SQLException;
import java.util.List;

import co.edu.unbosque.dao.AsignacionCeldaDAO;
import co.edu.unbosque.model.AsignacionCelda;

public class AsignacionCeldaService {
	
	private final AsignacionCeldaDAO asignacionCeldaDao = new AsignacionCeldaDAO();
	
	public List<AsignacionCelda> listar() throws SQLException {
		return asignacionCeldaDao.listar();
	}
}
