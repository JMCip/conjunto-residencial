package co.edu.unbosque.controller;

import io.javalin.http.Context;
import java.sql.SQLException;
import java.util.Map;

import co.edu.unbosque.exception.DatoInvalidoException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;
import io.javalin.http.HttpStatus;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiRequestBody;
import co.edu.unbosque.model.CeldaParqueadero;
import co.edu.unbosque.service.CeldaParqueaderoService;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;

/**
 * Controlador REST de las celdas de parqueadero (rutas {@code /api/celdas}).
 * <p>
 * Lee los parámetros y el cuerpo de la petición, delega en el servicio y responde en JSON. Las
 * excepciones se traducen a códigos HTTP en {@link co.edu.unbosque.App}.
 */
public class CeldaParqueaderoController {
	
	/** Servicio con la lógica de negocio de las celdas de parqueadero. */
	private final CeldaParqueaderoService celdaParqueaderoService = new CeldaParqueaderoService();
	
	/**
	 * Atiende {@code GET /api/celdas} (listar celdas de parqueadero). Devuelve las celdas del
	 * parqueadero con su tipo y estado.
	 *
	 * @param conx contexto de la petición y la respuesta HTTP
	 * @throws SQLException si falla el acceso a la base de datos; se traduce a HTTP según su SQLState
	 */
	@OpenApi(
			path = "/api/celdas",
			methods = HttpMethod.GET,
			summary = "Listar celdas de parqueadero",
			description = "Devuelve las celdas del parqueadero con su tipo y estado.",
			tags = {"Parqueadero"},
			responses = {
					@OpenApiResponse(status = "200", content = @OpenApiContent(from = CeldaParqueadero[].class)),
					@OpenApiResponse(status = "500", description = "Error consultando la base de datos")
			})
	public void listar(Context conx) throws SQLException {
		conx.json(celdaParqueaderoService.listar());
	}
	
	/**
	 * Atiende {@code GET /api/celdas/{id}} (consultar celda de parqueadero).
	 *
	 * @param ctx contexto de la petición y la respuesta HTTP
	 * @throws SQLException si falla el acceso a la base de datos; se traduce a HTTP según su SQLState
	 * @throws RecursoNoEncontradoException si el registro no existe (HTTP 404)
	 */
	@OpenApi(
			path = "/api/celdas/{id}",
			methods = HttpMethod.GET,
			summary = "Consultar celda de parqueadero",
			tags = {"Parqueadero"},
			pathParams = {
					@OpenApiParam(name = "id", type = Integer.class)
			},
			responses = {
					@OpenApiResponse(status = "200", content = @OpenApiContent(from = CeldaParqueadero.class)),
					@OpenApiResponse(status = "404", description = "Celda no encontrada")
			})
	public void consultar(Context ctx) throws SQLException, RecursoNoEncontradoException {
		ctx.json(celdaParqueaderoService.consultar(ctx.pathParamAsClass("id", Integer.class).get()));
	}

	/**
	 * Atiende {@code POST /api/celdas} (crear celda de parqueadero). No envíes idCelda, se genera
	 * solo. tipo: PROPIA, ADICIONAL o VISITANTES. estado: OCUPADA o DESOCUPADA.
	 *
	 * @param ctx contexto de la petición y la respuesta HTTP; de él se lee el cuerpo JSON
	 * @throws SQLException si falla el acceso a la base de datos; se traduce a HTTP según su SQLState
	 * @throws DatoInvalidoException si el cuerpo o los parámetros no son válidos (HTTP 400)
	 */
	@OpenApi(
			path = "/api/celdas",
			methods = HttpMethod.POST,
			summary = "Crear celda de parqueadero",
			description = "No envíes idCelda, se genera solo. tipo: PROPIA, ADICIONAL o VISITANTES. estado: OCUPADA o DESOCUPADA.",
			tags = {"Parqueadero"},
			requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = CeldaParqueadero.class)),
			responses = {
					@OpenApiResponse(status = "201", description = "Creada"),
					@OpenApiResponse(status = "400", description = "Datos inválidos")
			})
	public void crear(Context ctx) throws SQLException, DatoInvalidoException {
		CeldaParqueadero c = ctx.bodyAsClass(CeldaParqueadero.class);
		int id = celdaParqueaderoService.crear(c);
		ctx.status(HttpStatus.CREATED).json(Map.of("mensaje", "Celda creada", "idCelda", id));
	}

	/**
	 * Atiende {@code PUT /api/celdas/{id}} (actualizar celda de parqueadero). El id viaja en la URL.
	 * En el body envía tipo y estado.
	 *
	 * @param ctx contexto de la petición y la respuesta HTTP; de él se lee el cuerpo JSON
	 * @throws SQLException si falla el acceso a la base de datos; se traduce a HTTP según su SQLState
	 * @throws DatoInvalidoException si el cuerpo o los parámetros no son válidos (HTTP 400)
	 * @throws RecursoNoEncontradoException si el registro no existe (HTTP 404)
	 */
	@OpenApi(
			path = "/api/celdas/{id}",
			methods = HttpMethod.PUT,
			summary = "Actualizar celda de parqueadero",
			description = "El id viaja en la URL. En el body envía tipo y estado.",
			tags = {"Parqueadero"},
			pathParams = {
					@OpenApiParam(name = "id", type = Integer.class)
			},
			requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = CeldaParqueadero.class)),
			responses = {
					@OpenApiResponse(status = "200", description = "Actualizada"),
					@OpenApiResponse(status = "400", description = "Datos inválidos"),
					@OpenApiResponse(status = "404", description = "Celda no encontrada")
			})
	public void actualizar(Context ctx)
			throws SQLException, DatoInvalidoException, RecursoNoEncontradoException {
		CeldaParqueadero c = ctx.bodyAsClass(CeldaParqueadero.class);
		c.setIdCelda(ctx.pathParamAsClass("id", Integer.class).get());
		celdaParqueaderoService.actualizar(c);
		ctx.status(HttpStatus.OK).json(Map.of("mensaje", "Celda actualizada"));
	}

	/**
	 * Atiende {@code DELETE /api/celdas/{id}} (eliminar celda de parqueadero).
	 *
	 * @param ctx contexto de la petición y la respuesta HTTP
	 * @throws SQLException si falla el acceso a la base de datos; se traduce a HTTP según su SQLState
	 * @throws RecursoNoEncontradoException si el registro no existe (HTTP 404)
	 */
	@OpenApi(
			path = "/api/celdas/{id}",
			methods = HttpMethod.DELETE,
			summary = "Eliminar celda de parqueadero",
			tags = {"Parqueadero"},
			pathParams = {
					@OpenApiParam(name = "id", type = Integer.class)
			},
			responses = {
					@OpenApiResponse(status = "200", description = "Eliminada"),
					@OpenApiResponse(status = "404", description = "Celda no encontrada"),
					@OpenApiResponse(status = "409", description = "La celda tiene asignaciones o parqueos asociados")
			})
	public void eliminar(Context ctx) throws SQLException, RecursoNoEncontradoException {
		celdaParqueaderoService.eliminar(ctx.pathParamAsClass("id", Integer.class).get());
		ctx.status(HttpStatus.OK).json(Map.of("mensaje", "Celda eliminada"));
	}
	
}
