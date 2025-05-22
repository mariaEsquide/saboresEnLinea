package controlador;

import modelo.Categoria;
import util.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// Gestor de Categorías de Recetas, obteniendo todas las categorías de la BBDD 

public class CategoriaController implements Serializable {
	private static final long serialVersionUID = 1L;

	public List<Categoria> obtenerTodasLasCategorias() {
		Session session = null;
		Transaction tx = null;
		List<Categoria> categorias = new ArrayList<>();

		try {
			session = HibernateUtil.getSessionFactory().openSession();
			tx = session.beginTransaction();

			Query<Categoria> query = session.createQuery("FROM Categoria", Categoria.class);
			categorias = query.list();

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

		return categorias;
	}

	public void agregarCategoria(Categoria categoria) {
		Session session = null;
		Transaction tx = null;

		try {
			session = HibernateUtil.getSessionFactory().openSession();
			tx = session.beginTransaction();

			session.persist(categoria);

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

	public void actualizarCategoria(Categoria categoria) {
		Session session = null;
		Transaction tx = null;

		try {
			session = HibernateUtil.getSessionFactory().openSession();
			tx = session.beginTransaction();

			session.merge(categoria);

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

	public void eliminarCategoria(int id) {
		Session session = null;
		Transaction tx = null;

		try {
			session = HibernateUtil.getSessionFactory().openSession();
			tx = session.beginTransaction();

			Categoria categoria = session.get(Categoria.class, id);
			if (categoria != null) {
				session.remove(categoria);
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

	public Categoria obtenerCategoriaPorId(int id) {
		Session session = null;
		Transaction tx = null;
		Categoria categoria = null;

		try {
			session = HibernateUtil.getSessionFactory().openSession();
			tx = session.beginTransaction();

			categoria = session.get(Categoria.class, id);

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

		return categoria;
	}
}