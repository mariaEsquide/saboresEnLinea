package modelo;

import java.io.Serializable;

import jakarta.persistence.*;

// Entidad JPA mapeada a la tabla 'categorias' en BBDD - (Jakarta Persistence API) para el mapeo objeto-relacional

@Entity
@Table(name = "categorias")
public class Categoria implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Column(nullable = false, length = 50)
	private String nombre;

	@Column(columnDefinition = "TEXT")
	private String descripcion;

	public Categoria() {
	}

	public Categoria(String nombre, String descripcion) {
		this.nombre = nombre;
		this.descripcion = descripcion;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	@Override
	public String toString() {
		return "Categoria{" + "id=" + id + ", nombre='" + nombre + '\'' + ", descripcion='" + descripcion + '\'' + '}';
	}
}
