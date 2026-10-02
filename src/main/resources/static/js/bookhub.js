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
