package co.edu.unbosque.service;

import java.sql.SQLException;
import java.util.List;

import co.edu.unbosque.dao.ArriendoDAO;
import co.edu.unbosque.model.Arriendo;

public class ArriendoService {
	
	private final ArriendoDAO arriendoDao = new ArriendoDAO();
	
	public List<Arriendo> listar() throws SQLException {
		return arriendoDao.listar();
	}
}
