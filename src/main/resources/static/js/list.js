document.addEventListener('DOMContentLoaded', function () {
    // Dropdown do usuário
    const userIcon = document.querySelector('.user-icon');
    const dropdownMenu = document.getElementById('dropdownMenu');

    function toggleDropdown() {
        dropdownMenu.style.display = dropdownMenu.style.display === 'block' ? 'none' : 'block';
    }

    userIcon?.addEventListener('click', function (e) {
        e.stopPropagation();
        toggleDropdown();
    });

    window.addEventListener('click', function (e) {
        if (!userIcon.contains(e.target)) {
            dropdownMenu.style.display = 'none';
        }
    });

    // Upload por arrastar
    ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(event => {
        window.addEventListener(event, e => {
            e.preventDefault();
            e.stopPropagation();
        });
    });

    window.addEventListener('drop', e => {
        const files = e.dataTransfer.files;
        if (files.length > 0) {
            uploadFiles(files);
        }
    });

    function uploadFiles(files) {
        const folderPath = document.getElementById("folderPath").value;
        const formData = new FormData();
        formData.append("folderPath", folderPath);

        for (let i = 0; i < files.length; i++) {
            formData.append("files", files[i]);
        }

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

    // Ações dos botões de arquivo
    document.querySelectorAll('.btn-view').forEach(link => {
        link.addEventListener('click', e => {
            e.preventDefault();
            const file = e.target.closest('a').getAttribute('data-file');
            window.open(`/view/${encodeURIComponent(file)}`, '_blank');
        });
    });

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
            .catch(() => alert("Erro ao baixar."));
        });
    });

    // Compartilhar arquivo
    window.shareFileFromButton = function (button) {
        const fileName = button.getAttribute("data-file");
        const fileLink = `${window.location.origin}/view/${encodeURIComponent(fileName)}`;

        navigator.clipboard.writeText(fileLink)
            .then(() => alert('Link de compartilhamento copiado!'))
            .catch(err => alert('Falha ao copiar: ' + err));
    };

    // Renomear arquivo
    const renameModalElement = document.getElementById('modalRenameFile');
    const renameModal = new bootstrap.Modal(renameModalElement);

    document.querySelectorAll('.btn-rename').forEach(link => {
        link.addEventListener('click', e => {
            e.preventDefault();
            const fileName = e.target.closest('a').getAttribute('data-file');
            const fileNameWithoutExt = fileName.substring(0, fileName.lastIndexOf('.'));
            document.getElementById('oldFileName').value = fileName;
            document.getElementById('newFileName').value = fileNameWithoutExt;
            renameModal.show();
        });
    });

    document.getElementById('renameForm').addEventListener('submit', function (event) {
        event.preventDefault();

        const oldFileName = this.oldFileName.value;
        const newFileName = this.newFileName.value;
        const folderPath = document.getElementById("folderPath").value;

        const endpoint = (!folderPath || folderPath.trim() === "")
            ? `/storage/renameRootFile`
            : `/storage/renameFile`;

        fetch(`${endpoint}?oldFileName=${encodeURIComponent(oldFileName)}&newFileName=${encodeURIComponent(newFileName)}`, {
            method: 'PATCH'
        })
        .then(async res => {
            const isJson = res.headers.get("content-type")?.includes("application/json");
            const data = isJson ? await res.json() : null;

            if (!res.ok) {
                const message = data?.message || res.statusText;
                throw new Error(message);
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
        .catch(err => alert("Erro: " + err.message));
    });

    // Mover pasta
       /*  let folderToMove = "";
        let currentFolderPath = "";

        window.moveFolder = function (link) {
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

            const modal = new bootstrap.Modal(document.getElementById("moveModal"));
            modal.show();
        };

        window.confirmMove = function () {
            const targetFolder = document.getElementById("destinationSelect").value;
            const fullPath = `${currentFolderPath}/${folderToMove}`;

            fetch(`/storage/moveFolder?fullPath=${encodeURIComponent(fullPath)}&newParentFolder=${encodeURIComponent(targetFolder)}`, {
                method: "PATCH"
            })
            .then(res => {
                if (!res.ok) throw new Error("Erro ao mover pasta");
                return res.text();
            })
            .then(() => {
                alert("Pasta movida com sucesso!");
                location.reload();
            })
            .catch(err => {
                console.error("Erro:", err);
                alert("Erro inesperado.");
            });
        };

        // Dropdown ações de pasta
        window.toggleActions = function (icon) {
            const dropdown = icon.nextElementSibling;
            document.querySelectorAll(".dropdown-content").forEach(menu => {
                if (menu !== dropdown) menu.style.display = "none";
            });
            dropdown.style.display = dropdown.style.display === "block" ? "none" : "block";
        };

        document.addEventListener("click", function (event) {
            if (!event.target.closest(".folder")) {
                document.querySelectorAll(".dropdown-content").forEach(menu => {
                    menu.style.display = "none";
                });
            }
        }); */
      let folderToMove = ""; // Pasta que será movida
      let currentFolderPath = ""; // Caminho atual da pasta (se houver)

      // Função para abrir o modal e preencher o select com pastas e subpastas disponíveis
      window.moveFolder = function(link) {
          folderToMove = link.getAttribute("data-folder");
          currentFolderPath = document.getElementById("folderPath") ? document.getElementById("folderPath").value : "";

          document.getElementById("modalFolderName").innerText = folderToMove;

          const select = document.getElementById("destinationSelect");
          select.innerHTML = ""; // Limpa opções

          // Requisição para pegar todas as pastas
          fetch('/storage/folders')
              .then(res => res.json())
              .then(folders => {
                  const folderMap = {};

                  // Organiza pastas em uma estrutura hierárquica
                  folders.forEach(folder => {
                      const parts = folder.split('/');
                      let current = folderMap;
                      parts.forEach((part, idx) => {
                          if (!current[part]) {
                              current[part] = idx === parts.length - 1 ? null : {};
                          }
                          current = current[part];
                      });
                  });

                  // Função recursiva para adicionar pastas e subpastas no select
                  const addFolderToSelect = (parent, parentPath = '', depth = 0) => {
                      Object.keys(parent).forEach(folder => {
                          const path = parentPath ? `${parentPath}/${folder}` : folder;
                          if (folder !== folderToMove && folder.toLowerCase() !== "lixeira") {
                              const option = document.createElement("option");
                              option.value = path;

                              // Define a tabulação para as subpastas
                              option.style.paddingLeft = `${depth * 20}px`; // 20px para cada nível de profundidade

                              // Nome da pasta (indica se é subpasta com base na indentação)
                              option.textContent = folder;

                              select.appendChild(option);
                          }

                          // Se for uma subpasta, chama a função recursiva
                          if (parent[folder]) {
                              addFolderToSelect(parent[folder], path, depth + 1);
                          }
                      });
                  };

                  // Inicia o processo de adicionar pastas e subpastas ao select
                  addFolderToSelect(folderMap);
              })
              .catch(err => console.error("Erro ao carregar pastas:", err));

          // Mostra o modal
          const modal = new bootstrap.Modal(document.getElementById("moveModal"));
          modal.show();
      };

      // Função para confirmar movimentação da pasta
      window.confirmMove = function() {
          const targetFolder = document.getElementById("destinationSelect").value;
          if (!targetFolder) {
              alert("Selecione uma pasta de destino.");
              return;
          }

          const fullPath = currentFolderPath ? `${currentFolderPath}/${folderToMove}` : folderToMove;

          fetch(`/storage/moveFolder?fullPath=${encodeURIComponent(fullPath)}&newParentFolder=${encodeURIComponent(targetFolder)}`, {
              method: "PATCH"
          })
          .then(res => {
              if (!res.ok) throw new Error("Erro ao mover pasta");
              return res.text();
          })
          .then(() => {
              alert("Pasta movida com sucesso!");
              location.reload(); // Recarrega a página para refletir a mudança
          })
          .catch(err => {
              console.error("Erro:", err);
              alert("Erro inesperado ao mover pasta.");
          });
      };




      // Função opcional para abrir/fechar dropdown das ações
      function toggleActions(el) {
          const dropdown = el.nextElementSibling;
          dropdown.style.display = dropdown.style.display === "block" ? "none" : "block";

          document.addEventListener("click", function hideDropdown(event) {
              if (!el.contains(event.target) && !dropdown.contains(event.target)) {
                  dropdown.style.display = "none";
                  document.removeEventListener("click", hideDropdown);
              }
          });
      }


    // Download pasta
    window.handleDownload = function (element) {
        const folderName = element.getAttribute('data-folder-name');
        fetch(`/storage/downloadFolder?folderPath=${encodeURIComponent(folderName)}`, {
            method: 'GET',
            credentials: 'include'
        })
        .then(res => res.blob())
        .then(blob => {
            const url = URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `${folderName}.zip`;
            document.body.appendChild(a);
            a.click();
            a.remove();
        })
        .catch(() => alert("Erro ao baixar pasta."));
    };
});
