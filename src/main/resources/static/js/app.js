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
    return correo.toLowerCase().endsWith(this.DOMINIO) && correo.length > this.DOMINIO.length;
  },

  validarFormulario(e) {
    const correo = document.getElementById('auth-email-input')?.value.trim() || '';
    const warning = document.getElementById('auth-email-warning');
    if (!this.validarCorreo(correo)) {
      e.preventDefault();
      warning?.classList.remove('hidden');
    } else {
      warning?.classList.add('hidden');
    }
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
    const grid   = document.getElementById('catalog-grid');
    const empty  = document.getElementById('catalog-empty');
    const badge  = document.getElementById('catalog-count-badge');
    if (!form || !grid) return;

    let debounceTimer;
    let controller;

    function buscar() {
      if (controller) controller.abort();
      controller = new AbortController();

      const params = new URLSearchParams();
      const q = search?.value?.trim();
      if (q) params.set('q', q);
      if (cat?.value) params.set('categoria', cat.value);
      if (loc?.value) params.set('ubicacion', loc.value);

      fetch('/api/catalogo/buscar?' + params.toString(), { signal: controller.signal })
        .then(r => r.json())
        .then(data => {
          if (badge) {
            badge.textContent = data.length + ' ' + (data.length === 1 ? 'emprendimiento encontrado' : 'emprendimientos encontrados');
          }
          if (data.length === 0) {
            grid.innerHTML = '';
            grid.classList.add('hidden');
            if (empty) empty.classList.remove('hidden');
            return;
          }
          if (empty) empty.classList.add('hidden');
          grid.classList.remove('hidden');
          grid.innerHTML = data.map((emp, i) => Catalogo.renderCard(emp, i)).join('');
        })
        .catch(e => { if (e.name !== 'AbortError') console.error(e); });
    }

    search?.addEventListener('input', () => {
      clearTimeout(debounceTimer);
      debounceTimer = setTimeout(buscar, 200);
    });

    cat?.addEventListener('change', buscar);
    loc?.addEventListener('change', buscar);

    form.addEventListener('submit', e => e.preventDefault());
  },

  renderCard(emp, index) {
    const cover = emp.portadaUrl || emp.logoUrl || '/img/placeholder-cover.jpg';
    const logo = emp.logoUrl || '/img/placeholder-logo.jpg';
    const ubicText = emp.esVirtual ? 'Tienda virtual' : (emp.ubicacion || '');
    const locColor = emp.esVirtual ? 'color: var(--color-text-muted);' : 'color: var(--color-primary);';
    const statusBadge = emp.abierto
      ? `<span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-lg text-xs font-semibold bg-emerald-500/90 text-white backdrop-blur-sm shadow-sm">
           <span class="w-2 h-2 rounded-full bg-white animate-subtle-pulse"></span>Abierto ahora
         </span>`
      : `<span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-lg text-xs font-medium bg-slate-800/80 text-slate-200 backdrop-blur-sm shadow-sm">
           <span class="w-1.5 h-1.5 rounded-full bg-slate-400"></span>Cerrado
         </span>`;

    return `<article class="ps-surface rounded-2xl overflow-hidden card-hover cursor-pointer flex flex-col group stagger-item" style="animation-delay:${index * 0.04}s;">
      <a href="/catalogo/${emp.slug}" class="flex flex-col flex-1">
        <div class="relative w-full aspect-[16/9] overflow-hidden select-none" style="background: var(--color-badge-bg);">
          <img src="${cover}" alt="${emp.nombre}" class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-[1.04]" loading="lazy"/>
          <div class="absolute top-3 left-3 right-3 flex items-start justify-between gap-2 pointer-events-none">
            <span class="inline-flex items-center px-2.5 py-1 rounded-lg text-xs font-semibold bg-slate-900/75 text-white backdrop-blur-sm shadow-sm">${emp.categoriaNombre || ''}</span>
            ${statusBadge}
          </div>
          <div class="absolute bottom-3 left-3 pointer-events-none">
            <div class="w-14 h-14 rounded-xl border-2 border-white shadow-md overflow-hidden bg-white shrink-0">
              <img src="${logo}" alt="${emp.nombre} Logo" class="w-full h-full object-cover"/>
            </div>
          </div>
        </div>
        <div class="p-4 sm:p-5 flex-1 flex flex-col justify-between">
          <div>
            <h3 class="text-base sm:text-lg font-bold line-clamp-1 transition-colors" style="color: var(--color-text);">${emp.nombre}</h3>
            <p class="text-xs sm:text-sm line-clamp-2 mt-1 mb-3 leading-relaxed" style="color: var(--color-text-secondary);">${emp.descripcion || ''}</p>
          </div>
          <div class="pt-3 flex items-center justify-between gap-2 text-xs" style="border-top: 1px solid var(--color-border); color: var(--color-text-muted);">
            <span class="flex items-center gap-1.5 truncate max-w-[200px]">
              <svg style="${locColor}" class="w-4 h-4 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"/>
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"/>
              </svg>
              <span class="truncate">${ubicText}</span>
            </span>
            <span class="font-medium flex items-center gap-0.5 shrink-0 group-hover:gap-1.5 transition-all" style="color: var(--color-primary);">
              Ver negocio
              <svg class="w-3.5 h-3.5 transition-transform group-hover:translate-x-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
              </svg>
            </span>
          </div>
        </div>
      </a>
    </article>`;
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
// THEME TOGGLE (claro / oscuro)
// ============================================================

const ThemeToggle = {
  STORAGE_KEY: 'polishop-theme',

  init() {
    const btn = document.getElementById('theme-toggle');
    if (!btn) return;

    btn.addEventListener('click', () => {
      const html = document.documentElement;
      const isDark = html.getAttribute('data-theme') === 'dark';
      const next = isDark ? 'light' : 'dark';

      html.classList.add('theme-transitioning');
      html.setAttribute('data-theme', next);
      localStorage.setItem(this.STORAGE_KEY, next);

      setTimeout(() => html.classList.remove('theme-transitioning'), 400);
    });
  },
};


// ============================================================
// INICIALIZACION
// ============================================================

document.addEventListener('DOMContentLoaded', () => {
  document.querySelector('form[action*="/auth/login"]')
    ?.addEventListener('submit', e => Auth.validarFormulario(e));
  document.querySelector('form[action*="/auth/registro"]')
    ?.addEventListener('submit', e => Auth.validarFormulario(e));

  const regPass = document.getElementById('auth-password-input');
  const regConfirm = document.getElementById('confirmarContrasena');
  if (regPass && regConfirm && document.querySelector('form[action*="/auth/registro"]')) {
    const checkMatch = () => {
      const pass = regPass.value;
      const confirm = regConfirm.value;
      [regPass, regConfirm].forEach(inp => {
        inp.style.borderColor = '';
        inp.style.boxShadow = '';
      });
      if (confirm.length === 0) return;
      if (pass === confirm) {
        [regPass, regConfirm].forEach(inp => {
          inp.style.borderColor = '#10b981';
          inp.style.boxShadow = '0 0 0 2px rgba(16,185,129,0.25)';
        });
      } else {
        [regPass, regConfirm].forEach(inp => {
          inp.style.borderColor = '#ef4444';
          inp.style.boxShadow = '0 0 0 2px rgba(239,68,68,0.25)';
        });
      }
    };
    regPass.addEventListener('input', checkMatch);
    regConfirm.addEventListener('input', checkMatch);
  }

  const modalPass = document.getElementById('contrasena-nueva');
  const modalConfirm = document.getElementById('contrasena-confirmar');
  if (modalPass && modalConfirm) {
    const checkModalMatch = () => {
      const pass = modalPass.value;
      const confirm = modalConfirm.value;
      [modalPass, modalConfirm].forEach(inp => {
        inp.style.borderColor = '';
        inp.style.boxShadow = '';
      });
      if (confirm.length === 0) return;
      if (pass === confirm) {
        [modalPass, modalConfirm].forEach(inp => {
          inp.style.borderColor = '#10b981';
          inp.style.boxShadow = '0 0 0 2px rgba(16,185,129,0.25)';
        });
      } else {
        [modalPass, modalConfirm].forEach(inp => {
          inp.style.borderColor = '#ef4444';
          inp.style.boxShadow = '0 0 0 2px rgba(239,68,68,0.25)';
        });
      }
    };
    modalPass.addEventListener('input', checkModalMatch);
    modalConfirm.addEventListener('input', checkModalMatch);
  }

  MeGusta.inicializarEstilos();
  Catalogo.init();
  NavMobile.init();
  ThemeToggle.init();

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