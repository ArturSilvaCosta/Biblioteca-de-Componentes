BIBLIOTECA DE COMPONENTES
--Projeto em desenvolvimento--

O projeto utiliza tecnologias:
  - Java 17
  - NetBeans IDE 13
  - MySQL 8.0
  - Maven 3.9
  - GitBash

--Desenvolvido integralmente por Artur Costa--

Objetivo do Software:
- O software visa facilitar a manipulação de arquivos e informações de componentes utilizados em projetos de engenharia, 
categorizando-os em uma hierarquia de classificação de acordo com a natureza e uso do componente.
- Informações de mercado, especificações técnicas e arquivos CAD ficam atrelados ao objeto,
facilitando a compra, troca de produtos e escolha de uso do componente.

Requisitos do sistema:
•	RF001 – Gestão da Árvore de Classificação
O sistema deve permitir o cadastro da hierarquia técnica (Família, Categoria, Classe, Tipo, Subtipo). 
•	RF002 – Cadastro de Componentes
O sistema deve permitir o registro de componentes vinculando-os obrigatoriamente a um Subtipo (que por sua vez já carrega toda a árvore acima).
•	RF003 – Filtro Dinâmico de Busca
O sistema deve permitir a busca por componentes através de filtros combinados (ex: Filtrar por "Familia" + "Categoria Bombas").
•	RF004 – Consulta de registros
Requisito responsável por permitir a consulta de registros cadastrados. Todos os usuários devem ter acesso.
•	RF005 – Edição de registros
Requisito responsável por permitir a edição de registros cadastrados. Apenas os usuários GESTOR e ADMIN devem ter acesso.
•	RF006 – Exclusão de registros
Requisito responsável por permitir excluir um registro cadastrado. Apenas os usuários ADMIN deve ter acesso.
•	RF007 – Gerenciamento de usuários
Requisito responsável por permitir criar, alterar ou excluir o registro de um usuário. Apenas os usuários ADMIN deve ter acesso.
•	RF008 – Autenticação
O sistema deve validar login e senha para permitir o acesso às funcionalidades.

•	RNF001 – Segurança e Autorização 
O sistema deve desabilitar via código (setEnabled(false)) os botões de edição e exclusão caso o nível de acesso do usuário não seja compatível.
•	RNF002 – Persistência de Dados 
O sistema deve utilizar JPA (Java Persistence API) para garantir que as transações com o banco de dados MySQL sejam seguras e eficientes.
•	RNF003 – Portabilidade 
O gerenciamento de dependências deve ser realizado via Maven, garantindo que o sistema possa ser compilado em diferentes ambientes de desenvolvimento.
•	RNF004 – Performance de Consulta 
As consultas ao banco de dados (SELECT) devem ser otimizadas para retornar resultados em menos de 2 segundos, mesmo com grande volume de dados.
