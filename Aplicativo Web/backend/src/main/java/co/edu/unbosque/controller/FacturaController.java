package co.edu.unbosque.controller;

import java.sql.SQLException;
import java.util.Map;

import co.edu.unbosque.exception.ConflictoException;
import co.edu.unbosque.exception.DatoInvalidoException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;
import co.edu.unbosque.model.Factura;
import co.edu.unbosque.service.FacturaService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;

public class FacturaController {

	private final FacturaService facturaService = new FacturaService();

	@OpenApi(
			path = "/api/facturas",
			methods = HttpMethod.GET,
			summary = "Listar facturas",
			description = "Devuelve las facturas con lo pagado y el saldo pendiente, calculados a partir de los pagos.",
			tags = {"Finanzas"},
			responses = {
					@OpenApiResponse(status = "200", content = @OpenApiContent(from = Factura[].class)),
					@OpenApiResponse(status = "500", description = "Error consultando la base de datos")
			})
	public void listar(Context ctx) throws SQLException {
		ctx.json(facturaService.listar());
	}

	@OpenApi(
			path = "/api/facturas",
			methods = HttpMethod.POST,
			summary = "Crear factura",
			description = "Envía periodo (yyyy-MM), fechaEmision y fechaVencimiento (yyyy-MM-dd), casaNumero y casaManzana. Queda PENDIENTE y sin líneas (total 0): las líneas se agregan en detalles de factura. Solo una factura por casa y periodo.",
			tags = {"Finanzas"},
			requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Factura.class)),
			responses = {
					@OpenApiResponse(status = "201", description = "Creada"),
					@OpenApiResponse(status = "400", description = "Datos inválidos"),
					@OpenApiResponse(status = "404", description = "Casa no encontrada"),
					@OpenApiResponse(status = "409", description = "La casa ya tiene factura de ese periodo")
			})
	public void crear(Context ctx) throws SQLException, DatoInvalidoException,
			RecursoNoEncontradoException, ConflictoException {
		Factura f = ctx.bodyAsClass(Factura.class);
		int id = facturaService.crear(f);
		ctx.status(HttpStatus.CREATED).json(Map.of("mensaje", "Factura creada", "numeroFactura", id));
	}

	@OpenApi(
			path = "/api/facturas/marcar-vencidas",
			methods = HttpMethod.POST,
			summary = "Marcar facturas vencidas",
			description = "Pasa a VENCIDA las facturas PENDIENTE o ABONADA cuya fecha de vencimiento ya pasó. Una casa con una factura VENCIDA queda en mora y no puede reservar el salón. No lleva body.",
			tags = {"Finanzas"},
			responses = {
					@OpenApiResponse(status = "200", description = "Devuelve cuántas facturas se marcaron")
			})
	public void marcarVencidas(Context ctx) throws SQLException {
		int cantidad = facturaService.marcarVencidas();
		ctx.status(HttpStatus.OK).json(Map.of("mensaje", "Facturas marcadas como vencidas", "cantidad", cantidad));
	}

	@OpenApi(
			path = "/api/facturas/{id}",
			methods = HttpMethod.PUT,
			summary = "Cambiar el vencimiento de una factura",
			description = "Solo para facturas PENDIENTE o ABONADA. Envía {\"fechaVencimiento\": \"yyyy-MM-dd\"}. El periodo, la emisión y la casa no se pueden modificar.",
			tags = {"Finanzas"},
			pathParams = { @OpenApiParam(name = "id", type = Integer.class) },
			requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Factura.class)),
			responses = {
					@OpenApiResponse(status = "200", description = "Factura actualizada"),
					@OpenApiResponse(status = "400", description = "Datos inválidos"),
					@OpenApiResponse(status = "404", description = "Factura no encontrada"),
					@OpenApiResponse(status = "409", description = "La factura está PAGADA o VENCIDA")
			})
	public void actualizar(Context ctx) throws SQLException, DatoInvalidoException,
			RecursoNoEncontradoException, ConflictoException {
		int id = Integer.parseInt(ctx.pathParam("id"));
		Factura cambios = ctx.bodyAsClass(Factura.class);
		facturaService.actualizar(id, cambios);
		ctx.status(HttpStatus.OK).json(Map.of("mensaje", "Factura actualizada"));
	}

	@OpenApi(
			path = "/api/facturas/{id}",
			methods = HttpMethod.DELETE,
			summary = "Eliminar factura",
			description = "Solo se pueden eliminar facturas PENDIENTE que no tengan líneas de detalle ni pagos.",
			tags = {"Finanzas"},
			pathParams = { @OpenApiParam(name = "id", type = Integer.class) },
			responses = {
					@OpenApiResponse(status = "200", description = "Eliminada"),
					@OpenApiResponse(status = "404", description = "Factura no encontrada"),
					@OpenApiResponse(status = "409", description = "No está PENDIENTE o tiene líneas o pagos")
			})
	public void eliminar(Context ctx) throws SQLException, RecursoNoEncontradoException, ConflictoException {
		int id = Integer.parseInt(ctx.pathParam("id"));
		facturaService.eliminar(id);
		ctx.status(HttpStatus.OK).json(Map.of("mensaje", "Factura eliminada"));
	}
}