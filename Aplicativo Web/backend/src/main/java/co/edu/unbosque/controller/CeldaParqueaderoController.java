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

public class CeldaParqueaderoController {
	
	private final CeldaParqueaderoService celdaParqueaderoService = new CeldaParqueaderoService();
	
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
