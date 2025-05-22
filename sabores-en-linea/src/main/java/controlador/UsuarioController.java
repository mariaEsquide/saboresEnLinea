package controlador;

import modelo.Usuario;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import util.DatabaseUtil;
import util.HibernateUtil;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.List;
import java.util.function.Function;

// Gestor de Usuarios

public class UsuarioController implements Serializable {
	private static final long serialVersionUID = 1L;

	// marca como transient los recursos no serializables
	private transient SessionFactory sessionFactory;

	public UsuarioController() {
		initSessionFactory();
	}

	private void initSessionFactory() {
		this.sessionFactory = HibernateUtil.getSessionFactory();
	}

	// método : ejecuta operaciones con una sesión de Hibernate
	public <T> T executeWithSession(Function<Session, T> operation) {
		if (sessionFactory == null) {
			initSessionFactory();
		}

		Session session = null;
		Transaction tx = null;
		try {
			session = sessionFactory.openSession();
			tx = session.beginTransaction();
			T result = operation.apply(session);
			tx.commit();
			return result;
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			throw e;
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	// método : autentifica usuario (cambiado a nombre en lugar de email)
	public Usuario autenticarUsuario(String nombre, String password) {
		return DatabaseUtil.autenticarUsuario(nombre, password);
	}

	// método : obtiene todos los usuarios
	public List<Usuario> obtenerTodosLosUsuarios() {
		return DatabaseUtil.obtenerTodosLosUsuarios();
	}

	// método : obtiene usuario por ID
	public Usuario obtenerUsuarioPorId(int id) {
		return DatabaseUtil.obtenerUsuarioPorId(id);
	}

	// método : obtiene usuario por nombre
	public Usuario obtenerUsuarioPorNombre(String nombre) {
		return DatabaseUtil.obtenerUsuarioPorNombre(nombre);
	}

	// método : actualiza usuario
	public void actualizarUsuario(Usuario usuario) {
		DatabaseUtil.actualizarUsuario(usuario);
	}

	// método : elimina usuario
	public void eliminarUsuario(int id) {
		DatabaseUtil.eliminarUsuario(id);
	}

	// método : elimina usuario por nombre
	public void eliminarUsuarioPorNombre(String nombre) {
		DatabaseUtil.eliminarUsuarioPorNombre(nombre);
	}

	// método : maneja la deserialización
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		// reinicia la sessionFactory después de la deserialización
		initSessionFactory();
	}
}