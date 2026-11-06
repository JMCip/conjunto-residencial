package co.edu.unbosque.controller;

import java.sql.SQLException;
import java.util.Map;

import co.edu.unbosque.exception.ConflictoException;
import co.edu.unbosque.exception.DatoInvalidoException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;
import co.edu.unbosque.model.ConceptoCobro;
import co.edu.unbosque.service.ConceptoCobroService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;

/**
 * Controlador REST de los conceptos de cobro (rutas {@code /api/conceptos}).
 * <p>
 * Lee los parámetros y el cuerpo de la petición, delega en el servicio y responde en JSON. Las
 * excepciones se traducen a códigos HTTP en {@link co.edu.unbosque.App}.
 */
public class ConceptoCobroController {

	/** Servicio con la lógica de negocio de los conceptos de cobro. */
	private final ConceptoCobroService conceptoCobroService = new ConceptoCobroService();

	/**
	 * Atiende {@code GET /api/conceptos} (listar conceptos de cobro). Devuelve los conceptos que se
	 * pueden cobrar en una factura y su valor base.
	 *
	 * @param ctx contexto de la petición y la respuesta HTTP
	 * @throws SQLException si falla el acceso a la base de datos; se traduce a HTTP según su SQLState
	 */
	@OpenApi(
			path = "/api/conceptos",
			methods = HttpMethod.GET,
			summary = "Listar conceptos de cobro",
			description = "Devuelve los conceptos que se pueden cobrar en una factura y su valor base.",
			tags = {"Finanzas"},
			responses = {
					@OpenApiResponse(status = "200", content = @OpenApiContent(from = ConceptoCobro[].class)),
					@OpenApiResponse(status = "500", description = "Error consultando la base de datos")
			})
	public void listar(Context ctx) throws SQLException {
		ctx.json(conceptoCobroService.listar());
	}

	/**
	 * Atiende {@code POST /api/conceptos} (crear concepto de cobro). Envía nombre, descripcion y
	 * valorBase (mayor que cero). El nombre no puede repetirse.
	 *
	 * @param ctx contexto de la petición y la respuesta HTTP; de él se lee el cuerpo JSON
	 * @throws SQLException si falla el acceso a la base de datos; se traduce a HTTP según su SQLState
	 * @throws DatoInvalidoException si el cuerpo o los parámetros no son válidos (HTTP 400)
	 * @throws ConflictoException si la operación viola una regla de negocio (HTTP 409)
	 */
	@OpenApi(
			path = "/api/conceptos",
			methods = HttpMethod.POST,
			summary = "Crear concepto de cobro",
			description = "Envía nombre, descripcion y valorBase (mayor que cero). El nombre no puede repetirse.",
			tags = {"Finanzas"},
			requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = ConceptoCobro.class)),
			responses = {
					@OpenApiResponse(status = "201", description = "Creado"),
					@OpenApiResponse(status = "400", description = "Datos inválidos"),
					@OpenApiResponse(status = "409", description = "Ya existe un concepto con ese nombre")
			})
	public void crear(Context ctx) throws SQLException, DatoInvalidoException, ConflictoException {
		ConceptoCobro c = ctx.bodyAsClass(ConceptoCobro.class);
		int id = conceptoCobroService.crear(c);
		ctx.status(HttpStatus.CREATED).json(Map.of("mensaje", "Concepto de cobro creado", "idConcepto", id));
	}

	/**
	 * Atiende {@code PUT /api/conceptos/{id}} (actualizar concepto de cobro). Envía nombre,
	 * descripcion y valorBase. Cambiar el valor base no modifica las facturas ya emitidas.
	 *
	 * @param ctx contexto de la petición y la respuesta HTTP; de él se leen el parámetro de ruta
	 *         {@code id} y el cuerpo JSON
	 * @throws SQLException si falla el acceso a la base de datos; se traduce a HTTP según su SQLState
	 * @throws DatoInvalidoException si el cuerpo o los parámetros no son válidos (HTTP 400)
	 * @throws RecursoNoEncontradoException si el registro no existe (HTTP 404)
	 * @throws ConflictoException si la operación viola una regla de negocio (HTTP 409)
	 */
	@OpenApi(
			path = "/api/conceptos/{id}",
			methods = HttpMethod.PUT,
			summary = "Actualizar concepto de cobro",
			description = "Envía nombre, descripcion y valorBase. Cambiar el valor base no modifica las facturas ya emitidas. "
					+ "Los conceptos Sanción, Salón comunal y Parqueadero adicional no se pueden renombrar.",
			tags = {"Finanzas"},
			pathParams = { @OpenApiParam(name = "id", type = Integer.class) },
			requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = ConceptoCobro.class)),
			responses = {
					@OpenApiResponse(status = "200", description = "Concepto actualizado"),
					@OpenApiResponse(status = "400", description = "Datos inválidos"),
					@OpenApiResponse(status = "404", description = "Concepto no encontrado"),
					@OpenApiResponse(status = "409", description = "Nombre repetido o concepto protegido")
			})
	public void actualizar(Context ctx) throws SQLException, DatoInvalidoException,
			RecursoNoEncontradoException, ConflictoException {
		int id = Integer.parseInt(ctx.pathParam("id"));
		ConceptoCobro cambios = ctx.bodyAsClass(ConceptoCobro.class);
		conceptoCobroService.actualizar(id, cambios);
		ctx.status(HttpStatus.OK).json(Map.of("mensaje", "Concepto de cobro actualizado"));
	}

	/**
	 * Atiende {@code DELETE /api/conceptos/{id}} (eliminar concepto de cobro). Solo conceptos que no
	 * se hayan usado en ninguna línea de factura y que no sean Sanción, Salón comunal ni Parqueadero
	 * adicional.
	 *
	 * @param ctx contexto de la petición y la respuesta HTTP; de él se leen el parámetro de ruta
	 *         {@code id}
	 * @throws SQLException si falla el acceso a la base de datos; se traduce a HTTP según su SQLState
	 * @throws RecursoNoEncontradoException si el registro no existe (HTTP 404)
	 * @throws ConflictoException si la operación viola una regla de negocio (HTTP 409)
	 */
	@OpenApi(
			path = "/api/conceptos/{id}",
			methods = HttpMethod.DELETE,
			summary = "Eliminar concepto de cobro",
			description = "Solo conceptos que no se hayan usado en ninguna línea de factura y que no sean Sanción, Salón comunal ni Parqueadero adicional.",
			tags = {"Finanzas"},
			pathParams = { @OpenApiParam(name = "id", type = Integer.class) },
			responses = {
					@OpenApiResponse(status = "200", description = "Eliminado"),
					@OpenApiResponse(status = "404", description = "Concepto no encontrado"),
					@OpenApiResponse(status = "409", description = "Concepto protegido o ya usado en facturas")
			})
	public void eliminar(Context ctx) throws SQLException, RecursoNoEncontradoException, ConflictoException {
		int id = Integer.parseInt(ctx.pathParam("id"));
		conceptoCobroService.eliminar(id);
		ctx.status(HttpStatus.OK).json(Map.of("mensaje", "Concepto de cobro eliminado"));
	}
}