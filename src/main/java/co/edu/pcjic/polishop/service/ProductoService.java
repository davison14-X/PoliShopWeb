package co.edu.pcjic.polishop.service;

import co.edu.pcjic.polishop.dto.ProductoDTO;
import co.edu.pcjic.polishop.dto.ProductoForm;
import co.edu.pcjic.polishop.model.Emprendimiento;
import co.edu.pcjic.polishop.model.ImagenProducto;
import co.edu.pcjic.polishop.model.Producto;
import co.edu.pcjic.polishop.repository.MeGustaRepository;
import co.edu.pcjic.polishop.repository.ProductoRepository;
import co.edu.pcjic.polishop.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepo;
    private final MeGustaRepository meGustaRepo;
    private final CloudinaryService cloudinaryService;

    @Value("${cloudinary.carpeta.productos}")
    private String carpetaProductos;

    @Transactional(readOnly = true)
    public ProductoDTO findDTOBySlug(String slug) {
        Producto p = productoRepo.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        return toDTO(p);
    }

    public Producto findById(Long id) {
        return productoRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
    }

    /**
     * Busca un producto por ID verificando que pertenezca al emprendimiento indicado.
     * Lanza 403 si el producto existe pero no pertenece al emprendimiento (previene IDOR).
     */
    public Producto findByIdForOwner(Long id, Long emprendimientoId) {
        return productoRepo.findByIdAndEmprendimientoId(id, emprendimientoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado al producto"));
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> listarPorEmprendimiento(Long empId) {
        return productoRepo.findByEmprendimientoId(empId)
                .stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> listarDisponiblesPorEmprendimiento(Long empId) {
        return productoRepo.findByEmprendimientoIdAndDisponibleTrue(empId)
                .stream().map(this::toDTO).toList();
    }

    @Transactional
    public Producto guardar(ProductoForm form, Emprendimiento emprendimiento) throws IOException {
        Producto producto;

        if (form.getId() != null) {
            producto = productoRepo.findById(form.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        } else {
            producto = new Producto();
            producto.setEmprendimiento(emprendimiento);
            producto.setSlug(SlugUtil.uniqueSlugProducto(form.getNombre(), productoRepo));
        }

        producto.setNombre(form.getNombre());
        producto.setDescripcion(form.getDescripcion());
        producto.setPrecio(form.getPrecio());
        producto.setDisponible(form.isDisponible());

        // Subir imagen si hay archivo nuevo
        if (form.getImagen() != null && !form.getImagen().isEmpty()) {
            // Eliminar imagen anterior si existe
            if (!producto.getImagenes().isEmpty()) {
                cloudinaryService.eliminarImagen(producto.getImagenes().get(0).getUrl());
                producto.getImagenes().clear();
            }
            String url = cloudinaryService.subirImagen(form.getImagen(), carpetaProductos);
            ImagenProducto img = new ImagenProducto();
            img.setProducto(producto);
            img.setUrl(url);
            img.setOrden(0);
            producto.getImagenes().add(img);
        }

        return productoRepo.save(producto);
    }

    @Transactional
    public void eliminar(Long id) {
        Producto producto = productoRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        // Eliminar imágenes en Cloudinary
        producto.getImagenes().forEach(img -> cloudinaryService.eliminarImagen(img.getUrl()));
        productoRepo.delete(producto);
    }

    public ProductoDTO toDTO(Producto p) {
        List<String> urls = p.getImagenes().stream().map(ImagenProducto::getUrl).toList();
        String principal = urls.isEmpty() ? null : urls.get(0);
        List<String> adicionales = urls.size() > 1 ? urls.subList(1, urls.size()) : List.of();
        long likes = meGustaRepo.countByProductoId(p.getId());

        return ProductoDTO.builder()
                .id(p.getId())
                .nombre(p.getNombre())
                .slug(p.getSlug())
                .descripcion(p.getDescripcion())
                .precio(p.getPrecio())
                .disponible(p.isDisponible())
                .imagenPrincipal(principal)
                .imagenesAdicionales(adicionales)
                .emprendimientoNombre(p.getEmprendimiento().getNombre())
                .emprendimientoSlug(p.getEmprendimiento().getSlug())
                .logoEmprendimiento(p.getEmprendimiento().getLogoUrl())
                .totalMeGusta(likes)
                .meGusta(false)
                .build();
    }
}
