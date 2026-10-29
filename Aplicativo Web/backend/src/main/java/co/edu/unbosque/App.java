package co.edu.unbosque;

import co.edu.unbosque.config.ConexionBD;
import co.edu.unbosque.controller.CasaController;
import io.javalin.Javalin;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

public class App {

    public static void main(String[] args) {
        CasaController casaController = new CasaController();

        Javalin.create(config -> {
            config.registerPlugin(new OpenApiPlugin(openApi -> {}));
            config.registerPlugin(new SwaggerPlugin());

            config.routes.get("/api/estado", ctx -> ctx.result("Backend del Conjunto Residencial funcionando"));

            config.routes.get("/api/bd/prueba", ctx -> {
                String sql = "SELECT current_user, current_database(), version()";
                try (Connection cox = ConexionBD.obtenerConexion();
                     Statement st = cox.createStatement();
                     ResultSet rs = st.executeQuery(sql)) {
                    rs.next();
                    ctx.json(Map.of(
                            "usuario", rs.getString(1),
                            "base", rs.getString(2),
                            "version", rs.getString(3)));
                }
            });

            //---------------------------------------- Casa -----------------
            config.routes.get("/api/casas", ctx -> casaController.listar(ctx));

            config.routes.exception(SQLException.class, (e, conx) ->
                    conx.status(500).json(Map.of(
                            "error", "Error en la consulta de la BD",
                            "Descripcion", e.getMessage())));
        }).start(8080);
    }
}