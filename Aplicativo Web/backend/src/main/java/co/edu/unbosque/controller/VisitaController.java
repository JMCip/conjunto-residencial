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
import co.edu.unbosque.model.Visita;
import co.edu.unbosque.service.VisitaService;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;

public class VisitaController {
	
	private final VisitaService visitaService = new VisitaService();
	
	@OpenApi(
			path = "/api/visitas",
			methods = HttpMethod.GET,
			summary = "Listar visitas",
			description = "Devuelve las visitas registradas, de la más reciente a la más antigua. Si fechaSalida es null, el visitante sigue en el conjunto.",
			tags = {"Casas"},
			responses = {
					@OpenApiResponse(status = "200", content = @OpenApiContent(from = Visita[].class)),
					@OpenApiResponse(status = "500", description = "Error consultando la base de datos")
			})
	public void listar(Context conx) throws SQLException {
		conx.json(visitaService.listar());
	}
	
	
	@OpenApi(
			path = "/api/visitas/{id}",
			methods = HttpMethod.GET,
			summary = "Consultar visita",
			tags = {"Casas"},
			pathParams = {
					@OpenApiParam(name = "id", type = Integer.class)
			},
			responses = {
					@OpenApiResponse(status = "200", content = @OpenApiContent(from = Visita.class)),
					@OpenApiResponse(status = "404", description = "Visita no encontrada")
			})
	public void consultar(Context ctx) throws SQLException, RecursoNoEncontradoException {
		ctx.json(visitaService.consultar(ctx.pathParamAsClass("id", Integer.class).get()));
	}

	@OpenApi(
			path = "/api/visitas",
			methods = HttpMethod.POST,
			summary = "Crear visita",
			description = "No envíes idVisita, se genera solo. Fechas en formato AAAA-MM-DD. fechaSalida va null mientras la visita siga vigente.",
			tags = {"Casas"},
			requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Visita.class)),
			responses = {
					@OpenApiResponse(status = "201", description = "Creada"),
					@OpenApiResponse(status = "400", description = "Datos inválidos"),
					@OpenApiResponse(status = "409", description = "La casa o el visitante no existen")
			})
	public void crear(Context ctx) throws SQLException, DatoInvalidoException, ConflictoException {
		Visita v = ctx.bodyAsClass(Visita.class);
		int id = visitaService.crear(v);
		ctx.status(HttpStatus.CREATED).json(Map.of("mensaje", "Visita creada", "idVisita", id));
	}

	@OpenApi(
			path = "/api/visitas/{id}",
			methods = HttpMethod.PUT,
			summary = "Actualizar visita",
			description = "La casa y el visitante no se modifican. En el body envía fechaEntrada y fechaSalida (para cerrar la visita).",
			tags = {"Casas"},
			pathParams = {
					@OpenApiParam(name = "id", type = Integer.class)
			},
			requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Visita.class)),
			responses = {
					@OpenApiResponse(status = "200", description = "Actualizada"),
					@OpenApiResponse(status = "400", description = "Datos inválidos"),
					@OpenApiResponse(status = "404", description = "Visita no encontrada")
			})
	public void actualizar(Context ctx)
			throws SQLException, DatoInvalidoException, RecursoNoEncontradoException {
		Visita v = ctx.bodyAsClass(Visita.class);
		v.setIdVisita(ctx.pathParamAsClass("id", Integer.class).get());
		visitaService.actualizar(v);
		ctx.status(HttpStatus.OK).json(Map.of("mensaje", "Visita actualizada"));
	}

	@OpenApi(
			path = "/api/visitas/{id}",
			methods = HttpMethod.DELETE,
			summary = "Eliminar visita",
			tags = {"Casas"},
			pathParams = {
					@OpenApiParam(name = "id", type = Integer.class)
			},
			responses = {
					@OpenApiResponse(status = "200", description = "Eliminada"),
					@OpenApiResponse(status = "404", description = "Visita no encontrada"),
					@OpenApiResponse(status = "409", description = "La visita tiene datos asociados")
			})
	public void eliminar(Context ctx) throws SQLException, RecursoNoEncontradoException {
		visitaService.eliminar(ctx.pathParamAsClass("id", Integer.class).get());
		ctx.status(HttpStatus.OK).json(Map.of("mensaje", "Visita eliminada"));
	}
}
