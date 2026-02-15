# ROLEmmo Plugin

Plugin hệ thống role-playing với 3 vai trò chính (TANKER, DPS, HEALER) kèm hệ thống level, skill, danh hiệu và tích hợp với các plugin khác.

## ✨ Tính Năng

### 🎭 Role System
- **3 Roles**: TANKER, DPS, HEALER
- Chọn role lần đầu qua GUI
- Đổi role với cooldown 24 giờ hoặc trả 10 coins
- Tự động set rank trong LuckPerms khi chọn role

### 📊 Level & Experience System
- Level từ 1 đến 999 cho mỗi role
- Tự động convert exp nhân vật thành exp role
- Tự động level up khi đủ exp
- Mỗi level up: +1 skill point

### 🏆 Title System
- Unlock titles theo level cho từng role
- Titles được giữ mãi mãi, không mất khi đổi role
- Chọn và sử dụng title qua GUI
- Tự động unlock khi level up

### ⚔️ Skill System
- Mỗi role có nhiều skills (DPS có Fireball skill đầu tiên)
- Chọn skill để sử dụng (cooldown 30 phút giữa các lần đổi)
- Upgrade skills với skill points
- Skill items không thể drop, không thể move
- Cooldown system với actionbar display

### 🎨 GUI System
- **RoleSelectGUI**: Chọn role lần đầu
- **RoleInfoGUI**: GUI chính hiển thị thông tin role
- **RoleChangeGUI**: Đổi role
- **TitleGUI**: Chọn và sử dụng danh hiệu
- **SkillListGUI**: Xem danh sách skills
- **SkillUpgradeGUI**: Upgrade skills
- **SkillSelectionGUI**: Chọn skill để sử dụng

## 📦 Dependencies

### Required
- **Paper/Spigot**: 1.21.4+
- **Java**: 21+

### Soft Dependencies (Optional)
- **LuckPerms**: Tự động set rank khi chọn role
- **MoneyPlugin**: Trừ coins khi đổi role
- **ClanCore**: Team protection cho skills

## 🚀 Installation

1. Download plugin JAR file
2. Copy vào thư mục `plugins/` của server
3. Restart server
4. Plugin sẽ tự động tạo config và database

## ⚙️ Configuration

File `config.yml` chứa tất cả cấu hình:
- Role change cooldown và cost
- Titles cho từng role
- Skill upgrade costs
- Experience conversion rate
- LuckPerms group names
- Messages

## 📝 Commands

### Player Commands
- `/role` - Mở GUI chính (RoleInfoGUI hoặc RoleSelectGUI)
- `/role select` - Mở GUI chọn role
- `/role info` - Mở GUI thông tin role
- `/role change` - Mở GUI đổi role
- `/role titles` - Mở GUI danh hiệu

### Admin Commands
- `/roleadmin givelevel <player> <role> <level>` - Set level cho player
- `/roleadmin giveskillpoints <player> <amount>` - Give skill points
- `/roleadmin setrole <player> <role>` - Set role cho player
- `/roleadmin takeskill <player> <skillId>` - Remove skill item
- `/roleadmin giveexp <player> <role> <amount>` - Give experience

## 🗄️ Database

Plugin sử dụng SQLite database (`rolemmo.db`) để lưu:
- Player role data (level, exp, skill points)
- Skill levels
- Titles đã unlock
- Active title
- Role change history

## 🔧 Permissions

- `rolemmo.use` - Sử dụng player commands (default: true)
- `rolemmo.admin` - Sử dụng admin commands (default: op)

## 📋 Skills

### DPS - Fireball Skill
- **Level 1**: 5 cầu lửa, 10 HP damage, 12s cooldown
- **Level 2**: 5 cầu lửa, 19 HP damage, 11s cooldown, burn +5%
- **Level 3**: 6 cầu lửa, 25 HP damage, 10s cooldown, burn +10%
- **Level 4**: 6 cầu lửa, 38 HP damage, 9s cooldown, burn +15%
- **Level 5**: 7 cầu lửa, 50 HP damage, 8s cooldown, burn +20%
- **Level 6**: 7 cầu lửa, 67 HP damage, 8s cooldown, burn +25%

**Effect**: Particles đẹp, sound effects, team protection

## 🎯 Titles

### TANKER
- Level 1: TANKER
- Level 50: Luyện Thể Sơ Kỳ
- Level 120: Thiết Giáp Cảnh
- Level 340: Kim Cang Hộ Thể
- Level 570: Huyền Giáp Tông Sư
- Level 690: Thánh Giáp Chiến Tôn
- Level 860: Bất Diệt Kim Thân
- Level 990: Vạn Cổ Hộ Đạo

### DPS
- Level 1: DPS
- Level 50: Sát Khí Sơ Thành
- Level 120: Huyết Chiến Giả
- Level 340: Tu La Đao Tôn
- Level 570: Chiến Vương
- Level 690: Ma Diệt Chi Chủ
- Level 860: Thí Thiên Chiến Thánh
- Level 990: Vạn Kiếp Sát Thần

### HEALER
- Level 1: HEALER
- Level 50: Linh Y Sơ Cảnh
- Level 120: Thanh Tâm Hộ Pháp
- Level 340: Thánh Linh Sứ
- Level 570: Huyền Thiên Trị Giả
- Level 690: Thiên Đạo Hộ Mệnh
- Level 860: Thánh Quang Đại Tôn
- Level 990: Vạn Linh Chi Chủ

## 🐛 Troubleshooting

### Plugin không load
- Kiểm tra Java version (cần Java 21+)
- Kiểm tra Paper/Spigot version (cần 1.21.4+)
- Xem console logs để biết lỗi cụ thể

### Database errors
- Kiểm tra quyền ghi file trong thư mục plugins/ROLEmmo/
- Database sẽ tự động được tạo khi plugin enable

### LuckPerms không hoạt động
- Đảm bảo LuckPerms đã được load trước ROLEmmo
- Kiểm tra LuckPerms API version (cần 5.4+)

## 📄 License

Private project - All rights reserved

## 👤 Author

medu1122

## 📞 Support

Nếu gặp vấn đề, vui lòng kiểm tra logs trong console hoặc liên hệ developer.
