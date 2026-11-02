package co.edu.unbosque.controller;

import java.sql.SQLException;
import java.util.Map;

import co.edu.unbosque.exception.ConflictoException;
import co.edu.unbosque.exception.DatoInvalidoException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;
import co.edu.unbosque.model.DetalleFactura;
import co.edu.unbosque.service.DetalleFacturaService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;

public class DetalleFacturaController {

	private final DetalleFacturaService detalleFacturaService = new DetalleFacturaService();

	@OpenApi(
			path = "/api/detalles-factura",
			methods = HttpMethod.GET,
			summary = "Listar detalles de factura",
			description = "Devuelve las líneas de cada factura. idAsignacion, idReserva o idSancion indican de dónde sale el cobro.",
			tags = {"Finanzas"},
			responses = {
					@OpenApiResponse(status = "200", content = @OpenApiContent(from = DetalleFactura[].class)),
					@OpenApiResponse(status = "500", description = "Error consultando la base de datos")
			})
	public void listar(Context ctx) throws SQLException {
		ctx.json(detalleFacturaService.listar());
	}

	@OpenApi(
			path = "/api/detalles-factura",
			methods = HttpMethod.POST,
			summary = "Agregar una línea a una factura",
			description = "Envía numeroFactura e idConcepto. El número de línea lo asigna el sistema. "
					+ "Para una multa envía idSancion (concepto Sanción), para un salón idReserva (concepto Salón comunal) "
					+ "y para una celda adicional idAsignacion (concepto Parqueadero adicional): el valor se toma de ese origen y solo se permite uno. "
					+ "Las demás líneas (administración, cuota extraordinaria) usan el valor enviado o el valor base del concepto. "
					+ "Solo facturas PENDIENTE o ABONADA.",
			tags = {"Finanzas"},
			requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = DetalleFactura.class)),
			responses = {
					@OpenApiResponse(status = "201", description = "Línea creada"),
					@OpenApiResponse(status = "400", description = "Datos inválidos"),
					@OpenApiResponse(status = "404", description = "Factura, concepto u origen no encontrado"),
					@OpenApiResponse(status = "409", description = "Factura cerrada, origen de otra casa o ya cobrado")
			})
	public void crear(Context ctx) throws SQLException, DatoInvalidoException,
			RecursoNoEncontradoException, ConflictoException {
		DetalleFactura d = ctx.bodyAsClass(DetalleFactura.class);
		int linea = detalleFacturaService.crear(d);
		ctx.status(HttpStatus.CREATED).json(Map.of(
				"mensaje", "Línea agregada",
				"numeroFactura", d.getNumeroFactura(),
				"numeroLinea", linea));
	}

	@OpenApi(
			path = "/api/detalles-factura/{factura}/{linea}",
			methods = HttpMethod.PUT,
			summary = "Cambiar el valor de una línea",
			description = "Solo líneas sin sanción, reserva ni asignación, de facturas PENDIENTE o ABONADA. Envía {\"valor\": 120000}.",
			tags = {"Finanzas"},
			pathParams = {
					@OpenApiParam(name = "factura", type = Integer.class),
					@OpenApiParam(name = "linea", type = Integer.class)
			},
			requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = DetalleFactura.class)),
			responses = {
					@OpenApiResponse(status = "200", description = "Línea actualizada"),
					@OpenApiResponse(status = "400", description = "Valor inválido"),
					@OpenApiResponse(status = "404", description = "Línea no encontrada"),
					@OpenApiResponse(status = "409", description = "Factura cerrada, línea ligada a un origen o total menor a lo pagado")
			})
	public void actualizar(Context ctx) throws SQLException, DatoInvalidoException,
			RecursoNoEncontradoException, ConflictoException {
		int factura = Integer.parseInt(ctx.pathParam("factura"));
		int linea = Integer.parseInt(ctx.pathParam("linea"));
		DetalleFactura cambios = ctx.bodyAsClass(DetalleFactura.class);
		detalleFacturaService.actualizar(factura, linea, cambios);
		ctx.status(HttpStatus.OK).json(Map.of("mensaje", "Línea actualizada"));
	}

	@OpenApi(
			path = "/api/detalles-factura/{factura}/{linea}",
			methods = HttpMethod.DELETE,
			summary = "Eliminar una línea de factura",
			description = "Solo de facturas PENDIENTE o ABONADA, y siempre que el total no quede por debajo de lo ya pagado.",
			tags = {"Finanzas"},
			pathParams = {
					@OpenApiParam(name = "factura", type = Integer.class),
					@OpenApiParam(name = "linea", type = Integer.class)
			},
			responses = {
					@OpenApiResponse(status = "200", description = "Línea eliminada"),
					@OpenApiResponse(status = "404", description = "Línea no encontrada"),
					@OpenApiResponse(status = "409", description = "Factura cerrada o total menor a lo pagado")
			})
	public void eliminar(Context ctx) throws SQLException, RecursoNoEncontradoException, ConflictoException {
		int factura = Integer.parseInt(ctx.pathParam("factura"));
		int linea = Integer.parseInt(ctx.pathParam("linea"));
		detalleFacturaService.eliminar(factura, linea);
		ctx.status(HttpStatus.OK).json(Map.of("mensaje", "Línea eliminada"));
	}
}