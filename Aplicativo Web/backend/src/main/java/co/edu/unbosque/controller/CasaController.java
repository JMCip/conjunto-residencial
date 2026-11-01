package co.edu.unbosque.controller;

import co.edu.unbosque.exception.DatoInvalidoException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;
import co.edu.unbosque.model.Casa;
import co.edu.unbosque.service.CasaService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.sql.SQLException;
import java.util.Map;

import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;

public class CasaController {

	private final CasaService casaService = new CasaService();

	@OpenApi(
            path = "/api/casas",
            methods = HttpMethod.GET,
            summary = "Listar casas",
            description = "Devuelve todas las casas del conjunto, ordenadas por manzana y número.",
            tags = {"Casas"},
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(from = Casa[].class)),
                    @OpenApiResponse(status = "500", description = "Error consultando la base de datos")
            })
	public void listar(Context conx) throws SQLException {
		conx.json(casaService.listar());
	}

	@OpenApi(
	        path = "/api/casas",
	        methods = HttpMethod.POST,
	        summary = "Crear casa",
	        tags = {"Casas"},
	        requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Casa.class)),
	        responses = {
	                @OpenApiResponse(status = "201", description = "Creada"),
	                @OpenApiResponse(status = "400", description = "Faltan area o estado"),
	                @OpenApiResponse(status = "409", description = "La casa ya existe")
	        })
	public void crear(Context ctx) throws SQLException, DatoInvalidoException {
	    Casa c = ctx.bodyAsClass(Casa.class);
	    casaService.crear(c);
	    ctx.status(HttpStatus.CREATED).json(Map.of("mensaje", "Casa creada"));
	}

	@OpenApi(
	        path = "/api/casas/{manzana}/{numero}",
	        methods = HttpMethod.PUT,
	        summary = "Actualizar casa",
	        description = "Envía area, estado y numeroHabitaciones. numeroHabitaciones es opcional.",
	        tags = {"Casas"},
	        pathParams = {
	                @OpenApiParam(name = "manzana", type = Integer.class),
	                @OpenApiParam(name = "numero", type = Integer.class)
	        },
	        requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Casa.class)),
	        responses = {
	                @OpenApiResponse(status = "200", description = "Actualizada"),
	                @OpenApiResponse(status = "400", description = "Faltan area o estado"),
	                @OpenApiResponse(status = "404", description = "Casa no encontrada")
	        })
	public void actualizar(Context ctx)
	        throws SQLException, DatoInvalidoException, RecursoNoEncontradoException {
	    Casa c = ctx.bodyAsClass(Casa.class);
	    c.setManzana(Integer.parseInt(ctx.pathParam("manzana")));
	    c.setNumero(Integer.parseInt(ctx.pathParam("numero")));
	    casaService.actualizar(c);
	    ctx.status(HttpStatus.OK).json(Map.of("mensaje", "Casa actualizada"));
	}

	@OpenApi(
	        path = "/api/casas/{manzana}/{numero}",
	        methods = HttpMethod.DELETE,
	        summary = "Eliminar casa",
	        tags = {"Casas"},
	        pathParams = {
	                @OpenApiParam(name = "manzana", type = Integer.class),
	                @OpenApiParam(name = "numero", type = Integer.class)
	        },
	        responses = {
	                @OpenApiResponse(status = "200", description = "Eliminada"),
	                @OpenApiResponse(status = "404", description = "Casa no encontrada"),
	                @OpenApiResponse(status = "409", description = "La casa tiene datos asociados")
	        })
	public void eliminar(Context ctx) throws SQLException, RecursoNoEncontradoException {
	    int manzana = Integer.parseInt(ctx.pathParam("manzana"));
	    int numero = Integer.parseInt(ctx.pathParam("numero"));
	    casaService.eliminar(numero, manzana);
	    ctx.status(HttpStatus.OK).json(Map.of("mensaje", "Casa eliminada"));
	}
}