package co.edu.unbosque.service;

import java.sql.SQLException;
import java.util.List;

import co.edu.unbosque.dao.TelefonoDAO;
import co.edu.unbosque.model.Telefono;

public class TelefonoService {
	
	private final TelefonoDAO telefonoDao = new TelefonoDAO();
	
	public List<Telefono> listar() throws SQLException {
		return telefonoDao.listar();
	}
}
