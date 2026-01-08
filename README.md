# 🔥 AddonBuu - Meteor Client Addon
### Made with ❤️ by MajinBuu2k4 (Mai Cồ)

![Icon](src/main/resources/assets/addonbuu/icon.png)

**AddonBuu** là một bản mở rộng (addon) "hàng thửa" dành riêng cho **Meteor Client** (Minecraft 1.21.4), được thiết kế tối ưu hóa tận răng cho server **GrassMine** và các server sinh tồn khác.

> *"Auto từ A đến Á - Treo máy, săn boss, cày bí cảnh không lo bị cook!"*

---

## 📥 Cài đặt (Installation)

1.  **Yêu cầu bắt buộc:**
    * **Minecraft:** 1.21.4
    * **Fabric Loader:** Phiên bản mới nhất.
    * **Meteor Client:** Bản hỗ trợ 1.21.4 (Dev build hoặc Release).
    * **Baritone:** (Bắt buộc) Để chạy các tính năng tự tìm đường.

2.  **Cách cài:**
    * Tải file `.jar` của AddonBuu (Release).
    * Ném vào thư mục `%appdata%/.minecraft/mods`.
    * Vào game, bật Meteor menu (thường là phím `Right Shift`) và quẩy thôi!

---

## 🧩 Danh sách Modules (Tính năng)

Addon chia làm các Category chính để anh em dễ tìm kiếm: **LuckyVN**, **AddonBuu**, **ClickSlotCustom**.

### 🍀 Category: LuckyVN (Chuyên dụng cho Server)

#### 1. 🏯 Treo Phó Bản (`TreoPhoBan`)
Module cày cuốc "trấn phái" của addon. Tự động đi phó bản không cần não.
* **Chức năng:**
    * Tự Warp đến `Linh Thú Viên`.
    * Tự chạy vào cổng dịch chuyển.
    * **Auto GUI:** Tự chọn khu vực (Huyễn Ảnh, Thí Luyện...) và Slot phó bản theo cấu hình.
    * **Auto Cất đồ:** Tự chat `/tucatdo` khi vào game (tránh full rương).
    * **Farm Mode:**
        * `Baritone`: Tự tìm đường đến tọa độ định sẵn.
        * `WASD`: Chạy theo kịch bản đi bộ (VD: đi thẳng 3s, rẽ trái 2s...).
* **Cách dùng:** Chọn lại `Khu Vực` và `Phó Bản` trong setting module trước khi bật.

#### 2. 💊 Auto Enable Đan Dược (`AutoEnableDanDuoc`)
Dành cho mấy ông hay quên bật đan dược.
* **Cơ chế:** Tự động soi vào **Slot 19** trong GUI đan dược.
* **Logic:** Đọc Lore item, nếu thấy chữ "trạng thái: tắt" -> Tự click để BẬT lên ngay lập tức.
* **Lưu ý:** Phải mở GUI đan dược lên nó mới hoạt động nhé.

#### 3. 📦 Tự Cất Đồ (`TuCatDo`)
Không bao giờ lo rác rương.
* **Cơ chế:** "Nghe lén" gói tin từ server.
* **Logic:** Nếu server báo dòng "Tự động cất vật phẩm... [TẮT ❌]" -> Module sẽ tự động gửi lệnh `/tucatdo` để bật lại ngay.
* **Delay:** Có delay 10s để tránh spam lệnh bị server kick.

#### 4. 🚀 Auto Warp & Script (`AutoWarp`)
Tool hỗ trợ chạy kịch bản đa năng.
* **Tính năng:**
    1.  **Check tọa độ:** Đứng đúng vị trí XYZ cài sẵn mới chạy.
    2.  **Gửi lệnh:** Tự chat `/warp xxx` hoặc `/mine xxx`.
    3.  **Smart Jump:** Tự động nhảy thông minh khi gặp vật cản (Logic nhìn trước 1.5 block).
    4.  **Script WASD:** Sau khi warp xong sẽ chạy script đi bộ để vào bãi farm.

#### 5. 📝 Save Log Chế Tạo (`SaveLogCheTao`)
Dành cho dân cày đồ, thích khoe thành tích.
* **Chức năng:** Tự động lưu lại các dòng thông báo chế tạo thành công vào file log riêng.
* **Bộ lọc:** Chỉ lưu những item có từ khóa xịn (VD: `Linh Khí ⭐`, `Thần Binh`).
* **File lưu:** Nằm trong `.minecraft/addonbuu/log_chetao/`.

---

### 🖱️ Category: ClickSlotCustom & AddonBuu

#### 1. 📋 Copy Data Component (`CopyDataComp`)
Công cụ hỗ trợ lấy ID vật phẩm siêu nhanh.
* **Cách dùng:** Bật module -> Mở GUI -> Click chuột trái vào item bất kỳ.
* **Kết quả:** Nó sẽ copy chuỗi định dạng `slot_id:{component_data}` vào Clipboard.
* **Mục đích:** Dùng chuỗi này dán vào setting của module **AutoClickCustom**.

#### 2. 🎯 Auto Click Custom (`AutoClickCustom`)
Tự động nhặt đồ/mua đồ theo chỉ định.
* **Cách dùng:** Paste chuỗi data lấy được từ `CopyDataComp` vào phần `Danh sách target`.
* **Logic:** Khi mở GUI, nó quét toàn bộ slot. Nếu thấy item nào khớp Component -> Tự click mua/lấy ngay lập tức.

#### 3. 🙈 Ẩn Log (`AnLog`)
* Bật lên để ẩn toàn bộ thông báo nổi (HUD Notification) của AddonBuu cho đỡ rối mắt khi PvP.

---

## 🖥️ Hệ thống Commands (Lệnh)

Gõ các lệnh này vào khung chat (dấu chấm `.` là prefix của Meteor):

* `.anlog`: Tắt thông báo nổi trên màn hình.
* `.hienlog`: Bật lại thông báo nổi.
* `.component hien`: Hiển thị chi tiết NBT/Component của item khi chỉ chuột vào (Soi đồ pro).
* `.component an`: Tắt hiển thị component.
* `.guititle`: Bật/Tắt tính năng tự động copy tiêu đề GUI khi mở (Tiện để lấy tên menu).
* `.copy on`: Bật chế độ Click-to-Copy component (giống module `CopyDataComp` nhưng dùng lệnh).
* `.copy off`: Tắt chế độ Click-to-Copy.

---

## 🔧 Mixins & Tính năng ẩn (Passive)

Đây là những tính năng chạy ngầm, hỗ trợ cực mạnh:

1.  **Tooltip Slot ID:**
    * Di chuột vào bất cứ item nào trong rương, bạn sẽ thấy dòng `§aslot số X`.
    * Giúp bạn biết chính xác số slot để cài đặt cho `AutoClick` hoặc `TreoPhoBan`.

2.  **GUI Title Copier:**
    * Khi mở một GUI bất kỳ (Rương, Menu), addon sẽ tự động Copy tên của GUI đó vào Clipboard.

3.  **Smart Logs:**
    * Hệ thống log thông minh, tự động lưu file log theo ngày (`.minecraft/addonbuu/log/`).
    * Tự kẻ vạch phân chia khi bước sang ngày mới (00:00) cho anh em treo máy xuyên đêm dễ check.

---

## 🎨 Hướng dẫn HUD Notification

AddonBuu sử dụng hệ thống thông báo riêng, không spam kênh chat.

* **Thông báo nổi:** Hiện giữa màn hình, trôi lên trên.
* **Lịch sử (History Log):**
    * Nhấn `Ctrl` + `Shift` + `Mũi tên Phải (▶)`: Để hiện/ẩn bảng lịch sử log bên góc trái.
    * Nhấn `Ctrl` + `Shift` + `Delete`: Xóa sạch lịch sử log.

---

## 📝 Hướng dẫn viết Script WASD

Trong các module như `TreoPhoBan` hay `AutoWarp`, phần Script WASD viết như sau:

* **Cú pháp:** `hướng thời_gian`
* **Các hướng:** `up` (đi tới), `down` (lùi), `left` (trái), `right` (phải), `delay` (đứng im).
* **Đơn vị:** `s` (giây).

**Ví dụ một script đi từ cổng dịch chuyển vào bãi quái:**
```text
up 3.5s    <- Đi thẳng 3.5 giây
left 1s    <- Rẽ trái 1 giây
up 2s      <- Đi thẳng tiếp 2 giây
delay 1s   <- Nghỉ 1 giây cho đỡ lag
right 0.5s <- Nhích phải tí xíu
---

### 📞 Liên hệ & Support
* **Github:** [Maico/addonbuu](https://github.com/Maico/addonbuu)
* **Tác giả:** MajinBuu2k4 (Mai Cồ)
* **Donate:** *Gửi vài cái bánh mì là vui rồi :v*
