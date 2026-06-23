# Central Web

**Central Web** é um fórum colaborativo de compartilhamento de conhecimento e Q&A (semelhante ao StackOverflow e Medium). A plataforma permite que desenvolvedores criem perfis, façam perguntas, publiquem artigos técnicos, respondam a dúvidas de outros membros, façam comentários, curtam publicações e organizem seus conteúdos favoritos em listas personalizadas.


## Suporte a Markdown
A aplicação conta com suporte nativo a **Markdown** no frontend (utilizando a biblioteca `ngx-markdown`). Isso permite que os desenvolvedores formatem seus artigos, perguntas e respostas com blocos de código (com destaque de sintaxe), títulos, listas, tabelas, formatações de texto (negrito, itálico) e links, facilitando a escrita e a leitura de conteúdos técnicos.


## Sistema de Reputação e Nível de Experiência

A plataforma possui um **sistema de gamificação e reputação**. Usuários ganham pontos ao contribuir com a comunidade (escrevendo artigos ou tendo respostas aceitas), evoluindo de nível e conquistando credibilidade técnica. Além de que, se o usuário for um profissional e assim comprovar através do envio do currículo, ele será classificado como profissional em seu perfil e ganhará pontos de acordo com seu nível de experiência a cada empresa por qual passou, trazendo mais credibilidade sobre o conteúdo e resposta apresentados.

### 1. Sistema de Pontuação

Os pontos de reputação são atribuídos (ou removidos) automaticamente através de eventos no sistema:

| Ação Realizada | Alteração na Reputação |
| :--- | :--- |
| **Criar Artigo** | `+20 pontos` (ao excluir: `-20`) |
| **Criar Pergunta** | `+10 pontos` (ao excluir: `-10`) |
| **Ter Resposta Aceita** (marcada como resolvida) | `+50 pontos` (ao desmarcar: `-50`) |
| **Cadastrar Qualificação Júnior** (`JUNIOR`) | `+100 pontos` (ao excluir: `-100`) |
| **Cadastrar Qualificação Pleno** (`MID`) | `+200 pontos` (ao excluir: `-200`) |
| **Cadastrar Qualificação Sênior** (`SENIOR`) | `+300 pontos` (ao excluir: `-300`) |

### 2. Níveis de Experiência (Rankings)

O nível de experiência exibido no perfil do usuário é atualizado em tempo real conforme a pontuação de reputação atinge os seguintes marcos:

*   **Novato**: `< 150 pontos`
*   **Praticante**: `150` a `499 pontos`
*   **Veterano**: `500` a `999 pontos`
*   **Visionário**: `1000` a `2499 pontos`
*   **Domador de Legado**: `2500` a `4999 pontos`
*   **Compilador Humano**: `>= 5000 pontos`


## Permissões e Autorizações (Roles)

O sistema possui dois perfis principais de acesso (`UserRole`):

1.  **`PERSON` (Membro / Desenvolvedor)**:
    *   Pode ler todos os artigos, tags e perguntas públicas.
    *   Pode criar seu próprio perfil, perguntas, artigos, respostas, qualificações e coleções.
    *   Pode curtir artigos/perguntas/respostas e gerenciar seus próprios comentários.
    *   **Regra de Propriedade (Ownership)**: Só tem autorização para atualizar, editar ou excluir recursos criados por si mesmo.
2.  **`ADMIN` (Administrador)**:
    *   Possui todas as permissões de leitura e criação.
    *   **Superpoderes Administrativos**: Pode excluir qualquer recurso da plataforma (perguntas, artigos, qualificações, comentários, perfis), independente de quem seja o proprietário, para fins de moderação da comunidade.


## Requisitos e Variáveis de Ambiente

### Pré-requisitos
*   **JDK 21** ou superior instalado.
*   **Node.js 18** ou superior instalado (junto com o npm).
*   **PostgreSQL** (Banco de dados relacional).
*   **Redis** (Servidor de cache para otimização de consultas e controle de sessões. Recomenda-se utilizar via docker).

### 1. Configurações do Backend e Variáveis de Ambiente

O backend utiliza variáveis de ambiente para parametrizar conexões de banco de dados e chaves de segurança. Há duas formas recomendadas para configurar essas variáveis localmente:

#### Opção A: Definir no arquivo `application.yml`
Você pode declarar as variáveis e seus respectivos valores padrão diretamente na estrutura do arquivo [`application.yml`](backend/src/main/resources/application.yml) usando a sintaxe `${NOME_DA_VARIAVEL:valor_padrao}`. 

Veja um trecho do arquivo configurado:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/${POSTGRES_DB:centralweb}
    username: ${POSTGRES_USER:postgres}
    password: ${POSTGRES_PASSWORD:sua_senha_aqui}
    driver-class-name: org.postgresql.Driver
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}

TOKEN_SECRET: ${TOKEN_SECRET:minha-chave-secreta-padrao-desenvolvimento-12345678}
```
*Se as variáveis `POSTGRES_DB`, `POSTGRES_USER`, etc., não forem declaradas no sistema operacional, o Spring Boot utilizará automaticamente os valores após o caractere `:`.*

#### Opção B: Configurar diretamente no IntelliJ IDEA
Se você estiver rodando a aplicação pela IDE IntelliJ, pode definir as variáveis de ambiente sem precisar modificar o arquivo de configuração:
1. No menu superior do IntelliJ, clique na lista suspensa de execução de tarefas e selecione **Edit Configurations...** (Editar Configurações).
2. Na barra lateral esquerda, clique sob a classe principal da sua aplicação Spring Boot (`BackendApplication`).
3. Localize o campo **Environment variables** (Variáveis de Ambiente). Se o campo não for visível, clique em **Modify options** (Modificar opções) e selecione *Environment variables*.
4. Clique no ícone de pasta/documento ao lado do campo e insira as variáveis necessárias linha por linha (ou separadas por ponto e vírgula):
   * `POSTGRES_DB=centralweb`
   * `POSTGRES_USER=seu_usuario_postgres`
   * `POSTGRES_PASSWORD=sua_senha_postgres`
   * `TOKEN_SECRET=uma_chave_secreta_customizada_e_segura`
5. Clique em **Apply** (Aplicar) e em **OK**.
6. Rode a aplicação pelo botão de Play do IntelliJ.


### 2. Configurações do Frontend (`environment.ts`)

No Angular, por questões de segurança e boas práticas de controle de versão, a pasta e o arquivo de configurações do ambiente local não devem ser versionados. Portanto, **é necessário criar essa estrutura manualmente** antes de executar a aplicação pela primeira vez.

1. Navegue até a pasta `frontend/src/app/`.
2. Crie uma pasta chamada `environments`.
3. Dentro dessa pasta, crie um arquivo TypeScript chamado `environment.ts` (`frontend/src/app/environments/environment.ts`).
4. Adicione as seguintes propriedades de configuração com a rota base da API e o endereço de carregamento dos avatares:
   ```typescript
   export const environment = {
       production: false,
       apiUrl: 'http://localhost:8080',  // Rota base do servidor backend REST
       mediaUrl: 'http://localhost:8080/' // Endereço de carregamento de avatares/mídias
   };
   ```


## Rotas do Aplicativo

### 1. Frontend (Interface Angular)

*   `/` : Página inicial (feed de artigos e fluxo principal).
*   `/login` : Tela de autenticação de usuários.
*   `/create-profile` : Tela de cadastro de nova conta de desenvolvedor.
*   `/edit-profile/:id` : Edição de dados do perfil público.
*   `/profiles/:id` : Visualização pública do perfil de um desenvolvedor, reputação e qualificações.
*   `/create-article` e `/edit-article/:id` : Formulário para escrever/editar artigos.
*   `/articles` : Lista principal de artigos técnicos.
*   `/articles/:id` : Visualização completa e leitura de um artigo.
*   `/create-question` e `/edit-question/:id` : Formulário para fazer/editar perguntas técnicas.
*   `/questions` : Lista principal de perguntas e dúvidas.
*   `/questions/:id` : Detalhes de uma pergunta, contendo a listagem de respostas e campo de comentários.
*   `/create-qualification` : Adicionar experiências e qualificações profissionais ao perfil.
*   `/collections` : Gerenciamento de pastas de coleções salvas pelo usuário logado.
*   `/collections/:id` : Detalhes e listagem de conteúdos arquivados dentro de uma coleção.

### 2. Backend REST API (Endpoints)

#### Autenticação (`/auth`)
*   `POST /auth/login` : Autentica usuário e retorna JWT + Refresh Token.
*   `POST /auth/refresh` : Gera novos tokens de acesso.
*   `POST /auth/logout` : Invalida a sessão ativa.

#### Perfis (`/profiles` & `/photos`)
*   `POST /profiles` : Cria um novo usuário e perfil de desenvolvedor.
*   `GET /profiles/me` : Recupera os dados do perfil logado.
*   `GET /profiles/{profileId}` : Retorna dados públicos de um perfil.
*   `PUT /profiles/{profileId}` : Atualiza dados do perfil.
*   `DELETE /profiles/{profileId}` : Exclui um perfil e sua conta.
*   `POST /photos/{profileId}/avatar` : Upload de foto de avatar (`MultipartFile`).

#### Artigos (`/articles`)
*   `POST /articles` : Publica um novo artigo técnico.
*   `GET /articles` : Retorna a lista paginada de artigos publicados.
*   `GET /articles/{articleId}` : Retorna os detalhes de um artigo.
*   `GET /articles/search?query=...` : Pesquisa por texto completo nos artigos.
*   `GET /articles/{technologyName}/tag` : Filtra artigos por tag tecnológica.
*   `GET /articles/filter?tags=...` : Filtra por múltiplas tags (separadas por vírgula).
*   `GET /articles/{profileId}/profile` : Retorna os artigos publicados por um perfil específico.
*   `PUT /articles/{articleId}` : Atualiza o título/conteúdo/tags de um artigo.
*   `PATCH /articles/{articleId}/like` : Adiciona ou remove curtida no artigo.
*   `DELETE /articles/{articleId}` : Exclui um artigo.

#### Perguntas (`/questions`)
*   `POST /questions` : Registra uma nova dúvida na comunidade.
*   `GET /questions` : Retorna a lista paginada de dúvidas enviadas.
*   `GET /questions/{questionId}` : Retorna os detalhes de uma pergunta.
*   `GET /questions/search?query=...` : Busca perguntas por palavra-chave.
*   `GET /questions/{technologyName}/tag` : Filtra perguntas por tag.
*   `GET /questions/filter?tags=...` : Filtra perguntas por múltiplas tags.
*   `GET /questions/accepteds-answers` : Lista apenas as perguntas que possuem uma resposta marcada como correta/aceita.
*   `GET /questions/{profileId}/profile` : Lista as perguntas feitas por um perfil específico.
*   `PUT /questions/{questionId}` : Edita uma pergunta existente.
*   `PATCH /questions/{questionId}/like` : Adiciona ou remove curtida em uma pergunta.
*   `DELETE /questions/{questionId}` : Exclui uma pergunta.

#### Respostas (`/answers`)
*   `POST /answers/{questionId}` : Responde a uma pergunta específica.
*   `GET /answers/{questionId}` : Lista as respostas de uma pergunta (paginado).
*   `PATCH /answers/{answerId}` : Aceita uma resposta como correta (somente dono da pergunta).
*   `PATCH /answers/{answerId}/like` : Adiciona/remove curtida na resposta.
*   `DELETE /answers/{answerId}` : Exclui uma resposta.

#### Comentários (`/comments`)
*   `POST /comments/answer/{answerId}` : Comenta em uma resposta.
*   `GET /comments/answer/{answerId}` : Lista os comentários de uma resposta.
*   `PUT /comments/{commentId}` : Edita um comentário.
*   `DELETE /comments/{commentId}` : Exclui um comentário.

#### Coleções (`/collections`)
*   `POST /collections` : Cria uma nova pasta de coleção.
*   `GET /collections/my-collections` : Lista as coleções do usuário logado.
*   `GET /collections/{collectionId}` : Detalhes e conteúdos salvos na coleção.
*   `POST /collections/{collectionId}/articles/{articleId}` : Adiciona um artigo à coleção.
*   `POST /collections/{collectionId}/questions/{questionId}` : Adiciona uma pergunta à coleção.
*   `DELETE /collections/articles/{articleId}` : Remove um artigo de todas as coleções do usuário.
*   `DELETE /collections/questions/{questionId}` : Remove uma pergunta de todas as coleções do usuário.
*   `DELETE /collections/{collectionId}` : Exclui uma pasta de coleção.

#### Qualificações (`/qualifications`)
*   `POST /qualifications` : Cadastra experiência de trabalho no currículo.
*   `GET /qualifications/verified` / `/not-verified` : Lista qualificações verificadas ou pendentes.
*   `GET /qualifications/{profileId}/verified` : Qualificações verificadas de um perfil.
*   `DELETE /qualifications/{qualificationId}` : Remove uma qualificação.


## Documentação da API (Swagger / OpenAPI)

A aplicação possui integração com o **Swagger UI** (utilizando `springdoc-openapi`) para documentar e permitir a execução interativa de todas as rotas REST do backend. Com a aplicação backend rodando, você pode acessar:

*   **Interface Gráfica (Swagger UI)**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

> OBS:
> A interface do Swagger já está integrada com o esquema de segurança **JWT Bearer Token** (`bearerAuth`). Para testar rotas protegidas:
> 1. Execute a chamada no endpoint `/auth/login` para gerar um token válido.
> 2. Clique no botão verde **Authorize** no topo direito da página do Swagger.
> 3. Cole o token gerado e clique em **Authorize**.


## Como Executar a Aplicação Localmente

### 1. Executar o Backend (Spring Boot)
1.  Certifique-se de que o **PostgreSQL** e o **Redis** estejam em execução.
2.  Crie um banco de dados no PostgreSQL (ex: `central_web`).
3.  Configure as variáveis de ambiente necessárias ou preencha o arquivo [`application.yml`](backend/src/main/resources/application.yml) com as credenciais.
4.  No diretório `backend`, execute o comando:
    ```bash
    ./mvnw spring-boot:run
    ```
    *(O Flyway criará e migrará as tabelas do banco de dados automaticamente).*
5.  **Nota**: Um usuário administrador padrão é criado automaticamente no primeiro boot:
    *   **E-mail**: `admin@centralweb.io`
    *   **Senha**: `admin123`

### 2. Executar o Frontend (Angular)
1.  Acesse o diretório `frontend`.
2.  Instale as dependências:
    ```bash
    npm install
    ```
3.  Inicie o servidor de desenvolvimento local:
    ```bash
    npm start
    ```
4.  Abra o navegador em `http://localhost:4200`.
