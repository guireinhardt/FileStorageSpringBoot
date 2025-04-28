// Atualiza o filtro de subpalavras-chave
const subKeywordsMap = /*[[${subkeywordsMap}]]*/ {};

function updateSubKeywords() {
    const keyword = document.getElementById("keyword").value;
    const subkeywordSelect = document.getElementById("subkeyword");

    subkeywordSelect.innerHTML = '<option value="">Selecione uma subpalavra-chave</option>';

    if (subKeywordsMap[keyword]) {
        subkeywordSelect.style.display = 'inline-block';

        subKeywordsMap[keyword].forEach(subkeyword => {
            const option = document.createElement("option");
            option.value = subkeyword;
            option.textContent = subkeyword;
            subkeywordSelect.appendChild(option);
        });
    } else {
        subkeywordSelect.style.display = 'none';
    }
}

// Compartilhar arquivo
function shareFile(fileName) {
    const url = window.location.origin + "/view/" + fileName;
    navigator.clipboard.writeText(url).then(() => {
        alert('Link copiado para a área de transferência!');
    }).catch(err => {
        alert('Erro ao copiar link');
    });
}

// Submeter múltiplos downloads
const bulkDownloadForm = document.getElementById('bulkDownloadForm');
bulkDownloadForm.addEventListener('submit', function(e) {
    e.preventDefault();

    const selected = document.querySelectorAll('.file-checkbox:checked');
    if (selected.length === 0) {
        alert('Selecione pelo menos um arquivo para baixar.');
        return;
    }

    const formData = new FormData();
    selected.forEach(checkbox => {
        formData.append('selectedFiles', checkbox.value);
    });

    fetch('/storage/bulk-download', {
        method: 'POST',
        body: formData
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Erro ao gerar o arquivo zip.');
        }
        return response.blob();
    })
    .then(blob => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'arquivos.zip';
        document.body.appendChild(a);
        a.click();
        a.remove();
    })
    .catch(error => {
        console.error('Erro:', error);
        alert('Ocorreu um erro ao baixar os arquivos.');
    });
});
