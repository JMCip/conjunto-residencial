package co.edu.unbosque.controller;

import io.javalin.http.Context;
import java.sql.SQLException;
import java.util.Map;

import co.edu.unbosque.exception.ConflictoException;
import co.edu.unbosque.exception.DatoInvalidoException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;
import io.javalin.http.HttpStatus;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiRequestBody;
import co.edu.unbosque.model.AsignacionCelda;
import co.edu.unbosque.service.AsignacionCeldaService;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;

/**
 * Controlador REST de las asignaciones de celda (rutas {@code /api/asignaciones-celda}).
 * <p>
 * Lee los parámetros y el cuerpo de la petición, delega en el servicio y responde en JSON. Las
 * excepciones se traducen a códigos HTTP en {@link co.edu.unbosque.App}.
 */
public class AsignacionCeldaController {
	
	/** Servicio con la lógica de negocio de las asignaciones de celda. */
	private final AsignacionCeldaService asignacionCeldaService = new AsignacionCeldaService();
	
	/**
	 * Atiende {@code GET /api/asignaciones-celda} (listar asignaciones de celda). Devuelve qué celda
	 * tiene asignada cada casa. Si fechaFin es null, la asignación está vigente.
	 *
	 * @param conx contexto de la petición y la respuesta HTTP
	 * @throws SQLException si falla el acceso a la base de datos; se traduce a HTTP según su SQLState
	 */
	@OpenApi(
			path = "/api/asignaciones-celda",
			methods = HttpMethod.GET,
			summary = "Listar asignaciones de celda",
			description = "Devuelve qué celda tiene asignada cada casa. Si fechaFin es null, la asignación está vigente.",
			tags = {"Parqueadero"},
			responses = {
					@OpenApiResponse(status = "200", content = @OpenApiContent(from = AsignacionCelda[].class)),
					@OpenApiResponse(status = "500", description = "Error consultando la base de datos")
			})
	public void listar(Context conx) throws SQLException {
		conx.json(asignacionCeldaService.listar());
	}
	
	/**
	 * Atiende {@code GET /api/asignaciones-celda/{id}} (consultar asignación de celda).
	 *
	 * @param ctx contexto de la petición y la respuesta HTTP
	 * @throws SQLException si falla el acceso a la base de datos; se traduce a HTTP según su SQLState
	 * @throws RecursoNoEncontradoException si el registro no existe (HTTP 404)
	 */
	@OpenApi(
			path = "/api/asignaciones-celda/{id}",
			methods = HttpMethod.GET,
			summary = "Consultar asignación de celda",
			tags = {"Parqueadero"},
			pathParams = {
					@OpenApiParam(name = "id", type = Integer.class)
			},
			responses = {
					@OpenApiResponse(status = "200", content = @OpenApiContent(from = AsignacionCelda.class)),
					@OpenApiResponse(status = "404", description = "Asignación no encontrada")
			})
	public void consultar(Context ctx) throws SQLException, RecursoNoEncontradoException {
		ctx.json(asignacionCeldaService.consultar(ctx.pathParamAsClass("id", Integer.class).get()));
	}

	/**
	 * Atiende {@code POST /api/asignaciones-celda} (crear asignación de celda). No envíes
	 * idAsignacion, se genera solo. Fechas en formato AAAA-MM-DD. fechaFin va null mientras la
	 * asignación esté vigente.
	 *
	 * @param ctx contexto de la petición y la respuesta HTTP; de él se lee el cuerpo JSON
	 * @throws SQLException si falla el acceso a la base de datos; se traduce a HTTP según su SQLState
	 * @throws DatoInvalidoException si el cuerpo o los parámetros no son válidos (HTTP 400)
	 * @throws ConflictoException si la operación viola una regla de negocio (HTTP 409)
	 */
	@OpenApi(
			path = "/api/asignaciones-celda",
			methods = HttpMethod.POST,
			summary = "Crear asignación de celda",
			description = "No envíes idAsignacion, se genera solo. Fechas en formato AAAA-MM-DD. fechaFin va null mientras la asignación esté vigente.",
			tags = {"Parqueadero"},
			requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = AsignacionCelda.class)),
			responses = {
					@OpenApiResponse(status = "201", description = "Creada"),
					@OpenApiResponse(status = "400", description = "Datos inválidos"),
					@OpenApiResponse(status = "409", description = "La celda ya tiene una asignación vigente, o la casa o la celda no existen")
			})
	public void crear(Context ctx) throws SQLException, DatoInvalidoException, ConflictoException {
		AsignacionCelda a = ctx.bodyAsClass(AsignacionCelda.class);
		int id = asignacionCeldaService.crear(a);
		ctx.status(HttpStatus.CREATED).json(Map.of("mensaje", "Asignación creada", "idAsignacion", id));
	}

	/**
	 * Atiende {@code PUT /api/asignaciones-celda/{id}} (actualizar asignación de celda). La casa y la
	 * celda no se modifican. En el body envía fechaInicio y fechaFin (para cerrar la asignación).
	 *
	 * @param ctx contexto de la petición y la respuesta HTTP; de él se lee el cuerpo JSON
	 * @throws SQLException si falla el acceso a la base de datos; se traduce a HTTP según su SQLState
	 * @throws DatoInvalidoException si el cuerpo o los parámetros no son válidos (HTTP 400)
	 * @throws RecursoNoEncontradoException si el registro no existe (HTTP 404)
	 * @throws ConflictoException si la operación viola una regla de negocio (HTTP 409)
	 */
	@OpenApi(
			path = "/api/asignaciones-celda/{id}",
			methods = HttpMethod.PUT,
			summary = "Actualizar asignación de celda",
			description = "La casa y la celda no se modifican. En el body envía fechaInicio y fechaFin (para cerrar la asignación).",
			tags = {"Parqueadero"},
			pathParams = {
					@OpenApiParam(name = "id", type = Integer.class)
			},
			requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = AsignacionCelda.class)),
			responses = {
					@OpenApiResponse(status = "200", description = "Actualizada"),
					@OpenApiResponse(status = "400", description = "Datos inválidos"),
					@OpenApiResponse(status = "404", description = "Asignación no encontrada"),
					@OpenApiResponse(status = "409", description = "La celda ya tiene otra asignación vigente")
			})
	public void actualizar(Context ctx)
			throws SQLException, DatoInvalidoException, RecursoNoEncontradoException, ConflictoException {
		AsignacionCelda a = ctx.bodyAsClass(AsignacionCelda.class);
		a.setIdAsignacion(ctx.pathParamAsClass("id", Integer.class).get());
		asignacionCeldaService.actualizar(a);
		ctx.status(HttpStatus.OK).json(Map.of("mensaje", "Asignación actualizada"));
	}

	/**
	 * Atiende {@code DELETE /api/asignaciones-celda/{id}} (eliminar asignación de celda).
	 *
	 * @param ctx contexto de la petición y la respuesta HTTP
	 * @throws SQLException si falla el acceso a la base de datos; se traduce a HTTP según su SQLState
	 * @throws RecursoNoEncontradoException si el registro no existe (HTTP 404)
	 */
	@OpenApi(
			path = "/api/asignaciones-celda/{id}",
			methods = HttpMethod.DELETE,
			summary = "Eliminar asignación de celda",
			tags = {"Parqueadero"},
			pathParams = {
					@OpenApiParam(name = "id", type = Integer.class)
			},
			responses = {
					@OpenApiResponse(status = "200", description = "Eliminada"),
					@OpenApiResponse(status = "404", description = "Asignación no encontrada"),
					@OpenApiResponse(status = "409", description = "La asignación tiene datos asociados (por ejemplo, líneas de factura)")
			})
	public void eliminar(Context ctx) throws SQLException, RecursoNoEncontradoException {
		asignacionCeldaService.eliminar(ctx.pathParamAsClass("id", Integer.class).get());
		ctx.status(HttpStatus.OK).json(Map.of("mensaje", "Asignación eliminada"));
	}
	
}
