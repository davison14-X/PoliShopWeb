package co.edu.pcjic.polishop.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    /**
     * Sube una imagen a Cloudinary en la carpeta indicada.
     *
     * @param file    archivo recibido del formulario
     * @param carpeta ruta de carpeta en Cloudinary (ej. "polishop/logos")
     * @return URL segura (https) de la imagen subida
     */
    public String subirImagen(MultipartFile file, String carpeta) throws IOException {
        Map<?, ?> resultado = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap("folder", carpeta)
        );
        return (String) resultado.get("secure_url");
    }

    /**
     * Extrae el public_id de Cloudinary a partir de la URL completa.
     * Formato esperado: https://res.cloudinary.com/<cloud>/image/upload/v<ver>/<folder>/<name>.<ext>
     */
    public String extraerPublicId(String url) {
        int inicio = url.lastIndexOf("/upload/") + "/upload/".length();
        String ruta = url.substring(inicio);
        // Quitar prefijo de versión (v1234567890/)
        if (ruta.startsWith("v") && ruta.contains("/")) {
            ruta = ruta.substring(ruta.indexOf("/") + 1);
        }
        int punto = ruta.lastIndexOf(".");
        return punto > 0 ? ruta.substring(0, punto) : ruta;
    }

    /**
     * Elimina una imagen de Cloudinary. Los errores se registran pero no se propagan,
     * ya que una eliminación fallida no debe interrumpir el flujo principal.
     */
    public void eliminarImagen(String url) {
        try {
            String publicId = extraerPublicId(url);
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            log.warn("No se pudo eliminar la imagen de Cloudinary (url={}): {}", url, e.getMessage());
        }
    }
}
