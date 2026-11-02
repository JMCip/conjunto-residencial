package co.edu.unbosque.controller;

import java.sql.SQLException;
import java.util.Map;

import co.edu.unbosque.exception.ConflictoException;
import co.edu.unbosque.exception.DatoInvalidoException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;
import co.edu.unbosque.model.Sancion;
import co.edu.unbosque.service.SancionService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;

public class SancionController {

	private final SancionService sancionService = new SancionService();

	@OpenApi(
			path = "/api/sanciones",
			methods = HttpMethod.GET,
			summary = "Listar sanciones",
			description = "Devuelve las sanciones impuestas a las casas, de la más reciente a la más antigua.",
			tags = {"Sanciones"},
			responses = {
					@OpenApiResponse(status = "200", content = @OpenApiContent(from = Sancion[].class)),
					@OpenApiResponse(status = "500", description = "Error consultando la base de datos")
			})
	public void listar(Context ctx) throws SQLException {
		ctx.json(sancionService.listar());
	}

	@OpenApi(
			path = "/api/sanciones",
			methods = HttpMethod.POST,
			summary = "Registrar sanción",
			description = "Envía fechaIncidente y fechaNotificacion (yyyy-MM-dd), tipo (texto libre, máximo 30 caracteres, por ejemplo RUIDO o MASCOTAS), descripcion, casaNumero y casaManzana. descargos es opcional. Queda EN_DESCARGOS y sin valor.",
			tags = {"Sanciones"},
			requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Sancion.class)),
			responses = {
					@OpenApiResponse(status = "201", description = "Creada"),
					@OpenApiResponse(status = "400", description = "Datos inválidos"),
					@OpenApiResponse(status = "404", description = "Casa no encontrada")
			})
	public void crear(Context ctx) throws SQLException, DatoInvalidoException, RecursoNoEncontradoException {
		Sancion s = ctx.bodyAsClass(Sancion.class);
		int id = sancionService.crear(s);
		ctx.status(HttpStatus.CREATED).json(Map.of("mensaje", "Sanción registrada", "idSancion", id));
	}

	@OpenApi(
			path = "/api/sanciones/{id}",
			methods = HttpMethod.PUT,
			summary = "Avanzar el proceso de una sanción",
			description = "Solo para sanciones EN_DESCARGOS. Envía {\"descargos\": \"...\"} para registrar descargos, {\"estadoProceso\": \"CONFIRMADA\", \"valor\": 150000} para confirmar (valor opcional, solo si hay multa) o {\"estadoProceso\": \"ANULADA\"} para anular. Puedes enviar descargos y decisión juntos.",
			tags = {"Sanciones"},
			pathParams = { @OpenApiParam(name = "id", type = Integer.class) },
			requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Sancion.class)),
			responses = {
					@OpenApiResponse(status = "200", description = "Sanción actualizada"),
					@OpenApiResponse(status = "400", description = "Datos inválidos"),
					@OpenApiResponse(status = "404", description = "Sanción no encontrada"),
					@OpenApiResponse(status = "409", description = "La sanción ya fue decidida")
			})
	public void actualizar(Context ctx) throws SQLException, DatoInvalidoException,
			RecursoNoEncontradoException, ConflictoException {
		int id = Integer.parseInt(ctx.pathParam("id"));
		Sancion cambios = ctx.bodyAsClass(Sancion.class);
		sancionService.actualizar(id, cambios);
		ctx.status(HttpStatus.OK).json(Map.of("mensaje", "Sanción actualizada"));
	}

	@OpenApi(
			path = "/api/sanciones/{id}",
			methods = HttpMethod.DELETE,
			summary = "Eliminar sanción",
			description = "Solo se pueden eliminar sanciones ANULADAS. Las CONFIRMADAS y las EN_DESCARGOS se conservan como historial.",
			tags = {"Sanciones"},
			pathParams = { @OpenApiParam(name = "id", type = Integer.class) },
			responses = {
					@OpenApiResponse(status = "200", description = "Eliminada"),
					@OpenApiResponse(status = "404", description = "Sanción no encontrada"),
					@OpenApiResponse(status = "409", description = "La sanción no está ANULADA o ya fue facturada")
			})
	public void eliminar(Context ctx) throws SQLException, RecursoNoEncontradoException, ConflictoException {
		int id = Integer.parseInt(ctx.pathParam("id"));
		sancionService.eliminar(id);
		ctx.status(HttpStatus.OK).json(Map.of("mensaje", "Sanción eliminada"));
	}
}