let $ = jQuery;
let table = undefined;
let ctx = $("meta[name='ctx']").attr("content");
let options = {
    background: 'rgba(255, 255, 255, 0.75)'
}

document.getElementById("current-year").innerHTML = new Date().getFullYear() + "";

function reverseSelection(){
    let rows = table.rows({selected : true})[0];
    table.rows().select();
    table.rows(rows).deselect();
}

function changeTransportWay(event) {
    toggleTransportFields(event.target.value);
}

function toggleTransportFields(way){
    if(way === '0'){
        $('#transport-details').removeClass('d-none');
        $('#transfer-truck-number').removeAttr('disabled');
        $('#transfer-driver').removeAttr('disabled');
    }else{
        $('#transport-details').addClass('d-none');
        $('#transfer-truck-number').attr('disabled', 'disabled');
        $('#transfer-driver').attr('disabled', 'disabled');
    }
}

function deleteItem(id, url){
    $("#delete-messages").text("Voulez-vous vraiment supprimer cet élément ?");
    if(url.includes('user')) $("#delete-message").text("Voulez-vous vraiment supprimer cet utilisateur ?");
    let container = document.getElementById("delete-objects");
    container.innerHTML = '';
    let input = document.createElement("input");
    input.type = "hidden";
    input.name = "ids";
    input.value = id;
    container.appendChild(input);
    $("#modal-delete").modal('show');
}

function editUser(id, firstName, lastName, email, role) {
    $("#user-id").val(id);
    $("#first-name").val(firstName);
    $("#last-name").val(lastName);
    $("#user-email").val(email);
    $("#user-role").val(role);
    $("#modal-create").modal('show');
}

function editProduct(id, name, passage, passageTax, refinery, specialTax, transport, marking, markingTax) {
    $("#product-id").val(id);
    $("#product-name").val(name);
    $("#passage").val(passage);
    $("#passageTax").val(passageTax);
    $("#refinery").val(refinery);
    $("#specialTax").val(specialTax);
    $("#transport").val(transport);
    $("#marking").val(marking);
    $("#markingTax").val(markingTax);
    $("#modal-create").modal('show');
}

function editDepot(id, name) {
    $("#depot-id").val(id);
    $("#depot-name").val(name);
    $("#modal-create").modal('show');
}

function editInvoice(id, client, product, volume, validity, loadingDepot, transporter, driver, loadingDate, truckNumber, deliveryPlace, receiptDate) {
    $("#invoice-id").val(id);
    $("#invoice-client").val(client);
    $("#invoice-product").val(product);
    $("#invoice-volume").val(volume);
    $("#invoice-validity").val(validity);
    $("#invoice-loading-depot").val(loadingDepot);
    $("#invoice-transporter").val(transporter);
    $("#invoice-driver").val(driver);
    $("#invoice-loading-date").val(loadingDate);
    $("#invoice-truck-number").val(truckNumber);
    $("#invoice-delivery-place").val(deliveryPlace);
    $("#invoice-receipt-date").val(receiptDate);
    $("#modal-create").modal('show');
}

function editTransfer(id, client, product, volume, loadingDepot, transporter, driver, loadingDate, truckNumber, deliveryPlace, receiptDate) {
    $("#transfer-id").val(id);
    $("#transfer-client").val(client);
    $("#transfer-product").val(product);
    $("#transfer-volume").val(volume);
    $("#transfer-loading-depot").val(loadingDepot);
    $("#transfer-transporter").val(transporter);
    $("#transfer-driver").val(driver);
    $("#transfer-loading-date").val(loadingDate);
    $("#transfer-truck-number").val(truckNumber);
    $("#transfer-delivery-place").val(deliveryPlace);
    $("#transfer-receipt-date").val(receiptDate);
    let way = driver || truckNumber ? '0' : '1';
    $("#transport-road").val(way);
    toggleTransportFields(way);
    $("#modal-create").modal('show');
}

function editSupply(id, product, depot, volume) {
    $("#supply-id").val(id);
    $("#supply-product").val(product);
    $("#supply-depot").val(product);
    $("#supply-volume").val(volume);
    $("#modal-create").modal('show');
}

function editFuel(id, number, amount) {
    $("#fuel-id").val(id);
    $("#fuel-number").val(number);
    $("#fuel-amount").val(amount);
    $("#modal-create").modal('show');
}

function rejectInvoice(id) {
    $("#invoice-reject-id").val(id);
    $("#modal-reject").modal('show');
}

function rejectTransfer(id) {
    $("#transfer-reject-id").val(id);
    $("#modal-reject").modal('show');
}

function showDetails(reason){
    $("#error-details").html(reason);
    $("#modal-details").modal('show');
}

function invoke(action, object = 'user') {
    let values = $.makeArray(table.rows({selected : true}).data().map(line => $($.parseHTML(line[0])).val()));
    if(values === undefined || values.length === 0){
        new SnackBar({
            message: 'Aucun élément sélectionné',
            status: 'error',
            dismissible: false,
            position: 'bc',
            fixed: true,
            timeout: 3000,
        });
    }else{
        if(action === 'delete'){
            let objectName = 'élément';
            switch (object) {
                case 'user':
                    objectName = 'utilisateur';
                    break;
                case 'event':
                    objectName = 'évènement';
                    break;
                default:
                    break;
            }
            $("#delete-message").text("Voulez-vous vraiment supprimer " + (values.length < 2 ? "cet " + objectName :  "ces " + values.length + " " + objectName + "s") + " ?");
            let container = document.getElementById("delete-objects");
            container.innerHTML = '';
            for(let value of values){
                let input = document.createElement("input");
                input.type = "hidden";
                input.value = value;
                input.name = 'ids';
                container.appendChild(input);
            }
            $("#modal-delete").modal('show');
        }
    }
}


function monthlyReport(e){
    window.location = ctx + '/taxes?month=' + e.target.value;
}

function initPagination(){
    let paginator = $('#pagination');
    if(paginator.length !== 0){
        paginator.pagination({
            dataSource: Array.from(Array(parseInt(paginator.attr('title'))).keys()),
            pageSize: 1,
            pageNumber: parseInt(paginator.attr('aria-placeholder')),
            showGoInput: true,
            showGoButton: true,
            triggerPagingOnInit: false,
            callback: function(data, pagination) {
                if(paginator.hasClass('data-search')){
                    $('#current-page').val(pagination.pageNumber);
                    $('#search-button-id').click();
                }else{
                    window.location = ctx + '/' + paginator.attr('aria-label') + '?p=' + pagination.pageNumber;
                }
            }
        })
    }
}

$(document).ready( function () {
    initPagination();
    let list = $('#main-table');
    table = list.DataTable({
        dom: 'Bfrtip',
        aaSorting: [],
        pageLength: 50,
        responsive: true,
        paging: list.hasClass("paging"),
        searching: list.hasClass("searching"),
        orderCellsTop: true,
        fixedHeader: true,
        info: !list.hasClass("no-info"),
        columnDefs:  [
            list.hasClass("include-last-sort") ? {} : {orderable: false, targets: -1},
            list.hasClass("exclude-first-sort") ? {orderable: false, targets: 0} : {},
            list.hasClass("multiple-selection") ? {
                'targets': 0,
                'checkboxes': {
                    'selectRow': true,
                    'selectAllPages': false,
                    'selectAllRender': '<input type="checkbox" class="form-check-input">',
                },
                'render': function (data){
                    return data;
                }
            } : {}
        ],
        select: {
            style: 'multi',
            selector: 'td:first-child input:checkbox'
        },
    });
    $('#search-addon').on('keyup', function () {
        table.search(this.value).draw();
    });
});