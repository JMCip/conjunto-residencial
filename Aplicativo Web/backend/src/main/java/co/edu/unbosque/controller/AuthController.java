package co.edu.unbosque.controller;

import co.edu.unbosque.exception.DatoInvalidoException;
import co.edu.unbosque.exception.NoAutorizadoException;
import co.edu.unbosque.model.AuthResponse;
import co.edu.unbosque.model.LoginRequest;
import co.edu.unbosque.service.AuthService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;

public class AuthController {

	private final AuthService authService = new AuthService();

	@OpenApi(
	        path = "/api/auth/login",
	        methods = HttpMethod.POST,
	        summary = "Iniciar sesión",
	        description = "Validar las credenciales del administrador y devuelve un token JWT. "
	                + "Ese token se envía en las demás peticiones con el encabezado Authorization: Bearer <token>.",
	        tags = {"Autenticación"},
	        requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = LoginRequest.class)),
	        responses = {
	                @OpenApiResponse(status = "200", content = @OpenApiContent(from = AuthResponse.class)),
	                @OpenApiResponse(status = "400", description = "Faltan ingresar usuario o clave"),
	                @OpenApiResponse(status = "401", description = "Usuario o contraseña son incorrectos")
	        })
	public void login(Context ctx) throws DatoInvalidoException, NoAutorizadoException {
	    LoginRequest login = ctx.bodyAsClass(LoginRequest.class);
	    AuthResponse respuesta = authService.login(login);
	    ctx.status(HttpStatus.OK).json(respuesta);
	}
}