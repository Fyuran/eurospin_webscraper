window.addEventListener('DOMContentLoaded', function() {
    
    let category = document.getElementById("category");
    let brand = document.getElementById("brand");

    category.addEventListener('change', (event) => {
        document.forms["filter-form-category"].submit();
    })
    brand.addEventListener('change', (event) => {
        document.forms["filter-form-brand"].submit();
    })
});

