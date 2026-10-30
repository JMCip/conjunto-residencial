package co.edu.unbosque.service;

import java.sql.SQLException;
import java.util.List;

import co.edu.unbosque.dao.RolDAO;
import co.edu.unbosque.model.Persona;

public class RolService {
	
	private final RolDAO rolDao = new RolDAO();
	
	public List<Persona> listarPropietarios() throws SQLException {
		return rolDao.listarPropietarios();
	}
	
	public List<Persona> listarArrendatarios() throws SQLException {
		return rolDao.listarArrendatarios();
	}
	
	public List<Persona> listarResidentes() throws SQLException {
		return rolDao.listarResidentes();
	}
	
	public List<Persona> listarVisitantes() throws SQLException {
		return rolDao.listarVisitantes();
	}
}
