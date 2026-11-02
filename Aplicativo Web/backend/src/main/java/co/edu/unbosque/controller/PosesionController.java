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

import co.edu.unbosque.model.Posesion;
import co.edu.unbosque.service.PosesionService;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;

public class PosesionController {
	
	private final PosesionService posesionService = new PosesionService();
	
	@OpenApi(
			path = "/api/posesiones",
			methods = HttpMethod.GET,
			summary = "Listar posesiones",
			description = "Devuelve el historial de propietarios de cada casa. Si fechaVenta es null, es el dueño actual.",
			tags = {"Casas"},
			responses = {
					@OpenApiResponse(status = "200", content = @OpenApiContent(from = Posesion[].class)),
					@OpenApiResponse(status = "500", description = "Error consultando la base de datos")
			})
	public void listar(Context conx) throws SQLException {
		conx.json(posesionService.listar());
	}
	
	@OpenApi(
			path = "/api/posesiones/{id}",
			methods = HttpMethod.GET,
			summary = "Consultar posesión",
			tags = {"Casas"},
			pathParams = {
					@OpenApiParam(name = "id", type = Integer.class)
			},
			responses = {
					@OpenApiResponse(status = "200", content = @OpenApiContent(from = Posesion.class)),
					@OpenApiResponse(status = "404", description = "Posesión no encontrada")
			})
	public void consultar(Context ctx) throws SQLException, RecursoNoEncontradoException {
		ctx.json(posesionService.consultar(ctx.pathParamAsClass("id", Integer.class).get()));
	}

	@OpenApi(
			path = "/api/posesiones",
			methods = HttpMethod.POST,
			summary = "Crear posesión",
			description = "No envíes idPosesion, se genera solo. fechaVenta se deja null mientras la persona sea dueña. Fechas en formato AAAA-MM-DD.",
			tags = {"Casas"},
			requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Posesion.class)),
			responses = {
					@OpenApiResponse(status = "201", description = "Creada"),
					@OpenApiResponse(status = "400", description = "Datos inválidos"),
					@OpenApiResponse(status = "409", description = "La casa o el propietario no existen, o los porcentajes vigentes superan 100")
			})
	public void crear(Context ctx) throws SQLException, DatoInvalidoException, ConflictoException {
		Posesion p = ctx.bodyAsClass(Posesion.class);
		int id = posesionService.crear(p);
		ctx.status(HttpStatus.CREATED).json(Map.of("mensaje", "Posesión creada", "idPosesion", id));
	}

	@OpenApi(
			path = "/api/posesiones/{id}",
			methods = HttpMethod.PUT,
			summary = "Actualizar posesión",
			description = "La casa y el propietario no se modifican. En el body envía porcentaje, fechaCompra y fechaVenta (para registrar la venta).",
			tags = {"Casas"},
			pathParams = {
					@OpenApiParam(name = "id", type = Integer.class)
			},
			requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Posesion.class)),
			responses = {
					@OpenApiResponse(status = "200", description = "Actualizada"),
					@OpenApiResponse(status = "400", description = "Datos inválidos"),
					@OpenApiResponse(status = "404", description = "Posesión no encontrada"),
					@OpenApiResponse(status = "409", description = "Los porcentajes vigentes superan 100")
			})
	public void actualizar(Context ctx)
			throws SQLException, DatoInvalidoException, RecursoNoEncontradoException, ConflictoException {
		Posesion p = ctx.bodyAsClass(Posesion.class);
		p.setIdPosesion(ctx.pathParamAsClass("id", Integer.class).get());
		posesionService.actualizar(p);
		ctx.status(HttpStatus.OK).json(Map.of("mensaje", "Posesión actualizada"));
	}

	@OpenApi(
			path = "/api/posesiones/{id}",
			methods = HttpMethod.DELETE,
			summary = "Eliminar posesión",
			tags = {"Casas"},
			pathParams = {
					@OpenApiParam(name = "id", type = Integer.class)
			},
			responses = {
					@OpenApiResponse(status = "200", description = "Eliminada"),
					@OpenApiResponse(status = "404", description = "Posesión no encontrada")
			})
	public void eliminar(Context ctx) throws SQLException, RecursoNoEncontradoException {
		posesionService.eliminar(ctx.pathParamAsClass("id", Integer.class).get());
		ctx.status(HttpStatus.OK).json(Map.of("mensaje", "Posesión eliminada"));
	}
	
	
}
