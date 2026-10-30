package co.edu.unbosque.service;

import java.sql.SQLException;
import java.util.List;

import co.edu.unbosque.dao.ResidenciaDAO;
import co.edu.unbosque.model.Residencia;

public class ResidenciaService {
	
	private final ResidenciaDAO residenciaDao = new ResidenciaDAO();
	
	public List<Residencia> listar() throws SQLException {
		return residenciaDao.listar();
	}
}
