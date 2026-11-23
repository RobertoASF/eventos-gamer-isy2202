// Detalle del evento: revisa el peso del archivo antes de subirlo, registra cuando se comparte en una red
// social y copia el enlace del evento. Es un archivo propio del sitio porque la politica CSP no permite
// scripts escritos dentro del HTML.
(function () {
    'use strict';

    var formularioSubida = document.querySelector('[data-limite-foto]');
    if (formularioSubida) {
        var campoArchivo = formularioSubida.querySelector('input[type="file"]');
        var errorArchivo = document.getElementById('error-archivo');
        formularioSubida.addEventListener('submit', function (evento) {
            var archivo = campoArchivo.files[0];
            if (!archivo) {
                return;
            }
            var esVideo = archivo.type.indexOf('video/') === 0;
            var limite = Number(esVideo ? formularioSubida.dataset.limiteVideo : formularioSubida.dataset.limiteFoto);
            if (archivo.size > limite) {
                evento.preventDefault();
                errorArchivo.textContent = esVideo
                    ? 'El video supera los 7 MB permitidos.'
                    : 'La foto supera los 5 MB permitidos.';
                errorArchivo.hidden = false;
            }
        });
    }

    // El token CSRF de la sesion viaja en un encabezado; sin el, el servidor rechaza el registro
    var redes = document.getElementById('redes');
    if (redes) {
        redes.querySelectorAll('[data-registro]').forEach(function (enlace) {
            enlace.addEventListener('click', function () {
                var cabeceras = {};
                cabeceras[redes.dataset.csrfCabecera] = redes.dataset.csrf;
                fetch(enlace.dataset.registro, {
                    method: 'POST',
                    headers: cabeceras,
                    credentials: 'same-origin',
                    keepalive: true
                }).catch(function () {
                    // Si falla el registro igual se abre la red social
                });
            });
        });
    }

    var botonCopiar = document.querySelector('[data-copiar]');
    if (botonCopiar && navigator.clipboard) {
        var estado = document.getElementById('estado-copiar');
        botonCopiar.hidden = false;
        botonCopiar.addEventListener('click', function () {
            navigator.clipboard.writeText(botonCopiar.dataset.copiar).then(function () {
                estado.textContent = 'Enlace copiado.';
            }, function () {
                estado.textContent = 'No se pudo copiar, selecciona el enlace y cópialo a mano.';
            });
        });
    }
})();
