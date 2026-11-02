package co.edu.unbosque.service;

import java.sql.SQLException;
import java.util.List;
import java.time.Year;

import co.edu.unbosque.exception.DatoInvalidoException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;

import co.edu.unbosque.dao.VehiculoDAO;
import co.edu.unbosque.model.Vehiculo;

public class VehiculoService {
	
	private final VehiculoDAO vehiculoDao = new VehiculoDAO();
	
	public List<Vehiculo> listar() throws SQLException {
		return vehiculoDao.listar();
	}
	
	public Vehiculo consultar(String placa) throws SQLException, RecursoNoEncontradoException {
		Vehiculo v = vehiculoDao.buscar(placa.toUpperCase());
		if (v == null) {
			throw new RecursoNoEncontradoException("Vehículo no encontrado");
		}
		return v;
	}

	public void crear(Vehiculo v) throws SQLException, DatoInvalidoException {
		if (v.getPlaca() == null || !v.getPlaca().matches("[A-Za-z0-9]{5,7}")) {
			throw new DatoInvalidoException("placa debe tener entre 5 y 7 caracteres alfanuméricos");
		}
		v.setPlaca(v.getPlaca().toUpperCase());
		validarDatos(v);
		vehiculoDao.insertar(v);
	}

	public void actualizar(Vehiculo v) throws SQLException, DatoInvalidoException, RecursoNoEncontradoException {
		v.setPlaca(v.getPlaca().toUpperCase());
		validarDatos(v);
		if (!vehiculoDao.actualizar(v)) {
			throw new RecursoNoEncontradoException("Vehículo no encontrado");
		}
	}

	public void eliminar(String placa) throws SQLException, RecursoNoEncontradoException {
		if (!vehiculoDao.eliminar(placa.toUpperCase())) {
			throw new RecursoNoEncontradoException("Vehículo no encontrado");
		}
	}

	private void validarDatos(Vehiculo v) throws DatoInvalidoException {
		if (v.getTipo() == null || v.getTipo().isBlank()
				|| v.getPersonaTipoDocumento() == null || v.getPersonaTipoDocumento().isBlank()
				|| v.getPersonaNumeroDocumento() == null || v.getPersonaNumeroDocumento().isBlank()) {
			throw new DatoInvalidoException("tipo, personaTipoDocumento y personaNumeroDocumento son obligatorios");
		}
		if (v.getModelo() != null && (v.getModelo() < 1900 || v.getModelo() > Year.now().getValue() + 1)) {
			throw new DatoInvalidoException("modelo debe ser un año entre 1900 y " + (Year.now().getValue() + 1));
		}
	}
	
}
