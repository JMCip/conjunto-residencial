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

public class ConceptoCobroController {

	private final ConceptoCobroService conceptoCobroService = new ConceptoCobroService();

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