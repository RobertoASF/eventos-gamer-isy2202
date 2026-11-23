package cl.duoc.gamer.eventos.model;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "multimedia_evento")
@Getter
@Setter
@NoArgsConstructor
public class Multimedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    @Column(nullable = false)
    private Long usuarioId;

    @Column(nullable = false, length = 30)
    private String usuario;

    @Column(nullable = false, length = 80)
    private String autor;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 5)
    private TipoMultimedia tipo;

    @Column(nullable = false, length = 30)
    private String tipoContenido;

    @Column(nullable = false)
    private int tamano;

    @Column(nullable = false, length = 120)
    private String descripcion;

    // Los listados usan consultas que no traen esta columna, asi el archivo solo se lee cuando se muestra
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false, columnDefinition = "LONGBLOB")
    private byte[] contenido;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;
}
