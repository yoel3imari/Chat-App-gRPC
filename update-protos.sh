#!/bin/bash

# === CONFIGURATION ===
SOURCE="./proto"
DESTINATIONS=(
    "./backend/admin/src/main/proto"
    "./backend/auth/src/main/proto"
    "./backend/user/src/main/proto"
    "./backend/chat/src/main/proto"
    "./backend/notification/src/main/proto"
    "./frontend/proto"
)

if [ ! -d "$SOURCE" ]; then
  echo "❌ Source folder '$SOURCE' does not exist."
  exit 1
fi

for DEST in "${DESTINATIONS[@]}"; do
  echo "📁 Copying .proto files to $DEST..."

  # mkdir -p "$DEST"

  rsync -av --include='*/' --include='*.proto' --exclude='*' --delete "$SOURCE"/ "$DEST"/

  echo "✅ Done: $DEST"
done

echo "🎉 All destinations updated with .proto files only."
