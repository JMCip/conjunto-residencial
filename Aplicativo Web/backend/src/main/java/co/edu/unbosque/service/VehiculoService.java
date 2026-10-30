package co.edu.unbosque.service;

import java.sql.SQLException;
import java.util.List;

import co.edu.unbosque.dao.VehiculoDAO;
import co.edu.unbosque.model.Vehiculo;

public class VehiculoService {
	
	private final VehiculoDAO vehiculoDao = new VehiculoDAO();
	
	public List<Vehiculo> listar() throws SQLException {
		return vehiculoDao.listar();
	}
}
