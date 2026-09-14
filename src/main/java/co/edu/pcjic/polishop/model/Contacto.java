package co.edu.pcjic.polishop.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "contacto")
@Getter @Setter @NoArgsConstructor
public class Contacto {

    public enum Tipo { WHATSAPP, INSTAGRAM, TELEFONO, EMAIL, OTRO }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_emprendimiento", nullable = false)
    private Emprendimiento emprendimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Tipo tipo;

    @Column(nullable = false, length = 200)
    private String valor;

    @Column(nullable = false)
    private boolean principal = false;
}
