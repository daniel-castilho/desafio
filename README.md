# API de Gestão de Projetos e Tarefas

![Java](https://img.shields.io/badge/Java-21-blue?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.6-brightgreen?style=for-the-badge&logo=spring)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=for-the-badge&logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Ready-blue?style=for-the-badge&logo=docker)
![Maven](https://img.shields.io/badge/Maven-4-orange?style=for-the-badge&logo=apache-maven)

API RESTful completa para gerenciar projetos e tarefas, construída com as melhores práticas do Spring Boot, incluindo tratamento de erros centralizado, DTOs, mappers e documentação OpenAPI.

---

<details>
<summary><strong>🇧🇷 Instruções em Português</strong></summary>

### 🎯 Sobre o Projeto

Esta API permite o gerenciamento completo do ciclo de vida de projetos e suas tarefas associadas. Ela foi desenvolvida seguindo os princípios RESTful, incluindo HATEOAS (Nível 3 de Maturidade de Richardson) para uma navegação de API dinâmica e auto-descobrivel.

### ✨ Recursos

*   **CRUD completo** para Projetos e Tarefas.
*   Busca de tarefas com **filtros dinâmicos**.
*   **Tratamento de erros centralizado** com `@RestControllerAdvice`.
*   **Documentação interativa** com OpenAPI (Swagger UI).
*   **API RESTful Nível 3** com links HATEOAS.
*   Mapeamento de objetos otimizado com **MapStruct**.
*   Ambiente de desenvolvimento e produção containerizado com **Docker**.

### 🛠️ Pré-requisitos

Para executar este projeto, você precisará ter instalado em sua máquina:
*   **Java 21** ou superior.
*   **Maven 3.9** ou superior.
*   **Docker** e **Docker Compose**.

### 🚀 Como Executar (Recomendado com Docker)

Este é o método mais simples e recomendado, pois gerencia o banco de dados e a aplicação de forma automática.

1.  **Clone o Repositório**
    ```sh
    git clone https://github.com/daniel-castilho/desafio
    cd desafio
    ```

2.  **Crie o Arquivo de Ambiente**
    Na raiz do projeto, crie um arquivo chamado `.env` e cole o seguinte conteúdo. Este arquivo guarda as credenciais do banco de dados de forma segura.
    ```env
    # Credenciais do Banco de Dados PostgreSQL
    POSTGRES_DB=desafio
    POSTGRES_USER=desafio
    POSTGRES_PASSWORD='sua_senha'
    ```

3.  **Construa e Inicie os Contêineres**
    Este comando irá compilar o projeto, construir a imagem Docker e iniciar a aplicação e o banco de dados em segundo plano.
    ```sh
    docker compose up --build -d
    ```

4.  **Pronto!**
    Sua API estará rodando em `http://localhost:8080`.

5.  **Para Parar o Ambiente**
    ```sh
    docker compose stop
    ```

### 📚 Acessando a Documentação da API

Com a aplicação rodando, você pode acessar a documentação interativa (Swagger UI) através da seguinte URL:

*   **Swagger UI:** http://localhost:8080/swagger-ui.html

O JSON da especificação OpenAPI está disponível em:
*   **API Docs:** http://localhost:8080/v3/api-docs

</details>

---

<details>
<summary><strong>🇬🇧 Instructions in English</strong></summary>

### 🎯 About The Project

This is a complete RESTful API to manage projects and their associated tasks, built with Spring Boot best practices, including centralized error handling, DTOs, mappers, and OpenAPI documentation.

### ✨ Features

*   **Full CRUD** for Projects and Tasks.
*   Task search with **dynamic filters**.
*   **Centralized error handling** with `@RestControllerAdvice`.
*   **Interactive documentation** with OpenAPI (Swagger UI).
*   **Level 3 RESTful API** with HATEOAS links.
*   Optimized object mapping with **MapStruct**.
*   Containerized development and production environment with **Docker**.

### 🛠️ Prerequisites

To run this project, you will need to have the following installed on your machine:
*   **Java 21** or higher.
*   **Maven 3.9** or higher.
*   **Docker** and **Docker Compose**.

### 🚀 How to Run (Docker Recommended)

This is the simplest and recommended method, as it automatically manages the database and the application.

1.  **Clone the Repository**
    ```sh
    git clone https://github.com/daniel-castilho/desafio
    cd desafio
    ```

2.  **Create the Environment File**
    In the project root, create a file named `.env` and paste the following content. This file securely stores the database credentials.
    ```env
    # PostgreSQL Database Credentials
    POSTGRES_DB=desafio
    POSTGRES_USER=desafio
    POSTGRES_PASSWORD='your_password'
    ```

3.  **Build and Start the Containers**
    This command will compile the project, build the Docker image, and start the application and the database in the background.
    ```sh
    docker compose up --build -d
    ```

4.  **Done!**
    Your API will be running at `http://localhost:8080`.

5.  **To Stop the Environment**
    ```sh
    docker compose stop
    ```

### 📚 Accessing the API Documentation

With the application running, you can access the interactive documentation (Swagger UI) at the following URL:

*   **Swagger UI:** http://localhost:8080/swagger-ui.html

The OpenAPI specification JSON is available at:
*   **API Docs:** http://localhost:8080/v3/api-docs

</details>
