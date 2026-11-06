package co.edu.unbosque.controller;

import io.javalin.http.Context;
import java.sql.SQLException;

import co.edu.unbosque.model.Telefono;
import co.edu.unbosque.service.TelefonoService;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;

import java.util.Map;
import co.edu.unbosque.exception.DatoInvalidoException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;
import io.javalin.http.HttpStatus;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiRequestBody;

/**
 * Controlador REST de los teléfonos (rutas {@code /api/telefonos}).
 * <p>
 * Lee los parámetros y el cuerpo de la petición, delega en el servicio y responde en JSON. Las
 * excepciones se traducen a códigos HTTP en {@link co.edu.unbosque.App}.
 */
public class TelefonoController {
	
	/** Servicio con la lógica de negocio de los teléfonos. */
	private final TelefonoService telefonoService = new TelefonoService();
	
	/**
	 * Atiende {@code GET /api/telefonos} (listar teléfonos). Devuelve los teléfonos registrados de
	 * cada persona.
	 *
	 * @param conx contexto de la petición y la respuesta HTTP
	 * @throws SQLException si falla el acceso a la base de datos; se traduce a HTTP según su SQLState
	 */
	@OpenApi(
			path = "/api/telefonos",
			methods = HttpMethod.GET,
			summary = "Listar teléfonos",
			description = "Devuelve los teléfonos registrados de cada persona.",
			tags = {"Personas"},
			responses = {
					@OpenApiResponse(status = "200", content = @OpenApiContent(from = Telefono[].class)),
					@OpenApiResponse(status = "500", description = "Error consultando la base de datos")
			})
	public void listar(Context conx) throws SQLException {
		conx.json(telefonoService.listar());
	}
	
	/**
	 * Atiende {@code GET /api/telefonos/{tipoDocumento}/{numeroDocumento}/{telefono}} (consultar
	 * teléfono).
	 *
	 * @param ctx contexto de la petición y la respuesta HTTP; de él se leen los parámetros de ruta
	 *         {@code telefono}, {@code tipoDocumento} y {@code numeroDocumento}
	 * @throws SQLException si falla el acceso a la base de datos; se traduce a HTTP según su SQLState
	 * @throws RecursoNoEncontradoException si el registro no existe (HTTP 404)
	 */
	@OpenApi(
			path = "/api/telefonos/{tipoDocumento}/{numeroDocumento}/{telefono}",
			methods = HttpMethod.GET,
			summary = "Consultar teléfono",
			tags = {"Personas"},
			pathParams = {
					@OpenApiParam(name = "tipoDocumento", type = String.class),
					@OpenApiParam(name = "numeroDocumento", type = String.class),
					@OpenApiParam(name = "telefono", type = String.class)
			},
			responses = {
					@OpenApiResponse(status = "200", content = @OpenApiContent(from = Telefono.class)),
					@OpenApiResponse(status = "404", description = "Teléfono no encontrado")
			})
	public void consultar(Context ctx) throws SQLException, RecursoNoEncontradoException {
		ctx.json(telefonoService.consultar(
				ctx.pathParam("telefono"),
				ctx.pathParam("tipoDocumento"),
				ctx.pathParam("numeroDocumento")));
	}

	/**
	 * Atiende {@code POST /api/telefonos} (crear teléfono).
	 *
	 * @param ctx contexto de la petición y la respuesta HTTP; de él se lee el cuerpo JSON
	 * @throws SQLException si falla el acceso a la base de datos; se traduce a HTTP según su SQLState
	 * @throws DatoInvalidoException si el cuerpo o los parámetros no son válidos (HTTP 400)
	 */
	@OpenApi(
			path = "/api/telefonos",
			methods = HttpMethod.POST,
			summary = "Crear teléfono",
			tags = {"Personas"},
			requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Telefono.class)),
			responses = {
					@OpenApiResponse(status = "201", description = "Creado"),
					@OpenApiResponse(status = "400", description = "Datos inválidos"),
					@OpenApiResponse(status = "409", description = "El teléfono ya existe o la persona no existe")
			})
	public void crear(Context ctx) throws SQLException, DatoInvalidoException {
		Telefono t = ctx.bodyAsClass(Telefono.class);
		telefonoService.crear(t);
		ctx.status(HttpStatus.CREATED).json(Map.of("mensaje", "Teléfono creado"));
	}

	/**
	 * Atiende {@code PUT /api/telefonos/{tipoDocumento}/{numeroDocumento}/{telefono}} (cambiar el
	 * número de un teléfono). En el body envía solo telefono con el número nuevo.
	 *
	 * @param ctx contexto de la petición y la respuesta HTTP; de él se leen los parámetros de ruta
	 *         {@code telefono}, {@code tipoDocumento} y {@code numeroDocumento} y el cuerpo JSON
	 * @throws SQLException si falla el acceso a la base de datos; se traduce a HTTP según su SQLState
	 * @throws DatoInvalidoException si el cuerpo o los parámetros no son válidos (HTTP 400)
	 * @throws RecursoNoEncontradoException si el registro no existe (HTTP 404)
	 */
	@OpenApi(
			path = "/api/telefonos/{tipoDocumento}/{numeroDocumento}/{telefono}",
			methods = HttpMethod.PUT,
			summary = "Cambiar el número de un teléfono",
			description = "En el body envía solo telefono con el número nuevo.",
			tags = {"Personas"},
			pathParams = {
					@OpenApiParam(name = "tipoDocumento", type = String.class),
					@OpenApiParam(name = "numeroDocumento", type = String.class),
					@OpenApiParam(name = "telefono", type = String.class)
			},
			requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Telefono.class)),
			responses = {
					@OpenApiResponse(status = "200", description = "Actualizado"),
					@OpenApiResponse(status = "400", description = "Número inválido"),
					@OpenApiResponse(status = "404", description = "Teléfono no encontrado"),
					@OpenApiResponse(status = "409", description = "El número nuevo ya existe")
			})
	public void actualizar(Context ctx)
			throws SQLException, DatoInvalidoException, RecursoNoEncontradoException {
		Telefono t = ctx.bodyAsClass(Telefono.class);
		telefonoService.actualizar(
				ctx.pathParam("telefono"),
				ctx.pathParam("tipoDocumento"),
				ctx.pathParam("numeroDocumento"),
				t.getTelefono());
		ctx.status(HttpStatus.OK).json(Map.of("mensaje", "Teléfono actualizado"));
	}

	/**
	 * Atiende {@code DELETE /api/telefonos/{tipoDocumento}/{numeroDocumento}/{telefono}} (eliminar
	 * teléfono).
	 *
	 * @param ctx contexto de la petición y la respuesta HTTP; de él se leen los parámetros de ruta
	 *         {@code telefono}, {@code tipoDocumento} y {@code numeroDocumento}
	 * @throws SQLException si falla el acceso a la base de datos; se traduce a HTTP según su SQLState
	 * @throws RecursoNoEncontradoException si el registro no existe (HTTP 404)
	 */
	@OpenApi(
			path = "/api/telefonos/{tipoDocumento}/{numeroDocumento}/{telefono}",
			methods = HttpMethod.DELETE,
			summary = "Eliminar teléfono",
			tags = {"Personas"},
			pathParams = {
					@OpenApiParam(name = "tipoDocumento", type = String.class),
					@OpenApiParam(name = "numeroDocumento", type = String.class),
					@OpenApiParam(name = "telefono", type = String.class)
			},
			responses = {
					@OpenApiResponse(status = "200", description = "Eliminado"),
					@OpenApiResponse(status = "404", description = "Teléfono no encontrado")
			})
	public void eliminar(Context ctx) throws SQLException, RecursoNoEncontradoException {
		telefonoService.eliminar(
				ctx.pathParam("telefono"),
				ctx.pathParam("tipoDocumento"),
				ctx.pathParam("numeroDocumento"));
		ctx.status(HttpStatus.OK).json(Map.of("mensaje", "Teléfono eliminado"));
	}
	
}
