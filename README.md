# 🛸 UFO Tracker – Sistema de Avistamentos em Spring Boot

Bem-vindo ao **UFO Tracker**, um projeto moderno desenvolvido em **Java 21**, usando tecnologias de ponta como:

- **Spring Boot  4 (Web, JPA, Cache, Testes)**
- **PostgreSQL (latest)** para armazenamento primário
- **Elasticsearch 8.x** para buscas textuais, filtros avançados e agregações
- **Docker Compose** para orquestrar os serviços
- **ETL Python** que sincroniza os dados do Postgres com o Elasticsearch

Este projeto permite **cadastrar avistamentos de OVNIs**, realizar **buscas inteligentes**, gerar **relatórios semanais**, análises geográficas e muito mais.



---
![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Elasticsearch](https://img.shields.io/badge/Elasticsearch-8.x-005571?style=for-the-badge&logo=elasticsearch&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-latest-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Python](https://img.shields.io/badge/Python-3.x-3776AB?style=for-the-badge&logo=python&logoColor=white)

## 🚀 Objetivos do Projeto

✔ Registrar avistamentos contendo:
- Data e hora
- Localização (lat/long)
- Cidade / Estado
- Tipo de objeto observado
- Confiabilidade do relato
- Descrição completa

✔ Expor endpoints HTTP para CRUD e consultas avançadas.

✔ Usar Elasticsearch para:
- Buscas por texto (`match`, `match_phrase`, `multi_match`)
- Filtros por estado, tipo e confiabilidade
- Proximidade geográfica (`geo_distance`)
- Agregações por estado, por data, e top confiabilidade

✔ Gerar relatórios semanais como:
- **Top avistamentos por estado**
- **Resumo semanal com caching**
- **Contagens por período**

---

## 🏗 Arquitetura Geral

```
Spring Boot (Java 21)
 ├── Controllers REST
 ├── Services (JPA + Elasticsearch)
 ├── DTOs e mapeamentos
 ├── Cache de relatórios semanais
 └── Testes automatizados (MockMvc)

PostgreSQL
 └── Tabela principal: avistamentos

Python ETL
 └── Extrai dados do Postgres e indexa no Elasticsearch

Elasticsearch
 ├── Índice: ufo-avistamentos
 ├── Buscas textuais
 ├── Geo distance
 └── Agregações e métricas
```

---

## 🐘 Banco PostgreSQL

A tabela principal contém:

- `id` (UUID)
- `dataHora`
- `latitude`
- `longitude`
- `cidade`
- `estado`
- `tipoObjeto`
- `descricao`
- `confiabilidade`

O projeto inclui um `init.sql` com centenas de avistamentos semi-realistas.

---

## 🔍 Elasticsearch – Poder de Busca

A aplicação expõe endpoints como:

### Buscar por texto:
```
GET /api/avistamentos/search/texto?texto=luz&page=0&size=10
```

### Busca avançada:
```
GET /api/avistamentos/search/avancada?estado=RJ&tipoObjeto=cilindro&confiabilidadeMin=70
```

### Geo Distance:
```
GET /api/avistamentos/search/perto?lat=-22.9&lon=-43.1&distanciaKm=100
```

### Relatório semanal (com cache):
```
GET /api/avistamentos/search/top-semana?data=2025-04-02
```

---

## 🧠 Relatórios Semanais

O sistema gera:

- Top avistamento por estado da semana anterior
- Resumo semanal com caching
- Possibilidade de percorrer semana a semana

O cache usa chave personalizada:

```
ufo:semana:2025-04-02
```

---

## 🐳 Docker Compose

O projeto sobe automaticamente:

- `postgres:latest`
- `elasticsearch:8.x`
- `kibana` (opcional)
- serviço `etl` em Python

---

## 💡 Tecnologias Utilizadas

| Tecnologia | Versão |
|-----------|--------|
| **Java** | 21 |
| **Spring Boot** | 3.x / 4.x |
| **PostgreSQL** | latest |
| **Elasticsearch** | 8.x |
| **Python** | 3.x (para o ETL) |
| **Docker Compose** | latest |

---

## 📦 Como Rodar

1. Clone o projeto
2. Execute:

```
docker compose up -d
```

3. Aguarde Postgres + Elasticsearch subirem
4. Rode o ETL
5. Inicie a aplicação Spring Boot:

```
./mvnw spring-boot:run
```

---

## 📚 Endpoints Principais

- `POST /api/avistamentos`
- `GET /api/avistamentos?page=0&size=10`
- `GET /api/avistamentos/{id}`
- `PUT /api/avistamentos/{id}`

### Busca e Relatórios
- `/api/avistamentos/search/texto`
- `/api/avistamentos/search/avancada`
- `/api/avistamentos/search/perto`
- `/api/avistamentos/search/top-semana`
- `/api/avistamentos/search/agg/por-estado`

---

## ✨ Sobre o Projeto

Este sistema foi criado para estudar:

- Integração de Elasticsearch com Spring Boot
- Processamento de dados com ETL externo
- Consultas avançadas (textuais, geográficas e agregações)
- Cacheamento de relatórios e tuning
- Boas práticas REST

É um projeto ideal para aulas, experimentação ou demonstrações de arquitetura moderna.

---

## 🛸 Have fun exploring the skies!
