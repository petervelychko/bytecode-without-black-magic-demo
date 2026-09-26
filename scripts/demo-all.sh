#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

./scripts/build.sh

echo
echo "===== 1. INSPECT ====="
java -cp target/classes demo.inspect.InspectDemo

echo
echo "===== javap: Calculator before transform ====="
javap -c -p target/classes/demo/target/Calculator.class

echo
echo "===== 2. GENERATE ====="
java -cp target/classes demo.generate.GenerateDemo

echo
echo "===== javap: Generated ====="
javap -c -p target/classes/demo/generated/Generated.class

echo
echo "===== 3. TRANSFORM ====="
java -cp target/classes demo.transform.TransformDemo

echo
echo "===== javap: Calculator after transform ====="
javap -c -p target/classes/demo/target/Calculator.class
