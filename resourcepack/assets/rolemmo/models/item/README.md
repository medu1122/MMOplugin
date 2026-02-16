# Models cho icon (tự động dùng texture cùng tên)

Mỗi file PNG trong `../textures/item/` cần một file JSON cùng tên ở đây.

**Mẫu** (copy và đổi tên + đường dẫn texture):
```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "rolemmo:item/TÊN_FILE_KHÔNG_CÓ_PNG"
  }
}
```

Ví dụ: thêm `icon_role_tanker.png` → tạo `icon_role_tanker.json` với `"layer0": "rolemmo:item/icon_role_tanker"`.

Đã có sẵn: `icon_role_dps.json` (khi bạn bỏ `icon_role_dps.png` vào textures/item là dùng được).
