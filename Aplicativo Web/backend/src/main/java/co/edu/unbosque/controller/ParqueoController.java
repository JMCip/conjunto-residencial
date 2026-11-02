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
import co.edu.unbosque.model.Parqueo;
import co.edu.unbosque.service.ParqueoService;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;

public class ParqueoController {
	
	private final ParqueoService parqueoService = new ParqueoService();
	
	@OpenApi(
			path = "/api/parqueos",
			methods = HttpMethod.GET,
			summary = "Listar parqueos",
			description = "Devuelve las entradas y salidas de vehículos, de la más reciente a la más antigua. Si fechaHoraSalida es null, el vehículo sigue parqueado.",
			tags = {"Parqueadero"},
			responses = {
					@OpenApiResponse(status = "200", content = @OpenApiContent(from = Parqueo[].class)),
					@OpenApiResponse(status = "500", description = "Error consultando la base de datos")
			})
	public void listar(Context conx) throws SQLException {
		conx.json(parqueoService.listar());
	}
	
	@OpenApi(
			path = "/api/parqueos/{id}",
			methods = HttpMethod.GET,
			summary = "Consultar parqueo",
			tags = {"Parqueadero"},
			pathParams = {
					@OpenApiParam(name = "id", type = Integer.class)
			},
			responses = {
					@OpenApiResponse(status = "200", content = @OpenApiContent(from = Parqueo.class)),
					@OpenApiResponse(status = "404", description = "Parqueo no encontrado")
			})
	public void consultar(Context ctx) throws SQLException, RecursoNoEncontradoException {
		ctx.json(parqueoService.consultar(ctx.pathParamAsClass("id", Integer.class).get()));
	}

	@OpenApi(
			path = "/api/parqueos",
			methods = HttpMethod.POST,
			summary = "Crear parqueo",
			description = "No envíes idParqueo, se genera solo. Fechas en formato AAAA-MM-DDTHH:MM:SS. fechaHoraSalida va null mientras el vehículo siga parqueado.",
			tags = {"Parqueadero"},
			requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Parqueo.class)),
			responses = {
					@OpenApiResponse(status = "201", description = "Creado"),
					@OpenApiResponse(status = "400", description = "Datos inválidos"),
					@OpenApiResponse(status = "409", description = "La celda o el vehículo ya tienen un parqueo abierto, o el vehículo o la celda no existen")
			})
	public void crear(Context ctx) throws SQLException, DatoInvalidoException, ConflictoException {
		Parqueo p = ctx.bodyAsClass(Parqueo.class);
		int id = parqueoService.crear(p);
		ctx.status(HttpStatus.CREATED).json(Map.of("mensaje", "Parqueo creado", "idParqueo", id));
	}

	@OpenApi(
			path = "/api/parqueos/{id}",
			methods = HttpMethod.PUT,
			summary = "Actualizar parqueo",
			description = "El vehículo y la celda no se modifican. En el body envía fechaHoraEntrada y fechaHoraSalida (para cerrar el parqueo).",
			tags = {"Parqueadero"},
			pathParams = {
					@OpenApiParam(name = "id", type = Integer.class)
			},
			requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Parqueo.class)),
			responses = {
					@OpenApiResponse(status = "200", description = "Actualizado"),
					@OpenApiResponse(status = "400", description = "Datos inválidos"),
					@OpenApiResponse(status = "404", description = "Parqueo no encontrado"),
					@OpenApiResponse(status = "409", description = "La celda o el vehículo ya tienen otro parqueo abierto")
			})
	public void actualizar(Context ctx)
			throws SQLException, DatoInvalidoException, RecursoNoEncontradoException, ConflictoException {
		Parqueo p = ctx.bodyAsClass(Parqueo.class);
		p.setIdParqueo(ctx.pathParamAsClass("id", Integer.class).get());
		parqueoService.actualizar(p);
		ctx.status(HttpStatus.OK).json(Map.of("mensaje", "Parqueo actualizado"));
	}

	@OpenApi(
			path = "/api/parqueos/{id}",
			methods = HttpMethod.DELETE,
			summary = "Eliminar parqueo",
			tags = {"Parqueadero"},
			pathParams = {
					@OpenApiParam(name = "id", type = Integer.class)
			},
			responses = {
					@OpenApiResponse(status = "200", description = "Eliminado"),
					@OpenApiResponse(status = "404", description = "Parqueo no encontrado")
			})
	public void eliminar(Context ctx) throws SQLException, RecursoNoEncontradoException {
		parqueoService.eliminar(ctx.pathParamAsClass("id", Integer.class).get());
		ctx.status(HttpStatus.OK).json(Map.of("mensaje", "Parqueo eliminado"));
	}
	
}
