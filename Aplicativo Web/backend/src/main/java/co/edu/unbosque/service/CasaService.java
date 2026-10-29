package co.edu.unbosque.service;

import co.edu.unbosque.dao.CasaDao;
import co.edu.unbosque.model.Casa;

import java.sql.SQLException;
import java.util.List;

public class CasaService {
	
	private final CasaDao casaDao = new CasaDao();
	
	public List<Casa> listar() throws SQLException {
		return casaDao.listar();	
	}
}
