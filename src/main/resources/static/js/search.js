// Compartilhar arquivo a partir do botão
function shareFileFromButton(button) {
    const fileName = button.getAttribute('data-file');
    const baseUrl = window.location.origin;
    // Codifica parte por parte, preservando as barras
    const relativePath = fileName.split('/').map(encodeURIComponent).join('/');
    const shareUrl = `${baseUrl}/storage/shared/view/${relativePath}`;


    navigator.clipboard.writeText(shareUrl).then(function () {
        showToast(`Link copiado!`);
    }, function (err) {
        showToast("Erro ao copiar link: " + err);
    });
}



// Exibe um toast animado
function showToast(message) {
    const toast = document.getElementById("toast");
    toast.innerHTML = message;
    toast.classList.add("show");

    setTimeout(() => {
        toast.classList.remove("show");
    }, 4000); // some depois de 4 segundos
}

//download unico
document.querySelectorAll('.btn-download').forEach(link => {
    link.addEventListener('click', e => {
        e.preventDefault();
        const filePath = e.currentTarget.getAttribute('data-file');
        const encodedPath = filePath
            .split('/')
            .map(encodeURIComponent)
            .join('/');
        const downloadUrl = `/download/${encodedPath}`;
        window.open(downloadUrl, '_blank');
    });
});



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


// Função de renomear (Exemplo)
function renameFile(filePath) {
    const parts = filePath.split('/');
    const currentFileName = parts.pop();
    const directoryPath = parts.join('/');
    const newFileName = prompt("Digite o novo nome para o arquivo:", currentFileName);

    if (newFileName && newFileName !== currentFileName) {
        // Remove "uploads/" do início, pois o backend já adiciona esse caminho base
        const cleanPath = filePath.startsWith("uploads/") ? filePath.substring(8) : filePath;

        const params = new URLSearchParams({
            fullPath: cleanPath,  // <- sem o "uploads/" duplicado
            newName: newFileName
        });

        fetch('/storage/search/renameFile?' + params.toString(), {
            method: 'PATCH',
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('authToken')
            }
        })
        .then(response => {
            if (response.ok) {
                alert("Arquivo renomeado com sucesso!");
                location.reload();
            } else {
                return response.text().then(text => {
                    console.error("Erro ao renomear:", text);
                    alert("Erro ao renomear o arquivo:\n" + text);
                });
            }
        })
        .catch(error => {
            console.error("Erro ao renomear:", error);
            alert("Erro ao renomear o arquivo.");
        });
    }
}

// Função de deletar
// Função de deletar
function deleteFile(fileName) {
    const confirmation = confirm(`Tem certeza que deseja deletar o arquivo ${fileName}?`);

    if (confirmation) {
        const requestBody = JSON.stringify({ path: fileName });  // Envia 'path' no corpo da requisição

        fetch('/storage/delete', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('authToken')  // Envia o token de autenticação
            },
            body: requestBody,
            credentials: 'same-origin'  // Assegura que o cookie será enviado junto com a requisição
        })
        .then(response => {
            if (!response.ok) {
                return response.text()  // Pega a resposta como texto
                    .then(errorText => {
                        // Exibe o erro como texto (não tenta fazer JSON se for texto)
                        console.error('Erro do servidor:', errorText);
                        throw new Error(errorText);
                    });
            }
            return response.json();  // Retorna a resposta JSON normalmente
        })
        .then(data => {
            alert(`O arquivo ${fileName} foi deletado com sucesso!`);
            location.reload();
        })
        .catch(error => {
            console.error("Erro ao deletar:", error);
            alert("Erro ao deletar o arquivo.");
        });

    }
}





