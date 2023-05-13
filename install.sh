#!/usr/bin/env bash
mkdir -p ~/.monotony
clj -X:build uberjar
cp target/monotony.jar ~/.monotony/monotony.jar
cat << "EOF" > /usr/local/bin/monotony
exec java -jar ~/.monotony/monotony.jar "$@"
EOF
chmod +x /usr/local/bin/monotony