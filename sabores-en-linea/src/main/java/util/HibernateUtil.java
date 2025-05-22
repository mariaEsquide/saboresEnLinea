package util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import modelo.Categoria;
import modelo.FotoReceta;
import modelo.Receta;
import modelo.Usuario;
import modelo.Valoracion;
import modelo.VideoReceta;

import java.io.File;
import java.io.Serializable;
import java.net.URL;

public class HibernateUtil implements Serializable {
    private static final long serialVersionUID = 1L;

    // transient: evita serializar la SessionFactory
    private static transient SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            // Intenta diferentes estrategias para encontrar el archivo de configuración
            Configuration configuration = new Configuration();
            
            // Estrategia 1: Buscar en el classpath (método estándar)
            try {
                System.out.println("Intentando cargar hibernate.cfg.xml desde el classpath...");
                configuration.configure();
                System.out.println("¡Configuración cargada exitosamente desde el classpath!");
            } catch (Exception e) {
                System.out.println("No se pudo cargar desde el classpath: " + e.getMessage());
                
                // Estrategia 2: Buscar con ruta explícita
                try {
                    System.out.println("Intentando cargar con ruta explícita...");
                    URL url = HibernateUtil.class.getClassLoader().getResource("hibernate.cfg.xml");
                    if (url != null) {
                        System.out.println("Archivo encontrado en: " + url.getPath());
                        configuration.configure(url);
                        System.out.println("¡Configuración cargada exitosamente con URL!");
                    } else {
                        // Estrategia 3: Buscar en la raíz del proyecto
                        System.out.println("Intentando cargar desde la raíz del proyecto...");
                        File file = new File("hibernate.cfg.xml");
                        if (file.exists()) {
                            System.out.println("Archivo encontrado en: " + file.getAbsolutePath());
                            configuration.configure(file);
                            System.out.println("¡Configuración cargada exitosamente desde archivo!");
                        } else {
                            throw new RuntimeException("No se pudo encontrar hibernate.cfg.xml");
                        }
                    }
                } catch (Exception ex) {
                    System.err.println("Error al cargar la configuración: " + ex);
                    throw ex;
                }
            }

            // agrega las clases de entidad (Solo clases con anotaciones @Entity)
            configuration.addAnnotatedClass(Receta.class);
            configuration.addAnnotatedClass(Usuario.class);
            configuration.addAnnotatedClass(Valoracion.class);
            configuration.addAnnotatedClass(Categoria.class);
            configuration.addAnnotatedClass(FotoReceta.class);
            configuration.addAnnotatedClass(VideoReceta.class);

            return configuration.buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Error al crear SessionFactory: " + ex);
            ex.printStackTrace();
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        // Si la sessionFactory es null (después de deserialización), la reconstruimos
        if (sessionFactory == null) {
            sessionFactory = buildSessionFactory();
        }
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            getSessionFactory().close();
        }
    }

    // método : maneja la deserialización
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        // no necesita reconstruir sessionFactory aquí porque es estática-reconstruirá
        // en getSessionFactory() cuando sea necesario
    }
}