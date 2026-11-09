package cl.duoc.gamer.eventos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "eventos")
@Getter
@Setter
@NoArgsConstructor
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, length = 250)
    private String resumen;

    @Column(nullable = false, length = 2000)
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "videojuego_id", nullable = false)
    private Videojuego videojuego;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    // Se guarda como texto (PRESENCIAL u ONLINE) y no como ENUM nativo de MySQL
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 12)
    private Modalidad modalidad;

    @Column(nullable = false, length = 60)
    private String pais;

    @Column(nullable = false, length = 60)
    private String ciudad;

    // Recinto y direccion, solo para eventos presenciales
    @Column(length = 150)
    private String lugar;

    // Enlace de transmision o de la sala, solo para eventos online
    @Column(length = 255)
    private String enlaceAcceso;

    @Column(nullable = false)
    private LocalDateTime fechaInicio;

    @Column(nullable = false, length = 120)
    private String organizador;

    @Column(nullable = false, length = 1000)
    private String requisitos;

    @Column(nullable = false, length = 1000)
    private String inscripcion;

    @Column(nullable = false)
    private int valorEntrada;

    @Column(nullable = false)
    private int cupos;

    // Personas que marcaron el evento como "me interesa", se usa para ordenar por popularidad
    @Column(nullable = false)
    private int interesados;

    @Column(nullable = false, length = 150)
    private String imagenPortada;

    @Column(nullable = false)
    private LocalDateTime fechaPublicacion;

    @OneToMany(mappedBy = "evento")
    @OrderBy("orden ASC")
    private List<FotoEvento> fotos = new ArrayList<>();
}
