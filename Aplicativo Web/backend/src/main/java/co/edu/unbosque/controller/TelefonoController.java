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

public class TelefonoController {
	
	private final TelefonoService telefonoService = new TelefonoService();
	
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
