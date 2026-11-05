# Impossible Project

Projeto de e-commerce com Java 21 e Spring Boot no back-end e React, TypeScript e Vite no front-end.

## Estrutura

- `backend/` contém o projeto Maven. Em `src/main/java/com/marketplace/`, os pacotes `controllers`, `services`, `repositories`, `entities` e `dto` separam as rotas HTTP, as regras de negócio, o acesso ao banco, as entidades JPA e os dados da API.
- `frontend/` contém a aplicação React. Em `src/`, `pages` reúne as telas, `components` concentra os componentes reutilizáveis e `api` concentra as chamadas ao back-end.

## Desenvolvimento local

Use JDK 21, Docker em execução e Node.js 24 com npm. Configure `JAVA_HOME` para o JDK 21. O Maven Wrapper disponibiliza a versão do Maven usada pelo projeto.

Para testar o back-end, execute na raiz do repositório. O Testcontainers inicia um PostgreSQL em Docker para o teste de contexto.

```sh
cd backend
./mvnw test
```

Para iniciar a página do front-end, execute em outro terminal a partir da raiz do repositório e abra `http://localhost:5173`.

```sh
cd frontend
npm ci
npm run dev
```

Para gerar o build do front-end, execute em `frontend/`.

```sh
npm run build
```
