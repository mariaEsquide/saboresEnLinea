package controlador;

import modelo.Valoracion;
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

// Gestor de Valoraciones

public class ValoracionController implements Serializable {
	private static final long serialVersionUID = 1L;

	// marca como transient los recursos no serializables
	private transient SessionFactory sessionFactory;

	public ValoracionController() {
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

	// método : obtiene todas las valoraciones
	public List<Valoracion> obtenerTodasLasValoraciones() {
		return DatabaseUtil.obtenerTodasLasValoraciones();
	}

	// método : obtiene valoración por ID
	public Valoracion obtenerValoracionPorId(int id) {
		return DatabaseUtil.obtenerValoracionPorId(id);
	}

	// método : actualiza valoración
	public void actualizarValoracion(Valoracion valoracion) {
		DatabaseUtil.actualizarValoracion(valoracion);
	}

	// método : elimina valoración
	public void eliminarValoracion(int id) {
		DatabaseUtil.eliminarValoracion(id);
	}

	// método : maneja la deserialización
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		// reinicializa la sessionFactory después de la deserialización
		initSessionFactory();
	}
}