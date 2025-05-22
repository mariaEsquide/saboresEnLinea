package modelo;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "recetas")
public class Receta implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Column(nullable = false, length = 100)
	private String titulo;

	@Column(columnDefinition = "TEXT")
	private String descripcion;

	@Column(columnDefinition = "TEXT")
	private String instrucciones;

	@Column(name = "tiempo_preparacion")
	private int tiempoPreparacion;

	@Column(length = 20)
	private String dificultad;

	private int porciones;

	@ManyToOne
	@JoinColumn(name = "categoria_id")
	private Categoria categoria;

	@ManyToOne
	@JoinColumn(name = "usuario_id")
	private Usuario usuario;

	@Column(name = "fecha_creacion")
	@Temporal(TemporalType.TIMESTAMP)
	private Date fechaCreacion;

	@ElementCollection
	@CollectionTable(name = "ingredientes_receta", joinColumns = @JoinColumn(name = "receta_id"))
	@Column(name = "ingrediente")
	private List<String> ingredientes = new ArrayList<>();

	@Column(name = "valoracion_promedio")
	private double valoracionPromedio;

	// Cambiado de LAZY (por defecto) a EAGER para evitar el error de inicialización
	// perezosa
	@OneToMany(mappedBy = "receta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	private List<FotoReceta> fotos = new ArrayList<>();

	@OneToMany(mappedBy = "receta", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<VideoReceta> videos = new ArrayList<>();

	// constructor por defecto requerido por JPA
	public Receta() {
		this.fechaCreacion = new Date();
	}

	public Receta(String titulo, String descripcion, String instrucciones, int tiempoPreparacion, String dificultad,
			int porciones, Categoria categoria, Usuario usuario) {
		this.titulo = titulo;
		this.descripcion = descripcion;
		this.instrucciones = instrucciones;
		this.tiempoPreparacion = tiempoPreparacion;
		this.dificultad = dificultad;
		this.porciones = porciones;
		this.categoria = categoria;
		this.usuario = usuario;
		this.fechaCreacion = new Date();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getInstrucciones() {
		return instrucciones;
	}

	public void setInstrucciones(String instrucciones) {
		this.instrucciones = instrucciones;
	}

	public int getTiempoPreparacion() {
		return tiempoPreparacion;
	}

	public void setTiempoPreparacion(int tiempoPreparacion) {
		this.tiempoPreparacion = tiempoPreparacion;
	}

	public String getDificultad() {
		return dificultad;
	}

	public void setDificultad(String dificultad) {
		this.dificultad = dificultad;
	}

	public int getPorciones() {
		return porciones;
	}

	public void setPorciones(int porciones) {
		this.porciones = porciones;
	}

	public Categoria getCategoria() {
		return categoria;
	}

	public void setCategoria(Categoria categoria) {
		this.categoria = categoria;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public Date getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public List<String> getIngredientes() {
		return ingredientes;
	}

	public void setIngredientes(List<String> ingredientes) {
		this.ingredientes = ingredientes;
	}

	public double getValoracionPromedio() {
		return valoracionPromedio;
	}

	public void setValoracionPromedio(double valoracionPromedio) {
		this.valoracionPromedio = valoracionPromedio;
	}

	public List<FotoReceta> getFotos() {
		return fotos;
	}

	public void setFotos(List<FotoReceta> fotos) {
		this.fotos = fotos;
	}

	public List<VideoReceta> getVideos() {
		return videos;
	}

	public void setVideos(List<VideoReceta> videos) {
		this.videos = videos;
	}

	// metodo: agrega un ingrediente a la lista de ingredientes de la receta
	public void agregarIngrediente(String ingrediente) {
		this.ingredientes.add(ingrediente);
	}

	// metodo: agrega una foto a la receta
	public void agregarFoto(FotoReceta foto) {
		foto.setReceta(this);
		this.fotos.add(foto);
	}

	// metodo: agregar video a la receta
	public void agregarVideo(VideoReceta video) {
		video.setReceta(this);
		this.videos.add(video);
	}

	// metodo: actualiza la valoración promedio de la receta
	public void actualizarValoracionPromedio(int nuevaValoracion) {
		// implementación simple, en la práctica se debería considerar:
		// el número total de valoraciones para calcular el promedio correctamente
		this.valoracionPromedio = (this.valoracionPromedio + nuevaValoracion) / 2;
	}

	@Override
	public String toString() {
		return "Receta{" + "id=" + id + ", titulo='" + titulo + '\'' + ", descripcion='" + descripcion + '\''
				+ ", tiempoPreparacion=" + tiempoPreparacion + ", dificultad='" + dificultad + '\'' + ", porciones="
				+ porciones + ", categoria=" + categoria + ", valoracionPromedio=" + valoracionPromedio + '}';
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}