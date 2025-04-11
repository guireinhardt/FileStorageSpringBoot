// app.js

function checkAuthentication() {
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = 'login.html'; // Redireciona para login se não autenticado
    }
}

// Chame esta função em páginas que requerem autenticação
checkAuthentication();