/**
 * PoliShop — Interacciones del cliente
 * Solo UI: modales, validaciones, toast, likes via fetch, navegación móvil
 */

// ============================================================
// UTILIDADES GENERALES
// ============================================================

const Toast = {
  timeout: null,

  show(mensaje, tipo = 'success') {
    const toast   = document.getElementById('app-toast');
    const icon    = document.getElementById('toast-icon');
    const mensaje_el = document.getElementById('toast-message');
    if (!toast) return;

    // Icono según tipo
    const iconos = {
      success: '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>',
      error:   '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>',
      info:    '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>',
    };
    const colores = {
      success: 'text-emerald-400',
      error:   'text-red-400',
      info:    'text-blue-400',
    };

    icon.innerHTML    = iconos[tipo] ?? iconos.success;
    icon.className    = `w-4 h-4 ${colores[tipo] ?? colores.success}`;
    mensaje_el.textContent = mensaje;

    toast.classList.remove('hidden');
    toast.classList.add('toast-enter');

    clearTimeout(this.timeout);
    this.timeout = setTimeout(() => {
      toast.classList.add('hidden');
      toast.classList.remove('toast-enter');
    }, 3000);
  },
};


// ============================================================
// VALIDACIÓN DE CORREO INSTITUCIONAL
// ============================================================

const Auth = {
  DOMINIO: '@elpoli.edu.co',

  /**
   * Valida que el correo termine en @elpoli.edu.co.
   * Actualiza el UI de advertencia/éxito y habilita/deshabilita el botón.
   * @param {string} correo
   * @returns {boolean}
   */
  validarCorreo(correo) {
    const warning = document.getElementById('auth-email-warning');
    const success = document.getElementById('auth-email-success');
    const btn     = document.getElementById('auth-submit-btn');
    const valido  = correo.toLowerCase().endsWith(this.DOMINIO) && correo.length > this.DOMINIO.length;

    warning?.classList.toggle('hidden', valido || correo === '');
    success?.classList.toggle('hidden', !valido);
    if (btn) {
      btn.disabled = !valido;
      btn.classList.toggle('opacity-50', !valido);
      btn.classList.toggle('cursor-not-allowed', !valido);
    }
    return valido;
  },

  switchTab(tab) {
    const tabLogin = document.getElementById('auth-tab-login');
    const tabReg   = document.getElementById('auth-tab-register');
    const submitBtn    = document.getElementById('auth-submit-btn');
    const extraFields  = document.getElementById('auth-extra-register-fields');

    const activo   = 'bg-white text-slate-900 shadow-sm';
    const inactivo = 'text-slate-600 hover:text-slate-900';

    if (tab === 'login') {
      tabLogin?.classList.add(...activo.split(' '));
      tabLogin?.classList.remove(...inactivo.split(' '));
      tabReg?.classList.remove(...activo.split(' '));
      tabReg?.classList.add(...inactivo.split(' '));
      extraFields?.classList.add('hidden');
      if (submitBtn) submitBtn.textContent = 'Iniciar Sesión';
    } else {
      tabReg?.classList.add(...activo.split(' '));
      tabReg?.classList.remove(...inactivo.split(' '));
      tabLogin?.classList.remove(...activo.split(' '));
      tabLogin?.classList.add(...inactivo.split(' '));
      extraFields?.classList.remove('hidden');
      if (submitBtn) submitBtn.textContent = 'Crear cuenta';
    }
  },

  abrirModal() {
    const modal = document.getElementById('auth-modal');
    if (!modal) return;
    document.getElementById('auth-email-input').value    = '';
    document.getElementById('auth-password-input').value = '';
    document.getElementById('auth-email-warning')?.classList.add('hidden');
    document.getElementById('auth-email-success')?.classList.add('hidden');
    const btn = document.getElementById('auth-submit-btn');
    if (btn) { btn.disabled = true; btn.classList.add('opacity-50', 'cursor-not-allowed'); }
    modal.classList.remove('hidden');
  },

  cerrarModal() {
    document.getElementById('auth-modal')?.classList.add('hidden');
  },
};


// ============================================================
// MODAL DE DETALLE DE PRODUCTO
// ============================================================

const ProductoModal = {
  abrir(id) {
    // El backend renderea la URL; solo abrimos el modal si ya está en el DOM
    const modal = document.getElementById(`producto-modal-${id}`);
    modal?.classList.remove('hidden');
  },

  cerrar() {
    document.querySelectorAll('[id^="producto-modal-"]').forEach(m => m.classList.add('hidden'));
  },
};


// ============================================================
// MODAL DE COMPARTIR (OPENGRAPH)
// ============================================================

const ShareModal = {
  abrir(slug, titulo, descripcion, imagen, negocio) {
    const modal = document.getElementById('share-modal');
    if (!modal) return;

    const url = `${window.location.origin}/p/${slug}`;

    // Rellenar preview OG
    const ogImg   = document.getElementById('og-preview-img');
    const ogBiz   = document.getElementById('og-preview-biz');
    const ogTitle = document.getElementById('og-preview-title');
    const ogDesc  = document.getElementById('og-preview-desc');
    const urlInput = document.getElementById('share-url-input');
    const waLink   = document.getElementById('share-whatsapp-direct');

    if (ogImg)    ogImg.src             = imagen;
    if (ogBiz)    ogBiz.textContent     = negocio;
    if (ogTitle)  ogTitle.textContent   = titulo;
    if (ogDesc)   ogDesc.textContent    = descripcion;
    if (urlInput) urlInput.value        = url;
    if (waLink)   waLink.href           = `https://wa.me/?text=${encodeURIComponent(titulo + ' — ' + url)}`;

    modal.classList.remove('hidden');
  },

  cerrar() {
    document.getElementById('share-modal')?.classList.add('hidden');
  },

  async copiarEnlace() {
    const input = document.getElementById('share-url-input');
    if (!input) return;
    try {
      await navigator.clipboard.writeText(input.value);
      Toast.show('¡Enlace copiado al portapapeles!');
    } catch {
      input.select();
      document.execCommand('copy');
      Toast.show('¡Enlace copiado!');
    }
  },
};


// ============================================================
// ME GUSTA (fetch al backend)
// ============================================================

const MeGusta = {
  /**
   * @param {string} tipo       'producto' | 'emprendimiento'
   * @param {number} id         ID de la entidad
   * @param {HTMLElement} btn   Botón que disparó la acción
   */
  async toggle(tipo, id, btn) {
    try {
      const csrfToken  = document.querySelector('meta[name="_csrf"]')?.content;
      const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
      const headers = { 'X-Requested-With': 'XMLHttpRequest' };
      if (csrfToken && csrfHeader) headers[csrfHeader] = csrfToken;

      const res = await fetch(`/api/me-gusta/${tipo}/${id}`, {
        method: 'POST',
        headers,
      });

      if (res.status === 401) {
        Auth.abrirModal();
        Toast.show('Inicia sesión para dar me gusta', 'info');
        return;
      }

      if (!res.ok) throw new Error('Error al registrar me gusta');

      const { likes, liked } = await res.json();

      // Actualizar contador en el botón
      const contador = btn.querySelector('[data-likes]');
      if (contador) contador.textContent = likes;

      // Cambiar estilo activo/inactivo
      btn.dataset.liked = liked ? 'true' : 'false';
      this._actualizarEstilo(btn, liked);

      if (liked) Toast.show('¡Me gusta registrado!');
    } catch (err) {
      console.error(err);
      Toast.show('No se pudo registrar el me gusta', 'error');
    }
  },

  _actualizarEstilo(btn, liked) {
    const svg = btn.querySelector('svg');
    if (liked) {
      btn.classList.add('border-rose-200', 'bg-rose-50', 'text-rose-600');
      btn.classList.remove('border-slate-300', 'text-slate-700');
      svg?.classList.add('fill-rose-600', 'stroke-rose-600');
      svg?.classList.remove('fill-none');
    } else {
      btn.classList.remove('border-rose-200', 'bg-rose-50', 'text-rose-600');
      btn.classList.add('border-slate-300', 'text-slate-700');
      svg?.classList.remove('fill-rose-600', 'stroke-rose-600');
      svg?.classList.add('fill-none');
    }
  },

  // Inicializa el estilo de todos los botones de me gusta al cargar la página
  inicializarEstilos() {
    document.querySelectorAll('[data-liked]').forEach(btn => {
      this._actualizarEstilo(btn, btn.dataset.liked === 'true');
    });
  },
};


// ============================================================
// BÚSQUEDA Y FILTROS (catalogo)
// El servidor hace el filtrado real; esto solo gestiona el submit
// ============================================================

const Catalogo = {
  init() {
    const form   = document.getElementById('filtros-form');
    const search = document.getElementById('catalog-search-input');
    const cat    = document.getElementById('catalog-category-select');
    const loc    = document.getElementById('catalog-location-select');
    if (!form) return;

    // Búsqueda en tiempo real con debounce (500ms)
    let debounce;
    search?.addEventListener('input', () => {
      clearTimeout(debounce);
      debounce = setTimeout(() => form.submit(), 500);
    });

    // Filtros disparan submit inmediato
    cat?.addEventListener('change', () => form.submit());
    loc?.addEventListener('change', () => form.submit());
  },
};


// ============================================================
// NAVEGACIÓN MÓVIL (bottom bar)
// ============================================================

const NavMobile = {
  init() {
    const path = window.location.pathname;
    const tabs = {
      'mob-nav-catalog': '/',
      'mob-nav-panel':   '/panel',
      'mob-nav-account': null,
    };

    Object.entries(tabs).forEach(([id, ruta]) => {
      const btn = document.getElementById(id);
      if (!btn || ruta === null) return;
      const activo = path === ruta || (ruta !== '/' && path.startsWith(ruta));
      btn.classList.toggle('text-emerald-700', activo);
      btn.classList.toggle('font-semibold', activo);
      btn.classList.toggle('text-slate-500', !activo);
    });
  },
};


// ============================================================
// INICIALIZACIÓN
// ============================================================

document.addEventListener('DOMContentLoaded', () => {
  // Validación de correo en tiempo real
  document.getElementById('auth-email-input')
    ?.addEventListener('input', e => Auth.validarCorreo(e.target.value.trim()));

  // Me gusta — estado visual inicial
  MeGusta.inicializarEstilos();

  // Filtros del catálogo
  Catalogo.init();

  // Bottom bar móvil
  NavMobile.init();

  // Cerrar modales al hacer clic en el backdrop
  ['auth-modal', 'share-modal'].forEach(id => {
    document.getElementById(id)?.addEventListener('click', function(e) {
      if (e.target === this) this.classList.add('hidden');
    });
  });
});


// ============================================================
// EXPORTS GLOBALES (llamados desde atributos onclick en HTML)
// ============================================================

window.PoliShop = {
  // Auth
  abrirAuthModal:  () => Auth.abrirModal(),
  cerrarAuthModal: () => Auth.cerrarModal(),
  switchAuthTab:   (t) => Auth.switchTab(t),

  // Share
  abrirShareModal:  (slug, titulo, desc, img, biz) => ShareModal.abrir(slug, titulo, desc, img, biz),
  cerrarShareModal: () => ShareModal.cerrar(),
  copiarEnlace:     () => ShareModal.copiarEnlace(),

  // Producto modal
  cerrarProductoModal: () => ProductoModal.cerrar(),

  // Me gusta
  toggleMeGusta: (tipo, id, btn) => MeGusta.toggle(tipo, id, btn),
};