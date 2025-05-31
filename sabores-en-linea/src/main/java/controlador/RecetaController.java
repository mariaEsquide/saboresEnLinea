package controlador;

import modelo.*;
import util.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// Administrador de Recetas obteniendo todas las recetas de la BBDD

public class RecetaController implements Serializable {
	private static final long serialVersionUID = 1L;

	public List<Receta> obtenerTodasLasRecetas() {
		Session session = null;
		Transaction tx = null;
		List<Receta> recetas = new ArrayList<>();

		try {
			session = HibernateUtil.getSessionFactory().openSession();
			tx = session.beginTransaction();

			Query<Receta> query = session.createQuery("FROM Receta", Receta.class);
			recetas = query.list();

			tx.commit();
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}

		return recetas;
	}

	public void agregarReceta(Receta receta) {
		Session session = null;
		Transaction tx = null;

		try {
			session = HibernateUtil.getSessionFactory().openSession();
			tx = session.beginTransaction();

			session.persist(receta);

			tx.commit();
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	public void actualizarReceta(Receta receta) {
		Session session = null;
		Transaction tx = null;

		try {
			session = HibernateUtil.getSessionFactory().openSession();
			tx = session.beginTransaction();

			session.merge(receta);

			tx.commit();
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	public void eliminarReceta(int id) {
		Session session = null;
		Transaction tx = null;

		try {
			session = HibernateUtil.getSessionFactory().openSession();
			tx = session.beginTransaction();

			Receta receta = session.get(Receta.class, id);
			if (receta != null) {
				session.remove(receta);
			}

			tx.commit();
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	public List<Receta> buscarRecetas(String busqueda, String categoria, String dificultad) {
		Session session = null;
		Transaction tx = null;
		List<Receta> recetas = new ArrayList<>();

		try {
			session = HibernateUtil.getSessionFactory().openSession();
			tx = session.beginTransaction();

			StringBuilder hql = new StringBuilder("FROM Receta r WHERE 1=1");

			// añade condiciones de búsqueda
			if (busqueda != null && !busqueda.trim().isEmpty()) {
				hql.append(
						" AND (LOWER(r.titulo) LIKE LOWER(:busqueda) OR LOWER(r.descripcion) LIKE LOWER(:busqueda))");
			}

			// filtra por categoría si no es "Todas"
			if (categoria != null && !categoria.equals("Todas")) {
				hql.append(" AND r.categoria.nombre = :categoria");
			}

			// filtra por dificultad si no es "Todas"
			if (dificultad != null && !dificultad.equals("Todas")) {
				hql.append(" AND r.dificultad = :dificultad");
			}

			Query<Receta> query = session.createQuery(hql.toString(), Receta.class);

			// establece parámetros
			if (busqueda != null && !busqueda.trim().isEmpty()) {
				query.setParameter("busqueda", "%" + busqueda.trim() + "%");
			}

			if (categoria != null && !categoria.equals("Todas")) {
				query.setParameter("categoria", categoria);
			}

			if (dificultad != null && !dificultad.equals("Todas")) {
				query.setParameter("dificultad", dificultad);
			}

			recetas = query.list();
			tx.commit();
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}

		return recetas;
	}

	public Receta obtenerRecetaPorId(int id) {
		Session session = null;
		Transaction tx = null;
		Receta receta = null;

		try {
			session = HibernateUtil.getSessionFactory().openSession();
			tx = session.beginTransaction();

			receta = session.get(Receta.class, id);

			tx.commit();
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}

		return receta;
	}

	public void agregarFotoAReceta(FotoReceta foto) {
		Session session = null;
		Transaction tx = null;

		try {
			session = HibernateUtil.getSessionFactory().openSession();
			tx = session.beginTransaction();

			if (foto.getReceta() != null) {
				Receta receta = session.get(Receta.class, foto.getReceta().getId());
				if (receta != null) {
					foto.setReceta(receta);
					session.persist(foto);
					receta.getFotos().add(foto);
					session.merge(receta);
				}
			} else {
				throw new IllegalArgumentException("La foto debe tener una receta asociada");
			}

			tx.commit();
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
			throw new RuntimeException("Error al agregar foto a receta: " + e.getMessage(), e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	public void agregarVideoAReceta(VideoReceta video) {
		Session session = null;
		Transaction tx = null;

		try {
			session = HibernateUtil.getSessionFactory().openSession();
			tx = session.beginTransaction();

			if (video.getReceta() != null) {
				Receta receta = session.get(Receta.class, video.getReceta().getId());
				if (receta != null) {
					video.setReceta(receta);
					session.persist(video);
					receta.getVideos().add(video);
					session.merge(receta);
				}
			} else {
				throw new IllegalArgumentException("El video debe tener una receta asociada");
			}

			tx.commit();
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
			throw new RuntimeException("Error al agregar video a receta: " + e.getMessage(), e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}
}