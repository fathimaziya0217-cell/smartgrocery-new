// Smart Grocery Inventory & Shopping List Generator JavaScript

document.addEventListener('DOMContentLoaded', function () {
    // Auto dismiss alerts after 5 seconds
    const alerts = document.querySelectorAll('.alert-dismissible');
    alerts.forEach(function (alert) {
        setTimeout(function () {
            const bsAlert = bootstrap.Alert.getOrCreateInstance(alert);
            if (bsAlert) {
                bsAlert.close();
            }
        }, 5000);
    });

    // Barcode quick-fill handler
    const barcodeButtons = document.querySelectorAll('.btn-fill-barcode');
    barcodeButtons.forEach(function (btn) {
        btn.addEventListener('click', function () {
            const barcodeVal = this.getAttribute('data-barcode');
            const input = document.getElementById('barcodeInput');
            if (input) {
                input.value = barcodeVal;
            }
        });
    });

    // Dynamic Receipt Row Addition
    const addReceiptRowBtn = document.getElementById('addReceiptRowBtn');
    if (addReceiptRowBtn) {
        addReceiptRowBtn.addEventListener('click', function () {
            const tableBody = document.querySelector('#receiptItemsTable tbody');
            const rowCount = tableBody.querySelectorAll('tr').length;

            const newRow = document.createElement('tr');
            newRow.innerHTML = `
                <td>
                    <input type="text" name="items[${rowCount}].itemName" class="form-control form-control-sm" placeholder="Item Name" required>
                </td>
                <td>
                    <select name="items[${rowCount}].categoryName" class="form-select form-select-sm">
                        <option value="Fruits">Fruits</option>
                        <option value="Vegetables">Vegetables</option>
                        <option value="Dairy">Dairy</option>
                        <option value="Grains">Grains</option>
                        <option value="Bakery">Bakery</option>
                        <option value="Meat and Fish">Meat and Fish</option>
                        <option value="Beverages">Beverages</option>
                        <option value="Snacks">Snacks</option>
                        <option value="Spices">Spices</option>
                        <option value="Household Essentials">Household Essentials</option>
                        <option value="Other" selected>Other</option>
                    </select>
                </td>
                <td>
                    <input type="number" step="0.01" min="0.01" name="items[${rowCount}].quantity" class="form-control form-control-sm" value="1.0" required>
                </td>
                <td>
                    <select name="items[${rowCount}].unit" class="form-select form-select-sm">
                        <option value="piece">piece</option>
                        <option value="kg">kg</option>
                        <option value="g">g</option>
                        <option value="litre">litre</option>
                        <option value="ml">ml</option>
                    </select>
                </td>
                <td>
                    <input type="number" step="0.01" min="0.00" name="items[${rowCount}].price" class="form-control form-control-sm" value="0.00" required>
                </td>
                <td>
                    <input type="date" name="items[${rowCount}].expiryDate" class="form-control form-control-sm">
                </td>
                <td class="text-center">
                    <button type="button" class="btn btn-outline-danger btn-sm remove-row-btn">
                        <i class="bi bi-trash"></i>
                    </button>
                </td>
            `;
            tableBody.appendChild(newRow);

            newRow.querySelector('.remove-row-btn').addEventListener('click', function () {
                newRow.remove();
            });
        });
    }

    // Dynamic Recipe Ingredient Row Addition
    const addIngredientBtn = document.getElementById('addIngredientBtn');
    if (addIngredientBtn) {
        addIngredientBtn.addEventListener('click', function () {
            const container = document.getElementById('ingredientsContainer');
            const count = container.querySelectorAll('.ingredient-row').length;

            // Clone template row
            const firstRow = container.querySelector('.ingredient-row');
            if (firstRow) {
                const clone = firstRow.cloneNode(true);
                clone.querySelectorAll('input, select').forEach(function (input) {
                    const name = input.getAttribute('name');
                    if (name) {
                        input.setAttribute('name', name.replace(/\[\d+\]/, `[${count}]`));
                    }
                    if (input.tagName === 'INPUT') {
                        input.value = '';
                    }
                });
                const removeBtn = clone.querySelector('.remove-ingredient-btn');
                if (removeBtn) {
                    removeBtn.style.display = 'inline-block';
                    removeBtn.addEventListener('click', function () {
                        clone.remove();
                    });
                }
                container.appendChild(clone);
            }
        });
    }

    // Bind remove buttons
    document.querySelectorAll('.remove-ingredient-btn').forEach(function (btn) {
        btn.addEventListener('click', function () {
            this.closest('.ingredient-row').remove();
        });
    });
});
