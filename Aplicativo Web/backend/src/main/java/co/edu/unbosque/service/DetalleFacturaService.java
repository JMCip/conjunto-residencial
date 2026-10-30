package co.edu.unbosque.service;

import java.sql.SQLException;
import java.util.List;

import co.edu.unbosque.dao.DetalleFacturaDAO;
import co.edu.unbosque.model.DetalleFactura;

public class DetalleFacturaService {
	
	private final DetalleFacturaDAO detalleFacturaDao = new DetalleFacturaDAO();
	
	public List<DetalleFactura> listar() throws SQLException {
		return detalleFacturaDao.listar();
	}
}
