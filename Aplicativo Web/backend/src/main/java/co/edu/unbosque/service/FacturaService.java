package co.edu.unbosque.service;

import java.sql.SQLException;
import java.util.List;

import co.edu.unbosque.dao.FacturaDAO;
import co.edu.unbosque.model.Factura;

public class FacturaService {
	
	private final FacturaDAO facturaDao = new FacturaDAO();
	
	public List<Factura> listar() throws SQLException {
		return facturaDao.listar();
	}
}
