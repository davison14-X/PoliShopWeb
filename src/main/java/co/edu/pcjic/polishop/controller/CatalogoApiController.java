package co.edu.pcjic.polishop.controller;

import co.edu.pcjic.polishop.dto.EmprendimientoDTO;
import co.edu.pcjic.polishop.service.EmprendimientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/catalogo")
@RequiredArgsConstructor
public class CatalogoApiController {

    private final EmprendimientoService emprendimientoService;

    @GetMapping("/buscar")
    public List<EmprendimientoDTO> buscar(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoria,
            @RequestParam(required = false) String ubicacion) {
        return emprendimientoService.buscar(q, categoria, ubicacion);
    }
}
