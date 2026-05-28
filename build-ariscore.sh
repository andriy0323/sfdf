#!/usr/bin/env bash
# Сборка ArisCore — единого JAR-плагина.
# Требуется JDK 21+ и Maven 3.6+.
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/ArisCore"

mvn -B clean package -DskipTests

echo
echo "Готово!"
ls -lh "$SCRIPT_DIR/ArisCore/target/ArisCore-1.0.0.jar"
