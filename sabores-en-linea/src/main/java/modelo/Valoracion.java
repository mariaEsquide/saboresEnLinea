package modelo;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "valoraciones")
public class Valoracion implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @ManyToOne
    @JoinColumn(name = "receta_id", nullable = false)
    private Receta receta;
    
    @Column(nullable = false)
    private int puntuacion; // 1-5 estrellas
    
    @Column(length = 1000)
    private String comentario;
    
    @Column(name = "fecha")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha;
    
    
    public Valoracion() {
        this.fecha = new Date();
    }
    
    public Valoracion(Usuario usuario, Receta receta, int puntuacion, String comentario) {
        this.usuario = usuario;
        this.receta = receta;
        this.puntuacion = puntuacion;
        this.comentario = comentario;
        this.fecha = new Date();
    }
    
    
    public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public Usuario getUsuario() {
        return usuario;
    }
    
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
    
    public Receta getReceta() {
        return receta;
    }
    
    public void setReceta(Receta receta) {
        this.receta = receta;
    }
    
    public int getPuntuacion() {
        return puntuacion;
    }
    
    public void setPuntuacion(int puntuacion) {
        if (puntuacion < 1) {
            this.puntuacion = 1;
        } else if (puntuacion > 5) {
            this.puntuacion = 5;
        } else {
            this.puntuacion = puntuacion;
        }
    }
    
    public String getComentario() {
        return comentario;
    }
    
    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
    
    public Date getFecha() {
        return fecha;
    }
    
    public void setFecha(Date localDateTime) {
        this.fecha = localDateTime;
    }
    
    @Override
    public String toString() {
        return "Valoracion{" +
                "id=" + id +
                ", usuario=" + (usuario != null ? usuario.getNombre() : "null") +
                ", receta=" + (receta != null ? receta.getTitulo() : "null") +
                ", puntuacion=" + puntuacion +
                ", fecha=" + fecha +
                '}';
    }
}