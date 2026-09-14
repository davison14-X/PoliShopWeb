package co.edu.pcjic.polishop.util;

import co.edu.pcjic.polishop.repository.EmprendimientoRepository;
import co.edu.pcjic.polishop.repository.ProductoRepository;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Genera slugs URL-safe a partir de texto arbitrario.
 * Proceso: normalización NFD → minúsculas → reemplaza no-alfanuméricos por guiones
 * → elimina guiones dobles y de los extremos.
 */
public final class SlugUtil {

    private static final Pattern NO_ALPHANUM = Pattern.compile("[^a-z0-9]+");
    private static final Pattern MULTI_DASH  = Pattern.compile("-{2,}");

    private SlugUtil() {}

    /** Convierte {@code texto} en un slug limpio y en minúsculas. */
    public static String slugify(String texto) {
        String normalizado = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}", "");
        String lower = normalizado.toLowerCase(Locale.ROOT).trim();
        String conGuiones = NO_ALPHANUM.matcher(lower).replaceAll("-");
        return MULTI_DASH.matcher(conGuiones).replaceAll("-")
                .replaceAll("^-|-$", "");
    }

    /**
     * Genera un slug único para un emprendimiento, añadiendo sufijo numérico
     * si el slug base ya existe (ej. "mi-tienda-2").
     */
    public static String uniqueSlugEmprendimiento(String nombre, EmprendimientoRepository repo) {
        String base = slugify(nombre);
        String candidate = base;
        int suffix = 2;
        while (repo.existsBySlug(candidate)) {
            candidate = base + "-" + suffix++;
        }
        return candidate;
    }

    /**
     * Genera un slug único para un producto, añadiendo sufijo numérico si ya existe.
     */
    public static String uniqueSlugProducto(String nombre, ProductoRepository repo) {
        String base = slugify(nombre);
        String candidate = base;
        int suffix = 2;
        while (repo.existsBySlug(candidate)) {
            candidate = base + "-" + suffix++;
        }
        return candidate;
    }
}
