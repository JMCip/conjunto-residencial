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
import co.edu.unbosque.model.Arriendo;
import co.edu.unbosque.service.ArriendoService;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;

/**
 * Controlador REST de los contratos de arriendo (rutas {@code /api/arriendos}).
 * <p>
 * Lee los parámetros y el cuerpo de la petición, delega en el servicio y responde en JSON. Las
 * excepciones se traducen a códigos HTTP en {@link co.edu.unbosque.App}.
 */
public class ArriendoController {
	
	/** Servicio con la lógica de negocio de los contratos de arriendo. */
	private final ArriendoService arriendoService = new ArriendoService();
	
	/**
	 * Atiende {@code GET /api/arriendos} (listar arriendos). Devuelve los contratos de arriendo,
	 * vigentes y finalizados.
	 *
	 * @param conx contexto de la petición y la respuesta HTTP
	 * @throws SQLException si falla el acceso a la base de datos; se traduce a HTTP según su SQLState
	 */
	@OpenApi(
			path = "/api/arriendos",
			methods = HttpMethod.GET,
			summary = "Listar arriendos",
			description = "Devuelve los contratos de arriendo, vigentes y finalizados.",
			tags = {"Casas"},
			responses = {
					@OpenApiResponse(status = "200", content = @OpenApiContent(from = Arriendo[].class)),
					@OpenApiResponse(status = "500", description = "Error consultando la base de datos")
			})
	public void listar(Context conx) throws SQLException {
		conx.json(arriendoService.listar());
	}
	
	/**
	 * Atiende {@code GET /api/arriendos/{id}} (consultar arriendo).
	 *
	 * @param ctx contexto de la petición y la respuesta HTTP
	 * @throws SQLException si falla el acceso a la base de datos; se traduce a HTTP según su SQLState
	 * @throws RecursoNoEncontradoException si el registro no existe (HTTP 404)
	 */
	@OpenApi(
			path = "/api/arriendos/{id}",
			methods = HttpMethod.GET,
			summary = "Consultar arriendo",
			tags = {"Casas"},
			pathParams = {
					@OpenApiParam(name = "id", type = Integer.class)
			},
			responses = {
					@OpenApiResponse(status = "200", content = @OpenApiContent(from = Arriendo.class)),
					@OpenApiResponse(status = "404", description = "Contrato no encontrado")
			})
	public void consultar(Context ctx) throws SQLException, RecursoNoEncontradoException {
		ctx.json(arriendoService.consultar(ctx.pathParamAsClass("id", Integer.class).get()));
	}

	/**
	 * Atiende {@code POST /api/arriendos} (crear arriendo). No envíes idContrato, se genera solo.
	 * Fechas en formato AAAA-MM-DD. fechaFin puede ir null. Para un contrato activo usa estadoContrato
	 * = VIGENTE.
	 *
	 * @param ctx contexto de la petición y la respuesta HTTP; de él se lee el cuerpo JSON
	 * @throws SQLException si falla el acceso a la base de datos; se traduce a HTTP según su SQLState
	 * @throws DatoInvalidoException si el cuerpo o los parámetros no son válidos (HTTP 400)
	 * @throws ConflictoException si la operación viola una regla de negocio (HTTP 409)
	 */
	@OpenApi(
			path = "/api/arriendos",
			methods = HttpMethod.POST,
			summary = "Crear arriendo",
			description = "No envíes idContrato, se genera solo. Fechas en formato AAAA-MM-DD. fechaFin puede ir null. Para un contrato activo usa estadoContrato = VIGENTE.",
			tags = {"Casas"},
			requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Arriendo.class)),
			responses = {
					@OpenApiResponse(status = "201", description = "Creado"),
					@OpenApiResponse(status = "400", description = "Datos inválidos"),
					@OpenApiResponse(status = "409", description = "La casa ya tiene contrato vigente, el arrendatario es propietario de la casa, el propietario no es vigente, o alguna persona/casa no existe")
			})
	public void crear(Context ctx) throws SQLException, DatoInvalidoException, ConflictoException {
		Arriendo a = ctx.bodyAsClass(Arriendo.class);
		int id = arriendoService.crear(a);
		ctx.status(HttpStatus.CREATED).json(Map.of("mensaje", "Contrato creado", "idContrato", id));
	}

	/**
	 * Atiende {@code PUT /api/arriendos/{id}} (actualizar arriendo). La casa, el arrendatario y el
	 * propietario no se modifican. En el body envía cuotaMensual, fechaInicio, fechaFin y
	 * estadoContrato (por ejemplo para finalizar el contrato).
	 *
	 * @param ctx contexto de la petición y la respuesta HTTP; de él se lee el cuerpo JSON
	 * @throws SQLException si falla el acceso a la base de datos; se traduce a HTTP según su SQLState
	 * @throws DatoInvalidoException si el cuerpo o los parámetros no son válidos (HTTP 400)
	 * @throws RecursoNoEncontradoException si el registro no existe (HTTP 404)
	 * @throws ConflictoException si la operación viola una regla de negocio (HTTP 409)
	 */
	@OpenApi(
			path = "/api/arriendos/{id}",
			methods = HttpMethod.PUT,
			summary = "Actualizar arriendo",
			description = "La casa, el arrendatario y el propietario no se modifican. En el body envía cuotaMensual, fechaInicio, fechaFin y estadoContrato (por ejemplo para finalizar el contrato).",
			tags = {"Casas"},
			pathParams = {
					@OpenApiParam(name = "id", type = Integer.class)
			},
			requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Arriendo.class)),
			responses = {
					@OpenApiResponse(status = "200", description = "Actualizado"),
					@OpenApiResponse(status = "400", description = "Datos inválidos"),
					@OpenApiResponse(status = "404", description = "Contrato no encontrado"),
					@OpenApiResponse(status = "409", description = "La casa ya tiene otro contrato vigente")
			})
	public void actualizar(Context ctx)
			throws SQLException, DatoInvalidoException, RecursoNoEncontradoException, ConflictoException {
		Arriendo a = ctx.bodyAsClass(Arriendo.class);
		a.setIdContrato(ctx.pathParamAsClass("id", Integer.class).get());
		arriendoService.actualizar(a);
		ctx.status(HttpStatus.OK).json(Map.of("mensaje", "Contrato actualizado"));
	}

	/**
	 * Atiende {@code DELETE /api/arriendos/{id}} (eliminar arriendo).
	 *
	 * @param ctx contexto de la petición y la respuesta HTTP
	 * @throws SQLException si falla el acceso a la base de datos; se traduce a HTTP según su SQLState
	 * @throws RecursoNoEncontradoException si el registro no existe (HTTP 404)
	 */
	@OpenApi(
			path = "/api/arriendos/{id}",
			methods = HttpMethod.DELETE,
			summary = "Eliminar arriendo",
			tags = {"Casas"},
			pathParams = {
					@OpenApiParam(name = "id", type = Integer.class)
			},
			responses = {
					@OpenApiResponse(status = "200", description = "Eliminado"),
					@OpenApiResponse(status = "404", description = "Contrato no encontrado"),
					@OpenApiResponse(status = "409", description = "El contrato tiene datos asociados")
			})
	public void eliminar(Context ctx) throws SQLException, RecursoNoEncontradoException {
		arriendoService.eliminar(ctx.pathParamAsClass("id", Integer.class).get());
		ctx.status(HttpStatus.OK).json(Map.of("mensaje", "Contrato eliminado"));
	}
	
}
