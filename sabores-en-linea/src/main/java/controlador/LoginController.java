package controlador;

import modelo.Usuario;
import modelo.SessionManager;
import util.DatabaseUtil;
import vista.AdminFrame;
import vista.UserFrame;

import javax.swing.*;
import java.io.Serializable;

// Controlador de Autenticación y Sesiones

public class LoginController implements Serializable {
	private static final long serialVersionUID = 1L;

	// método : autenticar usuario
	public static boolean autenticarUsuario(String nombre, String password, JFrame parentFrame) {
		try {
			Usuario usuario = DatabaseUtil.autenticarUsuario(nombre, password);

			if (usuario != null) {
				// guarda el usuario en la sesión
				SessionManager.setUsuarioActual(usuario);

				// abre la ventana correspondiente según el tipo de usuario
				if (usuario.esAdmin()) {
					AdminFrame adminFrame = new AdminFrame();
					adminFrame.setVisible(true);
				} else {
					UserFrame userFrame = new UserFrame();
					userFrame.setVisible(true);
				}

				// cierra la ventana de login
				if (parentFrame != null) {
					parentFrame.dispose();
				}

				return true;
			} else {
				JOptionPane.showMessageDialog(parentFrame, "Nombre de usuario o contraseña incorrectos",
						"Error de autenticación", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(parentFrame, "Error al intentar autenticar: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}

	// método : registrar un nuevo usuario
	public static boolean registrarUsuario(String nombre, String email, String password, boolean esAdmin,
			JFrame parentFrame) {
		try {
			// verifica si ya existe un usuario con ese nombre
			Usuario usuarioExistente = DatabaseUtil.obtenerUsuarioPorNombre(nombre);

			if (usuarioExistente != null) {
				JOptionPane.showMessageDialog(parentFrame, "Ya existe un usuario con ese nombre", "Error de registro",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}

			// crea y guarda el nuevo usuario
			Usuario nuevoUsuario = new Usuario(nombre, email, password, esAdmin);
			DatabaseUtil.agregarUsuario(nuevoUsuario);

			JOptionPane.showMessageDialog(parentFrame, "Usuario registrado correctamente", "Registro exitoso",
					JOptionPane.INFORMATION_MESSAGE);

			return true;
		} catch (Exception e) {
			JOptionPane.showMessageDialog(parentFrame, "Error al registrar usuario: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}

	// método : cerrar sesión
	public static void cerrarSesion(JFrame currentFrame) {
		SessionManager.cerrarSesion();

		// cierra la ventana actual y abre la de login
		if (currentFrame != null) {
			currentFrame.dispose();
		}

		// abre la ventana de login
		vista.LoginFrame loginFrame = new vista.LoginFrame();
		loginFrame.setVisible(true);
	}
}