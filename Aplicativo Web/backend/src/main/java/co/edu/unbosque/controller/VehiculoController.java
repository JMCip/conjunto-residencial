package co.edu.unbosque.controller;

import io.javalin.http.Context;
import java.sql.SQLException;

import java.util.Map;

import co.edu.unbosque.exception.DatoInvalidoException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;
import io.javalin.http.HttpStatus;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiRequestBody;

import co.edu.unbosque.model.Vehiculo;
import co.edu.unbosque.service.VehiculoService;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;

public class VehiculoController {
	
	private final VehiculoService vehiculoService = new VehiculoService();
	
	@OpenApi(
			path = "/api/vehiculos",
			methods = HttpMethod.GET,
			summary = "Listar vehículos",
			description = "Devuelve los vehículos registrados con el documento de su dueño.",
			tags = {"Parqueadero"},
			responses = {
					@OpenApiResponse(status = "200", content = @OpenApiContent(from = Vehiculo[].class)),
					@OpenApiResponse(status = "500", description = "Error consultando la base de datos")
			})
	public void listar(Context conx) throws SQLException {
		conx.json(vehiculoService.listar());
	}
	
	@OpenApi(
			path = "/api/vehiculos/{placa}",
			methods = HttpMethod.GET,
			summary = "Consultar vehículo",
			tags = {"Parqueadero"},
			pathParams = {
					@OpenApiParam(name = "placa", type = String.class)
			},
			responses = {
					@OpenApiResponse(status = "200", content = @OpenApiContent(from = Vehiculo.class)),
					@OpenApiResponse(status = "404", description = "Vehículo no encontrado")
			})
	public void consultar(Context ctx) throws SQLException, RecursoNoEncontradoException {
		ctx.json(vehiculoService.consultar(ctx.pathParam("placa")));
	}

	@OpenApi(
			path = "/api/vehiculos",
			methods = HttpMethod.POST,
			summary = "Crear vehículo",
			tags = {"Parqueadero"},
			requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Vehiculo.class)),
			responses = {
					@OpenApiResponse(status = "201", description = "Creado"),
					@OpenApiResponse(status = "400", description = "Datos inválidos"),
					@OpenApiResponse(status = "409", description = "La placa ya existe o la persona no existe")
			})
	public void crear(Context ctx) throws SQLException, DatoInvalidoException {
		Vehiculo v = ctx.bodyAsClass(Vehiculo.class);
		vehiculoService.crear(v);
		ctx.status(HttpStatus.CREATED).json(Map.of("mensaje", "Vehículo creado"));
	}

	@OpenApi(
			path = "/api/vehiculos/{placa}",
			methods = HttpMethod.PUT,
			summary = "Actualizar vehículo",
			description = "La placa viaja en la URL y no se modifica. En el body envía marca, modelo, tipo y el documento del dueño.",
			tags = {"Parqueadero"},
			pathParams = {
					@OpenApiParam(name = "placa", type = String.class)
			},
			requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Vehiculo.class)),
			responses = {
					@OpenApiResponse(status = "200", description = "Actualizado"),
					@OpenApiResponse(status = "400", description = "Datos inválidos"),
					@OpenApiResponse(status = "404", description = "Vehículo no encontrado"),
					@OpenApiResponse(status = "409", description = "La persona no existe")
			})
	public void actualizar(Context ctx)
			throws SQLException, DatoInvalidoException, RecursoNoEncontradoException {
		Vehiculo v = ctx.bodyAsClass(Vehiculo.class);
		v.setPlaca(ctx.pathParam("placa"));
		vehiculoService.actualizar(v);
		ctx.status(HttpStatus.OK).json(Map.of("mensaje", "Vehículo actualizado"));
	}

	@OpenApi(
			path = "/api/vehiculos/{placa}",
			methods = HttpMethod.DELETE,
			summary = "Eliminar vehículo",
			tags = {"Parqueadero"},
			pathParams = {
					@OpenApiParam(name = "placa", type = String.class)
			},
			responses = {
					@OpenApiResponse(status = "200", description = "Eliminado"),
					@OpenApiResponse(status = "404", description = "Vehículo no encontrado"),
					@OpenApiResponse(status = "409", description = "El vehículo tiene parqueos asociados")
			})
	public void eliminar(Context ctx) throws SQLException, RecursoNoEncontradoException {
		vehiculoService.eliminar(ctx.pathParam("placa"));
		ctx.status(HttpStatus.OK).json(Map.of("mensaje", "Vehículo eliminado"));
	}
	
}
