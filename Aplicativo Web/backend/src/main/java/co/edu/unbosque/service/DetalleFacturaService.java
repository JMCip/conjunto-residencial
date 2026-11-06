package co.edu.unbosque.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;

import co.edu.unbosque.dao.DetalleFacturaDAO;
import co.edu.unbosque.dao.DetalleFacturaDAO.Origen;
import co.edu.unbosque.dao.FacturaDAO;
import co.edu.unbosque.exception.ConflictoException;
import co.edu.unbosque.exception.DatoInvalidoException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;
import co.edu.unbosque.model.ConceptoCobro;
import co.edu.unbosque.model.DetalleFactura;
import co.edu.unbosque.model.Factura;

/**
 * Lógica de negocio de las líneas de factura.
 * <p>
 * Valida los datos recibidos y las reglas del conjunto antes de llamar al DAO, y comunica los
 * errores con las excepciones del paquete {@code exception}.
 */
public class DetalleFacturaService {

	/** Nombre del concepto con el que se cobran las sanciones. */
	private static final String CONCEPTO_SANCION = "Sanción";
	/** Nombre del concepto con el que se cobran las reservas del salón comunal. */
	private static final String CONCEPTO_RESERVA = "Salón comunal";
	/** Nombre del concepto con el que se cobran las celdas adicionales. */
	private static final String CONCEPTO_PARQUEADERO = "Parqueadero adicional";
	/** Nombre del concepto de administración (a lo sumo una línea por factura). */
	private static final String CONCEPTO_ADMINISTRACION = "Administración";

	/** Valor máximo que admite la columna {@code NUMERIC(12,2)}. */
	private static final BigDecimal MAXIMO = new BigDecimal("9999999999.99");

	/** Acceso a datos de las líneas de factura. */
	private final DetalleFacturaDAO detalleFacturaDao = new DetalleFacturaDAO();
	/** Acceso a datos de las facturas. */
	private final FacturaDAO facturaDao = new FacturaDAO();

	/**
	 * Lista las líneas de factura registradas.
	 *
	 * @return lista de líneas de factura; vacía si no hay registros
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public List<DetalleFactura> listar() throws SQLException {
		return detalleFacturaDao.listar();
	}

	/**
	 * Agrega una línea a una factura abierta. Si la línea se liga a una sanción, una reserva o una
	 * asignación, exige el concepto correspondiente, que el origen sea de la misma casa y que no se
	 * haya cobrado antes, y toma el valor del origen. Si no se liga a nada, usa el valor enviado o el
	 * valor base del concepto; la administración solo se cobra una vez por factura.
	 *
	 * @param d línea que se va a crear
	 * @return número de línea asignado
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws DatoInvalidoException si la línea se liga a varios orígenes, usa un concepto que no
	 *         corresponde o el valor no es válido
	 * @throws RecursoNoEncontradoException si no existe la factura, el concepto o el origen
	 * @throws ConflictoException si la factura no está abierta o el origen no se puede cobrar
	 */
	public int crear(DetalleFactura d) throws SQLException, DatoInvalidoException,
			RecursoNoEncontradoException, ConflictoException {
		Factura factura = obtenerFacturaAbierta(d.getNumeroFactura());

		ConceptoCobro concepto = detalleFacturaDao.buscarConcepto(d.getIdConcepto());
		if (concepto == null) {
			throw new RecursoNoEncontradoException("Concepto de cobro no encontrado");
		}

		int origenes = (d.getIdSancion() != null ? 1 : 0)
				+ (d.getIdReserva() != null ? 1 : 0)
				+ (d.getIdAsignacion() != null ? 1 : 0);
		if (origenes > 1) {
			throw new DatoInvalidoException(
					"La línea solo puede ligarse a una sanción, una reserva o una asignación, no a varias");
		}

		BigDecimal valor;
		if (d.getIdSancion() != null) {
			exigirConcepto(concepto, CONCEPTO_SANCION);
			Origen s = detalleFacturaDao.buscarSancion(d.getIdSancion());
			if (s == null) {
				throw new RecursoNoEncontradoException("Sanción no encontrada");
			}
			exigirMismaCasa(factura, s, "La sanción");
			if (!"CONFIRMADA".equals(s.estado())) {
				throw new ConflictoException(
						"Solo se cobra una sanción CONFIRMADA (estado actual: " + s.estado() + ")");
			}
			if (s.valor() == null || s.valor().signum() <= 0) {
				throw new ConflictoException("La sanción no tiene una multa para cobrar");
			}
			if (detalleFacturaDao.sancionFacturada(d.getIdSancion())) {
				throw new ConflictoException("La sanción ya fue cobrada en otra factura");
			}
			valor = s.valor();

		} else if (d.getIdReserva() != null) {
			exigirConcepto(concepto, CONCEPTO_RESERVA);
			Origen r = detalleFacturaDao.buscarReserva(d.getIdReserva());
			if (r == null) {
				throw new RecursoNoEncontradoException("Reserva no encontrada");
			}
			exigirMismaCasa(factura, r, "La reserva");
			if ("CANCELADA".equals(r.estado())) {
				throw new ConflictoException("Una reserva CANCELADA no se cobra");
			}
			if (detalleFacturaDao.reservaFacturada(d.getIdReserva())) {
				throw new ConflictoException("La reserva ya fue cobrada en otra factura");
			}
			valor = r.valor();

		} else if (d.getIdAsignacion() != null) {
			exigirConcepto(concepto, CONCEPTO_PARQUEADERO);
			Origen a = detalleFacturaDao.buscarAsignacion(d.getIdAsignacion());
			if (a == null) {
				throw new RecursoNoEncontradoException("Asignación de celda no encontrada");
			}
			exigirMismaCasa(factura, a, "La asignación");
			if (detalleFacturaDao.asignacionEnFactura(d.getIdAsignacion(), d.getNumeroFactura())) {
				throw new ConflictoException("La asignación ya está cobrada en esta factura");
			}
			valor = concepto.getValorBase();

		} else {
		    if (esConceptoLigado(concepto)) {
		        throw new DatoInvalidoException("El concepto '" + concepto.getNombre()
		                + "' exige indicar la sanción, reserva o asignación que se cobra");
		    }

		    if (CONCEPTO_ADMINISTRACION.equalsIgnoreCase(concepto.getNombre())
		            && detalleFacturaDao.conceptoEnFactura(
		                    d.getIdConcepto(), d.getNumeroFactura())) {

		        throw new ConflictoException(
		                "La factura ya tiene una línea de Administración"
		        );
		    }

		    valor = d.getValor() != null ? d.getValor() : concepto.getValorBase();
		}

		valor = validarValor(valor);
		return detalleFacturaDao.insertar(d, valor);
	}

	/**
	 * Cambia el valor de una línea libre (no ligada a un origen) de una factura abierta, sin que el
	 * total quede por debajo de lo ya pagado.
	 *
	 * @param numeroFactura número de la factura
	 * @param numeroLinea número de la línea dentro de la factura
	 * @param cambios línea con el valor nuevo
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws DatoInvalidoException si algún dato no cumple las validaciones
	 * @throws RecursoNoEncontradoException si la línea o la factura no existen
	 * @throws ConflictoException si la factura no está abierta, la línea está ligada a un origen o el
	 *         total quedaría bajo lo pagado
	 */
	public void actualizar(int numeroFactura, int numeroLinea, DetalleFactura cambios)
			throws SQLException, DatoInvalidoException, RecursoNoEncontradoException, ConflictoException {
		DetalleFactura actual = detalleFacturaDao.buscar(numeroFactura, numeroLinea);
		if (actual == null) {
			throw new RecursoNoEncontradoException("Línea de factura no encontrada");
		}
		Factura factura = obtenerFacturaAbierta(numeroFactura);

		if (actual.getIdSancion() != null || actual.getIdReserva() != null || actual.getIdAsignacion() != null) {
			throw new ConflictoException(
					"El valor de una línea ligada a sanción, reserva o asignación sale de su origen; "
							+ "elimina la línea y créala de nuevo");
		}
		if (cambios.getValor() == null) {
			throw new DatoInvalidoException("valor es obligatorio");
		}
		BigDecimal nuevo = validarValor(cambios.getValor());

		BigDecimal totalNuevo = factura.getTotal().subtract(actual.getValor()).add(nuevo);
		exigirNoQuedarBajoLoPagado(factura, totalNuevo);

		if (!detalleFacturaDao.actualizarValor(numeroFactura, numeroLinea, nuevo)) {
			throw new RecursoNoEncontradoException("Línea de factura no encontrada");
		}
	}

	/**
	 * Elimina una línea de una factura abierta, sin que el total quede por debajo de lo ya pagado.
	 *
	 * @param numeroFactura número de la factura
	 * @param numeroLinea número de la línea dentro de la factura
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws RecursoNoEncontradoException si la línea o la factura no existen
	 * @throws ConflictoException si la factura no está abierta o el total quedaría bajo lo pagado
	 */
	public void eliminar(int numeroFactura, int numeroLinea)
			throws SQLException, RecursoNoEncontradoException, ConflictoException {
		DetalleFactura actual = detalleFacturaDao.buscar(numeroFactura, numeroLinea);
		if (actual == null) {
			throw new RecursoNoEncontradoException("Línea de factura no encontrada");
		}
		Factura factura = obtenerFacturaAbierta(numeroFactura);

		BigDecimal totalNuevo = factura.getTotal().subtract(actual.getValor());
		exigirNoQuedarBajoLoPagado(factura, totalNuevo);

		if (!detalleFacturaDao.eliminar(numeroFactura, numeroLinea)) {
			throw new RecursoNoEncontradoException("Línea de factura no encontrada");
		}
	}

	/**
	 * Busca la factura y verifica que esté {@code PENDIENTE} o {@code ABONADA}.
	 *
	 * @param numeroFactura número de la factura
	 * @return factura encontrada
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws RecursoNoEncontradoException si la factura no existe
	 * @throws ConflictoException si la factura no está abierta
	 */
	private Factura obtenerFacturaAbierta(int numeroFactura)
			throws SQLException, RecursoNoEncontradoException, ConflictoException {
		Factura factura = facturaDao.buscarPorId(numeroFactura);
		if (factura == null) {
			throw new RecursoNoEncontradoException("Factura no encontrada");
		}
		if (!"PENDIENTE".equals(factura.getEstado()) && !"ABONADA".equals(factura.getEstado())) {
			throw new ConflictoException(
					"Solo se pueden cambiar las líneas de una factura PENDIENTE o ABONADA (estado actual: "
							+ factura.getEstado() + "); cóbralo en la factura del siguiente periodo");
		}
		return factura;
	}

	/**
	 * Verifica que la línea use el concepto esperado para su origen.
	 *
	 * @param concepto concepto enviado
	 * @param esperado nombre del concepto que corresponde
	 * @throws DatoInvalidoException si el concepto no es el esperado
	 */
	private void exigirConcepto(ConceptoCobro concepto, String esperado) throws DatoInvalidoException {
		if (!esperado.equalsIgnoreCase(concepto.getNombre())) {
			throw new DatoInvalidoException("Esta línea debe usar el concepto '" + esperado
					+ "' (se envió '" + concepto.getNombre() + "')");
		}
	}

	/**
	 * Indica si el concepto solo se puede cobrar ligado a un origen (sanción, salón comunal o
	 * parqueadero adicional).
	 *
	 * @param c concepto que se revisa
	 * @return {@code true} si el concepto exige un origen
	 */
	private boolean esConceptoLigado(ConceptoCobro c) {
		String n = c.getNombre();
		return CONCEPTO_SANCION.equalsIgnoreCase(n) || CONCEPTO_RESERVA.equalsIgnoreCase(n)
				|| CONCEPTO_PARQUEADERO.equalsIgnoreCase(n);
	}

	/**
	 * Verifica que el origen pertenezca a la misma casa de la factura.
	 *
	 * @param f factura
	 * @param o origen del cobro
	 * @param que nombre del origen para el mensaje de error
	 * @throws ConflictoException si el origen es de otra casa
	 */
	private void exigirMismaCasa(Factura f, Origen o, String que) throws ConflictoException {
		if (o.casaNumero() != f.getCasaNumero() || o.casaManzana() != f.getCasaManzana()) {
			throw new ConflictoException(que + " pertenece a otra casa que la de la factura");
		}
	}

	/**
	 * Verifica que el nuevo total de la factura no quede por debajo de lo ya pagado.
	 *
	 * @param f factura
	 * @param totalNuevo total que tendría la factura después del cambio
	 * @throws ConflictoException si el total quedaría por debajo de lo pagado
	 */
	private void exigirNoQuedarBajoLoPagado(Factura f, BigDecimal totalNuevo) throws ConflictoException {
		if (totalNuevo.compareTo(f.getTotalPagado()) < 0) {
			throw new ConflictoException("El total de la factura quedaría por debajo de lo ya pagado ("
					+ f.getTotalPagado() + ")");
		}
	}

	/**
	 * Valida que el valor sea positivo y quepa en la columna, redondeado a 2 decimales.
	 *
	 * @param valor valor recibido
	 * @return valor redondeado
	 * @throws DatoInvalidoException si algún dato no cumple las validaciones
	 */
	private BigDecimal validarValor(BigDecimal valor) throws DatoInvalidoException {
		if (valor == null || valor.signum() <= 0) {
			throw new DatoInvalidoException("valor debe ser mayor que cero");
		}
		BigDecimal v = valor.setScale(2, RoundingMode.HALF_UP);
		if (v.compareTo(MAXIMO) > 0) {
			throw new DatoInvalidoException("valor es demasiado grande");
		}
		return v;
	}
}