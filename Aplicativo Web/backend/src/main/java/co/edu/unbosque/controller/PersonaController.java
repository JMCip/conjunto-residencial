package co.edu.unbosque.controller;

import io.javalin.http.Context;
import java.sql.SQLException;

import co.edu.unbosque.model.Casa;
import co.edu.unbosque.service.PersonaService;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;

import java.util.Map;

import co.edu.unbosque.exception.DatoInvalidoException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;
import co.edu.unbosque.model.Persona;
import io.javalin.http.HttpStatus;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiRequestBody;

/**
 * Controlador REST de las personas (rutas {@code /api/personas}).
 * <p>
 * Lee los parámetros y el cuerpo de la petición, delega en el servicio y responde en JSON. Las
 * excepciones se traducen a códigos HTTP en {@link co.edu.unbosque.App}.
 */
public class PersonaController {
	/** Servicio con la lógica de negocio de las personas. */
	private final PersonaService pService = new PersonaService();
	
	/**
	 * Atiende {@code GET /api/personas} (listar personas). Devuelve todas las personas del conjunto,
	 * ordenadas por el primer nombre y apellido.
	 *
	 * @param conx contexto de la petición y la respuesta HTTP
	 * @throws SQLException si falla el acceso a la base de datos; se traduce a HTTP según su SQLState
	 */
	@OpenApi (
			path = "/api/personas",
			methods = HttpMethod.GET,
			summary = "Listar personas",
			description = "Devuelve todas las personas del conjunto, ordenadas por el primer nombre y apellido.",
			tags = {"Personas"},
			responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(from = Persona[].class)),
                    @OpenApiResponse(status = "500", description = "Error consultando la base de datos")
            })
	
	public void listar(Context conx) throws SQLException {
		conx.json(pService.listar());
	}
	
	/**
	 * Atiende {@code GET /api/personas/{tipoDocumento}/{numeroDocumento}} (consultar persona).
	 *
	 * @param ctx contexto de la petición y la respuesta HTTP; de él se leen los parámetros de ruta
	 *         {@code tipoDocumento} y {@code numeroDocumento}
	 * @throws SQLException si falla el acceso a la base de datos; se traduce a HTTP según su SQLState
	 * @throws RecursoNoEncontradoException si el registro no existe (HTTP 404)
	 */
	@OpenApi(
			path = "/api/personas/{tipoDocumento}/{numeroDocumento}",
			methods = HttpMethod.GET,
			summary = "Consultar persona",
			tags = {"Personas"},
			pathParams = {
					@OpenApiParam(name = "tipoDocumento", type = String.class),
					@OpenApiParam(name = "numeroDocumento", type = String.class)
			},
			responses = {
					@OpenApiResponse(status = "200", content = @OpenApiContent(from = Persona.class)),
					@OpenApiResponse(status = "404", description = "Persona no encontrada")
			})
	public void consultar(Context ctx) throws SQLException, RecursoNoEncontradoException {
		ctx.json(pService.consultar(
				ctx.pathParam("tipoDocumento"),
				ctx.pathParam("numeroDocumento")));
	}

	/**
	 * Atiende {@code POST /api/personas} (crear persona).
	 *
	 * @param ctx contexto de la petición y la respuesta HTTP; de él se lee el cuerpo JSON
	 * @throws SQLException si falla el acceso a la base de datos; se traduce a HTTP según su SQLState
	 * @throws DatoInvalidoException si el cuerpo o los parámetros no son válidos (HTTP 400)
	 */
	@OpenApi(
			path = "/api/personas",
			methods = HttpMethod.POST,
			summary = "Crear persona",
			tags = {"Personas"},
			requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Persona.class)),
			responses = {
					@OpenApiResponse(status = "201", description = "Creada"),
					@OpenApiResponse(status = "400", description = "Datos inválidos"),
					@OpenApiResponse(status = "409", description = "La persona ya existe")
			})
	public void crear(Context ctx) throws SQLException, DatoInvalidoException {
		Persona p = ctx.bodyAsClass(Persona.class);
		pService.crear(p);
		ctx.status(HttpStatus.CREATED).json(Map.of("mensaje", "Persona creada"));
	}

	/**
	 * Atiende {@code PUT /api/personas/{tipoDocumento}/{numeroDocumento}} (actualizar persona). El
	 * documento viaja en la URL y no se modifica. En el body envía correo, nombres y apellidos.
	 *
	 * @param ctx contexto de la petición y la respuesta HTTP; de él se leen los parámetros de ruta
	 *         {@code tipoDocumento} y {@code numeroDocumento} y el cuerpo JSON
	 * @throws SQLException si falla el acceso a la base de datos; se traduce a HTTP según su SQLState
	 * @throws DatoInvalidoException si el cuerpo o los parámetros no son válidos (HTTP 400)
	 * @throws RecursoNoEncontradoException si el registro no existe (HTTP 404)
	 */
	@OpenApi(
			path = "/api/personas/{tipoDocumento}/{numeroDocumento}",
			methods = HttpMethod.PUT,
			summary = "Actualizar persona",
			description = "El documento viaja en la URL y no se modifica. En el body envía correo, nombres y apellidos.",
			tags = {"Personas"},
			pathParams = {
					@OpenApiParam(name = "tipoDocumento", type = String.class),
					@OpenApiParam(name = "numeroDocumento", type = String.class)
			},
			requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Persona.class)),
			responses = {
					@OpenApiResponse(status = "200", description = "Actualizada"),
					@OpenApiResponse(status = "400", description = "Datos inválidos"),
					@OpenApiResponse(status = "404", description = "Persona no encontrada")
			})
	public void actualizar(Context ctx)
			throws SQLException, DatoInvalidoException, RecursoNoEncontradoException {
		Persona p = ctx.bodyAsClass(Persona.class);
		p.setTipoDocumento(ctx.pathParam("tipoDocumento"));
		p.setNumeroDocumento(ctx.pathParam("numeroDocumento"));
		pService.actualizar(p);
		ctx.status(HttpStatus.OK).json(Map.of("mensaje", "Persona actualizada"));
	}

	/**
	 * Atiende {@code DELETE /api/personas/{tipoDocumento}/{numeroDocumento}} (eliminar persona).
	 *
	 * @param ctx contexto de la petición y la respuesta HTTP; de él se leen los parámetros de ruta
	 *         {@code tipoDocumento} y {@code numeroDocumento}
	 * @throws SQLException si falla el acceso a la base de datos; se traduce a HTTP según su SQLState
	 * @throws RecursoNoEncontradoException si el registro no existe (HTTP 404)
	 */
	@OpenApi(
			path = "/api/personas/{tipoDocumento}/{numeroDocumento}",
			methods = HttpMethod.DELETE,
			summary = "Eliminar persona",
			tags = {"Personas"},
			pathParams = {
					@OpenApiParam(name = "tipoDocumento", type = String.class),
					@OpenApiParam(name = "numeroDocumento", type = String.class)
			},
			responses = {
					@OpenApiResponse(status = "200", description = "Eliminada"),
					@OpenApiResponse(status = "404", description = "Persona no encontrada"),
					@OpenApiResponse(status = "409", description = "La persona tiene datos asociados")
			})
	public void eliminar(Context ctx) throws SQLException, RecursoNoEncontradoException {
		pService.eliminar(
				ctx.pathParam("tipoDocumento"),
				ctx.pathParam("numeroDocumento"));
		ctx.status(HttpStatus.OK).json(Map.of("mensaje", "Persona eliminada"));
	}
	
}
