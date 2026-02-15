đây là plugin chia role vai trò để quyết định lối chơi của player 
sẽ có 3 role chính là TANKER , DPS , HEALER
khi chọn xong server sẽ set role cho người chơi đó (lưu database) để không bị mất , và chỉ được chuyển role khi chờ đủ 1 ngày hoặc tiêu 10 coins từ plugin moneyPlugin để đổi ngay lập tực
sau khi chọn thì sẽ set rank người đó là role mà họ chọn 
config phần rank này giúp t trong luckperms , t lấy folder này bỏ vào server luôn 
ngoài ra có bổ sung thêm kho danh hiệu , cho người dùng chọn và sử dụng danh hiệu họ đang sở hữu tuỳ ý . ở roleplugin này có level cho từng role và player có thể upgade role của họ bằng cách tiêu thụ exp nhân vật hiện tại , các danh hiệu được tặng sẽ như sau (lưu ý max cấp 1 role là 999)
TANK – Hệ Hộ Thể / Kim Cang
Level	Danh hiệu
1 TANKER
50	Luyện Thể Sơ Kỳ
120	Thiết Giáp Cảnh
340	Kim Cang Hộ Thể
570	Huyền Giáp Tông Sư
690	Thánh Giáp Chiến Tôn
860	Bất Diệt Kim Thân
990	Vạn Cổ Hộ Đạo
⚔ DPS – Hệ Sát Phạt / Chiến Đạo
Level	Danh hiệu
1 DPS
50	Sát Khí Sơ Thành
120	Huyết Chiến Giả
340	Tu La Đao Tôn
570	Chiến Vương
690	Ma Diệt Chi Chủ
860	Thí Thiên Chiến Thánh
990	Vạn Kiếp Sát Thần
✝ HEALER – Hệ Linh Lực / Thánh Đạo
Level	Danh hiệu
1 HEALER
50	Linh Y Sơ Cảnh
120	Thanh Tâm Hộ Pháp
340	Thánh Linh Sứ
570	Huyền Thiên Trị Giả
690	Thiên Đạo Hộ Mệnh
860	Thánh Quang Đại Tôn
990	Vạn Linh Chi Chủ
họ được giữ các dánh hiệu này mãi mài và có thể tuỳ ý sử dụng .
ngoài ra việc đổi role khác để chơi cũng không ảnh hưởng và làm mất đi danh hiệu của họ 
mỗi khi họ up cấp 1 role sẽ cho họ 1 điểm up skill (điểm up skill này sử dụng để up toàn bộ skill có trong plugin này 
skill sẽ có 6 level để nâng 1 level thì yêu cầu cần ... số điểm / phần này sẽ do dev config trong file config
về hệ thống skill , khi chọn role (/role select)  xong sẽ nhảy tới giao diện chính gọi là role info (/role info) , có 1 nút trỏ tới phần giao diện bảng tất cả các skill của role đó ( cứ chừa chỗ ra và dev sẽ congfig từng skill sau) player di chuột vào họ sẽ thấy rõ thông tin lên skill, thông tin skill , level hiện tại level tiếp theo cần ... điểm , thêm 1 dòng cuối nhấn để xem chi tiết , bấm vào sẽ hiện ra giao diện upgrade skill , cho player thấy rõ từng bật sẽ cộng thêm gì buff thêm % gì ..
hiện tại làm trước 1 skill cho hệ dps cho t , 1 skill hiện 5 cầu lửa sau lưng và bắn vào mục tiêu phía trước , tầm xa 36 block dame thì 10hp và có gây hiệu ứng đốt các level tiếp theo bao gồm /19hp/25+1cục cầu lửa/38/50+1 cục cầu lửa/ 67
st đốt tăng dần theo 5% mỗi lần nâng cấp 
cooldown 12s/11s/10s/9s/8s

khi player chọn sử dụng skill này hệ thống sẽ trao 1 vật phẩm item này sử dụng chuột phải để sử dụng skill và ko để bị vứt chết ko rơi ra dính liền với player từ đầu tới cuối , ko thể bỏ ra khỏi túi đồ và đẩy vào chest ,chỉ nhận dc số lượng là 1 trong túi đồ và ko vượt quá 1 (nói chung tính hết cả trường hợp phá phách của người dùng giúpt ) . tiếp đến là trên hub của người dùng luôn hiện dòng chữ nhỏ bên dưới skill đã sẵn sàng , nhưng nếu họ sử dụng thì nó sẽ chuyển sang đếm cooldown ,để ý skill không được gây thương tích với người cùng team trONG clanCore . 

lưu ý mọi thứ đều thao tác trên 1 giao diện , yêu cầu giao diện phải bắt mắt hạn chế ép text vào 1 item quá nhiều 
các command thì sẽ là một vài command mở giao diện lên ko dc thao tác nhiều trong thanh chat 

về các câu lệnh của admin (nên ẩn với player thường ko cho họ thấy)
bao gồm give level cho player , take skill (nhập id hoặc tên để lấy) trao role cho ai đó , give điểm skill ,




