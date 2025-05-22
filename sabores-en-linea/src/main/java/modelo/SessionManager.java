package modelo;

import java.io.Serializable;

public class SessionManager implements Serializable {
	private static final long serialVersionUID = 1L;
	private static Usuario usuarioActual;
	private static Long clienteId;
	@SuppressWarnings("unused")
	private static boolean isUser;

	public static void setUsuarioActual(Usuario usuario) {
		usuarioActual = usuario;
		if (usuario != null) {
			setUser(!usuario.esAdmin()); // isUser es true si NO es admin
			clienteId = (long) usuario.getId();
		} else {
			setUser(false);
			clienteId = null;
		}
	}

	public static Usuario getUsuarioActual() {
		return usuarioActual;
	}

	public static boolean isAdmin() {
		return usuarioActual != null && usuarioActual.esAdmin();
	}

	public static void cerrarSesion() {
		usuarioActual = null;
		clienteId = null;
	}

	public static void setClienteId(Long id) {
		clienteId = id;
	}

	public static Long getClienteId() {
		return clienteId;
	}

	public static boolean isUser() {
		return usuarioActual != null && !usuarioActual.esAdmin();
	}

	public static void logUserStatus(String location) {
		System.out.println("=== " + location + " ===");
		System.out.println("usuarioActual: "
				+ (usuarioActual != null ? usuarioActual.getNombre() + " (ID: " + usuarioActual.getId() + ")"
						: "null"));
		System.out.println("isAdmin: " + isAdmin());
		System.out.println("isUser: " + isUser());
		System.out.println("clienteId: " + clienteId);
		System.out.println("==================");
	}

	public static void setUser(boolean isUser) {
		SessionManager.isUser = isUser;
	}
}
