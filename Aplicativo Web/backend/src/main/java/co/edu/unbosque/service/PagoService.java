package co.edu.unbosque.service;

import java.sql.SQLException;
import java.util.List;

import co.edu.unbosque.dao.PagoDAO;
import co.edu.unbosque.model.Pago;

public class PagoService {
	
	private final PagoDAO pagoDao = new PagoDAO();
	
	public List<Pago> listar() throws SQLException {
		return pagoDao.listar();
	}
}
