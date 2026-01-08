# 🔥 AddonBuu - Meteor Client Addon
### Made with ❤️ by MajinBuu2k4 (Mai Cồ)

![Icon](src/main/resources/assets/addonbuu/icon.png)

**AddonBuu** là một bản mở rộng (addon) "hàng thửa" dành riêng cho **Meteor Client** (Minecraft 1.21.4), được thiết kế tối ưu cho server **GrassMine** và các server sinh tồn khác.

> *"Va chạm cực mạnh, săn lùng kẻ thù, treo máy farm đồ - Tất cả trong một!"*

---

## 📥 Cài đặt (Installation)

1.  **Yêu cầu:**
    * Minecraft **1.21.4**
    * Fabric Loader
    * [Meteor Client](https://meteorclient.com/) (Phiên bản mới nhất hỗ trợ 1.21.4)
    * [Baritone](https://github.com/cabalitta/baritone) (Bắt buộc để chạy các module tự động di chuyển)

2.  **Cách cài:**
    * Tải file `.jar` của AddonBuu (Build từ source hoặc tải release).
    * Ném vào thư mục `.minecraft/mods`.
    * Vào game và tận hưởng!

---

## 🛠️ Tính năng chính (Modules)

Addon chia làm 2 Category chính: **GrassMine** (Dành cho server) và **AddonBuu** (Tiện ích chung).

### 🌾 Category: GrassMine

#### 1. ⚔️ Truy Sát Pro (`TruySatModule`)
Hệ thống săn người tự động sử dụng Baritone.
* **Chức năng:** Tự động đi tuần tra theo script, khi phát hiện kẻ thù trong **Blacklist** sẽ lao vào "múc" ngay lập tức.
* **States:**
    * `PATROLLING`: Đi tuần theo tọa độ cài sẵn.
    * `HUNTING`: Phát hiện mục tiêu -> Dí theo.
    * `RECOVERING`: Tự động hồi phục/quay lại sau khi chết.
* **Lưu ý:** Cần cài đặt `Script` (các lệnh warp/goto) và `Blacklist` (tên kẻ thù) trong setting.

#### 2. 🛡️ Buu Aura (`BuuAura`)
KillAura phiên bản nâng cấp, thông minh hơn.
* **Targeting:** Ưu tiên đánh người trong **Blacklist**.
* **Anti-Bot:** Tự động bỏ qua các thực thể có tên bắt đầu bằng `CIT-` (Bot chống hack của server).
* **Thông minh:**
    * Tự động đổi vũ khí (Auto Switch) sang Rìu nếu đối thủ dùng Khiên.
    * Chỉ đánh khi cầm vũ khí (tránh tay không đấm đá).
    * Tạm dừng Baritone khi đang va chạm để tránh lỗi di chuyển.

#### 3. 🏯 Treo Phó Bản (`TreoPhoBan`)
Module cày cuốc tự động xịn nhất hệ mặt trời.
* **Hỗ trợ:** Huyễn Ảnh Bí Cảnh, Thí Luyện Đạo Tràng, Thiên Uyên Cấm Địa...
* **Tính năng:**
    * Tự động Warp, tự nhảy vào cổng.
    * Tự chọn Slot phó bản trong GUI (Config được slot).
    * **Auto Cất Đồ:** Tự chat `/tucatdo` khi vào.
    * **Mode Di Chuyển:** Hỗ trợ cả Baritone (tự tìm đường) và WASD (Script đi bộ: `up 3s`, `left 2s`...).

#### 4. 🔄 Auto Return Multi (`AutoReturnMulti`)
Tự động quay lại điểm farm và chạy kịch bản phức tạp.
* Thích hợp cho việc farm ở nhiều điểm khác nhau (Multi-target).
* Hỗ trợ vòng lặp (Loop) kịch bản vô tận.

#### 5. 📍 Auto Return GrassMine (`AutoReturnGrassMine`)
Phiên bản đơn giản hơn của Auto Return.
* Tự Warp -> Đợi load map (có thanh kéo delay) -> Dùng Baritone chạy đến tọa độ cố định.
* Có check Lobby (nếu bị văng ra Lobby sẽ tự warp lại).

#### 6. 🔑 Auto Login Grass (`AutoLoginGrass`)
Tự động đăng nhập và chọn server.
* Tự điền mật khẩu `/login`.
* Tự click Nether Star/Compass để mở menu server.
* Tự chọn chế độ chơi (Click slot).
* **Fix lỗi:** Tự phát hiện kẹt ở Spawn hoặc lỗi kết nối để login lại.

#### 7. ⚖️ Auto Ân Xá (`AutoAnXa`)
Dành cho mấy ông hay đi tù.
* Tự động phát hiện khi nhân vật bị tele vào nhà tù.
* Tự chat `/anxa` và click GUI để ra tù ngay lập tức (cần có lệnh bài/tiền nhé).
* Có thống kê lịch sử số lần ra tù.

---

### 📦 Category: AddonBuu

#### 1. 🧪 Test Log Module
* Module dùng để test hệ thống thông báo HUD mới. Bật lên để xem màu mè hoa lá cành.

---

## 🎨 Hệ thống HUD & Notification

AddonBuu không sử dụng Chat spam kênh chat, mà sử dụng hệ thống **HUD Notification** riêng biệt cực đẹp.

* **Thông báo nổi:** Hiện giữa màn hình và trôi dần lên.
* **Lịch sử thông báo (History Log):** Lưu lại các hoạt động của tool.

**Phím tắt (Shortcuts):**
* `Ctrl` + `Shift` + `Mũi tên Phải (▶)`: Bật/Tắt bảng lịch sử log (cho đỡ chướng mắt).
* `Ctrl` + `Shift` + `Delete`: Xóa sạch lịch sử log.

---

## 🔧 Mixins (Các tính năng ẩn)

1.  **Anti-CIT Bot (KillAuraMixin):**
    * Can thiệp sâu vào KillAura của Meteor. Nếu thực thể có tên bắt đầu bằng `CIT-`, Aura sẽ **TUYỆT ĐỐI KHÔNG ĐÁNH**. Giúp tránh bị ban acc oan uổng.

2.  **Tooltip Slot ID (TooltipSlotMixin):**
    * Khi di chuột vào vật phẩm trong rương/túi đồ, nó sẽ hiện thêm dòng: `§aslot số X`.
    * **Tác dụng:** Giúp bạn biết số slot để cài đặt cho module **Treo Phó Bản** hoặc **Auto Login** mà không cần đếm tay.

3.  **Startup Log:**
    * Khi game khởi động xong sẽ hiện log chào mừng của Mai Cồ.

---

## 💻 Commands (Lệnh)

* `.example`: Lệnh test cơ bản.

---

## ⚠️ Lưu ý

* Script di chuyển của **Treo Phó Bản** (Mode WASD) có dạng: `hướng thời_gian`.
    * Ví dụ: `up 3s` (đi thẳng 3 giây), `down 1.5s` (lùi 1.5 giây), `left 1s`, `right 1s`.
* Để sử dụng tính năng **Auto Return**, hãy chắc chắn bạn đã cài đặt điểm warp (`/setwarp mine`) trong server.

---

### 📞 Liên hệ & Support
* **Github:** [Maico/addonbuu](https://github.com/Maico/addonbuu)
* **Tác giả:** MajinBuu2k4 (Mai Cồ)
* **Donate:** *Gửi vài cái bánh mì là vui rồi :v*
