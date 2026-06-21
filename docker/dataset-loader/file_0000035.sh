#!/usr/bin/env bash
#
# Loads real search queries into PostgreSQL from the Microsoft ORCAS dataset.
# Each row in orcas.tsv is a (query, clicked-document) pair, so the number of
# rows per distinct query approximates its click popularity. We take the top-N
# queries by that count and COPY them into query_frequency.
#
# Idempotent: exits early if data is already present. The download is cached on
# a volume; set ORCAS_LOCAL_PATH to reuse a pre-downloaded file.
set -euo pipefail

: "${DB_HOST:=postgres}"
: "${DB_PORT:=5432}"
: "${DB_NAME:=typeahead}"
: "${DB_USER:=typeahead}"
: "${DB_PASSWORD:=typeahead}"
: "${ORCAS_URL:=https://msmarco.z22.web.core.windows.net/msmarcoranking/orcas.tsv.gz}"
: "${TOP_N:=1000000}"
: "${DATA_DIR:=/data}"

export PGHOST="$DB_HOST" PGPORT="$DB_PORT" PGDATABASE="$DB_NAME" PGUSER="$DB_USER" PGPASSWORD="$DB_PASSWORD"
export TMPDIR="$DATA_DIR"
mkdir -p "$DATA_DIR"

echo "[orcas] checking whether query_frequency is already populated..."
EXISTING=$(psql -tAc "SELECT count(*) FROM query_frequency" 2>/dev/null || echo 0)
if [ "${EXISTING:-0}" -gt 0 ]; then
    echo "[orcas] already loaded ($EXISTING rows); nothing to do."
    exit 0
fi

SOURCE="$DATA_DIR/orcas.tsv.gz"
if [ -n "${ORCAS_LOCAL_PATH:-}" ] && [ -f "$ORCAS_LOCAL_PATH" ]; then
    SOURCE="$ORCAS_LOCAL_PATH"
    echo "[orcas] using local file $SOURCE"
elif [ -f "$SOURCE" ]; then
    echo "[orcas] using cached download $SOURCE"
else
    echo "[orcas] downloading dataset (~315 MB) from $ORCAS_URL ..."
    curl -fSL "$ORCAS_URL" -o "$SOURCE"
fi

COUNTS="$DATA_DIR/query_counts.txt"
TOP="$DATA_DIR/top_queries.tsv"
if [ -s "$COUNTS" ]; then
    echo "[orcas] reusing cached counts $COUNTS"
else
    echo "[orcas] counting clicks per query (this can take a few minutes)..."
    # Write the full sorted-by-count list to a file first. Piping straight into
    # `head` would close the pipe early and kill `sort` with SIGPIPE, which
    # `pipefail` would treat as a failure.
    zcat "$SOURCE" \
        | cut -f2 \
        | sed 's/\\/ /g' \
        | awk 'NF' \
        | LC_ALL=C sort -S 20% \
        | uniq -c \
        | LC_ALL=C sort -rn -S 20% \
        > "$COUNTS"
fi

echo "[orcas] taking top $TOP_N and formatting..."
# uniq -c emits "  <count> <query>"; turn it into "<query>\t<count>" with sed so
# the query text is preserved byte-for-byte (awk field rebuild would collapse
# internal whitespace and create duplicate keys).
head -n "$TOP_N" "$COUNTS" \
    | sed -E 's/^ *([0-9]+) (.*)$/\2\t\1/' \
    > "$TOP"

echo "[orcas] loading $(wc -l < "$TOP") rows into query_frequency ..."
psql -c "\copy query_frequency(query_text, total_count) FROM '$TOP' WITH (FORMAT text)"

echo "[orcas] done. query_frequency now has $(psql -tAc 'SELECT count(*) FROM query_frequency') rows."
