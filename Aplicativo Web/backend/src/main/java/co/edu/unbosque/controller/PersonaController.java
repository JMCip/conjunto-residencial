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


public class PersonaController {
	private final PersonaService pService = new PersonaService();
	
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
