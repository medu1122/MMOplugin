# Resource pack ROLEmmo – Icon GUI

Pack chứa icon dùng trong giao diện plugin (role, skill, nút bấm). Plugin dùng **CustomModelData** trên item Paper để hiển thị icon; client có pack sẽ thấy hình ảnh thay vì tờ giấy.

## Cấu trúc

```
resourcepack/
├── pack.mcmeta
├── README.md
├── PROMPTS_ICONS.md          ← Prompt tạo từng icon
├── assets/minecraft/
│   ├── items/
│   │   └── paper.json        ← (1.21.4) Items model: range_dispatch CMD 1–10 → icon
│   └── models/item/
│       └── paper.json        ← (cũ, <1.21.4) Override: custom_model_data 1–10 → icon
└── assets/rolemmo/
    ├── textures/item/        ← Bỏ ảnh PNG vào đây
    │   └── README.md
    └── models/item/          ← Model cho từng icon (đã có sẵn)
        ├── icon_role_dps.json
        ├── icon_btn_change.json
        └── ...
```

## Cách dùng

1. **Bỏ ảnh** vào `assets/rolemmo/textures/item/` với đúng tên file (xem README trong thư mục đó). Kích thước 32×32 hoặc 64×64 px, PNG, nền trong suốt.
2. **Nén pack:** Nén **nội dung** bên trong folder `resourcepack` (gồm `pack.mcmeta`, `assets/`) thành một file `.zip`.
3. **Gửi pack cho client:** Đặt URL pack vào `server.properties`: `resource-pack=https://.../rolemmo_icons.zip` (và mở port nếu host trên server). Hoặc cho người chơi tải file zip và bật trong Resource Packs.
4. **Kết quả:** Khi vào server (hoặc bật pack), GUI Role Info / Chọn role / Đổi role sẽ hiện icon thay vì item vanilla. Không có pack thì vẫn thấy item Paper bình thường, plugin vẫn chạy.

## Lưu ý

- **pack_format 61** – Pack dùng format cho **Minecraft 1.21.4**. Nếu client/server khác phiên bản có thể cần đổi số trong `pack.mcmeta` (xem [Pack format](https://minecraft.wiki/w/Pack_format)).
- **Ảnh thumbnail** – Trong danh sách Resource Pack, Minecraft hiện ảnh mặc định (phong cảnh) nếu không có file `pack.png`. Muốn thumbnail riêng: thêm file **pack.png** (128×128 px) vào cùng thư mục với `pack.mcmeta`, rồi nén lại.
- Sau khi sửa `pack.mcmeta` hoặc thêm/sửa ảnh, cần **nén lại pack** và (nếu dùng server pack) **upload lại** hoặc đổi URL/version để client tải bản mới.
