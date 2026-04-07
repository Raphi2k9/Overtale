#!/bin/bash
cd src/main/resources/assets/textures/
for f in *.png; do
  python3 -c "from PIL import Image; Image.open(\"$f\").convert(\"RGBA\").save(\"$f\")"
  echo "Fixed $f"
done
