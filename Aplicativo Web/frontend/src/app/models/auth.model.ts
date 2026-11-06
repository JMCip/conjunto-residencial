/** Credenciales que se envían a POST /api/auth/login. */
export interface LoginRequestModel {
  /** Nombre de usuario del administrador. */
  usuario: string;
  /** Contraseña del administrador. */
  clave: string;
}

/** Respuesta de POST /api/auth/login cuando las credenciales son correctas. */
export interface LoginResponseModel {
  /** Token JWT que se envía en el encabezado Authorization de las demás peticiones. */
  token: string;
  /** Rol del usuario autenticado. */
  roles: string;
}
