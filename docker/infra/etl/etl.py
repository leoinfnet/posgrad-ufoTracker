import os
import time
import json

import psycopg2
import requests

# Configurações via variáveis de ambiente (vêm do docker-compose)
PG_HOST = os.getenv("POSTGRES_HOST", "localhost")
PG_DB = os.getenv("POSTGRES_DB", "ufotracker")
PG_USER = os.getenv("POSTGRES_USER", "ufo")
PG_PASS = os.getenv("POSTGRES_PASSWORD", "ufo123docker")

ES_HOST = os.getenv("ELASTIC_HOST", "http://localhost:9200")
INDEX = "ufo-avistamentos"


def esperar_dependencias(segundos: int = 10):
    print(f"⏳ Aguardando {segundos} segundos para Postgres e Elasticsearch subirem...")
    time.sleep(segundos)


def criar_indice():
    mapping = {
        "settings": {
            "analysis": {
                "analyzer": {
                    "pt_text": {
                        "type": "standard",
                        "stopwords": "_portuguese_"
                    }
                }
            }
        },
        "mappings": {
            "properties": {
                "id": {"type": "keyword"},
                "dataHora": {"type": "date"},
                "cidade": {
                    "type": "text",
                    "fields": {
                        "keyword": {"type": "keyword"}
                    }
                },
                "estado": {
                    "type": "keyword"
                },
                "tipoObjeto": {
                    "type": "keyword"
                },
                "confiabilidade": {"type": "integer"},
                "location": {"type": "geo_point"},
                "descricao": {
                    "type": "text",
                    "analyzer": "pt_text"
                }
            }
        }
    }

    url = f"{ES_HOST}/{INDEX}"
    print(f"📌 Criando índice '{INDEX}' em {url} ...")
    resp = requests.put(url, json=mapping)

    if resp.status_code in (200, 201):
        print("✅ Índice criado com sucesso.")
    elif resp.status_code == 400 and "resource_already_exists_exception" in resp.text:
        print("ℹ️ Índice já existe, seguindo em frente.")
    else:
        print(f"⚠️ Resposta inesperada ao criar índice: {resp.status_code}")
        print(resp.text)


def conectar_postgres():
    print(f"📌 Conectando ao Postgres em {PG_HOST}, banco {PG_DB} ...")
    conn = psycopg2.connect(
        host=PG_HOST,
        dbname=PG_DB,
        user=PG_USER,
        password=PG_PASS
    )
    print("✅ Conexão com Postgres estabelecida.")
    return conn


def carregar_avistamentos(conn):
    sql = """
          SELECT
              id,
              datahora,
              latitude,
              longitude,
              city,
              estado,
              descricao,
              confiabilidade,
              tipo_objeto
          FROM avistamentos; \
          """
    cur = conn.cursor()
    cur.execute(sql)
    rows = cur.fetchall()
    cur.close()
    print(f"📥 Carregados {len(rows)} avistamentos do Postgres.")
    return rows


def montar_bulk(rows):
    """
    Monta o corpo de requisição para o _bulk do Elasticsearch no formato NDJSON:
    { "index": { "_index": "ufo-avistamentos" } }
    { ...documento... }
    """
    lines = []

    for (id_, datahora, lat, lon, cidade, estado, descricao, confiab, tipo_objeto) in rows:
        # pequenos fallbacks pra não quebrar se algo vier null do banco
        estado_val = estado if estado is not None else "EX"
        tipo_val = tipo_objeto if tipo_objeto is not None else "desconhecido"

        doc = {
            "id": str(id_),
            "dataHora": datahora.isoformat() if datahora else None,
            "cidade": cidade,
            "estado": estado_val,
            "tipoObjeto": tipo_val,
            "confiabilidade": int(confiab) if confiab is not None else None,
            "location": {
                "lat": float(lat),
                "lon": float(lon)
            },
            "descricao": descricao or ""
        }

        meta = {"index": {"_index": INDEX}}
        lines.append(json.dumps(meta, ensure_ascii=False))
        lines.append(json.dumps(doc, ensure_ascii=False))

    body = "\n".join(lines) + "\n"
    return body


def enviar_bulk(body: str):
    url = f"{ES_HOST}/_bulk"
    print(f"📤 Enviando bulk para {url} ... (tamanho: {len(body)} bytes)")
    resp = requests.post(url, data=body, headers={"Content-Type": "application/x-ndjson"})

    print(f"🧾 Status do bulk: {resp.status_code}")
    if resp.status_code >= 300:
        print("⚠️ Erro ao enviar bulk:")
        print(resp.text[:2000])  # evita floodar o log
    else:
        # Mesmo com 200, pode ter errors:true — vale logar o começo
        print(resp.text[:2000])


def main():
    esperar_dependencias(10)
    criar_indice()

    conn = conectar_postgres()
    try:
        rows = carregar_avistamentos(conn)
        if not rows:
            print("⚠️ Nenhum avistamento encontrado no Postgres. Encerrando ETL.")
            return

        bulk_body = montar_bulk(rows)
        enviar_bulk(bulk_body)
        print("✅ ETL concluído com sucesso.")
    finally:
        conn.close()
        print("🔌 Conexão com Postgres encerrada.")


if __name__ == "__main__":
    main()
