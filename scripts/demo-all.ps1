$ErrorActionPreference = "Stop"
Set-Location (Join-Path $PSScriptRoot "..")

& ./scripts/build.ps1

Write-Host "`n===== 1. INSPECT ====="
& java -cp target/classes demo.inspect.InspectDemo

Write-Host "`n===== javap: Calculator before transform ====="
& javap -c -p target/classes/demo/target/Calculator.class

Write-Host "`n===== 2. GENERATE ====="
& java -cp target/classes demo.generate.GenerateDemo

Write-Host "`n===== javap: Generated ====="
& javap -c -p target/classes/demo/generated/Generated.class

Write-Host "`n===== 3. TRANSFORM ====="
& java -cp target/classes demo.transform.TransformDemo

Write-Host "`n===== javap: Calculator after transform ====="
& javap -c -p target/classes/demo/target/Calculator.class
