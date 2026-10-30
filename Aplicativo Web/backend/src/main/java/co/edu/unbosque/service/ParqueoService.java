package co.edu.unbosque.service;

import java.sql.SQLException;
import java.util.List;

import co.edu.unbosque.dao.ParqueoDAO;
import co.edu.unbosque.model.Parqueo;

public class ParqueoService {
	
	private final ParqueoDAO parqueoDao = new ParqueoDAO();
	
	public List<Parqueo> listar() throws SQLException {
		return parqueoDao.listar();
	}
}
