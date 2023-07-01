#!/usr/bin/env bash
set -e -u -o pipefail
mkdir -p ~/.monotony
clojure -X:build uberjar
cp target/monotony.jar ~/.monotony/monotony.jar
cat << "EOF" > /usr/local/bin/monotony
exec java -jar ~/.monotony/monotony.jar "$@"
EOF
chmod +x /usr/local/bin/monotony