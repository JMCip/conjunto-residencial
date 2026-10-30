package co.edu.unbosque.service;

import java.sql.SQLException;
import java.util.List;

import co.edu.unbosque.dao.PosesionDAO;
import co.edu.unbosque.model.Posesion;

public class PosesionService {
	
	private final PosesionDAO posesionDao = new PosesionDAO();
	
	public List<Posesion> listar() throws SQLException {
		return posesionDao.listar();
	}
}
