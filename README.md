# 📁 FileStorage

Sistema web de gerenciamento de arquivos institucionais desenvolvido com **Java 21** e **Spring Boot 3.4**, criado para centralizar o armazenamento, organização, busca e disponibilização de documentos em ambientes corporativos e governamentais.

> Projeto desenvolvido como parte do trabalho na Secretaria de Agricultura e Abastecimento do Estado de São Paulo.

---

## ✅ Funcionalidades

- 🔐 Autenticação com JWT e controle de acesso por perfil (ADMIN / USER)
- 📂 Upload, download, movimentação e renomeação de arquivos e pastas
- 🗜️ Download em lote com geração de arquivo ZIP
- 🗑️ Lixeira com restauração de itens excluídos
- 🔍 Busca simples e avançada com filtros por palavra-chave, cidade, pasta, data e tipo de arquivo
- 🌐 Área pública para consulta e download de arquivos finalizados (sem autenticação)
- 📊 Dashboard com relatórios de acessos por dia e buscas por palavra-chave
- 👥 Administração de usuários (ativação, perfil, redefinição de senha)
- 📋 Log de auditoria por arquivo (rastreamento de operações)
- 🔄 Registro de metadados de arquivos e pastas em banco de dados (em migração)

---

## 🛠️ Stack

| Camada        | Tecnologia                     |
|---------------|-------------------------------|
| Linguagem     | Java 21                        |
| Framework     | Spring Boot 3.4.3              |
| Segurança     | Spring Security + JWT (Auth0)  |
| Persistência  | Spring Data JPA + MySQL 8      |
| Frontend      | Thymeleaf + Bootstrap + JS     |
| Build         | Maven                          |
| PDF           | iTextPDF 5.5                   |
| Containerização | Docker + Docker Compose      |
| Testes        | JUnit + H2 (banco em memória)  |

---

## 🏗️ Arquitetura

O projeto segue arquitetura em camadas:

```
controller/     → Rotas e requisições HTTP (MVC + REST)
service/        → Regras de negócio
repository/     → Acesso a dados via JPA
entity/         → Entidades JPA mapeadas no banco
dto/            → Objetos de transferência de dados
vo/             → Value Objects de resposta
infra/          → Segurança (JWT, Filters) e auditoria
config/         → Configurações da aplicação
utils/          → Utilitários (validação, streams)
```

---

## ⚙️ Como rodar localmente

### Pré-requisitos

- Java 21
- Maven
- MySQL 8 rodando localmente

### 1. Clone o repositório

```bash
git clone https://github.com/guireinhardt/FileStorageSpringBoot.git
cd FileStorageSpringBoot
```

### 2. Configure o `application.properties`

As variáveis sensíveis são lidas via ambiente. Crie um arquivo `.env` na raiz ou exporte as variáveis:

```env
JWT_SECRET=sua_secret_jwt
DB_URL=jdbc:mysql://localhost:3306/saa?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
DB_USERNAME=seu_usuario
DB_PASSWORD=sua_senha
UPLOAD_DIR=C:/uploads
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

### 1. Crie o arquivo `.env` na raiz do projeto

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

> Os arquivos enviados são persistidos via volume Docker (`uploads_data`) e não são perdidos ao reiniciar o container.

---

## 🔐 Como funciona a autenticação

1. Usuário envia login e senha via `POST /auth/login`
2. Spring Security valida as credenciais
3. É retornado um token JWT assinado
4. O token é enviado nas requisições às rotas protegidas via header ou cookie
5. O perfil do usuário (ADMIN ou USER) define o nível de acesso às funcionalidades

---

## 📋 Log de auditoria

O sistema registra operações em arquivos de log diários em `logs/audit-YYYY-MM-DD.txt`, rastreando ações como upload, download, exclusão e restauração de arquivos por usuário.

---

## ⚠️ Estado atual da persistência de arquivos

### Como funciona hoje

A navegação e listagem de arquivos é feita percorrendo diretamente o **sistema de arquivos em disco** (`File.listFiles()`). Os metadados de arquivos e pastas (`FileEntity`, `FolderEntity`) já estão modelados e sendo persistidos no banco a cada upload, mas a leitura ainda depende do disco em boa parte dos fluxos.

### O que está sendo migrado

A arquitetura está sendo evoluída para um modelo **totalmente orientado a metadados persistidos em banco de dados**, onde toda navegação, busca e listagem passará a consultar as tabelas `files` e `folders` em vez do disco. As entidades já estão definidas com:

- `FileEntity`: UUID, nome original, content-type, tamanho, storage key, visibilidade, data de criação e criador
- `FolderEntity`: UUID, nome, visibilidade pública, suporte a hierarquia (parent_id), data de criação

Essa migração vai permitir:
- Listagem e busca sem I/O de disco
- Controle de visibilidade por arquivo (PUBLIC / RESTRICTED)
- Suporte futuro a armazenamento em nuvem (AWS S3, Google Cloud Storage)
- Histórico e rastreabilidade completos das operações

---

## 📌 Próximos passos

- [ ] Concluir migração de leitura/listagem para metadados em banco
- [ ] Documentação de endpoints com Swagger / OpenAPI
- [ ] Ampliar cobertura de testes unitários e de integração
- [ ] Deploy em cloud (AWS / Railway)
- [ ] Separação clara entre controllers MVC e controllers REST API

---

## 👨‍💻 Autor

Desenvolvido por **Guilherme Reinhardt**

[![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=flat&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/guilherme-reinhardt/)
[![GitHub](https://img.shields.io/badge/GitHub-181717?style=flat&logo=github&logoColor=white)](https://github.com/guireinhardt)
