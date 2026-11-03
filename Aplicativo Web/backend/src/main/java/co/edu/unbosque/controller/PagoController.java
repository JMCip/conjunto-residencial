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

public class PagoController {

	private final PagoService pagoService = new PagoService();

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