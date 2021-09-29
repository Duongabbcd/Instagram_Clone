# Instagram_Clone

BÁO CÁO DỰ ÁN INSTAGRAM CLONE :
Ứng dụng sử dụng API Firbase và các thư viện trên github
Các chức năng được xây dựng trong từng giao diện :


STARTACTIVITY : Cho phép user lựa chọn đăng nhập hay đăng ký


REGISTERACTIVITY : Cho phép user đăng ký / tạo tài khoản


LOGINACTIVITY : Cho phép user đăng nhập vào hệ thống


MAINACTIVITY : Sử dụng BOTTOM NAVIGATION trong đó :
1)Home Fragment :Hiện Story và Post của user và những người user theo dõi .

2)Click vào Post có chức năng Report .Đối với chủ bài viết có thêm quyền edit title và delete bài viết .

3)Mỗi Post trong RecycleView có khả năng chuyển hướng hiện số like ,người đã like ,số bình luận , imageView bình luận cho phép chuyển sang COMMENTACTIVITY , click vào ảnh đại diện,ảnh bài viết ,tên username của người đăng cho phép chuyển sang Profile Fragment .

4)Tạo thông báo về người đã comment,người đã like bằng cách chuyển Data sang NotificationFragment.Cho phép user save bài viết có ở Home Fragment về danh sách Saved ở trong ProfileFragment .

5)Story cho phép hiện các Story được user đăng lên nếu vẫn còn trong khoảng thòi gian 1 ngày tính từ thời điểm đăng . Người đăng có quyền check số người xem và delete Story .

6)Nếu User trong phiên đăng nhập trên thiết bị chưa đăng Story hoặc Story đã bị biến mất khỏi HomeFragment cho phép intent sang ADDSTORYACTIVITY .Hoặc nếu có hơn 1 Story tạo hộp thoại với 2 lựa chọn xem Story hoặc tạo thêm .

7)Đặt chế độ đảo bài viết ,bài viết mới nhất ở trên cùng ,cũ nhất ở dưới cùng .

SearchFragment : Hiện tất cả các user và các hashtag ,cho phép tìm kiếm người dùng và hashtag có trong các bài viết trên thanh search bar .

1)Mặc định nút Follow của chính tài khoản của user đó ở trạng tháI GONE ,các user khác ở trạng thái FOLLOW ,click lần 1 chuyển sang FOLLOWING và add user đó vào danh sách các user được user hiện tại theo dõi ,lần 2 quay lại trạng thái FOLLOW,và xóa user vừa add khỏi danh sách trên .

2)Sau khi nút chuyển sang FOLLOWING ngay lập tức tạo thông báo về người theo dõi cho user có nút FOLLOWING đó ,và chuyển vào NotificationFragmnet của người đó và chuyển data vào FOLLOWERSACTIVITY.  


POSTACTIVITY : Sử dụng để đăng bài viết kèm title và ảnh được chụp hoặc lấy từ thư viện .

NotificationFragment : (Implicit Fragment )

1)Cho phép nhận data từ các Fragment phía trên để hiện thông báo ,sử dụng Recycle View hiện danh sách thông báo .

2)Có 3 loại thông báo : Thông báo về Comment ,về người like và người theo dõi .
ProfileFragment :Đối với User đang trong phiên đăng nhập trên thiết bị ,cho phép click vào Button Edit Profile để intent sang EDITPROFILE ACTIVITY ,tại đây User có thể update tiểu sử (bio) ,username ,fullname ,và avatar .

1)Cho phép mọi tài khoản có thể nhìn thấy số người theo dõi ,số người được mình thep dõi ,show những tài khoản đó ra bởi các hàm làm việc trong FOLLOWERSACTIVITY.

2)Có Toolbar nơi chứa tên của tài khoản đó ,getString từ PostAdapter của có trong HomeFragment , 1 nút intent sang OPTIONACTIVITY nơi chứa chức năng Log out và 1 tab Setting(để làm màu thôi ! ).

3)1 Relative Layout cho phép để đè 2 RecycleView gồm các bài viết do user đã viết và 1 list các bài viết được Save từ bất kỳ bài viết nào mà User đó nhìn thấy .


STORYACTIVITY :Cho phép user thực hiện chức năng hiện thông tin người đăng (Avatar và username) ,stop,resume 1 story,cho phép skip để nhảy sang story tiếp theo hoặc reverse để quay lại story trước .


1)Đối với người đăng Story cho phép xem số người đã xem Story bằng hàm tính toán trong FOLLOWERSACTIVITY và xóa Story đó ,set up thời gian xem mỗi story là 5 giây (5000L) .
ADDSTORYACTIVITY :Thực hiện chức năng tạo Story .
