package co.edu.pcjic.polishop.controller;

import co.edu.pcjic.polishop.repository.CategoriaRepository;
import co.edu.pcjic.polishop.service.EmprendimientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CatalogoController {

    private final EmprendimientoService emprendimientoService;
    private final CategoriaRepository categoriaRepo;

    @GetMapping("/")
    public String catalogo(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoria,
            @RequestParam(required = false) String ubicacion,
            Model model) {

        var emprendimientos = emprendimientoService.buscar(q, categoria, ubicacion);

        model.addAttribute("emprendimientos", emprendimientos);
        model.addAttribute("categorias", categoriaRepo.findAll());
        model.addAttribute("totalResultados", emprendimientos.size());
        model.addAttribute("busqueda", q);
        model.addAttribute("categoriaSeleccionada", categoria);
        model.addAttribute("ubicacionSeleccionada", ubicacion);
        model.addAttribute("paginaActual", "catalogo");

        return "catalogo/index";
    }
}
