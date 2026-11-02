package co.edu.unbosque.service;

import java.sql.SQLException;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import co.edu.unbosque.exception.ConflictoException;
import co.edu.unbosque.exception.DatoInvalidoException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;
import co.edu.unbosque.dao.ParqueoDAO;
import co.edu.unbosque.model.Parqueo;

public class ParqueoService {
	
	private final ParqueoDAO parqueoDao = new ParqueoDAO();
	
	public List<Parqueo> listar() throws SQLException {
		return parqueoDao.listar();
	}
	
	public Parqueo consultar(int id) throws SQLException, RecursoNoEncontradoException {
		Parqueo p = parqueoDao.buscar(id);
		if (p == null) {
			throw new RecursoNoEncontradoException("Parqueo no encontrado");
		}
		return p;
	}

	public int crear(Parqueo p) throws SQLException, DatoInvalidoException, ConflictoException {
		if (p.getIdCelda() <= 0 || p.getVehiculoPlaca() == null || p.getVehiculoPlaca().isBlank()) {
			throw new DatoInvalidoException("vehiculoPlaca e idCelda son obligatorios");
		}
		p.setVehiculoPlaca(p.getVehiculoPlaca().toUpperCase());
		validarFechas(p);
		if (p.getFechaHoraSalida() == null) {
			if (parqueoDao.celdaConParqueoAbierto(p.getIdCelda(), 0)) {
				throw new ConflictoException("La celda ya tiene un parqueo abierto");
			}
			if (parqueoDao.vehiculoConParqueoAbierto(p.getVehiculoPlaca(), 0)) {
				throw new ConflictoException("El vehículo ya tiene un parqueo abierto");
			}
		}
		return parqueoDao.insertar(p);
	}

	public void actualizar(Parqueo p)
			throws SQLException, DatoInvalidoException, RecursoNoEncontradoException, ConflictoException {
		Parqueo actual = consultar(p.getIdParqueo());
		validarFechas(p);
		if (p.getFechaHoraSalida() == null) {
			if (parqueoDao.celdaConParqueoAbierto(actual.getIdCelda(), p.getIdParqueo())) {
				throw new ConflictoException("La celda ya tiene un parqueo abierto");
			}
			if (parqueoDao.vehiculoConParqueoAbierto(actual.getVehiculoPlaca(), p.getIdParqueo())) {
				throw new ConflictoException("El vehículo ya tiene un parqueo abierto");
			}
		}
		if (!parqueoDao.actualizar(p)) {
			throw new RecursoNoEncontradoException("Parqueo no encontrado");
		}
	}

	public void eliminar(int id) throws SQLException, RecursoNoEncontradoException {
		if (!parqueoDao.eliminar(id)) {
			throw new RecursoNoEncontradoException("Parqueo no encontrado");
		}
	}

	private void validarFechas(Parqueo p) throws DatoInvalidoException {
		if (p.getFechaHoraSalida() != null && p.getFechaHoraSalida().isBlank()) {
			p.setFechaHoraSalida(null);
		}
		if (p.getFechaHoraEntrada() == null || p.getFechaHoraEntrada().isBlank()) {
			throw new DatoInvalidoException("fechaHoraEntrada es obligatoria (formato AAAA-MM-DDTHH:MM:SS)");
		}
		try {
			LocalDateTime entrada = LocalDateTime.parse(p.getFechaHoraEntrada());
			if (p.getFechaHoraSalida() != null && LocalDateTime.parse(p.getFechaHoraSalida()).isBefore(entrada)) {
				throw new DatoInvalidoException("fechaHoraSalida no puede ser anterior a fechaHoraEntrada");
			}
		} catch (DateTimeParseException e) {
			throw new DatoInvalidoException("Las fechas deben tener el formato AAAA-MM-DDTHH:MM:SS");
		}
	}
	
}
