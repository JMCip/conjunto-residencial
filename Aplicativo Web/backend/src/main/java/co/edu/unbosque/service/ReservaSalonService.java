package co.edu.unbosque.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import co.edu.unbosque.dao.ReservaSalonDAO;
import co.edu.unbosque.exception.ConflictoException;
import co.edu.unbosque.exception.DatoInvalidoException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;
import co.edu.unbosque.model.ReservaSalon;

public class ReservaSalonService {

	
	private static final Set<String> ESTADOS_PUT =
			Set.of("CANCELADA", "FINALIZADA", "DEPOSITO_RETENIDO");

	private final ReservaSalonDAO reservaSalonDao = new ReservaSalonDAO();

	public List<ReservaSalon> listar() throws SQLException {
		return reservaSalonDao.listar();
	}

	public int crear(ReservaSalon r) throws SQLException, DatoInvalidoException,
			RecursoNoEncontradoException, ConflictoException {
		validarCampos(r);
		LocalDateTime inicio = parsear(r.getHoraInicio());
		LocalDateTime fin = parsear(r.getHoraFinal());
		validarRango(inicio, fin);

		if (inicio.isBefore(LocalDateTime.now())) {
			throw new DatoInvalidoException("La reserva debe ser en una fecha y hora futura");
		}

		if (!reservaSalonDao.existeCasa(r.getCasaNumero(), r.getCasaManzana())) {
			throw new RecursoNoEncontradoException("Casa no encontrada");
		}
		if (reservaSalonDao.casaEnMora(r.getCasaNumero(), r.getCasaManzana())) {
			throw new ConflictoException("La casa tiene cartera en mora y no puede reservar el salón");
		}
		if (reservaSalonDao.casaConSancionPendiente(r.getCasaNumero(), r.getCasaManzana())) {
			throw new ConflictoException("La casa tiene sanciones pendientes y no puede reservar el salón");
		}
		if (reservaSalonDao.haySolapamiento(inicio, fin, 0)) {
			throw new ConflictoException("Ya existe una reserva en esa franja de horario");
		}

		r.setEstado("CONFIRMADA");
		return reservaSalonDao.insertar(r, inicio, fin);
	}

	public void actualizarEstado(int id, String nuevoEstado) throws SQLException, DatoInvalidoException,
			RecursoNoEncontradoException, ConflictoException {
		if (nuevoEstado == null || !ESTADOS_PUT.contains(nuevoEstado)) {
			throw new DatoInvalidoException(
					"estado debe ser CANCELADA, FINALIZADA o DEPOSITO_RETENIDO");
		}

		ReservaSalon actual = reservaSalonDao.buscarPorId(id);
		if (actual == null) {
			throw new RecursoNoEncontradoException("Reserva no encontrada");
		}
		if (!"CONFIRMADA".equals(actual.getEstado())) {
			throw new ConflictoException(
					"Solo se puede cambiar el estado de una reserva CONFIRMADA (estado actual: "
							+ actual.getEstado() + ")");
		}
		if (!"CANCELADA".equals(nuevoEstado)
				&& LocalDateTime.parse(actual.getHoraInicio().replace(' ', 'T')).isAfter(LocalDateTime.now())) {
			throw new ConflictoException(
					"No se puede marcar " + nuevoEstado + " una reserva que aún no ha comenzado");
		}

		if (!reservaSalonDao.actualizarEstado(id, nuevoEstado)) {
			throw new RecursoNoEncontradoException("Reserva no encontrada");
		}
	}

	public void eliminar(int id) throws SQLException, RecursoNoEncontradoException, ConflictoException {
		ReservaSalon actual = reservaSalonDao.buscarPorId(id);
		if (actual == null) {
			throw new RecursoNoEncontradoException("Reserva no encontrada");
		}
		if (!"CANCELADA".equals(actual.getEstado())) {
			throw new ConflictoException(
					"Solo se puede eliminar una reserva CANCELADA (estado actual: "
							+ actual.getEstado() + "). Cancélala primero.");
		}
		if (!reservaSalonDao.eliminar(id)) {
			throw new RecursoNoEncontradoException("Reserva no encontrada");
		}
	}

	private void validarCampos(ReservaSalon r) throws DatoInvalidoException {
		if (r.getHoraInicio() == null || r.getHoraFinal() == null
				|| r.getValorDeposito() == null || r.getValorPorHora() == null) {
			throw new DatoInvalidoException(
					"horaInicio, horaFinal, valorDeposito y valorPorHora son obligatorios");
		}
		if (r.getValorDeposito().compareTo(BigDecimal.ZERO) < 0
				|| r.getValorPorHora().compareTo(BigDecimal.ZERO) <= 0) {
			throw new DatoInvalidoException(
					"valorDeposito no puede ser negativo y valorPorHora debe ser mayor que 0");
		}
	}

	private LocalDateTime parsear(String texto) throws DatoInvalidoException {
		try {
			return LocalDateTime.parse(texto.replace(' ', 'T'));
		} catch (Exception e) {
			throw new DatoInvalidoException(
					"Formato de fecha y hora: yyyy-MM-ddTHH:mm:ss, por ejemplo 2026-12-05T14:00:00");
		}
	}

	private void validarRango(LocalDateTime inicio, LocalDateTime fin) throws DatoInvalidoException {
		if (!fin.isAfter(inicio)) {
			throw new DatoInvalidoException("horaFinal debe ser posterior a horaInicio");
		}
	}
}