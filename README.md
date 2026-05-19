# 📁 FileStorage

Sistema web de gerenciamento de arquivos desenvolvido com **Java 21** e **Spring Boot**, criado para centralizar o armazenamento, organização, busca e disponibilização de documentos.

> Projeto real desenvolvido para uso interno, com autenticação, controle de acesso, área pública e dashboard de relatórios.

---

## 🖼️ Screenshots

> 🔜 Screenshots em breve

---

## ✅ Funcionalidades

- 🔐 Autenticação com JWT e controle de acesso por perfil
- 📂 Upload, download, movimentação e renomeação de arquivos e pastas
- 🗑️ Lixeira com restauração de itens excluídos
- 🔍 Busca simples e avançada com filtros (cidade, pasta, data, palavra-chave)
- 🌐 Área pública para consulta e download de arquivos finalizados
- 📊 Dashboard com relatórios de acessos e buscas
- 👥 Administração de usuários (ativação, perfil, senha)
- 🔄 Sincronização entre arquivos físicos e base de dados

---

## 🛠️ Stack

| Camada | Tecnologia |
|--------|-----------|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.4.3 |
| Segurança | Spring Security + JWT |
| Persistência | Spring Data JPA + MySQL |
| Frontend | Thymeleaf |
| Build | Maven |
| PDF | iTextPDF |
| Testes locais | H2 |

---

## ⚙️ Como rodar localmente

### Pré-requisitos

- Java 21
- Maven
- MySQL

### 1. Clone o repositório

```bash
git clone https://github.com/guireinhardt/FileStorageSpringBoot.git
cd FileStorageSpringBoot
```

### 2. Configure o `application.properties`

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/saa?useSSL=false&serverTimezone=UTC
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha

file.upload-dir=C:\uploads

api.security.token.secret=sua_secret_jwt
```

### 3. Execute

```bash
mvn spring-boot:run
```

Acesse em: `http://localhost:8080`

---

## 🐳 Como rodar com Docker

### Pré-requisitos

- Docker
- MySQL rodando localmente ou em outro container

### 1. Configure as variáveis de ambiente

Crie um arquivo `.env` na raiz do projeto:

```env
JWT_SECRET=sua_secret_jwt
DB_USERNAME=seu_usuario
DB_PASSWORD=sua_senha
```

### 2. Suba o container

```bash
docker compose up -d
```

Acesse em: `http://localhost:8080`

### 3. Para derrubar

```bash
docker compose down
```

> O diretório de uploads é persistido via volume Docker (`uploads_data`), ou seja, os arquivos não são perdidos ao reiniciar o container.

---

## 🔐 Como funciona a autenticação

1. Usuário envia login e senha via `POST /auth/login`
2. Sistema valida com Spring Security e retorna um token JWT
3. Token é usado nas requisições às rotas protegidas
4. Perfil do usuário define o nível de acesso

---

## 📌 Pontos de evolução futura

- [ ] Documentação automática com Swagger/OpenAPI
- [ ] Separação clara entre controllers web e controllers de API
- [ ] Ampliação da cobertura de testes
- [ ] Padronização de rotas
- [ ] Deploy em cloud (AWS/Railway)

---

## 👨‍💻 Autor

Desenvolvido por **Guilherme Reinhardt**  
[![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=flat&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/guilherme-reinhardt/)