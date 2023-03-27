let $ = jQuery;
let table = undefined;
let ctx = $("meta[name='ctx']").attr("content");
let options = {
    background: 'rgba(255, 255, 255, 0.75)'
}

function reverseSelection(){
    let rows = table.rows({selected : true})[0];
    table.rows().select();
    table.rows(rows).deselect();
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

function editInvoice(id, client, product, volume, loadingDepot, transporter, driver, loadingDate, truckNumber, deliveryPlace, receiptDate) {
    $("#invoice-id").val(id);
    $("#invoice-client").val(client);
    $("#invoice-product").val(product);
    $("#invoice-volume").val(volume);
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
    $("#modal-create").modal('show');
}

function editSupply(id, product, depot, volume) {
    $("#supply-id").val(id);
    $("#supply-product").val(product);
    $("#supply-depot").val(product);
    $("#supply-volume").val(volume);
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
        if(action === 'download'){
            downloadFile(values, '/download/files');
        }else if(action === 'delete'){
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

function downloadFile(paths, url) {
    $("#wrapper").LoadingOverlay('show', options);
    let xhr = new XMLHttpRequest();
    let params = paths.map(path => 'path=' + path).join('&');
    xhr.open('GET', ctx + url + '?' + params, true);
    xhr.responseType = 'arraybuffer';
    xhr.onload = function() {
        if(this.status === 200) {
            let filename = '';
            let disposition = xhr.getResponseHeader('Content-Disposition');
            if (disposition && disposition.indexOf('attachment') !== -1) {
                let filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
                let matches = filenameRegex.exec(disposition);
                if (matches !== null && matches[1]) filename = matches[1].replace(/['"]/g, '');
            }
            let type = xhr.getResponseHeader('Content-Type');
            let blob = new Blob([this.response],  {type: type});
            //workaround for IE
            if(typeof window.navigator.msSaveBlob != 'undefined') {
                window.navigator.msSaveBlob(blob, filename);
            } else {
                let URL = window.URL || window.webkitURL;
                let download_URL = URL.createObjectURL(blob);
                if(filename) {
                    let a_link = document.createElement('a');
                    if(typeof a_link.download == 'undefined') {
                        window.location = download_URL;
                    }else {
                        a_link.href = download_URL;
                        a_link.download = filename;
                        document.body.appendChild(a_link);
                        a_link.click();
                    }
                }else {
                    window.location = download_URL;
                }
                setTimeout(function() {
                    URL.revokeObjectURL(download_URL);
                }, 10000);
            }
        }else {
            new SnackBar({
                message: 'Téléchargement échoué',
                status: 'error',
                dismissible: false,
                position: 'bc',
                fixed: true,
                timeout: 3000,
            });
        }
        $('#wrapper').LoadingOverlay('hide', options);
        $('#wrapper').scrollTop(0);
    };
    xhr.setRequestHeader('Content-type', 'application/*');
    xhr.send();
}

function showLogDetails(id, level){
    if(level !== 'ERROR'){
        new SnackBar({
            message: 'Aucun détail concernant cet évènement',
            status: 'error',
            dismissible: false,
            position: 'bc',
            fixed: true,
            timeout: 3000,
        });
        return;
    }
    $("#wrapper").LoadingOverlay('show', options);
    let xhr = new XMLHttpRequest();
    xhr.open('GET', ctx + '/logs/' + id, true);
    xhr.onload = function() {
        if(this.status === 200 && this.response !== undefined && this.response.length > 0) {
            $('#error-details').html(this.response);
            $("#modal-details").modal('show');
        }else {
            new SnackBar({
                message: 'Aucun détail concernant cet évènement',
                status: 'error',
                dismissible: false,
                position: 'bc',
                fixed: true,
                timeout: 3000,
            });
        }
        $('#wrapper').LoadingOverlay('hide', options);
        $('#wrapper').scrollTop(0);
    };
    xhr.send();
}

$.ajaxSetup({
    beforeSend: function () {
        $("#wrapper").LoadingOverlay('show', options);
    },
    complete: function () {
        $('#wrapper').LoadingOverlay('hide', options);
        $('#wrapper').scrollTop(0);
    }
})

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
                window.location = ctx + '/' + paginator.attr('aria-label') + '?p=' + pagination.pageNumber;
            }
        })
    }
}

function fetch(url){
    $.get(url, function (data) {});
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