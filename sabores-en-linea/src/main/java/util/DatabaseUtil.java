package util;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;
import modelo.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Clase de utilidad para operaciones de BBDD con Hibernate

public class DatabaseUtil implements Serializable {
    private static final long serialVersionUID = 1L;

    // transient: evita serializar la SessionFactory
    private static transient SessionFactory sessionFactory;

    static {
        initSessionFactory();
    }

    private static void initSessionFactory() {
        try {
            // inicializa la SessionFactory de Hibernate
            Configuration configuration = new Configuration().configure("hibernate.cfg.xml");

            // configuración para manejar objetos grandes
            configuration.setProperty("hibernate.jdbc.batch_size", "20");
            configuration.setProperty("hibernate.jdbc.fetch_size", "50");
            configuration.setProperty("hibernate.jdbc.batch_versioned_data", "true");
            configuration.setProperty("hibernate.connection.provider_class",
                    "org.hibernate.connection.C3P0ConnectionProvider");
            configuration.setProperty("hibernate.c3p0.max_size", "20");
            configuration.setProperty("hibernate.c3p0.min_size", "5");
            configuration.setProperty("hibernate.c3p0.timeout", "1800");
            configuration.setProperty("hibernate.c3p0.max_statements", "50");

            sessionFactory = configuration.buildSessionFactory();

        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    // obtiene una nueva sesión de Hibernate
    public static Session getSession() {
        if (sessionFactory == null || sessionFactory.isClosed()) {
            initSessionFactory();
        }
        return sessionFactory.openSession();
    }
    
    // obtiene una conexión JDBC directa desde el pool de conexiones de Hibernate
  
    public static Connection getConnection() throws SQLException {
        if (sessionFactory == null || sessionFactory.isClosed()) {
            initSessionFactory();
        }
        
        try {
        	 // metodo : obtiene una conexión directa
            Session session = sessionFactory.openSession();
            Connection connection = session.doReturningWork(conn -> {
                // devuelve una copia de la conexión para que no se cierre cuando se cierre la sesión
                return conn;
            });
            
            // no cerramos la sesión aquí para mantener la conexión abierta
            // la conexión se cerrará cuando se cierre explícitamente
            
            if (connection == null) {
                throw new SQLException("No se pudo obtener una conexión válida de Hibernate");
            }
            
            return connection;
        } catch (Exception e) {
            System.err.println("Error al obtener conexión JDBC: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("Error al obtener conexión JDBC: " + e.getMessage(), e);
        }
    }

    // metodo : maneja la deserialización
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        // Si la sessionFactory es null o está cerrada, la inicializamos
        if (sessionFactory == null || sessionFactory.isClosed()) {
            initSessionFactory();
        }
    }

    // autentica un usuario por nombre y contraseña
    public static Usuario autenticarUsuario(String nombre, String password) {
        try (Session session = getSession()) {
            Query<Usuario> query = session.createQuery("FROM Usuario WHERE nombre = :nombre AND password = :password",
                    Usuario.class);
            query.setParameter("nombre", nombre);
            query.setParameter("password", password);
            return query.uniqueResult();
        }
    }

    // obtiene todas las recetas de la BBDD
    public static List<Receta> obtenerTodasLasRecetas() {
        try (Session session = getSession()) {
            return session.createQuery("FROM Receta", Receta.class).list();
        }
    }

    // agrega nueva receta a la BBDD
    public static void agregarReceta(Receta receta) {
        try (Session session = getSession()) {
            session.beginTransaction();
            session.persist(receta);
            session.getTransaction().commit();
        }
    }

    // actualiza receta en la BBDD
    public static void actualizarReceta(Receta receta) {
        try (Session session = getSession()) {
            session.beginTransaction();
            session.merge(receta);
            session.getTransaction().commit();
        }
    }

    // elimina una receta de la BBDD por su ID
    public static void eliminarReceta(int id) {
        try (Session session = getSession()) {
            session.beginTransaction();
            Receta receta = session.get(Receta.class, id);
            if (receta != null) {
                session.remove(receta);
            }
            session.getTransaction().commit();
        }
    }

    // busca recetas según criterio:
    public static List<Receta> buscarRecetas(String busqueda, String categoria, String dificultad) {
        try (Session session = getSession()) {
            String hql = "FROM Receta r WHERE 1=1";
            if (busqueda != null && !busqueda.isEmpty()) {
                hql += " AND r.titulo LIKE :busqueda";
            }
            if (categoria != null && !categoria.isEmpty()) {
                hql += " AND r.categoria.id = :categoriaId";
            }
            if (dificultad != null && !dificultad.isEmpty()) {
                hql += " AND r.dificultad = :dificultad";
            }

            Query<Receta> query = session.createQuery(hql, Receta.class);

            if (busqueda != null && !busqueda.isEmpty()) {
                query.setParameter("busqueda", "%" + busqueda + "%");
            }
            if (categoria != null && !categoria.isEmpty()) {
                query.setParameter("categoriaId", Integer.parseInt(categoria));
            }
            if (dificultad != null && !dificultad.isEmpty()) {
                query.setParameter("dificultad", dificultad);
            }

            return query.list();
        }
    }

    public static Receta obtenerRecetaPorId(int id) {
        try (Session session = getSession()) {
            return session.get(Receta.class, id);
        }
    }

    public static Receta obtenerRecetaCompleta(int recetaId) {
        try (Session session = getSession()) {
            // primero obtiene la receta con sus fotos
            Query<Receta> query = session.createQuery(
                    "SELECT DISTINCT r FROM Receta r " + "LEFT JOIN FETCH r.fotos " + "WHERE r.id = :recetaId",
                    Receta.class);
            query.setParameter("recetaId", recetaId);

            Receta receta = query.uniqueResult();

            if (receta != null) {
                // en una segunda consulta, carga los videos
                Query<Receta> videoQuery = session.createQuery(
                        "SELECT DISTINCT r FROM Receta r " + "LEFT JOIN FETCH r.videos " + "WHERE r.id = :recetaId",
                        Receta.class);
                videoQuery.setParameter("recetaId", recetaId);

                // actualiza la sesión con la receta que incluye videos
                Receta recetaConVideos = videoQuery.uniqueResult();

                // fuerza la carga de ingredientes si existen
                if (receta.getIngredientes() != null) {
                    receta.getIngredientes().size();
                }

                // asegura que los videos estén cargados
                if (recetaConVideos != null && recetaConVideos.getVideos() != null) {
                    // fuerza la inicialización de la colección de videos
                    recetaConVideos.getVideos().size();

                    // transfiere los videos ya inicializados a nuestra receta original (truco:evita
                    // problemas de sesión cerrada)
                    session.evict(receta);
                    return recetaConVideos;
                }
            }

            return receta;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // metodos para FotoReceta:

    public static FotoReceta obtenerFotoPorId(int id) {
        try (Session session = getSession()) {
            return session.get(FotoReceta.class, id);
        }
    }

    public static List<FotoReceta> obtenerFotosPorReceta(int recetaId) {
        try (Session session = getSession()) {
            Query<FotoReceta> query = session.createQuery("FROM FotoReceta WHERE receta.id = :recetaId",
                    FotoReceta.class);
            query.setParameter("recetaId", recetaId);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static List<Receta> obtenerRecetasConFotos() {
        try (Session session = getSession()) {
            // usa JOIN FETCH para cargar las fotos en una sola consulta
            Query<Receta> query = session.createQuery("SELECT DISTINCT r FROM Receta r LEFT JOIN FETCH r.fotos",
                    Receta.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static FotoReceta obtenerFotoPrincipalDeReceta(int recetaId) {
        try (Session session = getSession()) {
            Query<FotoReceta> query = session.createQuery(
                    "FROM FotoReceta WHERE receta.id = :recetaId AND esPrincipal = true", FotoReceta.class);
            query.setParameter("recetaId", recetaId);
            FotoReceta resultado = query.uniqueResult();

            // Si no hay foto principal, devuelve la primera foto
            if (resultado == null) {
                query = session.createQuery("FROM FotoReceta WHERE receta.id = :recetaId", FotoReceta.class);
                query.setParameter("recetaId", recetaId);
                query.setMaxResults(1);
                resultado = query.uniqueResult();
            }

            return resultado;
        }
    }

    public static void agregarFotoAReceta(FotoReceta foto, int recetaId) {
        try (Session session = getSession()) {
            session.beginTransaction();

            Receta receta = session.get(Receta.class, recetaId);
            if (receta != null) {
                foto.setReceta(receta);
                session.persist(foto);
            }

            session.getTransaction().commit();
        }
    }

    public static void eliminarFoto(int fotoId) {
        try (Session session = getSession()) {
            session.beginTransaction();
            FotoReceta foto = session.get(FotoReceta.class, fotoId);
            if (foto != null) {
                session.remove(foto);
            }
            session.getTransaction().commit();
        }
    }

    // metodos para VideoReceta:

    public static VideoReceta obtenerVideoPorId(int id) {
        try (Session session = getSession()) {
            return session.get(VideoReceta.class, id);
        }
    }

    public static List<VideoReceta> obtenerVideosPorReceta(int recetaId) {
        try (Session session = getSession()) {
            Query<VideoReceta> query = session.createQuery("FROM VideoReceta WHERE receta.id = :recetaId",
                    VideoReceta.class);
            query.setParameter("recetaId", recetaId);
            return query.list();
        }
    }

    public static void agregarVideoAReceta(VideoReceta video, int recetaId) {
        try (Session session = getSession()) {
            session.beginTransaction();

            Receta receta = session.get(Receta.class, recetaId);
            if (receta != null) {
                video.setReceta(receta);
                session.persist(video);
            }

            session.getTransaction().commit();
        }
    }

    public static void eliminarVideo(int videoId) {
        try (Session session = getSession()) {
            session.beginTransaction();
            VideoReceta video = session.get(VideoReceta.class, videoId);
            if (video != null) {
                session.remove(video);
            }
            session.getTransaction().commit();
        }
    }

    // metodos para Usuario, Categoria y Valoracion:

    public static List<Usuario> obtenerTodosLosUsuarios() {
        try (Session session = getSession()) {
            return session.createQuery("FROM Usuario", Usuario.class).list();
        }
    }

    public static void agregarUsuario(Usuario usuario) {
        try (Session session = getSession()) {
            session.beginTransaction();
            session.persist(usuario);
            session.getTransaction().commit();
        }
    }

    public static void actualizarUsuario(Usuario usuario) {
        try (Session session = getSession()) {
            session.beginTransaction();
            session.merge(usuario);
            session.getTransaction().commit();
        }
    }

    public static void eliminarUsuario(int id) {
        try (Session session = getSession()) {
            session.beginTransaction();
            Usuario usuario = session.get(Usuario.class, id);
            if (usuario != null) {
                session.remove(usuario);
            }
            session.getTransaction().commit();
        }
    }

    // metodo : elimina usuario por nombre
    public static void eliminarUsuarioPorNombre(String nombre) {
        try (Session session = getSession()) {
            session.beginTransaction();
            Query<Usuario> query = session.createQuery("FROM Usuario WHERE nombre = :nombre", Usuario.class);
            query.setParameter("nombre", nombre);
            Usuario usuario = query.uniqueResult();
            if (usuario != null) {
                session.remove(usuario);
            }
            session.getTransaction().commit();
        }
    }

    public static Usuario obtenerUsuarioPorId(int id) {
        try (Session session = getSession()) {
            return session.get(Usuario.class, id);
        }
    }

    // metodo : obtiene usuario por nombre
    public static Usuario obtenerUsuarioPorNombre(String nombre) {
        try (Session session = getSession()) {
            Query<Usuario> query = session.createQuery("FROM Usuario WHERE nombre = :nombre", Usuario.class);
            query.setParameter("nombre", nombre);
            return query.uniqueResult();
        }
    }

    public static List<Categoria> obtenerTodasLasCategorias() {
        try (Session session = getSession()) {
            return session.createQuery("FROM Categoria", Categoria.class).list();
        }
    }

    public static void agregarCategoria(Categoria categoria) {
        try (Session session = getSession()) {
            session.beginTransaction();
            session.persist(categoria);
            session.getTransaction().commit();
        }
    }

    public static void actualizarCategoria(Categoria categoria) {
        try (Session session = getSession()) {
            session.beginTransaction();
            session.merge(categoria);
            session.getTransaction().commit();
        }
    }

    public static void eliminarCategoria(int id) {
        try (Session session = getSession()) {
            session.beginTransaction();
            Categoria categoria = session.get(Categoria.class, id);
            if (categoria != null) {
                session.remove(categoria);
            }
            session.getTransaction().commit();
        }
    }

    public static Categoria obtenerCategoriaPorId(int id) {
        try (Session session = getSession()) {
            return session.get(Categoria.class, id);
        }
    }

    public static List<Valoracion> obtenerValoracionesPorReceta(int recetaId) {
        try (Session session = getSession()) {
            Query<Valoracion> query = session.createQuery("FROM Valoracion WHERE receta.id = :recetaId",
                    Valoracion.class);
            query.setParameter("recetaId", recetaId);
            return query.list();
        }
    }

    public static void agregarValoracion(Valoracion valoracion) {
        try (Session session = getSession()) {
            session.beginTransaction();
            session.persist(valoracion);
            session.getTransaction().commit();
        }
    }

    public static void actualizarValoracion(Valoracion valoracion) {
        try (Session session = getSession()) {
            session.beginTransaction();
            session.merge(valoracion);
            session.getTransaction().commit();
        }
    }

    public static void eliminarValoracion(int id) {
        try (Session session = getSession()) {
            session.beginTransaction();
            Valoracion valoracion = session.get(Valoracion.class, id);
            if (valoracion != null) {
                session.remove(valoracion);
            }
            session.getTransaction().commit();
        }
    }

    public static Valoracion obtenerValoracionPorId(int id) {
        try (Session session = getSession()) {
            return session.get(Valoracion.class, id);
        }
    }

    public static List<Valoracion> obtenerTodasLasValoraciones() {
        try (Session session = getSession()) {
            return session.createQuery("FROM Valoracion", Valoracion.class).list();
        }
    }

    public static boolean usuarioYaValoroReceta(int usuarioId, int recetaId) {
        try (Session session = getSession()) {
            Query<Long> query = session.createQuery(
                    "SELECT COUNT(*) FROM Valoracion WHERE usuario.id = :usuarioId AND receta.id = :recetaId",
                    Long.class);
            query.setParameter("usuarioId", usuarioId);
            query.setParameter("recetaId", recetaId);
            Long count = query.uniqueResult();
            return count != null && count > 0;
        }
    }

    public static List<Receta> obtenerRecetasValoradasPorUsuario(int usuarioId) {
        try (Session session = getSession()) {
            Query<Receta> query = session
                    .createQuery("SELECT v.receta FROM Valoracion v WHERE v.usuario.id = :usuarioId", Receta.class);
            query.setParameter("usuarioId", usuarioId);
            return query.list();
        }
    }
}