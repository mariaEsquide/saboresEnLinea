package modelo;

import jakarta.persistence.*;
import org.hibernate.Session;
import util.HibernateUtil;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "videos_receta")
public class VideoReceta implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@ManyToOne
	@JoinColumn(name = "receta_id", nullable = false)
	private Receta receta;

	@Column(name = "nombre_archivo")
	private String nombreArchivo;

	@Column(name = "url_video")
	private String urlVideo;

	private String descripcion;

	private Integer duracion; // duración en segundos

	@Column(name = "fecha_subida")
	@Temporal(TemporalType.TIMESTAMP)
	private Date fechaSubida;

	// marca el contenido como transient para evitar serializar datos grandes
	@Lob
	@Column(name = "contenido")
	private transient byte[] contenido;

	@Column(name = "tipo_mime", length = 50)
	private String tipoMime;

	// constructor por defecto requerido por JPA
	public VideoReceta() {
		this.fechaSubida = new Date();
	}

	// constructor para videos con URL (YouTube, etc.)
	public VideoReceta(String nombreArchivo, String urlVideo, String descripcion, Integer duracion) {
		this.nombreArchivo = nombreArchivo;
		this.urlVideo = urlVideo;
		this.descripcion = descripcion;
		this.duracion = duracion;
		this.fechaSubida = new Date();
	}

	// constructor para videos almacenados en la BBDD
	public VideoReceta(String nombreArchivo, String descripcion, Integer duracion, byte[] contenido, String tipoMime) {
		this.nombreArchivo = nombreArchivo;
		this.descripcion = descripcion;
		this.duracion = duracion;
		this.fechaSubida = new Date();
		this.contenido = contenido;
		this.tipoMime = tipoMime;
	}

	// getters y setters

	public int getId() {
		return id;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
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

	public String getUrlVideo() {
		return urlVideo;
	}

	public void setUrlVideo(String urlVideo) {
		this.urlVideo = urlVideo;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public Integer getDuracion() {
		return duracion;
	}

	public void setDuracion(Integer duracion) {
		this.duracion = duracion;
	}

	public Date getFechaSubida() {
		return fechaSubida;
	}

	public void setFechaSubida(Date fechaSubida) {
		this.fechaSubida = fechaSubida;
	}

	// método : carga el contenido bajo demanda
	public byte[] getContenido() {
		if (contenido == null && id > 0 && !esVideoRemoto()) {
			// Si el contenido es null pero tenemos un ID y no es un video remoto, lo cargamos
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

	// metodo: verifica si el video es local (almacenado BBDD)
	public boolean esVideoLocal() {
		return (contenido != null && contenido.length > 0) || (id > 0 && urlVideo == null);
	}

	// metodo: verifica si el video es remoto (URL)
	public boolean esVideoRemoto() {
		return urlVideo != null && !urlVideo.isEmpty();
	}

	// método : carga el contenido desde la base de datos
	private void loadContenido() {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			VideoReceta video = session.get(VideoReceta.class, this.id);
			if (video != null) {
				this.contenido = video.contenido;
			}
		} catch (Exception e) {
			System.err.println("Error al cargar el contenido del video: " + e.getMessage());
		}
	}

	// métodos personalizados para serialización
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
		return "VideoReceta{" + "id=" + id + ", nombreArchivo='" + nombreArchivo + '\'' + ", urlVideo='" + urlVideo
				+ '\'' + ", descripcion='" + descripcion + '\'' + ", duracion=" + duracion + ", fechaSubida="
				+ fechaSubida + ", tipoMime='" + tipoMime + '\'' + '}';
	}
}