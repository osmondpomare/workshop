#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
REPO_DIR="$(cd .. && pwd)/libs/repository"
mvn -B clean deploy -DaltDeploymentRepository=workshop-libs::file://${REPO_DIR}
echo "Libraries published to ${REPO_DIR}"
