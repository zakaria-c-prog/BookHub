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

    // Faint grid behind the page
    const grid = document.createElement('div');
    grid.className = 'bh-grid-bg';
    document.body.prepend(grid);

    // Staggered entrance for the page header, cards and alerts
    document.querySelectorAll('main .bh-page-head, main .breadcrumb, main .bh-card, main > h2')
        .forEach(function (el, i) {
            el.classList.add('bh-reveal');
            el.style.setProperty('--i', Math.min(i, 12));
        });

    // Navbar gets a stronger background once the page scrolls
    const nav = document.querySelector('.bh-navbar');
    if (nav) {
        const onScroll = function () { nav.classList.toggle('scrolled', window.scrollY > 8); };
        window.addEventListener('scroll', onScroll, { passive: true });
        onScroll();
    }

    // Cursor spotlight on cards
    document.addEventListener('pointermove', function (event) {
        const card = event.target.closest && event.target.closest('.bh-card');
        if (!card) return;
        const box = card.getBoundingClientRect();
        card.style.setProperty('--mx', (event.clientX - box.left) + 'px');
        card.style.setProperty('--my', (event.clientY - box.top) + 'px');
    }, { passive: true });

    // Ripple on button press
    document.addEventListener('pointerdown', function (event) {
        const btn = event.target.closest && event.target.closest('.btn');
        if (!btn || btn.disabled || reduceMotion) return;
        const box = btn.getBoundingClientRect();
        const size = Math.max(box.width, box.height);
        const ripple = document.createElement('span');
        ripple.className = 'bh-ripple';
        ripple.style.width = ripple.style.height = size + 'px';
        ripple.style.left = (event.clientX - box.left - size / 2) + 'px';
        ripple.style.top = (event.clientY - box.top - size / 2) + 'px';
        btn.appendChild(ripple);
        ripple.addEventListener('animationend', function () { ripple.remove(); });
    });

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
            const duration = 1200;
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
