package modelo;

import jakarta.persistence.*;
import util.HibernateUtil;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "fotos_receta")
public class FotoReceta implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@ManyToOne
	@JoinColumn(name = "receta_id", nullable = false)
	private Receta receta;

	@Column(name = "nombre_archivo", nullable = false)
	private String nombreArchivo;

	private String descripcion;

	@Column(name = "es_principal")
	private boolean esPrincipal;

	@Column(name = "fecha_subida")
	@Temporal(TemporalType.TIMESTAMP)
	private Date fechaSubida;

	// transient : evita serializar datos grandes
	@Lob
	@Column(name = "contenido", nullable = false)
	private transient byte[] contenido;

	@Column(name = "tipo_mime", nullable = false, length = 50)
	private String tipoMime;

	public FotoReceta() {
		this.fechaSubida = new Date();
	}

	public FotoReceta(String nombreArchivo, String descripcion, boolean esPrincipal, byte[] contenido,
			String tipoMime) {
		this.nombreArchivo = nombreArchivo;
		this.descripcion = descripcion;
		this.esPrincipal = esPrincipal;
		this.fechaSubida = new Date();
		this.contenido = contenido;
		this.tipoMime = tipoMime;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Receta getReceta() {
		return receta;
	}

	public void setReceta(Receta receta) {
		this.receta = receta;
	}

	public String getNombreArchivo() {
		return nombreArchivo;
	}

	public void setNombreArchivo(String nombreArchivo) {
		this.nombreArchivo = nombreArchivo;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public boolean isEsPrincipal() {
		return esPrincipal;
	}

	public void setEsPrincipal(boolean esPrincipal) {
		this.esPrincipal = esPrincipal;
	}

	public Date getFechaSubida() {
		return fechaSubida;
	}

	public void setFechaSubida(Date fechaSubida) {
		this.fechaSubida = fechaSubida;
	}

	// metodo : carga el contenido bajo demanda
	public byte[] getContenido() {
		if (contenido == null && id > 0) {
			// Si el contenido es null pero tenemos un ID, lo cargamos de la BBDD
			loadContenido();
		}
		return contenido;
	}

	public void setContenido(byte[] contenido) {
		this.contenido = contenido;
	}

	public String getTipoMime() {
		return tipoMime;
	}

	public void setTipoMime(String tipoMime) {
		this.tipoMime = tipoMime;
	}

	// metodo : carga el contenido desde la BBDD
	private void loadContenido() {
		try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
			FotoReceta foto = session.get(FotoReceta.class, this.id);
			if (foto != null) {
				this.contenido = foto.contenido;
			}
		} catch (Exception e) {
			System.err.println("Error al cargar el contenido de la foto: " + e.getMessage());
		}
	}

	// metodos personalizados para serialización
	private void writeObject(ObjectOutputStream out) throws IOException {
		// guarda el ID y otros campos básicos, pero no el contenido
		out.defaultWriteObject();
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		// restaura los campos básicos
		in.defaultReadObject();
		// El contenido se cargará bajo demanda cuando se llame a getContenido()
	}

	@Override
	public String toString() {
		return "FotoReceta{" + "id=" + id + ", nombreArchivo='" + nombreArchivo + '\'' + ", descripcion='" + descripcion
				+ '\'' + ", esPrincipal=" + esPrincipal + ", fechaSubida=" + fechaSubida + ", tipoMime='" + tipoMime
				+ '\'' + '}';
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}