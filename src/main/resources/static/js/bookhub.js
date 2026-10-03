// Ask before submitting any form marked with data-confirm (used by the delete buttons)
document.addEventListener('submit', function (event) {
    const form = event.target;
    const message = form.getAttribute('data-confirm');
    if (message && !window.confirm(message)) {
        event.preventDefault();
    }
});

// Price panel: switch the label/limits of the value box between "set price" and "discount %"
document.querySelectorAll('[data-price-mode]').forEach(function (radio) {
    radio.addEventListener('change', function () {
        const input = document.getElementById('priceValue');
        const unit = document.getElementById('priceUnit');
        if (radio.value === 'discount') {
            input.min = 1; input.max = 90; input.step = 1;
            input.placeholder = 'e.g. 15';
            unit.textContent = '%';
        } else {
            input.min = 0.01; input.removeAttribute('max'); input.step = 0.01;
            input.placeholder = 'new price';
            unit.textContent = '¥';
        }
        input.value = '';
    });
});

// ---------- UI effects ----------
(function () {
    const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    const finePointer = window.matchMedia('(pointer: fine)').matches;

    // Sections fade and rise into place: at once if visible, otherwise as they scroll into view
    const revealables = document.querySelectorAll('main .bh-page-head, main .bh-card, main > h2, main .row > [class*="col"] > .bh-card');
    if ('IntersectionObserver' in window && !reduceMotion) {
        let batch = 0;
        const observer = new IntersectionObserver(function (entries) {
            entries.forEach(function (entry) {
                if (entry.isIntersecting) {
                    entry.target.style.setProperty('--i', Math.min(batch++, 10));
                    entry.target.classList.add('in');
                    observer.unobserve(entry.target);
                }
            });
            batch = 0;
        }, { threshold: 0.08 });
        revealables.forEach(function (el) {
            if (el.hasAttribute('data-tilt')) return; // their transform belongs to the tilt
            el.classList.add('bh-reveal');
            observer.observe(el);
        });
    }

    // Navbar gets a stronger background once the page scrolls
    const nav = document.querySelector('.bh-navbar');
    if (nav) {
        const onScroll = function () { nav.classList.toggle('scrolled', window.scrollY > 8); };
        window.addEventListener('scroll', onScroll, { passive: true });
        onScroll();
    }

    // Pointer-driven 3D tilt: [data-tilt] cards lean gently, [data-book3d] books turn further
    function tilt(el, maxX, maxY, glare) {
        el.addEventListener('pointermove', function (event) {
            const box = el.getBoundingClientRect();
            const x = (event.clientX - box.left) / box.width;
            const y = (event.clientY - box.top) / box.height;
            el.classList.add('tracking');
            el.style.setProperty('--ry', ((x - 0.5) * maxY).toFixed(2) + 'deg');
            el.style.setProperty('--rx', ((0.5 - y) * maxX).toFixed(2) + 'deg');
            if (glare) {
                el.style.setProperty('--gx', (x * 100).toFixed(1) + '%');
                el.style.setProperty('--gy', (y * 100).toFixed(1) + '%');
            }
        });
        el.addEventListener('pointerleave', function () {
            el.classList.remove('tracking');
            el.style.setProperty('--rx', '0deg');
            el.style.setProperty('--ry', '0deg');
        });
    }
    if (finePointer && !reduceMotion) {
        document.querySelectorAll('[data-tilt]').forEach(function (el) { tilt(el, 10, 12, true); });
        document.querySelectorAll('[data-book3d]').forEach(function (el) { tilt(el, 12, 26, false); });
    }

    // Shelf: show the title of the book under the pointer
    const caption = document.querySelector('[data-shelf-caption]');
    if (caption) {
        const idle = caption.textContent;
        document.querySelectorAll('.bh-shelf a').forEach(function (link) {
            link.addEventListener('pointerenter', function () { caption.textContent = link.dataset.caption; caption.style.color = '#fff'; });
            link.addEventListener('pointerleave', function () { caption.textContent = idle; caption.style.color = ''; });
        });
    }

    // Count the dashboard numbers up from zero, keeping any prefix like "¥"
    if (!reduceMotion) {
        document.querySelectorAll('.bh-stat .value').forEach(function (el) {
            const match = el.textContent.trim().match(/^(\D*)([\d,]+(?:\.\d+)?)(.*)$/);
            if (!match) return;
            const prefix = match[1], suffix = match[3];
            const target = parseFloat(match[2].replace(/,/g, ''));
            const decimals = (match[2].split('.')[1] || '').length;
            const format = function (n) {
                return prefix + n.toLocaleString('en-US', { minimumFractionDigits: decimals, maximumFractionDigits: decimals }) + suffix;
            };
            const duration = 1400;
            let start = null;
            const step = function (time) {
                if (start === null) start = time;
                const t = Math.min((time - start) / duration, 1);
                el.textContent = format(target * (1 - Math.pow(1 - t, 4)));
                if (t < 1) requestAnimationFrame(step);
            };
            el.textContent = format(0);
            requestAnimationFrame(step);
        });
    }

    // Success messages fade away on their own after a few seconds
    document.querySelectorAll('.alert-success').forEach(function (alert) {
        setTimeout(function () {
            if (window.bootstrap && document.body.contains(alert)) {
                bootstrap.Alert.getOrCreateInstance(alert).close();
            }
        }, 5000);
    });
})();
