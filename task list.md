# ROLEmmo Plugin - Task List

## 📋 Tổng Quan Dự Án

Plugin ROLEmmo là hệ thống role-playing với 3 vai trò chính (TANKER, DPS, HEALER) kèm hệ thống level, skill, danh hiệu và tích hợp với các plugin khác (LuckPerms, MoneyPlugin, ClanCore).

---

## 🎯 Phase 1: Setup & Foundation

### 1.1 Project Configuration
- [ ] **Cập nhật pom.xml**
  - Thêm dependency cho LuckPerms API (version 5.4)
  - Thêm dependency cho MoneyPlugin (soft dependency)
  - Thêm dependency cho ClanCore (soft dependency)
  - Thêm SQLite dependency cho database
  - Cấu hình maven-shade-plugin để package dependencies

- [ ] **Cập nhật plugin.yml**
  - Thêm softdepend: [LuckPerms, moneyPlugin, ClanCore]
  - Định nghĩa các commands: `/role`, `/roleadmin`
  - Thêm permissions cho commands

- [ ] **Tạo cấu trúc package**
  ```
  me.skibidi.rolemmo/
    ├── ROLEmmo.java (main class)
    ├── manager/
    ├── model/
    ├── command/
    ├── gui/
    ├── skill/
    ├── storage/
    ├── listener/
    ├── util/
    └── config/
  ```

### 1.2 Database Setup
- [ ] **Tạo DatabaseManager**
  - Kết nối SQLite database
  - Tạo các bảng cần thiết:
    - `role_players`: Lưu thông tin role, level, skill points của player
    - `role_skills`: Lưu skill level của từng player cho từng role
    - `role_titles`: Lưu danh sách danh hiệu player đã sở hữu
    - `role_active_title`: Lưu danh hiệu đang active của player
    - `role_change_history`: Lưu lịch sử đổi role (để check cooldown 1 ngày)

- [ ] **Tạo Repository classes**
  - `PlayerRoleRepository`: CRUD cho player role data
  - `SkillRepository`: CRUD cho skill data
  - `TitleRepository`: CRUD cho title data

### 1.3 Config System
- [ ] **Tạo ConfigManager**
  - Load/save config.yml
  - Các section cần có:
    - `roles`: Config cho 3 role (TANKER, DPS, HEALER)
    - `titles`: Config danh hiệu theo level cho từng role
    - `skills`: Config skill system (số điểm cần để upgrade mỗi level)
    - `role_change`: Config cooldown và cost đổi role
    - `database`: Config database connection
    - `messages`: Config messages

- [ ] **Tạo file config.yml mẫu**
  - Định nghĩa đầy đủ các danh hiệu cho 3 role
  - Config skill upgrade costs
  - Config cooldown và costs

---

## 🎭 Phase 2: Core Role System

### 2.1 Role Model & Manager
- [ ] **Tạo Role enum**
  - TANKER, DPS, HEALER
  - Methods: `getDisplayName()`, `getColor()`, `getIcon()`

- [ ] **Tạo PlayerRole model**
  - Fields: UUID, Role, level (int), skillPoints (int), lastRoleChange (long)
  - Methods: `canChangeRole()`, `getTimeUntilCanChange()`

- [ ] **Tạo RoleManager**
  - `selectRole(Player, Role)`: Chọn role cho player
  - `changeRole(Player, Role)`: Đổi role (check cooldown/cost)
  - `getPlayerRole(Player)`: Lấy role hiện tại
  - `getRoleLevel(Player, Role)`: Lấy level của role
  - `addRoleLevel(Player, Role, int)`: Tăng level role
  - `getSkillPoints(Player)`: Lấy số skill points
  - `addSkillPoints(Player, int)`: Thêm skill points
  - Tích hợp với LuckPerms để set rank khi chọn role

### 2.2 Role Change System
- [ ] **Cooldown System**
  - Check thời gian đã qua từ lần đổi role cuối
  - Nếu < 1 ngày: yêu cầu trả 10 coins hoặc đợi
  - Nếu >= 1 ngày: cho phép đổi miễn phí

- [ ] **Cost System**
  - Tích hợp với MoneyPlugin API
  - Trừ 10 coins nếu muốn đổi ngay (khi chưa đủ 1 ngày)
  - Validate đủ coins trước khi đổi

- [ ] **LuckPerms Integration**
  - Khi chọn/đổi role: set rank tương ứng trong LuckPerms
  - Sử dụng LuckPerms API để set group cho player
  - Tạo các group trong LuckPerms: `tanker`, `dps`, `healer`

---

## 🏆 Phase 3: Title System

### 3.1 Title Model
- [ ] **Tạo Title model**
  - Fields: String id, String name, Role role, int requiredLevel
  - Methods: `getDisplayName()`, `isUnlocked(int level)`

- [ ] **Tạo TitleManager**
  - `getUnlockedTitles(Player, Role)`: Lấy danh sách danh hiệu đã unlock
  - `unlockTitle(Player, Title)`: Unlock danh hiệu khi đạt level
  - `setActiveTitle(Player, Title)`: Set danh hiệu đang active
  - `getActiveTitle(Player)`: Lấy danh hiệu đang active
  - `getAllTitles(Player)`: Lấy tất cả danh hiệu đã sở hữu (từ tất cả role)

### 3.2 Title Unlock Logic
- [ ] **Auto-unlock khi level up**
  - Khi player level up role, check xem có danh hiệu nào unlock không
  - Tự động unlock và thông báo cho player
  - Lưu vào database

- [ ] **Title Persistence**
  - Danh hiệu được giữ mãi mãi, không mất khi đổi role
  - Lưu vào database với UUID và role gốc

---

## 📊 Phase 4: Level & Experience System

### 4.1 Level System
- [ ] **Level Manager**
  - `getLevel(Player, Role)`: Lấy level hiện tại của role
  - `addExperience(Player, Role, int)`: Thêm exp cho role
  - `getExperience(Player, Role)`: Lấy exp hiện tại
  - `getRequiredExp(int level)`: Tính exp cần để lên level tiếp theo
  - Max level: 999

- [ ] **Level Up Logic**
  - Khi đủ exp: tự động level up
  - Mỗi lần level up: thêm 1 skill point
  - Check và unlock title mới nếu có
  - Thông báo cho player

- [ ] **Experience Source**
  - Tích hợp với exp nhân vật hiện tại của player
  - Listener: `PlayerExpChangeEvent` hoặc tương tự
  - Convert exp nhân vật thành exp role (có thể config tỷ lệ)

---

## ⚔️ Phase 5: Skill System

### 5.1 Skill Model
- [ ] **Tạo Skill model**
  - Fields: String id, String name, Role role, int maxLevel (6)
  - Methods: `getDescription()`, `getLevelInfo(int level)`

- [ ] **Tạo SkillLevel model**
  - Fields: String skillId, int level, Map<String, Object> properties
  - Lưu thông tin: damage, cooldown, effects, etc.

- [ ] **Tạo PlayerSkill model**
  - Fields: UUID, String skillId, int currentLevel
  - Methods: `canUpgrade()`, `getUpgradeCost()`

### 5.2 Skill Manager
- [ ] **SkillManager**
  - `getSkills(Role)`: Lấy danh sách skill của role
  - `getPlayerSkill(Player, String skillId)`: Lấy skill level của player
  - `upgradeSkill(Player, String skillId)`: Upgrade skill (check points và cost)
  - `getSkillPoints(Player)`: Lấy số skill points hiện có
  - `getUpgradeCost(String skillId, int currentLevel)`: Lấy cost để upgrade

### 5.3 Skill Configuration
- [ ] **Config skill trong config.yml**
  - Định nghĩa cost để upgrade mỗi level
  - Format: `skills.upgrade_costs.1`, `skills.upgrade_costs.2`, etc.
  - Hoặc có thể config riêng cho từng skill

### 5.4 DPS Skill Implementation (Fireball Skill)
- [ ] **Tạo FireballSkill class**
  - Extends base Skill class hoặc implement Skill interface
  - Properties theo level:
    - Level 1: 5 cầu lửa, 10hp damage, 36 block range, 12s cooldown, burn effect
    - Level 2: 5 cầu lửa, 19hp damage, 36 block range, 11s cooldown, burn +5%
    - Level 3: 6 cầu lửa, 25hp damage, 36 block range, 10s cooldown, burn +10%
    - Level 4: 6 cầu lửa, 38hp damage, 36 block range, 9s cooldown, burn +15%
    - Level 5: 7 cầu lửa, 50hp damage, 36 block range, 8s cooldown, burn +20%
    - Level 6: 7 cầu lửa, 67hp damage, 36 block range, 8s cooldown, burn +25%

- [ ] **Skill Item System**
  - Tạo custom item cho skill (không thể drop, không thể đưa vào chest)
  - Item chỉ có thể có 1 trong inventory
  - Item không rơi ra khi chết
  - Item không thể bỏ ra khỏi inventory bằng cách thông thường
  - Item tự động được thêm vào inventory khi chọn role DPS
  - Item tự động được remove khi đổi role

- [ ] **Skill Execution**
  - Right-click item để sử dụng skill
  - Check cooldown trước khi sử dụng
  - Spawn 5-7 fireball particles sau lưng player
  - Bắn fireball về phía trước (theo hướng nhìn)
  - Tầm xa: 36 blocks
  - Damage theo level
  - Apply burn effect với duration tăng theo level
  - **Quan trọng**: Không gây damage cho teammate (check ClanCore TeamManager)

- [ ] **Cooldown System**
  - Lưu cooldown time cho mỗi player
  - Hiển thị trên actionbar khi đang cooldown
  - Hiển thị "Skill đã sẵn sàng" khi không cooldown

- [ ] **Team Protection**
  - Tích hợp với ClanCore TeamManager
  - Check `teamManager.sameTeam(attacker, target)` trước khi damage
  - Nếu cùng team: cancel damage event

---

## 🎨 Phase 6: GUI System

### 6.1 Role Selection GUI
- [ ] **Tạo RoleSelectGUI**
  - Hiển thị 3 role: TANKER, DPS, HEALER
  - Mỗi role có icon, mô tả
  - Click để chọn role
  - Nếu đã có role: hiển thị thông báo và chuyển sang RoleInfoGUI

### 6.2 Role Info GUI (Main GUI)
- [ ] **Tạo RoleInfoGUI**
  - Hiển thị thông tin role hiện tại
  - Hiển thị level, exp, skill points
  - Hiển thị danh hiệu đang active
  - Nút "Xem Skills" → mở SkillListGUI
  - Nút "Danh Hiệu" → mở TitleGUI
  - Nút "Đổi Role" → mở RoleChangeGUI (nếu có thể)
  - GUI đẹp, không ép quá nhiều text vào 1 item

### 6.3 Skill List GUI
- [ ] **Tạo SkillListGUI**
  - Hiển thị tất cả skill của role hiện tại
  - Mỗi skill item hiển thị:
    - Tên skill
    - Level hiện tại
    - Level tiếp theo cần X điểm
    - Hover để xem thông tin chi tiết
  - Click vào skill → mở SkillUpgradeGUI
  - Có nút "Quay lại" về RoleInfoGUI

### 6.4 Skill Upgrade GUI
- [ ] **Tạo SkillUpgradeGUI**
  - Hiển thị thông tin skill chi tiết
  - Hiển thị từng level và buff/damage tăng thêm
  - Hiển thị cost để upgrade
  - Nút "Upgrade" (nếu đủ điểm)
  - Nút "Quay lại" về SkillListGUI

### 6.5 Title GUI
- [ ] **Tạo TitleGUI**
  - Hiển thị tất cả danh hiệu đã sở hữu (từ tất cả role)
  - Danh hiệu đã unlock: có thể click để active
  - Danh hiệu chưa unlock: hiển thị mờ, không click được
  - Hiển thị level yêu cầu để unlock
  - Nút "Quay lại" về RoleInfoGUI

### 6.6 Role Change GUI
- [ ] **Tạo RoleChangeGUI**
  - Hiển thị thông tin đổi role
  - Hiển thị cooldown còn lại hoặc cost (10 coins)
  - Nút "Đổi ngay" (nếu đủ coins)
  - Nút "Đợi cooldown" (hiển thị thời gian còn lại)
  - Nút "Hủy" về RoleInfoGUI

### 6.7 GUI Utilities
- [ ] **Tạo GUIUtil class**
  - Helper methods để tạo items với lore
  - Helper methods để format text
  - Helper methods để tạo borders, fillers
  - Color codes và formatting

---

## 💬 Phase 7: Commands

### 7.1 Player Commands
- [ ] **RoleCommand**
  - `/role` - Mở RoleInfoGUI (nếu đã có role) hoặc RoleSelectGUI (nếu chưa có)
  - `/role select` - Mở RoleSelectGUI
  - `/role info` - Mở RoleInfoGUI
  - Tab completer cho subcommands

### 7.2 Admin Commands (Ẩn với player thường)
- [ ] **RoleAdminCommand**
  - `/roleadmin givelevel <player> <role> <level>` - Set level cho player
  - `/roleadmin giveskillpoints <player> <amount>` - Give skill points
  - `/roleadmin setrole <player> <role>` - Set role cho player (bypass cooldown)
  - `/roleadmin takeskill <player> <skillId>` - Remove skill item (nếu cần)
  - Permissions: `rolemmo.admin.*`
  - Tab completer

---

## 🔧 Phase 8: Integration & Listeners

### 8.1 LuckPerms Integration
- [ ] **Tạo LuckPermsManager**
  - Get LuckPerms API instance
  - `setPlayerRole(Player, Role)`: Set group trong LuckPerms
  - `removePlayerRole(Player)`: Remove group (nếu cần)
  - Handle khi LuckPerms không có sẵn (soft dependency)

### 8.2 MoneyPlugin Integration
- [ ] **Tạo MoneyPluginManager**
  - Get MoneyPlugin instance và CoinsManager
  - `hasEnoughCoins(Player, long)`: Check đủ coins
  - `removeCoins(Player, long)`: Trừ coins
  - Handle khi MoneyPlugin không có sẵn (soft dependency)

### 8.3 ClanCore Integration
- [ ] **Tạo ClanCoreManager**
  - Get ClanCore instance và TeamManager
  - `areSameTeam(Player, Player)`: Check cùng team
  - Sử dụng trong skill damage logic
  - Handle khi ClanCore không có sẵn (soft dependency)

### 8.4 Event Listeners
- [ ] **PlayerJoinListener**
  - Load player data từ database
  - Apply role, title, skill items
  - Set LuckPerms group

- [ ] **PlayerQuitListener**
  - Save player data vào database
  - Cleanup temporary data

- [ ] **ExpChangeListener**
  - Convert exp nhân vật thành exp role
  - Trigger level up nếu đủ

- [ ] **InventoryListener**
  - Prevent drop skill items
  - Prevent move skill items vào chest
  - Prevent duplicate skill items
  - Auto-remove skill items khi đổi role

- [ ] **ItemInteractListener**
  - Handle right-click skill items
  - Execute skill logic
  - Check cooldown

- [ ] **DamageListener**
  - Check team trước khi damage
  - Cancel damage nếu cùng team (ClanCore)

- [ ] **DeathListener**
  - Prevent skill items rơi ra khi chết
  - Hoặc tự động thêm lại vào inventory

---

## 📝 Phase 9: Actionbar & HUD

### 9.1 Actionbar Display
- [ ] **ActionbarManager**
  - Hiển thị skill cooldown trên actionbar
  - Format: "Skill đã sẵn sàng" hoặc "Cooldown: Xs"
  - Update mỗi tick hoặc mỗi giây

- [ ] **BossBar Display (Optional)**
  - Có thể dùng BossBar để hiển thị cooldown progress
  - Hoặc chỉ dùng actionbar

---

## 🗄️ Phase 10: Database Schema

### 10.1 Tables Design
- [ ] **role_players table**
  ```sql
  CREATE TABLE role_players (
    uuid TEXT PRIMARY KEY,
    current_role TEXT,
    tanker_level INTEGER DEFAULT 1,
    tanker_exp INTEGER DEFAULT 0,
    dps_level INTEGER DEFAULT 1,
    dps_exp INTEGER DEFAULT 0,
    healer_level INTEGER DEFAULT 1,
    healer_exp INTEGER DEFAULT 0,
    skill_points INTEGER DEFAULT 0,
    last_role_change BIGINT DEFAULT 0
  );
  ```

- [ ] **role_skills table**
  ```sql
  CREATE TABLE role_skills (
    uuid TEXT,
    skill_id TEXT,
    level INTEGER DEFAULT 0,
    PRIMARY KEY (uuid, skill_id)
  );
  ```

- [ ] **role_titles table**
  ```sql
  CREATE TABLE role_titles (
    uuid TEXT,
    title_id TEXT,
    role TEXT,
    unlocked_at BIGINT,
    PRIMARY KEY (uuid, title_id)
  );
  ```

- [ ] **role_active_title table**
  ```sql
  CREATE TABLE role_active_title (
    uuid TEXT PRIMARY KEY,
    title_id TEXT
  );
  ```

---

## 🧪 Phase 11: Testing & Polish

### 11.1 Testing
- [ ] Test role selection và change
- [ ] Test level up và title unlock
- [ ] Test skill upgrade system
- [ ] Test skill execution (fireball)
- [ ] Test team protection
- [ ] Test cooldown system
- [ ] Test item protection (không drop, không move)
- [ ] Test database persistence
- [ ] Test integration với LuckPerms, MoneyPlugin, ClanCore
- [ ] Test edge cases (player offline, server restart, etc.)

### 11.2 Polish
- [ ] Optimize database queries
- [ ] Add error handling
- [ ] Add logging
- [ ] Optimize GUI performance
- [ ] Add message localization (nếu cần)
- [ ] Code cleanup và documentation

---

## 📦 Phase 12: Deployment

### 12.1 Build & Package
- [ ] Build plugin với Maven
- [ ] Test trên server thực tế
- [ ] Tạo LuckPerms groups config
- [ ] Tạo documentation cho admin

### 12.2 LuckPerms Setup
- [ ] Tạo các groups trong LuckPerms:
  - `tanker`
  - `dps`
  - `healer`
- [ ] Set permissions cho các groups
- [ ] Export config để admin có thể import

---

## 🔗 Dependencies & Integration Points

### External Dependencies
1. **LuckPerms API (v5.4)**
   - Soft dependency
   - Sử dụng để set rank khi chọn role
   - API: `LuckPermsProvider.get()`

2. **MoneyPlugin**
   - Soft dependency
   - Sử dụng để trừ coins khi đổi role
   - API: `CoinsManager` từ MoneyPlugin instance

3. **ClanCore**
   - Soft dependency
   - Sử dụng để check team trước khi damage
   - API: `TeamManager.sameTeam(Player, Player)`

### Internal Dependencies
- Paper/Spigot 1.21.4+
- Java 21
- SQLite (embedded)

---

## 📌 Notes & Considerations

1. **Performance**
   - Database queries nên async khi có thể
   - Cache player data trong memory
   - Update database định kỳ hoặc khi player quit

2. **Security**
   - Validate tất cả inputs
   - Prevent SQL injection
   - Check permissions cho admin commands

3. **User Experience**
   - GUI phải đẹp, không ép text quá nhiều
   - Messages rõ ràng, dễ hiểu
   - Cooldown hiển thị rõ ràng

4. **Extensibility**
   - Skill system phải dễ mở rộng
   - Config-driven để dễ thêm skill mới
   - Title system linh hoạt

5. **Data Migration**
   - Có thể cần migration script nếu thay đổi schema
   - Backup database trước khi update

---

## ✅ Priority Order

1. **High Priority** (Core functionality)
   - Phase 1: Setup & Foundation
   - Phase 2: Core Role System
   - Phase 3: Title System
   - Phase 4: Level & Experience System
   - Phase 6: GUI System (basic)

2. **Medium Priority** (Features)
   - Phase 5: Skill System
   - Phase 7: Commands
   - Phase 8: Integration & Listeners

3. **Low Priority** (Polish)
   - Phase 9: Actionbar & HUD
   - Phase 11: Testing & Polish
   - Phase 12: Deployment

---

## 📚 Additional Resources

- Paper API Documentation: https://docs.papermc.io/
- LuckPerms API: https://luckperms.net/wiki/Developer-API
- SQLite Documentation: https://www.sqlite.org/docs.html
- ItemStack API: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/inventory/ItemStack.html

---

**Last Updated**: [Date]
**Version**: 1.0-SNAPSHOT
**Author**: medu1122
