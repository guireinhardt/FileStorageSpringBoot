// Toggle do menu do usuário
   function toggleDropdown() {
    const dropdown = document.getElementById("dropdownMenu");
    dropdown.style.display = (dropdown.style.display === "block") ? "none" : "block";
}

// Fecha o menu se clicar fora dele
window.addEventListener('click', function(e) {
    const menu = document.getElementById("dropdownMenu");
    const icon = document.querySelector(".user-icon");
    if (!icon.contains(e.target)) {
        menu.style.display = "none";
    }
});
document.addEventListener('DOMContentLoaded', function () {
    window.addEventListener('click', function (e) {
        const menu = document.getElementById("dropdownMenu");
        const icon = document.querySelector(".user-icon");
        if (!icon.contains(e.target)) {
            menu.style.display = "none";
        }
    });

    document.querySelector(".user-icon").addEventListener('click', function () {
        const dropdown = document.getElementById("dropdownMenu");
        dropdown.style.display = (dropdown.style.display === "block") ? "none" : "block";
    });
});


    // Botão flutuante abre o modal de Nova Pasta
    document.getElementById("btnNovaPasta").addEventListener("click", function () {
        document.getElementById("modalNovaPasta").style.display = "block";
    });

    // Fecha o modal ao clicar no X
    document.getElementById("closeModal").addEventListener("click", function () {
        document.getElementById("modalNovaPasta").style.display = "none";
    });

    // Fecha o modal ao clicar fora dele
    window.addEventListener("click", function (event) {
        const modal = document.getElementById("modalNovaPasta");
        if (event.target === modal) {
            modal.style.display = "none";
        }
    });

    // Reforço: botão esc fecha o modal
    window.addEventListener("keydown", function (event) {
        if (event.key === "Escape") {
            document.getElementById("modalNovaPasta").style.display = "none";
        }
    });

    // Previne o comportamento padrão ao arrastar arquivos
    ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(event => {
        window.addEventListener(event, e => {
            e.preventDefault();
            e.stopPropagation();
        });
    });

    // Soltar arquivo na janela e realizar o upload
    window.addEventListener('drop', e => {
        const files = e.dataTransfer.files;
        if (files.length > 0) {
            uploadFiles(files);
        }
    });

    // Função de upload dos arquivos
    function uploadFiles(files) {
        const folderPath = document.getElementById("folderPath").value;
        const formData = new FormData();
        formData.append("folderPath", folderPath);
        for (let i = 0; i < files.length; i++) {
            formData.append("files", files[i]);
        }

        // Mostrar barra de progresso
        const progressBar = document.getElementById('progressBar');
        const progressFill = document.getElementById('progressFill');
        progressBar.style.display = 'block';
        progressFill.style.width = '0';
        progressFill.style.animation = 'loading 2s linear forwards';

        fetch("/storage/uploadFiles", {
            method: "POST",
            body: formData,
            credentials: "include"
        })
        .then(response => {
            if (!response.ok) throw new Error("Falha no upload");
            return response.text();
        })
        .then(() => {
            setTimeout(() => {
                progressBar.style.display = 'none';
                alert("Upload realizado com sucesso!");
                location.reload();
            }, 2000);
        })
        .catch(err => {
            progressBar.style.display = 'none';
            console.error("Erro:", err);
            alert("Erro ao enviar arquivos.");
        });
    }

    // Visualizar arquivo
    document.querySelectorAll('.btn-view').forEach(link => {
        link.addEventListener('click', e => {
            e.preventDefault();
            const file = e.target.closest('a').getAttribute('data-file');
            window.open(`/view/${encodeURIComponent(file)}`, '_blank');
        });
    });

    // Download de arquivo
    document.querySelectorAll('.btn-download').forEach(link => {
        link.addEventListener('click', e => {
            e.preventDefault();
            const file = e.target.closest('a').getAttribute('data-file');
            fetch(`/storage/downloadFile/${encodeURIComponent(file)}`, {
                method: 'GET',
                credentials: 'include'
            })
            .then(res => res.blob())
            .then(blob => {
                const url = URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = file;
                document.body.appendChild(a);
                a.click();
                a.remove();
            })
            .catch(err => alert("Erro ao baixar."));
        });
    });

    // Abrir modal de renomear ao clicar no ícone
    document.querySelectorAll('.btn-rename').forEach(link => {
        link.addEventListener('click', e => {
            e.preventDefault();
            const fileName = e.target.closest('a').getAttribute('data-file');
            const fileNameWithoutExt = fileName.substring(0, fileName.lastIndexOf('.')); // Nome sem a extensão
            const fileExt = fileName.substring(fileName.lastIndexOf('.'));
            document.getElementById('oldFileName').value = fileName;
            document.getElementById('newFileName').value = fileNameWithoutExt;
            document.getElementById('modalRenameFile').style.display = 'block';
        });
    });

    // Fecha o modal de renomear ao clicar no X
    document.getElementById("closeRenameModal").addEventListener("click", function () {
        document.getElementById("modalRenameFile").style.display = "none";
    });

  // Enviar o formulário de renomear
document.getElementById('renameForm').addEventListener('submit', function (event) {
    event.preventDefault();

    const form = event.target;
    const oldFileName = form.oldFileName.value;
    const newFileName = form.newFileName.value;
    const folderPath = document.getElementById("folderPath").value;

    const isRoot = !folderPath || folderPath.trim() === "";

    const params = new URLSearchParams({
        oldFileName: oldFileName,
        newFileName: newFileName
    });

    const endpoint = isRoot
        ? `/storage/renameRootFile?${params.toString()}`
        : `/storage/renameFile?${params.toString()}`;

    fetch(endpoint, {
        method: 'PATCH'
    })
    .then(async response => {
        const isJson = response.headers.get("content-type")?.includes("application/json");
        const data = isJson ? await response.json() : null;

        if (!response.ok) {
            const errorMessage = data?.message || response.statusText;
            throw new Error(errorMessage);
        }

        return data;
    })
    .then(data => {
        if (data.success) {
            alert("Arquivo renomeado com sucesso!");
            location.reload();
        } else {
            alert("Erro ao renomear arquivo: " + data.message);
        }
    })
    .catch(err => {
        console.error("Erro:", err);
        alert("Erro ao renomear arquivo: " + err.message);
    });
});
    // Função chamada ao clicar no botão de compartilhar
function shareFileFromButton(button) {
    const fileName = button.getAttribute("data-file");

    // Gera o link de compartilhamento baseado na visualização
    const fileLink = `${window.location.origin}/view/${encodeURIComponent(fileName)}`;

    // Copia o link para a área de transferência
    navigator.clipboard.writeText(fileLink)
        .then(() => {
            alert('Link de compartilhamento copiado para a área de transferência!');
        })
        .catch(err => {
            alert('Falha ao copiar o link: ' + err);
        });
}
    let moveMode = false;

function toggleMoveFolderMode() {
    moveMode = !moveMode;

    // Mostra ou oculta os botões de mover por pasta
    document.querySelectorAll('.btn-move-folder').forEach(btn => {
        btn.style.display = moveMode ? 'inline-block' : 'none';
    });

    // Atualiza o texto ou cor do botão
    document.getElementById("moveModeStatus").style.display = moveMode ? 'inline' : 'none';

    document.getElementById("toggleMoveMode").textContent = moveMode
        ? "Desativar Modo Mover"
        : "Ativar Modo Mover";
}

// Ação ao clicar para mover a pasta
let folderToMove = "";
let currentFolderPath = "";

function moveFolder(link) {
    folderToMove = link.getAttribute("data-folder");
    currentFolderPath = document.getElementById("folderPath").value;

    document.getElementById("modalFolderName").innerText = folderToMove;

    const select = document.getElementById("destinationSelect");
    select.innerHTML = "";

    availableFolders
        .filter(folder => folder !== folderToMove)
        .forEach(folder => {
            const option = document.createElement("option");
            option.value = folder;
            option.textContent = folder;
            select.appendChild(option);
        });

    document.getElementById("moveModal").style.display = "flex";
}

function closeModal() {
    document.getElementById("moveModal").style.display = "none";
}

function confirmMove() {
    const targetFolder = document.getElementById("destinationSelect").value;
    const fullPath = `${currentFolderPath}/${folderToMove}`;

    fetch(`/storage/moveFolder?fullPath=${encodeURIComponent(fullPath)}&newParentFolder=${encodeURIComponent(targetFolder)}`, {
        method: "PATCH"
    })
    .then(res => {
        if (!res.ok) throw new Error("Erro ao mover pasta");
        return res.text();
    })
    .then(data => {
        alert("Pasta movida com sucesso!");
        location.reload();
    })
    .catch(err => {
        console.error("Erro:", err);
        alert("Erro inesperado.");
    });

    closeModal();
}

// Ícone de menu (três pontos)
function toggleActions(icon) {
    const dropdown = icon.nextElementSibling;

    // Fecha todos os outros menus abertos
    document.querySelectorAll(".dropdown-content").forEach(menu => {
        if (menu !== dropdown) {
            menu.style.display = "none";
        }
    });

    // Alterna visibilidade do menu clicado
    dropdown.style.display = dropdown.style.display === "block" ? "none" : "block";
}

// Fecha dropdown se clicar fora
document.addEventListener("click", function(event) {
    if (!event.target.closest(".folder")) {
        document.querySelectorAll(".dropdown-content").forEach(menu => {
            menu.style.display = "none";
        });
    }
});

    function downloadFolder(folderName) {
    fetch(`/storage/downloadFolder?folderPath=${encodeURIComponent(folderName)}`, {
        method: 'GET',
        credentials: 'include'
    })
    .then(res => res.blob())
    .then(blob => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = folderName + ".zip";
        document.body.appendChild(a);
        a.click();
        a.remove();
    })
    .catch(err => alert("Erro ao baixar pasta."));
}
    function handleDownload(element) {
    const folderName = element.getAttribute('data-folder-name');
    downloadFolder(folderName);
}