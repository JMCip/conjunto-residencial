package co.edu.unbosque.controller;

import java.sql.SQLException;
import java.util.Map;

import co.edu.unbosque.exception.ConflictoException;
import co.edu.unbosque.exception.DatoInvalidoException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;
import co.edu.unbosque.model.Pago;
import co.edu.unbosque.service.PagoService;
import co.edu.unbosque.service.PagoService.Registro;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;

/**
 * Controlador REST de los pagos (rutas {@code /api/pagos}).
 * <p>
 * Lee los parámetros y el cuerpo de la petición, delega en el servicio y responde en JSON. Las
 * excepciones se traducen a códigos HTTP en {@link co.edu.unbosque.App}.
 */
public class PagoController {

	/** Servicio con la lógica de negocio de los pagos. */
	private final PagoService pagoService = new PagoService();

	/**
	 * Atiende {@code GET /api/pagos} (listar pagos). Devuelve los pagos realizados, con la factura que
	 * pagan y quién pagó.
	 *
	 * @param ctx contexto de la petición y la respuesta HTTP
	 * @throws SQLException si falla el acceso a la base de datos; se traduce a HTTP según su SQLState
	 */
	@OpenApi(
			path = "/api/pagos",
			methods = HttpMethod.GET,
			summary = "Listar pagos",
			description = "Devuelve los pagos realizados, con la factura que pagan y quién pagó.",
			tags = {"Finanzas"},
			responses = {
					@OpenApiResponse(status = "200", content = @OpenApiContent(from = Pago[].class)),
					@OpenApiResponse(status = "500", description = "Error consultando la base de datos")
			})
	public void listar(Context ctx) throws SQLException {
		ctx.json(pagoService.listar());
	}

	/**
	 * Atiende {@code POST /api/pagos} (registrar un pago (abono) de una factura). Envía fecha
	 * (yyyy-MM-dd), valor, metodoPago (EFECTIVO, TRANSFERENCIA, PSE o TARJETA), numeroFactura.
	 *
	 * @param ctx contexto de la petición y la respuesta HTTP; de él se lee el cuerpo JSON
	 * @throws SQLException si falla el acceso a la base de datos; se traduce a HTTP según su SQLState
	 * @throws DatoInvalidoException si el cuerpo o los parámetros no son válidos (HTTP 400)
	 * @throws RecursoNoEncontradoException si el registro no existe (HTTP 404)
	 * @throws ConflictoException si la operación viola una regla de negocio (HTTP 409)
	 */
	@OpenApi(
			path = "/api/pagos",
			methods = HttpMethod.POST,
			summary = "Registrar un pago (abono) de una factura",
			description = "Envía fecha (yyyy-MM-dd), valor, metodoPago (EFECTIVO, TRANSFERENCIA, PSE o TARJETA), numeroFactura "
					+ "y la persona que paga (personaTipoDocumento y personaNumeroDocumento), que puede ser el propietario, "
					+ "el arrendatario u otra persona registrada. El valor no puede superar el saldo. "
					+ "La factura pasa a PAGADA si el saldo llega a 0, o a ABONADA si estaba PENDIENTE; una VENCIDA con saldo sigue VENCIDA. "
					+ "Los pagos no se editan ni se eliminan.",
			tags = {"Finanzas"},
			requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Pago.class)),
			responses = {
					@OpenApiResponse(status = "201", description = "Pago registrado; devuelve el estado y el saldo de la factura"),
					@OpenApiResponse(status = "400", description = "Datos inválidos"),
					@OpenApiResponse(status = "404", description = "Factura o persona no encontrada"),
					@OpenApiResponse(status = "409", description = "Factura ya pagada o el pago supera el saldo")
			})
	public void crear(Context ctx) throws SQLException, DatoInvalidoException,
			RecursoNoEncontradoException, ConflictoException {
		Pago p = ctx.bodyAsClass(Pago.class);
		Registro r = pagoService.registrar(p);
		ctx.status(HttpStatus.CREATED).json(Map.of(
				"mensaje", "Pago registrado",
				"idPago", r.idPago(),
				"estadoFactura", r.estadoFactura(),
				"saldo", r.saldo()));
	}
}