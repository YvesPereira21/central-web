# Central Web

**Central Web** é um fórum colaborativo de compartilhamento de conhecimento e Q&A (semelhante ao StackOverflow e Medium). A plataforma permite que desenvolvedores criem perfis, publiquem artigos técnicos e façam perguntas (com suporte a **modo preview** em tempo real), pesquisem conteúdos via **Full Text Search**, respondam a dúvidas de outros membros, façam comentários, curtam publicações e organizem seus conteúdos favoritos em listas personalizadas.


## Tecnologias Utilizadas

### Backend
* **Java 21**: Linguagem de programação principal utilizada com recursos modernos da plataforma Java.
* **Spring Boot 3**: Framework base para construção da API RESTful, injeção de dependências e segurança com **Spring Security**.
* **PostgreSQL & Full Text Search**: Banco de dados relacional para persistência de dados e motor de busca textual otimizada com `tsvector`, índices `GIN`, dicionário em português (`websearch_to_tsquery`) e ranqueamento por relevância com `ts_rank`.
* **Flyway**: Ferramenta de migração e versionamento automatizado do schema do banco de dados PostgreSQL.
* **Redis**: Banco de dados em memória para suporte a **Spring Cache** (`@Cacheable`, `@CacheEvict`), otimizando a performance de consultas.
* **SLF4J (via `@Slf4j` do Lombok)**: Logging estruturado aplicada em services, controllers e filtros de segurança para auditoria, rastreamento de fluxos e diagnósticos de erros.

### Frontend
* **Angular 19**: Framework SPA para construção de uma interface visual moderna e reativa.
* **Biblioteca Markdown (`ngx-markdown` com `marked` e `prismjs`)**: Processamento e renderização de conteúdos em Markdown com destaque de sintaxe (*syntax highlighting*) para blocos de código técnicos e suporte a **Modo Preview** ao vivo.
* **TailwindCSS & DaisyUI**: Estilização do sistema.


## Arquitetura de Segurança, Erros e Boas Práticas

### 1. Tratamento Global de Erros (`GlobalHandlerException`)
A aplicação utiliza um manipulador centralizado de exceções implementado na classe **`GlobalHandlerException`** anotada com `@RestControllerAdvice`. 
* **Padronização de Respostas**: Transforma exceções de runtime e validações em um DTO padronizado (`ApiError`), contendo timestamp, código HTTP, status e a lista detalhada de erros.
* **Exceções de Domínio Tratadas**: Intercepta exceções personalizadas como `ProfileIsNotTheOwnerException` (401), `ObjectNotFoundException` (404), `ObjectAlreadyExistsException` (409), `QuestionHaveAnswerAlreadyAcceptedException` (409), `TokenRefreshException` (403), além de falhas de validação DTO (`MethodArgumentNotValidException` e `ConstraintViolationException`).

### 2. Autenticação com Cookies HTTP-Only (`CookieUtil`)
A aplicação utiliza tokens de acesso JWT e tokens de atualização (*Refresh Tokens*) armazenados em **Cookies seguros**:
* Os cookies `access_token` e `refresh_token` são gerados via `CookieUtil` com as propriedades **`httpOnly(true)`** e **`sameSite("Lax")`**.
* Isso previne a exposição de tokens a scripts client-side JavaScript, mitigando drasticamente vetores de ataque **XSS (Cross-Site Scripting)**.

### 3. Redução de Margens para Ataques IDOR (Insecure Direct Object Reference)
Para conter vulnerabilidades de **IDOR** (onde um atacante tenta alterar IDs na requisição HTTP para manipular recursos de terceiros), a aplicação valida a propriedade (*ownership*) dos recursos na camada de serviço antes de efetuar qualquer alteração ou deleção:

* **Exemplo Prático - Aceitar Resposta em Pergunta**:
  No método `AnswerService.acceptAnswer(answerId, userProfileId)`, o backend recupera a resposta e a pergunta associada. Somente o usuário proprietário da pergunta tem permissão para aceitar a resposta. Caso um usuário tente aceitar uma resposta em uma pergunta que não lhe pertence, o backend lança a exceção `ProfileIsNotTheOwnerException`:
  ```java
  // Apenas o autor/proprietário pode gerenciar a solução
  if (!question.getProfile().getUser().getUserId().equals(userProfileId)) {
      throw new ProfileIsNotTheOwnerException("Você não tem permissão para isso");
  }
  ```
* **Exemplo Prático - Exclusão de Pergunta ou Resposta**:
  Em `QuestionService.deleteQuestionById` e `AnswerService.deleteAnswerById`, o sistema verifica se o `userId` autenticado corresponde exatamente ao criador do recurso ou se possui perfil `ADMIN`. Caso contrário, a requisição é negada e capturada pelo `GlobalHandlerException`, retornando HTTP 401.


## Suporte a Markdown e Modo Preview

A aplicação conta com suporte nativo a **Markdown** no frontend (utilizando a biblioteca `ngx-markdown`). Isso permite que os desenvolvedores formatem seus artigos, perguntas e respostas com blocos de código (com destaque de sintaxe), títulos, listas, tabelas, formatações de texto (negrito, itálico) e links, facilitando a escrita e a leitura de conteúdos técnicos.

### Modo Preview (Pré-visualização em Tempo Real)
Ao criar ou editar artigos e perguntas, os usuários contam com o recurso de **Modo Preview** (tela dividida):
* **Visualização Lado a Lado**: Permite redigir o conteúdo no editor enquanto acompanha simultaneamente como o artigo ou pergunta será exibido para a comunidade.
* **Sincronização em Tempo Real**: Qualquer texto ou bloco de código inserido é renderizado instantaneamente com destaque de sintaxe, facilitando a revisão visual antes da publicação.


## Pesquisa com Full Text Search

A plataforma conta com um sistema de **Full Text Search** nativo do PostgreSQL para a busca rápida e inteligente de artigos e perguntas, substituindo abordagens tradicionais baseadas em `Specification` / `LIKE`:

* **Indexação com Pesos e Dicionário em Português**: As tabelas de artigos e perguntas utilizam colunas geradas (`tsvector`) configuradas com o dicionário `portuguese`. O título recebe maior relevância (peso `A`) em relação ao corpo do texto (peso `B`), com aceleração de consultas via índices `GIN`.
* **Consultas em Linguagem Natural**: A pesquisa utiliza `websearch_to_tsquery('portuguese', :termo)`, permitindo buscas amigáveis com suporte a múltiplos termos, frases exatas entre aspas e exclusão de palavras.
* **Ranqueamento por Relevância**: Os resultados são ordenados pelo grau de relevância textual calculado pela função `ts_rank`, trazendo os conteúdos mais pertinentes no topo da listagem.


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

### 3. Selo de Perfil Profissional

Quando um usuário tem pelo menos uma de suas qualificações (experiências profissionais) aprovada por um administrador, o sistema automaticamente classifica seu perfil como **Perfil Profissional** (`professional = true`).

Com essa classificação:
*   Um selo de destaque **"Profissional"** (badge verde esmeralda) é exibido no topo de sua página de perfil.
*   Um ícone de verificação verde (checkmark com tooltip) é exibido ao lado de seu nome nas seguintes exibições:
    *   Listagens e visualizações detalhadas de perguntas.
    *   Listagens e visualizações detalhadas de artigos.
    *   Listagem de respostas a perguntas.

Se o perfil deixar de ter qualquer qualificação aprovada (por exemplo, caso todas as qualificações aprovadas sejam excluídas), o status de profissional é automaticamente revogado (`professional = false`).


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

## Documentação da API (Swagger / OpenAPI)

A aplicação possui integração com o **Swagger UI** (utilizando `springdoc-openapi`) para documentar e permitir a execução interativa de todas as rotas REST do backend. Com a aplicação backend rodando, você pode acessar:

*   **Interface Gráfica (Swagger UI)**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

> OBS:
> A interface do Swagger já está integrada com o esquema de segurança **JWT Bearer Token** (`bearerAuth`). Para testar rotas protegidas:
> 1. Execute a chamada no endpoint `/auth/login` para gerar um token válido.
> 2. Clique no botão verde **Authorize** no topo direito da página do Swagger.
> 3. Cole o token gerado e clique em **Authorize**.

## Consulta via MCP

Clientes MCP compatíveis com Streamable HTTP podem conectar-se ao endpoint `/mcp` do backend, sem autenticação. Localmente, o endereço é `http://localhost:8080/mcp`; em produção, use o endereço HTTPS do backend seguido de `/mcp`.

O servidor oferece apenas ferramentas de consulta:

| Ferramenta | Argumentos | Resultado |
| --- | --- | --- |
| `search_questions` | `termo`, `pagina` | Perguntas publicadas que correspondem ao termo |
| `search_articles` | `termo`, `pagina` | Artigos publicados que correspondem ao termo |
| `search_answers` | `perguntaId`, `pagina` | Respostas de uma pergunta publicada |
| `search_comments` | `respostaId`, `pagina` | Comentários de uma resposta vinculada a uma pergunta publicada |

As páginas começam em zero e contêm até cinco resultados. Ao adicionar outras ferramentas ao servidor, mantenha a restrição de conteúdo público ou implemente autorização apropriada para elas.


## Como Executar a Aplicação Localmente

### 1. Executar o Backend (Spring Boot)
1.  Certifique-se de que o **PostgreSQL** e o **Redis** estejam em execução.
2.  Crie um banco de dados no PostgreSQL (ex: `central_web`).
3.  Configure as variáveis de ambiente necessárias ou preencha o arquivo [`application.yml`](backend/src/main/resources/application.yml) com as credenciais.
4.  No diretório `backend`, execute o comando:
    ```bash
    ./mvnw spring-boot:run
    ```
    *(O Flyway criará e migrará as tabelas do banco de dados automaticamente com dados de teste).*

### 2. Credenciais de Acesso (Dados de Teste)

Ao iniciar o banco de dados, o Flyway cria automaticamente dados e contas pré-configuradas para testes:

* **Administrador (`ADMIN`)**:
  * **E-mail / Login**: `admin@centralweb.io`
  * **Senha**: `admin123`

* **Usuário Comum / Pessoa (`PERSON`)**:
  * **E-mail / Login**: `lucas.silva@centralweb.io` *(outras opções: `mariana.costa@centralweb.io`, `carlos.eduardo@centralweb.io`, `ana.beatris@centralweb.io`, etc.)*
  * **Senha**: `12345678` *(exceto o admin, todos os outros usuários possuem a senha `12345678`)*

### 3. Executar o Frontend (Angular)
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


## Suíte de testes

O projeto possui uma cobertura de testes automatizados no backend, garantindo a estabilidade da aplicação em manutenções e refatorações.

Os testes do backend cobrem:
*   **Testes de Integração de API (Controllers)**: Validam o comportamento e as restrições de acesso de todos os endpoints REST, segurança JWT e tratamento de erros do servidor.
*   **Testes de Serviços (Services)**: Verificam a lógica de negócios e validações (curtidas, exclusões, relacionamentos).
*   **Testes de Eventos (Listeners)**: Certificam que os eventos assíncronos de transação (ex: atualizar a pontuação de reputação do perfil do desenvolvedor após cadastrar qualificações ou criar artigos/perguntas) funcionam corretamente.

Para rodar os testes do backend, navegue até a pasta `backend/` e execute:
```bash
./mvnw test
```

