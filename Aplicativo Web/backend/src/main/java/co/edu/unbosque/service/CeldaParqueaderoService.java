package co.edu.unbosque.service;

import java.sql.SQLException;
import java.util.List;

import co.edu.unbosque.dao.CeldaParqueaderoDAO;
import co.edu.unbosque.model.CeldaParqueadero;

public class CeldaParqueaderoService {
	
	private final CeldaParqueaderoDAO celdaParqueaderoDao = new CeldaParqueaderoDAO();
	
	public List<CeldaParqueadero> listar() throws SQLException {
		return celdaParqueaderoDao.listar();
	}
}
