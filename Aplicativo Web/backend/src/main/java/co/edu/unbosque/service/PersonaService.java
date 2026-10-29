package co.edu.unbosque.service;



import java.sql.SQLException;
import java.util.List;

import co.edu.unbosque.dao.PersonaDAO;
import co.edu.unbosque.model.Persona;

public class PersonaService {
	
	private final PersonaDAO pDao = new PersonaDAO();
		
	public List<Persona> listar () throws SQLException {
		return pDao.listar();
	}
}
