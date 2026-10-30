package co.edu.unbosque.service;

import java.sql.SQLException;
import java.util.List;

import co.edu.unbosque.dao.ReservaSalonDAO;
import co.edu.unbosque.model.ReservaSalon;

public class ReservaSalonService {
	
	private final ReservaSalonDAO reservaSalonDao = new ReservaSalonDAO();
	
	public List<ReservaSalon> listar() throws SQLException {
		return reservaSalonDao.listar();
	}
}
