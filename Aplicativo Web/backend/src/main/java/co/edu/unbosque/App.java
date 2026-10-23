package co.edu.unbosque;

import co.edu.unbosque.config.ConexionBD;
import io.javalin.Javalin;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

public class App {

    public static void main(String[] args) {
        Javalin.create(config -> {
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
        }).start(8080);
    }
}