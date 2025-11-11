#!/bin/bash

# --- PASSO 0: Criar usuários de teste (MANAGER e DEVELOPER) ---
echo "Criando usuários de teste..."

# Criar usuário MANAGER
curl -s -o /dev/null -X POST http://localhost:8080/auth/register \
-H "Content-Type: application/json" \
-d '{
    "username": "manager@gmail.com",
    "password": "senha123",
    "role": "MANAGER"
}'
echo "  - Usuário manager@gmail.com criado."

# Criar usuário DEVELOPER
curl -s -o /dev/null -X POST http://localhost:8080/auth/register \
-H "Content-Type: application/json" \
-d '{
    "username": "developer@gmail.com",
    "password": "senha123",
    "role": "DEVELOPER"
}'
echo "  - Usuário developer@gmail.com criado."

# Adicionar uma pequena pausa para garantir que os usuários sejam processados antes de tentar o login
sleep 2
echo ""


# --- PASSO 1: Autenticar e obter o token de acesso ---
echo "Autenticando e obtendo token para o usuário manager@gmail.com..."

# Faz a requisição de login e extrai o token do JSON de resposta usando a ferramenta 'jq'
# Certifique-se de ter o 'jq' instalado (ex: sudo apt-get install jq)
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
-H "Content-Type: application/json" \
-d '{
    "username": "manager@gmail.com",
    "password": "senha123"
}' | jq -r '.token')

# Verifica se o token foi obtido com sucesso
if [ -z "$TOKEN" ] || [ "$TOKEN" == "null" ]; then
    echo "Falha ao obter o token de autenticação. Verifique as credenciais e se a aplicação está rodando."
    exit 1
fi

echo "Token obtido com sucesso!"
echo ""


# --- PASSO 2: Criar 5 projetos diversificados com datas dinâmicas ---
echo "Criando 5 projetos..."

# Projeto 1: Desenvolvimento de Software (Começa em 1 semana, dura 8 meses)
PROJECT_ID_1=$(curl -s -X POST http://localhost:8080/projects \
-H "Authorization: Bearer $TOKEN" \
-H "Content-Type: application/json" \
-d "{
    \"name\": \"Sistema de E-commerce B2C\",
    \"description\": \"Desenvolvimento de uma nova plataforma de e-commerce para o mercado de varejo, com foco em experiência do usuário e performance.\",
    \"startDate\": \"$(date -d "+1 week" +%Y-%m-%d)\",
    \"endDate\": \"$(date -d "+8 months" +%Y-%m-%d)\"
}" | jq -r '.id')
echo "  - Projeto 1 (E-commerce) criado com ID: $PROJECT_ID_1"

# Projeto 2: Infraestrutura e DevOps (Começa amanhã, dura 3 meses)
PROJECT_ID_2=$(curl -s -X POST http://localhost:8080/projects \
-H "Authorization: Bearer $TOKEN" \
-H "Content-Type: application/json" \
-d "{
    \"name\": \"Migração para Cloud AWS\",
    \"description\": \"Migrar toda a infraestrutura on-premise para a nuvem da AWS, utilizando EC2, RDS e S3 para maior escalabilidade e resiliência.\",
    \"startDate\": \"$(date -d "+1 day" +%Y-%m-%d)\",
    \"endDate\": \"$(date -d "+3 months" +%Y-%m-%d)\"
}" | jq -r '.id')
echo "  - Projeto 2 (Migração AWS) criado com ID: $PROJECT_ID_2"

# Projeto 3: Marketing Digital (Projeto já em andamento)
PROJECT_ID_3=$(curl -s -X POST http://localhost:8080/projects \
-H "Authorization: Bearer $TOKEN" \
-H "Content-Type: application/json" \
-d "{
    \"name\": \"Campanha de Marketing Q3\",
    \"description\": \"Planejamento e execução da campanha de marketing digital para o lançamento do novo produto no terceiro trimestre.\",
    \"startDate\": \"$(date -d "-1 month" +%Y-%m-%d)\",
    \"endDate\": \"$(date -d "+2 months" +%Y-%m-%d)\"
}" | jq -r '.id')
echo "  - Projeto 3 (Marketing Q3) criado com ID: $PROJECT_ID_3"

# Projeto 4: Pesquisa e Desenvolvimento (Longo prazo)
PROJECT_ID_4=$(curl -s -X POST http://localhost:8080/projects \
-H "Authorization: Bearer $TOKEN" \
-H "Content-Type: application/json" \
-d "{
    \"name\": \"Análise de Dados com IA\",
    \"description\": \"Projeto de P&D para criar um modelo de machine learning que prevê o churn de clientes com base em dados de uso.\",
    \"startDate\": \"$(date -d "+2 weeks" +%Y-%m-%d)\",
    \"endDate\": \"$(date -d "+1 year" +%Y-%m-%d)\"
}" | jq -r '.id')
echo "  - Projeto 4 (Análise de IA) criado com ID: $PROJECT_ID_4"

# Projeto 5: Recursos Humanos (Curto prazo, urgente)
PROJECT_ID_5=$(curl -s -X POST http://localhost:8080/projects \
-H "Authorization: Bearer $TOKEN" \
-H "Content-Type: application/json" \
-d "{
    \"name\": \"Implementação de Novo Sistema de RH\",
    \"description\": \"Substituir o sistema de RH legado por uma nova plataforma SaaS, incluindo treinamento e migração de dados dos funcionários.\",
    \"startDate\": \"$(date -d "today" +%Y-%m-%d)\",
    \"endDate\": \"$(date -d "+45 days" +%Y-%m-%d)\"
}" | jq -r '.id')
echo "  - Projeto 5 (Sistema de RH) criado com ID: $PROJECT_ID_5"
echo ""


# --- PASSO 3: Criar 2 tasks para cada projeto com datas dinâmicas ---
echo "Criando tarefas para cada projeto..."

# Tarefas para o Projeto 1 (E-commerce)
echo "  - Tarefas para o Projeto 1 (E-commerce):"
curl -s -o /dev/null -X POST http://localhost:8080/tasks -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d "{\"title\": \"Desenvolver carrinho de compras\", \"description\": \"Implementar a funcionalidade completa do carrinho, incluindo adicionar, remover e atualizar itens.\", \"status\": \"DOING\", \"priority\": \"HIGH\", \"dueDate\": \"$(date -d "+2 months" +%Y-%m-%d)\", \"projectId\": \"$PROJECT_ID_1\"}"
curl -s -o /dev/null -X POST http://localhost:8080/tasks -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d "{\"title\": \"Integrar com gateway de pagamento\", \"description\": \"Conectar a plataforma com Stripe e PayPal.\", \"status\": \"TODO\", \"priority\": \"HIGH\", \"dueDate\": \"$(date -d "+4 months" +%Y-%m-%d)\", \"projectId\": \"$PROJECT_ID_1\"}"
echo "    ... 2 tarefas criadas."

# Tarefas para o Projeto 2 (Migração AWS)
echo "  - Tarefas para o Projeto 2 (Migração AWS):"
curl -s -o /dev/null -X POST http://localhost:8080/tasks -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d "{\"title\": \"Provisionar instâncias EC2 e RDS\", \"description\": \"Criar a infraestrutura base na AWS usando Terraform.\", \"status\": \"DONE\", \"priority\": \"HIGH\", \"dueDate\": \"$(date -d "+1 month" +%Y-%m-%d)\", \"projectId\": \"$PROJECT_ID_2\"}"
curl -s -o /dev/null -X POST http://localhost:8080/tasks -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d "{\"title\": \"Configurar pipeline de CI/CD no CodePipeline\", \"description\": \"Automatizar o build e deploy da aplicação na nova infraestrutura.\", \"status\": \"DOING\", \"priority\": \"MEDIUM\", \"dueDate\": \"$(date -d "+2 months" +%Y-%m-%d)\", \"projectId\": \"$PROJECT_ID_2\"}"
echo "    ... 2 tarefas criadas."

# Tarefas para o Projeto 3 (Marketing Q3)
echo "  - Tarefas para o Projeto 3 (Marketing Q3):"
curl -s -o /dev/null -X POST http://localhost:8080/tasks -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d "{\"title\": \"Criar criativos para redes sociais\", \"description\": \"Desenvolver banners e vídeos para Facebook, Instagram e LinkedIn.\", \"status\": \"DONE\", \"priority\": \"MEDIUM\", \"dueDate\": \"$(date -d "-15 days" +%Y-%m-%d)\", \"projectId\": \"$PROJECT_ID_3\"}"
curl -s -o /dev/null -X POST http://localhost:8080/tasks -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d "{\"title\": \"Configurar campanha de Google Ads\", \"description\": \"Definir palavras-chave, orçamento e segmentação para a campanha de busca.\", \"status\": \"DOING\", \"priority\": \"HIGH\", \"dueDate\": \"$(date -d "+1 month" +%Y-%m-%d)\", \"projectId\": \"$PROJECT_ID_3\"}"
echo "    ... 2 tarefas criadas."

# Tarefas para o Projeto 4 (Análise de IA)
echo "  - Tarefas para o Projeto 4 (Análise de IA):"
curl -s -o /dev/null -X POST http://localhost:8080/tasks -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d "{\"title\": \"Coleta e limpeza de dados históricos\", \"description\": \"Extrair dados de uso dos últimos 2 anos do data warehouse e tratar valores ausentes.\", \"status\": \"DOING\", \"priority\": \"HIGH\", \"dueDate\": \"$(date -d "+3 months" +%Y-%m-%d)\", \"projectId\": \"$PROJECT_ID_4\"}"
curl -s -o /dev/null -X POST http://localhost:8080/tasks -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d "{\"title\": \"Treinar modelo de regressão logística\", \"description\": \"Usar os dados limpos para treinar um modelo baseline e avaliar a acurácia.\", \"status\": \"TODO\", \"priority\": \"MEDIUM\", \"dueDate\": \"$(date -d "+6 months" +%Y-%m-%d)\", \"projectId\": \"$PROJECT_ID_4\"}"
echo "    ... 2 tarefas criadas."

# Tarefas para o Projeto 5 (Sistema de RH)
echo "  - Tarefas para o Projeto 5 (Sistema de RH):"
curl -s -o /dev/null -X POST http://localhost:8080/tasks -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d "{\"title\": \"Mapear processos de RH atuais\", \"description\": \"Documentar os fluxos de trabalho de admissão, férias e desligamento.\", \"status\": \"DONE\", \"priority\": \"LOW\", \"dueDate\": \"$(date -d "+10 days" +%Y-%m-%d)\", \"projectId\": \"$PROJECT_ID_5\"}"
curl -s -o /dev/null -X POST http://localhost:8080/tasks -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d "{\"title\": \"Realizar treinamento com a equipe de RH\", \"description\": \"Agendar e conduzir sessões de treinamento sobre a nova plataforma SaaS.\", \"status\": \"TODO\", \"priority\": \"MEDIUM\", \"dueDate\": \"$(date -d "+30 days" +%Y-%m-%d)\", \"projectId\": \"$PROJECT_ID_5\"}"
echo "    ... 2 tarefas criadas."

echo ""
echo "População de dados concluída!"
