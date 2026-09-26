#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

version="$(javac -version 2>&1 | awk '{print $2}')"
if [[ "$version" != 25* ]]; then
  echo "This demo requires JDK 25. Found javac $version" >&2
  exit 1
fi

rm -rf target/classes
mkdir -p target/classes

mapfile -t sources < <(find src/main/java -name '*.java' -print | sort)
javac --release 25 -d target/classes "${sources[@]}"

echo "Compiled ${#sources[@]} source files with JDK $version"
echo "Output: target/classes"
