package modelo;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "usuarios")
public class Usuario implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Column(nullable = false, unique = true)
	private String nombre;

	@Column(nullable = false)
	private String email;

	@Column(nullable = false)
	private String password;

	@Column(name = "es_admin")
	private boolean esAdmin;

	@Column(name = "fecha_registro")
	@Temporal(TemporalType.TIMESTAMP)
	private Date fechaRegistro;

	@OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<Valoracion> valoraciones = new ArrayList<>();

	public Usuario() {
		this.fechaRegistro = new Date();
	}

	public Usuario(String nombre, String email, String password, boolean esAdmin) {
		this.nombre = nombre;
		this.email = email;
		this.password = password;
		this.esAdmin = esAdmin;
		this.fechaRegistro = new Date();
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public boolean isEsAdmin() {
		return esAdmin;
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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean esAdmin() {
		return esAdmin;
	}

	public void setEsAdmin(boolean esAdmin) {
		this.esAdmin = esAdmin;
	}

	public Date getFechaRegistro() {
		return fechaRegistro;
	}

	public void setFechaRegistro(Date fechaRegistro) {
		this.fechaRegistro = fechaRegistro;
	}

	public List<Valoracion> getValoraciones() {
		return valoraciones;
	}

	public void setValoraciones(List<Valoracion> valoraciones) {
		this.valoraciones = valoraciones;
	}

	// método : agrega una valoración
	public void agregarValoracion(Valoracion valoracion) {
		valoraciones.add(valoracion);
		valoracion.setUsuario(this);
	}

	@Override
	public String toString() {
		return "Usuario{" + "id=" + id + ", nombre='" + nombre + '\'' + ", email='" + email + '\'' + ", esAdmin="
				+ esAdmin + ", fechaRegistro=" + fechaRegistro + '}';
	}
}