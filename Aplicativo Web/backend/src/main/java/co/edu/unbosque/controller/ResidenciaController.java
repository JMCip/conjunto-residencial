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
import co.edu.unbosque.model.Residencia;
import co.edu.unbosque.service.ResidenciaService;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;

public class ResidenciaController {
	
	private final ResidenciaService residenciaService = new ResidenciaService();
	
	@OpenApi(
			path = "/api/residencias",
			methods = HttpMethod.GET,
			summary = "Listar residencias",
			description = "Devuelve quién vive o ha vivido en cada casa. Si fechaSalida es null, vive ahí actualmente.",
			tags = {"Casas"},
			responses = {
					@OpenApiResponse(status = "200", content = @OpenApiContent(from = Residencia[].class)),
					@OpenApiResponse(status = "500", description = "Error consultando la base de datos")
			})
	public void listar(Context conx) throws SQLException {
		conx.json(residenciaService.listar());
	}
	
	@OpenApi(
			path = "/api/residencias/{id}",
			methods = HttpMethod.GET,
			summary = "Consultar residencia",
			tags = {"Casas"},
			pathParams = {
					@OpenApiParam(name = "id", type = Integer.class)
			},
			responses = {
					@OpenApiResponse(status = "200", content = @OpenApiContent(from = Residencia.class)),
					@OpenApiResponse(status = "404", description = "Residencia no encontrada")
			})
	public void consultar(Context ctx) throws SQLException, RecursoNoEncontradoException {
		ctx.json(residenciaService.consultar(ctx.pathParamAsClass("id", Integer.class).get()));
	}

	@OpenApi(
			path = "/api/residencias",
			methods = HttpMethod.POST,
			summary = "Crear residencia",
			description = "No envíes idResidencia, se genera solo. Fechas en formato AAAA-MM-DD. fechaSalida va null mientras la persona viva en la casa.",
			tags = {"Casas"},
			requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Residencia.class)),
			responses = {
					@OpenApiResponse(status = "201", description = "Creada"),
					@OpenApiResponse(status = "400", description = "Datos inválidos"),
					@OpenApiResponse(status = "409", description = "Ya es residente vigente de esa casa, o la casa o el residente no existen")
			})
	public void crear(Context ctx) throws SQLException, DatoInvalidoException, ConflictoException {
		Residencia r = ctx.bodyAsClass(Residencia.class);
		int id = residenciaService.crear(r);
		ctx.status(HttpStatus.CREATED).json(Map.of("mensaje", "Residencia creada", "idResidencia", id));
	}

	@OpenApi(
			path = "/api/residencias/{id}",
			methods = HttpMethod.PUT,
			summary = "Actualizar residencia",
			description = "La casa y el residente no se modifican. En el body envía fechaIngreso y fechaSalida (para cerrar la ocupación).",
			tags = {"Casas"},
			pathParams = {
					@OpenApiParam(name = "id", type = Integer.class)
			},
			requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Residencia.class)),
			responses = {
					@OpenApiResponse(status = "200", description = "Actualizada"),
					@OpenApiResponse(status = "400", description = "Datos inválidos"),
					@OpenApiResponse(status = "404", description = "Residencia no encontrada"),
					@OpenApiResponse(status = "409", description = "Ya existe otra residencia vigente de esa persona en la casa")
			})
	public void actualizar(Context ctx)
			throws SQLException, DatoInvalidoException, RecursoNoEncontradoException, ConflictoException {
		Residencia r = ctx.bodyAsClass(Residencia.class);
		r.setIdResidencia(ctx.pathParamAsClass("id", Integer.class).get());
		residenciaService.actualizar(r);
		ctx.status(HttpStatus.OK).json(Map.of("mensaje", "Residencia actualizada"));
	}

	@OpenApi(
			path = "/api/residencias/{id}",
			methods = HttpMethod.DELETE,
			summary = "Eliminar residencia",
			tags = {"Casas"},
			pathParams = {
					@OpenApiParam(name = "id", type = Integer.class)
			},
			responses = {
					@OpenApiResponse(status = "200", description = "Eliminada"),
					@OpenApiResponse(status = "404", description = "Residencia no encontrada"),
					@OpenApiResponse(status = "409", description = "La residencia tiene datos asociados")
			})
	public void eliminar(Context ctx) throws SQLException, RecursoNoEncontradoException {
		residenciaService.eliminar(ctx.pathParamAsClass("id", Integer.class).get());
		ctx.status(HttpStatus.OK).json(Map.of("mensaje", "Residencia eliminada"));
	}
	
}
