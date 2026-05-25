package main

import (
	"fmt"
	"time"

	"github.com/lib/pq"
	"github.com/zplus/lichso/internal/models"
	"github.com/zplus/lichso/internal/utils"
	"gorm.io/gorm"
)

// seedContentData runs all content-related seeders.
func seedContentData(db *gorm.DB) {
	fmt.Println("\n🌱 Seeding Content Data...")
	seedArticleCategories(db)
	seedQuotes(db)
	seedFamousPeople(db)
	seedEvents(db)
	seedFolkFestivals(db)
	seedContentPermissions(db)
	seedAllArticles(db)
}

// seedArticleCategories creates default article categories.
func seedArticleCategories(db *gorm.DB) {
	fmt.Println("\n📂 Seeding Article Categories...")

	categories := []models.ArticleCategory{
		{Name: "Lịch sử", Slug: "lich-su", Description: "Bài viết về lịch sử Việt Nam và thế giới", Icon: "📜", SortOrder: 1, IsActive: true},
		{Name: "Văn hóa", Slug: "van-hoa", Description: "Bài viết về văn hóa, phong tục tập quán", Icon: "🎭", SortOrder: 2, IsActive: true},
		{Name: "Phong thủy", Slug: "phong-thuy", Description: "Kiến thức phong thủy, ngày tốt xấu", Icon: "🧭", SortOrder: 3, IsActive: true},
		{Name: "Lễ hội", Slug: "le-hoi", Description: "Tìm hiểu các lễ hội truyền thống", Icon: "🏮", SortOrder: 4, IsActive: true},
		{Name: "Nhân vật lịch sử", Slug: "nhan-vat-lich-su", Description: "Tiểu sử các nhân vật lịch sử nổi tiếng", Icon: "👤", SortOrder: 5, IsActive: true},
		{Name: "Âm lịch", Slug: "am-lich", Description: "Kiến thức về lịch âm, tiết khí", Icon: "🌙", SortOrder: 6, IsActive: true},
		{Name: "Tử vi", Slug: "tu-vi", Description: "Tử vi, cung hoàng đạo, bói toán", Icon: "⭐", SortOrder: 7, IsActive: true},
		{Name: "Tin tức", Slug: "tin-tuc", Description: "Tin tức mới nhất liên quan", Icon: "📰", SortOrder: 8, IsActive: true},
	}

	for _, cat := range categories {
		var existing models.ArticleCategory
		if db.Where("slug = ?", cat.Slug).First(&existing).Error == nil {
			fmt.Printf("   ⏭️  Category '%s' already exists, skipping\n", cat.Name)
			continue
		}
		if err := db.Create(&cat).Error; err != nil {
			fmt.Printf("   ❌ Failed to create category '%s': %v\n", cat.Name, err)
			continue
		}
		fmt.Printf("   ✅ Created category: %s %s\n", cat.Icon, cat.Name)
	}
}

// seedQuotes creates initial quotes collection.
func seedQuotes(db *gorm.DB) {
	fmt.Println("\n💬 Seeding Quotes...")

	intPtr := func(i int) *int { return &i }

	quotes := []models.Quote{
		// Vietnamese quotes
		{Quote: "Không thầy đố mày làm nên.", Author: "Tục ngữ Việt Nam", OriginalLanguage: "vi", Tags: pq.StringArray{"giáo dục", "truyền thống"}, DayOfYear: intPtr(1), IsActive: true},
		{Quote: "Học, học nữa, học mãi.", Author: "Vladimir Lenin", OriginalQuote: "Учиться, учиться и учиться", OriginalLanguage: "ru", AuthorNationality: "Nga", AuthorBirthYear: intPtr(1870), AuthorDeathYear: intPtr(1924), Tags: pq.StringArray{"giáo dục", "nỗ lực"}, DayOfYear: intPtr(2), IsActive: true},
		{Quote: "Một cây làm chẳng nên non, ba cây chụm lại nên hòn núi cao.", Author: "Tục ngữ Việt Nam", OriginalLanguage: "vi", Tags: pq.StringArray{"đoàn kết", "truyền thống"}, DayOfYear: intPtr(3), IsActive: true},
		{Quote: "Dân ta phải biết sử ta, cho tường gốc tích nước nhà Việt Nam.", Author: "Hồ Chí Minh", OriginalLanguage: "vi", AuthorNationality: "Việt Nam", AuthorBirthYear: intPtr(1890), AuthorDeathYear: intPtr(1969), Tags: pq.StringArray{"lịch sử", "yêu nước"}, DayOfYear: intPtr(4), IsActive: true},
		{Quote: "Không có việc gì khó, chỉ sợ lòng không bền. Đào núi và lấp biển, quyết chí ắt làm nên.", Author: "Hồ Chí Minh", OriginalLanguage: "vi", AuthorNationality: "Việt Nam", AuthorBirthYear: intPtr(1890), AuthorDeathYear: intPtr(1969), Tags: pq.StringArray{"ý chí", "nỗ lực"}, DayOfYear: intPtr(5), IsActive: true},
		{Quote: "Đường đi khó không khó vì ngăn sông cách núi mà khó vì lòng người ngại núi e sông.", Author: "Nguyễn Bá Học", OriginalLanguage: "vi", AuthorNationality: "Việt Nam", Tags: pq.StringArray{"ý chí", "quyết tâm"}, DayOfYear: intPtr(6), IsActive: true},
		{Quote: "Muốn sang thì bắc cầu kiều, muốn con hay chữ thì yêu lấy thầy.", Author: "Ca dao Việt Nam", OriginalLanguage: "vi", Tags: pq.StringArray{"giáo dục", "truyền thống"}, DayOfYear: intPtr(7), IsActive: true},
		{Quote: "Trăm hay không bằng tay quen.", Author: "Tục ngữ Việt Nam", OriginalLanguage: "vi", Tags: pq.StringArray{"thực hành", "kinh nghiệm"}, DayOfYear: intPtr(8), IsActive: true},
		{Quote: "Thất bại là mẹ thành công.", Author: "Tục ngữ Việt Nam", OriginalLanguage: "vi", Tags: pq.StringArray{"thành công", "nỗ lực"}, DayOfYear: intPtr(9), IsActive: true},
		{Quote: "Có công mài sắt, có ngày nên kim.", Author: "Tục ngữ Việt Nam", OriginalLanguage: "vi", Tags: pq.StringArray{"kiên trì", "nỗ lực"}, DayOfYear: intPtr(10), IsActive: true},

		// International quotes (Vietnamese translated)
		{Quote: "Cuộc sống là những gì xảy ra khi bạn đang bận lập kế hoạch khác.", Author: "John Lennon", OriginalQuote: "Life is what happens when you're busy making other plans.", OriginalLanguage: "en", AuthorNationality: "Anh", AuthorBirthYear: intPtr(1940), AuthorDeathYear: intPtr(1980), Tags: pq.StringArray{"cuộc sống", "triết lý"}, DayOfYear: intPtr(11), IsActive: true},
		{Quote: "Cách duy nhất để làm công việc tuyệt vời là yêu những gì bạn làm.", Author: "Steve Jobs", OriginalQuote: "The only way to do great work is to love what you do.", OriginalLanguage: "en", AuthorNationality: "Mỹ", AuthorBirthYear: intPtr(1955), AuthorDeathYear: intPtr(2011), Tags: pq.StringArray{"đam mê", "sự nghiệp"}, DayOfYear: intPtr(12), IsActive: true},
		{Quote: "Tôi nghĩ, vậy nên tôi tồn tại.", Author: "René Descartes", OriginalQuote: "Cogito, ergo sum.", OriginalLanguage: "la", AuthorNationality: "Pháp", AuthorBirthYear: intPtr(1596), AuthorDeathYear: intPtr(1650), Tags: pq.StringArray{"triết học", "tư duy"}, DayOfYear: intPtr(13), IsActive: true},
		{Quote: "Hành trình ngàn dặm bắt đầu từ một bước chân.", Author: "Lão Tử", OriginalQuote: "千里之行，始於足下", OriginalLanguage: "zh", AuthorNationality: "Trung Quốc", Tags: pq.StringArray{"hành động", "triết học"}, DayOfYear: intPtr(14), IsActive: true},
		{Quote: "Giáo dục là vũ khí mạnh nhất mà bạn có thể dùng để thay đổi thế giới.", Author: "Nelson Mandela", OriginalQuote: "Education is the most powerful weapon which you can use to change the world.", OriginalLanguage: "en", AuthorNationality: "Nam Phi", AuthorBirthYear: intPtr(1918), AuthorDeathYear: intPtr(2013), Tags: pq.StringArray{"giáo dục", "thay đổi"}, DayOfYear: intPtr(15), IsActive: true},
		{Quote: "Hãy là sự thay đổi mà bạn muốn thấy trên thế giới.", Author: "Mahatma Gandhi", OriginalQuote: "Be the change you wish to see in the world.", OriginalLanguage: "en", AuthorNationality: "Ấn Độ", AuthorBirthYear: intPtr(1869), AuthorDeathYear: intPtr(1948), Tags: pq.StringArray{"thay đổi", "hành động"}, DayOfYear: intPtr(16), IsActive: true},
		{Quote: "Trí tưởng tượng quan trọng hơn kiến thức.", Author: "Albert Einstein", OriginalQuote: "Imagination is more important than knowledge.", OriginalLanguage: "en", AuthorNationality: "Đức", AuthorBirthYear: intPtr(1879), AuthorDeathYear: intPtr(1955), Tags: pq.StringArray{"sáng tạo", "khoa học"}, DayOfYear: intPtr(17), IsActive: true},
		{Quote: "Không ai là một hòn đảo.", Author: "John Donne", OriginalQuote: "No man is an island.", OriginalLanguage: "en", AuthorNationality: "Anh", AuthorBirthYear: intPtr(1572), AuthorDeathYear: intPtr(1631), Tags: pq.StringArray{"cộng đồng", "kết nối"}, DayOfYear: intPtr(18), IsActive: true},
		{Quote: "Biết mình biết ta, trăm trận trăm thắng.", Author: "Tôn Tử", OriginalQuote: "知彼知己，百戰不殆", OriginalLanguage: "zh", AuthorNationality: "Trung Quốc", Tags: pq.StringArray{"chiến lược", "tư duy"}, DayOfYear: intPtr(19), IsActive: true},
		{Quote: "Tôi chỉ biết một điều, đó là tôi không biết gì cả.", Author: "Socrates", OriginalQuote: "I know that I know nothing.", OriginalLanguage: "el", AuthorNationality: "Hy Lạp", AuthorBirthYear: intPtr(-470), AuthorDeathYear: intPtr(-399), Tags: pq.StringArray{"triết học", "khiêm tốn"}, DayOfYear: intPtr(20), IsActive: true},

		// More Vietnamese wisdom
		{Quote: "Uống nước nhớ nguồn.", Author: "Tục ngữ Việt Nam", OriginalLanguage: "vi", Tags: pq.StringArray{"biết ơn", "truyền thống"}, DayOfYear: intPtr(21), IsActive: true},
		{Quote: "Ăn quả nhớ kẻ trồng cây.", Author: "Tục ngữ Việt Nam", OriginalLanguage: "vi", Tags: pq.StringArray{"biết ơn", "truyền thống"}, DayOfYear: intPtr(22), IsActive: true},
		{Quote: "Tốt gỗ hơn tốt nước sơn.", Author: "Tục ngữ Việt Nam", OriginalLanguage: "vi", Tags: pq.StringArray{"nhân cách", "giá trị"}, DayOfYear: intPtr(23), IsActive: true},
		{Quote: "Gần mực thì đen, gần đèn thì sáng.", Author: "Tục ngữ Việt Nam", OriginalLanguage: "vi", Tags: pq.StringArray{"môi trường", "ảnh hưởng"}, DayOfYear: intPtr(24), IsActive: true},
		{Quote: "Giấy rách phải giữ lấy lề.", Author: "Tục ngữ Việt Nam", OriginalLanguage: "vi", Tags: pq.StringArray{"phẩm giá", "truyền thống"}, DayOfYear: intPtr(25), IsActive: true},
		{Quote: "Đất lành chim đậu.", Author: "Tục ngữ Việt Nam", OriginalLanguage: "vi", Tags: pq.StringArray{"hòa bình", "an cư"}, DayOfYear: intPtr(26), IsActive: true},
		{Quote: "Ở hiền gặp lành.", Author: "Tục ngữ Việt Nam", OriginalLanguage: "vi", Tags: pq.StringArray{"đạo đức", "nhân quả"}, DayOfYear: intPtr(27), IsActive: true},
		{Quote: "Cái nết đánh chết cái đẹp.", Author: "Tục ngữ Việt Nam", OriginalLanguage: "vi", Tags: pq.StringArray{"nhân cách", "giá trị"}, DayOfYear: intPtr(28), IsActive: true},
		{Quote: "Chín bỏ làm mười.", Author: "Tục ngữ Việt Nam", OriginalLanguage: "vi", Tags: pq.StringArray{"bao dung", "đại lượng"}, DayOfYear: intPtr(29), IsActive: true},
		{Quote: "Người ta là hoa đất.", Author: "Tục ngữ Việt Nam", OriginalLanguage: "vi", Tags: pq.StringArray{"con người", "giá trị"}, DayOfYear: intPtr(30), IsActive: true},

		// More international
		{Quote: "Trong mỗi khó khăn đều ẩn chứa một cơ hội.", Author: "Albert Einstein", OriginalQuote: "In the middle of difficulty lies opportunity.", OriginalLanguage: "en", AuthorNationality: "Đức", AuthorBirthYear: intPtr(1879), AuthorDeathYear: intPtr(1955), Tags: pq.StringArray{"cơ hội", "thử thách"}, DayOfYear: intPtr(31), IsActive: true},
		{Quote: "Sống như ngày mai sẽ chết. Học như mình sẽ sống mãi.", Author: "Mahatma Gandhi", OriginalQuote: "Live as if you were to die tomorrow. Learn as if you were to live forever.", OriginalLanguage: "en", AuthorNationality: "Ấn Độ", AuthorBirthYear: intPtr(1869), AuthorDeathYear: intPtr(1948), Tags: pq.StringArray{"sống", "học hỏi"}, DayOfYear: intPtr(32), IsActive: true},
		{Quote: "Thành công không phải là điểm cuối, thất bại không phải là kết thúc: Điều quan trọng là can đảm để tiếp tục.", Author: "Winston Churchill", OriginalQuote: "Success is not final, failure is not fatal: It is the courage to continue that counts.", OriginalLanguage: "en", AuthorNationality: "Anh", AuthorBirthYear: intPtr(1874), AuthorDeathYear: intPtr(1965), Tags: pq.StringArray{"thành công", "kiên trì"}, DayOfYear: intPtr(33), IsActive: true},
		{Quote: "Cách tốt nhất để dự đoán tương lai là tạo ra nó.", Author: "Peter Drucker", OriginalQuote: "The best way to predict the future is to create it.", OriginalLanguage: "en", AuthorNationality: "Mỹ", AuthorBirthYear: intPtr(1909), AuthorDeathYear: intPtr(2005), Tags: pq.StringArray{"tương lai", "hành động"}, DayOfYear: intPtr(34), IsActive: true},
		{Quote: "Đừng sợ thất bại. Hãy sợ không dám thử.", Author: "Jeff Bezos", OriginalQuote: "I knew that if I failed I wouldn't regret that, but the one thing I might regret is not trying.", OriginalLanguage: "en", AuthorNationality: "Mỹ", AuthorBirthYear: intPtr(1964), Tags: pq.StringArray{"can đảm", "khởi nghiệp"}, DayOfYear: intPtr(35), IsActive: true},

		// More Vietnamese historical figures
		{Quote: "Hiền dữ phải đâu là tính sẵn, phần nhiều do giáo dục mà nên.", Author: "Hồ Chí Minh", OriginalLanguage: "vi", AuthorNationality: "Việt Nam", AuthorBirthYear: intPtr(1890), AuthorDeathYear: intPtr(1969), Tags: pq.StringArray{"giáo dục", "con người"}, DayOfYear: intPtr(36), IsActive: true},
		{Quote: "Các vua Hùng đã có công dựng nước, Bác cháu ta phải cùng nhau giữ lấy nước.", Author: "Hồ Chí Minh", OriginalLanguage: "vi", AuthorNationality: "Việt Nam", AuthorBirthYear: intPtr(1890), AuthorDeathYear: intPtr(1969), Tags: pq.StringArray{"yêu nước", "dựng nước"}, DayOfYear: intPtr(37), IsActive: true},
		{Quote: "Đem đại nghĩa để thắng hung tàn, lấy chí nhân để thay cường bạo.", Author: "Nguyễn Trãi", OriginalLanguage: "vi", AuthorNationality: "Việt Nam", AuthorBirthYear: intPtr(1380), AuthorDeathYear: intPtr(1442), Tags: pq.StringArray{"đại nghĩa", "nhân nghĩa"}, DayOfYear: intPtr(38), IsActive: true},
		{Quote: "Nam quốc sơn hà Nam đế cư, Tiệt nhiên định phận tại thiên thư.", Author: "Lý Thường Kiệt", OriginalLanguage: "vi", AuthorNationality: "Việt Nam", AuthorBirthYear: intPtr(1019), AuthorDeathYear: intPtr(1105), Tags: pq.StringArray{"độc lập", "yêu nước"}, DayOfYear: intPtr(39), IsActive: true},
		{Quote: "Trăm năm trồng người.", Author: "Hồ Chí Minh", OriginalLanguage: "vi", AuthorNationality: "Việt Nam", AuthorBirthYear: intPtr(1890), AuthorDeathYear: intPtr(1969), Tags: pq.StringArray{"giáo dục", "con người"}, DayOfYear: intPtr(40), IsActive: true},

		// Philosophy & Literature
		{Quote: "Sách là ngọn đèn sáng bất diệt của trí tuệ con người.", Author: "Samuel Johnson", OriginalQuote: "Books are the ever-burning lamps of accumulated wisdom.", OriginalLanguage: "en", AuthorNationality: "Anh", AuthorBirthYear: intPtr(1709), AuthorDeathYear: intPtr(1784), Tags: pq.StringArray{"đọc sách", "tri thức"}, DayOfYear: intPtr(41), IsActive: true},
		{Quote: "Người đọc sách sống hàng ngàn cuộc đời trước khi chết. Người không đọc chỉ sống một.", Author: "George R.R. Martin", OriginalQuote: "A reader lives a thousand lives before he dies. The man who never reads lives only one.", OriginalLanguage: "en", AuthorNationality: "Mỹ", AuthorBirthYear: intPtr(1948), Tags: pq.StringArray{"đọc sách", "cuộc sống"}, DayOfYear: intPtr(42), IsActive: true},
		{Quote: "Chúng ta không thể giải quyết vấn đề bằng cùng một tư duy đã tạo ra chúng.", Author: "Albert Einstein", OriginalQuote: "We cannot solve our problems with the same thinking we used when we created them.", OriginalLanguage: "en", AuthorNationality: "Đức", AuthorBirthYear: intPtr(1879), AuthorDeathYear: intPtr(1955), Tags: pq.StringArray{"tư duy", "sáng tạo"}, DayOfYear: intPtr(43), IsActive: true},
		{Quote: "Người thực sự vĩ đại khiến bạn cảm thấy rằng bạn cũng có thể trở nên vĩ đại.", Author: "Mark Twain", OriginalQuote: "Really great people make you feel that you, too, can become great.", OriginalLanguage: "en", AuthorNationality: "Mỹ", AuthorBirthYear: intPtr(1835), AuthorDeathYear: intPtr(1910), Tags: pq.StringArray{"lãnh đạo", "truyền cảm hứng"}, DayOfYear: intPtr(44), IsActive: true},
		{Quote: "Tương lai thuộc về những người tin vào vẻ đẹp của ước mơ họ.", Author: "Eleanor Roosevelt", OriginalQuote: "The future belongs to those who believe in the beauty of their dreams.", OriginalLanguage: "en", AuthorNationality: "Mỹ", AuthorBirthYear: intPtr(1884), AuthorDeathYear: intPtr(1962), Tags: pq.StringArray{"ước mơ", "tương lai"}, DayOfYear: intPtr(45), IsActive: true},

		// Asian philosophy
		{Quote: "Người quân tử cầu ở mình, kẻ tiểu nhân cầu ở người.", Author: "Khổng Tử", OriginalQuote: "君子求諸己，小人求諸人", OriginalLanguage: "zh", AuthorNationality: "Trung Quốc", AuthorBirthYear: intPtr(-551), AuthorDeathYear: intPtr(-479), Tags: pq.StringArray{"đạo đức", "tự lập"}, DayOfYear: intPtr(46), IsActive: true},
		{Quote: "Học mà không hành, không bằng không học.", Author: "Khổng Tử", OriginalQuote: "學而不思則罔，思而不學則殆", OriginalLanguage: "zh", AuthorNationality: "Trung Quốc", AuthorBirthYear: intPtr(-551), AuthorDeathYear: intPtr(-479), Tags: pq.StringArray{"học hỏi", "thực hành"}, DayOfYear: intPtr(47), IsActive: true},
		{Quote: "Thắng người là có sức, thắng mình là mạnh.", Author: "Lão Tử", OriginalQuote: "勝人者有力，自勝者強", OriginalLanguage: "zh", AuthorNationality: "Trung Quốc", Tags: pq.StringArray{"bản thân", "sức mạnh"}, DayOfYear: intPtr(48), IsActive: true},
		{Quote: "Gieo hạt tốt, gặt quả ngọt.", Author: "Phật giáo", OriginalLanguage: "vi", Tags: pq.StringArray{"nhân quả", "đạo đức"}, DayOfYear: intPtr(49), IsActive: true},
		{Quote: "Nước chảy đá mòn.", Author: "Tục ngữ Việt Nam", OriginalLanguage: "vi", Tags: pq.StringArray{"kiên trì", "nỗ lực"}, DayOfYear: intPtr(50), IsActive: true},

		// Modern leaders
		{Quote: "Đổi mới hay là chết.", Author: "Nguyễn Văn Linh", OriginalLanguage: "vi", AuthorNationality: "Việt Nam", AuthorBirthYear: intPtr(1915), AuthorDeathYear: intPtr(1998), Tags: pq.StringArray{"đổi mới", "phát triển"}, DayOfYear: intPtr(51), IsActive: true},
		{Quote: "Cái khó ló cái khôn.", Author: "Tục ngữ Việt Nam", OriginalLanguage: "vi", Tags: pq.StringArray{"sáng tạo", "khó khăn"}, DayOfYear: intPtr(52), IsActive: true},
		{Quote: "Tiên học lễ, hậu học văn.", Author: "Tục ngữ Việt Nam", OriginalLanguage: "vi", Tags: pq.StringArray{"giáo dục", "đạo đức"}, DayOfYear: intPtr(53), IsActive: true},
		{Quote: "Kiến tha lâu đầy tổ.", Author: "Tục ngữ Việt Nam", OriginalLanguage: "vi", Tags: pq.StringArray{"kiên trì", "tích lũy"}, DayOfYear: intPtr(54), IsActive: true},
		{Quote: "Lá lành đùm lá rách.", Author: "Tục ngữ Việt Nam", OriginalLanguage: "vi", Tags: pq.StringArray{"đoàn kết", "tương trợ"}, DayOfYear: intPtr(55), IsActive: true},
		{Quote: "Nhiễu điều phủ lấy giá gương, người trong một nước phải thương nhau cùng.", Author: "Ca dao Việt Nam", OriginalLanguage: "vi", Tags: pq.StringArray{"đoàn kết", "yêu thương"}, DayOfYear: intPtr(56), IsActive: true},
		{Quote: "Con hơn cha là nhà có phúc.", Author: "Tục ngữ Việt Nam", OriginalLanguage: "vi", Tags: pq.StringArray{"giáo dục", "gia đình"}, DayOfYear: intPtr(57), IsActive: true},
		{Quote: "Bầu ơi thương lấy bí cùng, tuy rằng khác giống nhưng chung một giàn.", Author: "Ca dao Việt Nam", OriginalLanguage: "vi", Tags: pq.StringArray{"đoàn kết", "yêu thương"}, DayOfYear: intPtr(58), IsActive: true},

		// Science & Technology
		{Quote: "Khoa học là tổ chức của tri thức.", Author: "Herbert Spencer", OriginalQuote: "Science is organized knowledge.", OriginalLanguage: "en", AuthorNationality: "Anh", AuthorBirthYear: intPtr(1820), AuthorDeathYear: intPtr(1903), Tags: pq.StringArray{"khoa học", "tri thức"}, DayOfYear: intPtr(59), IsActive: true},
		{Quote: "Mọi thứ nên được làm đơn giản nhất có thể, nhưng không đơn giản hơn.", Author: "Albert Einstein", OriginalQuote: "Everything should be made as simple as possible, but not simpler.", OriginalLanguage: "en", AuthorNationality: "Đức", AuthorBirthYear: intPtr(1879), AuthorDeathYear: intPtr(1955), Tags: pq.StringArray{"đơn giản", "thiết kế"}, DayOfYear: intPtr(60), IsActive: true},

		// Vietnamese literature
		{Quote: "Trăm năm trong cõi người ta, chữ tài chữ mệnh khéo là ghét nhau.", Author: "Nguyễn Du", OriginalLanguage: "vi", AuthorNationality: "Việt Nam", AuthorBirthYear: intPtr(1766), AuthorDeathYear: intPtr(1820), Tags: pq.StringArray{"văn học", "số phận"}, DayOfYear: intPtr(61), IsActive: true},
		{Quote: "Nhất nghệ tinh, nhất thân vinh.", Author: "Tục ngữ Việt Nam", OriginalLanguage: "vi", Tags: pq.StringArray{"nghề nghiệp", "chuyên tâm"}, DayOfYear: intPtr(62), IsActive: true},
		{Quote: "Đi một ngày đàng, học một sàng khôn.", Author: "Tục ngữ Việt Nam", OriginalLanguage: "vi", Tags: pq.StringArray{"trải nghiệm", "học hỏi"}, DayOfYear: intPtr(63), IsActive: true},
		{Quote: "Phi thương bất phú.", Author: "Tục ngữ Việt Nam", OriginalLanguage: "vi", Tags: pq.StringArray{"kinh doanh", "kinh tế"}, DayOfYear: intPtr(64), IsActive: true},
		{Quote: "Chớ thấy sóng cả mà ngã tay chèo.", Author: "Tục ngữ Việt Nam", OriginalLanguage: "vi", Tags: pq.StringArray{"kiên trì", "can đảm"}, DayOfYear: intPtr(65), IsActive: true},

		// More modern
		{Quote: "Hãy khao khát. Hãy dại khờ.", Author: "Steve Jobs", OriginalQuote: "Stay hungry. Stay foolish.", OriginalLanguage: "en", AuthorNationality: "Mỹ", AuthorBirthYear: intPtr(1955), AuthorDeathYear: intPtr(2011), Tags: pq.StringArray{"đam mê", "sáng tạo"}, DayOfYear: intPtr(66), IsActive: true},
		{Quote: "Đời người có hạn, đừng lãng phí thời gian sống cuộc đời của người khác.", Author: "Steve Jobs", OriginalQuote: "Your time is limited, don't waste it living someone else's life.", OriginalLanguage: "en", AuthorNationality: "Mỹ", AuthorBirthYear: intPtr(1955), AuthorDeathYear: intPtr(2011), Tags: pq.StringArray{"cuộc sống", "bản thân"}, DayOfYear: intPtr(67), IsActive: true},
		{Quote: "Điều duy nhất cần sợ là chính nỗi sợ hãi.", Author: "Franklin D. Roosevelt", OriginalQuote: "The only thing we have to fear is fear itself.", OriginalLanguage: "en", AuthorNationality: "Mỹ", AuthorBirthYear: intPtr(1882), AuthorDeathYear: intPtr(1945), Tags: pq.StringArray{"can đảm", "lãnh đạo"}, DayOfYear: intPtr(68), IsActive: true},
		{Quote: "Tôi có một giấc mơ.", Author: "Martin Luther King Jr.", OriginalQuote: "I have a dream.", OriginalLanguage: "en", AuthorNationality: "Mỹ", AuthorBirthYear: intPtr(1929), AuthorDeathYear: intPtr(1968), Tags: pq.StringArray{"ước mơ", "bình đẳng"}, DayOfYear: intPtr(69), IsActive: true},
		{Quote: "Muốn đi nhanh thì đi một mình. Muốn đi xa thì đi cùng nhau.", Author: "Tục ngữ châu Phi", OriginalQuote: "If you want to go fast, go alone. If you want to go far, go together.", OriginalLanguage: "en", Tags: pq.StringArray{"đoàn kết", "hợp tác"}, DayOfYear: intPtr(70), IsActive: true},

		// More quotes for more days
		{Quote: "Sự giáo dục tốt nhất là lấy gương mình dạy người.", Author: "Hồ Chí Minh", OriginalLanguage: "vi", AuthorNationality: "Việt Nam", AuthorBirthYear: intPtr(1890), AuthorDeathYear: intPtr(1969), Tags: pq.StringArray{"giáo dục", "gương mẫu"}, DayOfYear: intPtr(71), IsActive: true},
		{Quote: "Ai chiến thắng mà không hề chiến bại, ai nên khôn mà chẳng dại đôi lần.", Author: "Ca dao Việt Nam", OriginalLanguage: "vi", Tags: pq.StringArray{"kinh nghiệm", "trưởng thành"}, DayOfYear: intPtr(72), IsActive: true},
		{Quote: "Công cha như núi Thái Sơn, nghĩa mẹ như nước trong nguồn chảy ra.", Author: "Ca dao Việt Nam", OriginalLanguage: "vi", Tags: pq.StringArray{"gia đình", "hiếu thảo"}, DayOfYear: intPtr(73), IsActive: true},
		{Quote: "Tri thức là sức mạnh.", Author: "Francis Bacon", OriginalQuote: "Knowledge is power.", OriginalLanguage: "en", AuthorNationality: "Anh", AuthorBirthYear: intPtr(1561), AuthorDeathYear: intPtr(1626), Tags: pq.StringArray{"tri thức", "sức mạnh"}, DayOfYear: intPtr(74), IsActive: true},
		{Quote: "Để thành công, bạn phải sẵn sàng thất bại.", Author: "Thomas Edison", OriginalQuote: "I have not failed. I've just found 10,000 ways that won't work.", OriginalLanguage: "en", AuthorNationality: "Mỹ", AuthorBirthYear: intPtr(1847), AuthorDeathYear: intPtr(1931), Tags: pq.StringArray{"thất bại", "kiên trì"}, DayOfYear: intPtr(75), IsActive: true},
	}

	created := 0
	for _, q := range quotes {
		var existing models.Quote
		if db.Where("quote = ? AND author = ?", q.Quote, q.Author).First(&existing).Error == nil {
			continue
		}
		if err := db.Create(&q).Error; err != nil {
			fmt.Printf("   ❌ Failed: %v\n", err)
			continue
		}
		created++
	}
	fmt.Printf("   ✅ Created %d quotes (skipped %d existing)\n", created, len(quotes)-created)
}

// seedFamousPeople creates famous historical figures.
func seedFamousPeople(db *gorm.DB) {
	fmt.Println("\n👤 Seeding Famous People...")

	intPtr := func(i int) *int { return &i }
	datePtr := func(y, m, d int) *time.Time {
		t := time.Date(y, time.Month(m), d, 0, 0, 0, 0, time.UTC)
		return &t
	}

	people := []models.FamousPerson{
		// Vietnamese Historical Figures
		{Name: "Hồ Chí Minh", BirthDate: datePtr(1890, 5, 19), BirthDay: intPtr(19), BirthMonth: intPtr(5), BirthYear: intPtr(1890), DeathDate: datePtr(1969, 9, 2), Nationality: "Việt Nam", Occupation: "Chủ tịch nước, Nhà cách mạng", Category: models.FamousPersonCategoryPolitics, ShortBio: "Chủ tịch nước Việt Nam Dân chủ Cộng hòa, lãnh tụ cách mạng, người cha của dân tộc Việt Nam", IsVietnamese: true, Tags: pq.StringArray{"chính trị", "cách mạng", "lãnh đạo"}, IsActive: true},
		{Name: "Võ Nguyên Giáp", BirthDate: datePtr(1911, 8, 25), BirthDay: intPtr(25), BirthMonth: intPtr(8), BirthYear: intPtr(1911), DeathDate: datePtr(2013, 10, 4), Nationality: "Việt Nam", Occupation: "Đại tướng, Nhà quân sự", Category: models.FamousPersonCategoryHistory, ShortBio: "Đại tướng đầu tiên của Quân đội Nhân dân Việt Nam, anh hùng Điện Biên Phủ", IsVietnamese: true, Tags: pq.StringArray{"quân sự", "lịch sử", "Điện Biên Phủ"}, IsActive: true},
		{Name: "Nguyễn Trãi", BirthDate: datePtr(1380, 1, 1), BirthDay: intPtr(1), BirthMonth: intPtr(1), BirthYear: intPtr(1380), DeathDate: datePtr(1442, 9, 19), Nationality: "Việt Nam", Occupation: "Nhà chính trị, Nhà văn", Category: models.FamousPersonCategoryLiterature, ShortBio: "Nhà chính trị, nhà quân sự, nhà thơ, nhà văn hóa lỗi lạc, tác giả Bình Ngô đại cáo", IsVietnamese: true, Tags: pq.StringArray{"văn học", "chính trị", "Bình Ngô đại cáo"}, IsActive: true},
		{Name: "Nguyễn Du", BirthDate: datePtr(1766, 1, 3), BirthDay: intPtr(3), BirthMonth: intPtr(1), BirthYear: intPtr(1766), DeathDate: datePtr(1820, 9, 16), Nationality: "Việt Nam", Occupation: "Đại thi hào", Category: models.FamousPersonCategoryLiterature, ShortBio: "Đại thi hào dân tộc, tác giả Truyện Kiều, được UNESCO vinh danh danh nhân văn hóa thế giới", IsVietnamese: true, Tags: pq.StringArray{"văn học", "Truyện Kiều", "UNESCO"}, IsActive: true},
		{Name: "Lý Thường Kiệt", BirthDate: datePtr(1019, 1, 1), BirthDay: intPtr(1), BirthMonth: intPtr(1), BirthYear: intPtr(1019), DeathDate: datePtr(1105, 7, 19), Nationality: "Việt Nam", Occupation: "Thái úy, Danh tướng", Category: models.FamousPersonCategoryHistory, ShortBio: "Danh tướng nhà Lý, tác giả bản tuyên ngôn độc lập đầu tiên 'Nam quốc sơn hà'", IsVietnamese: true, Tags: pq.StringArray{"quân sự", "lịch sử", "độc lập"}, IsActive: true},
		{Name: "Trần Hưng Đạo", BirthDate: datePtr(1228, 1, 1), BirthDay: intPtr(1), BirthMonth: intPtr(1), BirthYear: intPtr(1228), DeathDate: datePtr(1300, 9, 20), Nationality: "Việt Nam", Occupation: "Quốc công Tiết chế, Nhà quân sự", Category: models.FamousPersonCategoryHistory, ShortBio: "Anh hùng dân tộc, 3 lần đánh bại quân Nguyên Mông xâm lược", IsVietnamese: true, Tags: pq.StringArray{"quân sự", "lịch sử", "Nguyên Mông"}, IsActive: true},
		{Name: "Lê Lợi", BirthDate: datePtr(1385, 9, 10), BirthDay: intPtr(10), BirthMonth: intPtr(9), BirthYear: intPtr(1385), DeathDate: datePtr(1433, 9, 5), Nationality: "Việt Nam", Occupation: "Hoàng đế, Nhà quân sự", Category: models.FamousPersonCategoryPolitics, ShortBio: "Vua sáng lập nhà Hậu Lê, lãnh đạo cuộc khởi nghĩa Lam Sơn chống giặc Minh", IsVietnamese: true, Tags: pq.StringArray{"lịch sử", "khởi nghĩa", "Lam Sơn"}, IsActive: true},
		{Name: "Hai Bà Trưng", BirthDay: intPtr(1), BirthMonth: intPtr(3), BirthYear: intPtr(14), Nationality: "Việt Nam", Occupation: "Nữ vương, Anh hùng dân tộc", Category: models.FamousPersonCategoryHistory, ShortBio: "Hai chị em Trưng Trắc và Trưng Nhị, lãnh đạo cuộc khởi nghĩa chống quân Hán năm 40 SCN", IsVietnamese: true, Tags: pq.StringArray{"lịch sử", "khởi nghĩa", "nữ anh hùng"}, IsActive: true},
		{Name: "Bà Triệu", BirthDay: intPtr(1), BirthMonth: intPtr(1), BirthYear: intPtr(225), Nationality: "Việt Nam", Occupation: "Anh hùng dân tộc", Category: models.FamousPersonCategoryHistory, ShortBio: "Nữ anh hùng dân tộc, lãnh đạo cuộc khởi nghĩa chống quân Ngô năm 248", IsVietnamese: true, Tags: pq.StringArray{"lịch sử", "khởi nghĩa", "nữ anh hùng"}, IsActive: true},
		{Name: "Nguyễn Huệ", BirthDate: datePtr(1753, 1, 1), BirthDay: intPtr(1), BirthMonth: intPtr(1), BirthYear: intPtr(1753), DeathDate: datePtr(1792, 9, 16), Nationality: "Việt Nam", Occupation: "Hoàng đế Quang Trung", Category: models.FamousPersonCategoryPolitics, ShortBio: "Hoàng đế Quang Trung, anh hùng áo vải, người đánh bại 20 vạn quân Thanh trong 5 ngày", IsVietnamese: true, Tags: pq.StringArray{"lịch sử", "Tây Sơn", "Quang Trung"}, IsActive: true},
		{Name: "Phan Bội Châu", BirthDate: datePtr(1867, 12, 26), BirthDay: intPtr(26), BirthMonth: intPtr(12), BirthYear: intPtr(1867), DeathDate: datePtr(1940, 10, 29), Nationality: "Việt Nam", Occupation: "Nhà cách mạng, Nhà văn", Category: models.FamousPersonCategoryPolitics, ShortBio: "Nhà cách mạng, chí sĩ yêu nước đầu thế kỷ XX, người sáng lập phong trào Đông Du", IsVietnamese: true, Tags: pq.StringArray{"cách mạng", "Đông Du", "yêu nước"}, IsActive: true},
		{Name: "Nguyễn Ái Quốc", OriginalName: "Nguyễn Sinh Cung", BirthDate: datePtr(1890, 5, 19), BirthDay: intPtr(19), BirthMonth: intPtr(5), BirthYear: intPtr(1890), Nationality: "Việt Nam", Occupation: "Nhà cách mạng", Category: models.FamousPersonCategoryPolitics, ShortBio: "Tên hoạt động cách mạng của Hồ Chí Minh trong giai đoạn tìm đường cứu nước", IsVietnamese: true, Tags: pq.StringArray{"cách mạng", "tìm đường"}, IsActive: false}, // duplicate with HCM, keep for reference

		// International Figures
		{Name: "Albert Einstein", BirthDate: datePtr(1879, 3, 14), BirthDay: intPtr(14), BirthMonth: intPtr(3), BirthYear: intPtr(1879), DeathDate: datePtr(1955, 4, 18), Nationality: "Đức / Mỹ", Occupation: "Nhà vật lý lý thuyết", Category: models.FamousPersonCategoryScience, ShortBio: "Nhà vật lý lý thuyết, cha đẻ thuyết tương đối, đoạt giải Nobel Vật lý 1921", IsVietnamese: false, Tags: pq.StringArray{"vật lý", "Nobel", "tương đối"}, IsActive: true},
		{Name: "Mahatma Gandhi", BirthDate: datePtr(1869, 10, 2), BirthDay: intPtr(2), BirthMonth: intPtr(10), BirthYear: intPtr(1869), DeathDate: datePtr(1948, 1, 30), Nationality: "Ấn Độ", Occupation: "Nhà hoạt động, Luật sư", Category: models.FamousPersonCategoryPolitics, ShortBio: "Linh hồn phong trào độc lập Ấn Độ, biểu tượng đấu tranh bất bạo động", IsVietnamese: false, Tags: pq.StringArray{"bất bạo động", "độc lập", "hòa bình"}, IsActive: true},
		{Name: "Nelson Mandela", BirthDate: datePtr(1918, 7, 18), BirthDay: intPtr(18), BirthMonth: intPtr(7), BirthYear: intPtr(1918), DeathDate: datePtr(2013, 12, 5), Nationality: "Nam Phi", Occupation: "Tổng thống, Nhà hoạt động", Category: models.FamousPersonCategoryPolitics, ShortBio: "Tổng thống da đen đầu tiên của Nam Phi, biểu tượng chống phân biệt chủng tộc apartheid", IsVietnamese: false, Tags: pq.StringArray{"tự do", "bình đẳng", "apartheid"}, IsActive: true},
		{Name: "Leonardo da Vinci", BirthDate: datePtr(1452, 4, 15), BirthDay: intPtr(15), BirthMonth: intPtr(4), BirthYear: intPtr(1452), DeathDate: datePtr(1519, 5, 2), Nationality: "Ý", Occupation: "Họa sĩ, Nhà phát minh, Nhà khoa học", Category: models.FamousPersonCategoryArt, ShortBio: "Thiên tài toàn năng thời Phục Hưng, tác giả Mona Lisa, The Last Supper", IsVietnamese: false, Tags: pq.StringArray{"hội họa", "phát minh", "Phục Hưng"}, IsActive: true},
		{Name: "Marie Curie", BirthDate: datePtr(1867, 11, 7), BirthDay: intPtr(7), BirthMonth: intPtr(11), BirthYear: intPtr(1867), DeathDate: datePtr(1934, 7, 4), Nationality: "Ba Lan / Pháp", Occupation: "Nhà vật lý, Nhà hóa học", Category: models.FamousPersonCategoryScience, ShortBio: "Người phụ nữ đầu tiên đoạt giải Nobel, 2 lần Nobel (Vật lý & Hóa học)", IsVietnamese: false, Tags: pq.StringArray{"phóng xạ", "Nobel", "khoa học"}, IsActive: true},
		{Name: "William Shakespeare", BirthDate: datePtr(1564, 4, 23), BirthDay: intPtr(23), BirthMonth: intPtr(4), BirthYear: intPtr(1564), DeathDate: datePtr(1616, 4, 23), Nationality: "Anh", Occupation: "Nhà viết kịch, Nhà thơ", Category: models.FamousPersonCategoryLiterature, ShortBio: "Đại văn hào Anh, tác giả Romeo & Juliet, Hamlet, Macbeth", IsVietnamese: false, Tags: pq.StringArray{"kịch", "văn học", "thơ"}, IsActive: true},
		{Name: "Khổng Tử", OriginalName: "孔子 (Kong Qiu)", BirthDay: intPtr(28), BirthMonth: intPtr(9), BirthYear: intPtr(-551), Nationality: "Trung Quốc", Occupation: "Triết gia, Nhà giáo dục", Category: models.FamousPersonCategoryHistory, ShortBio: "Triết gia vĩ đại nhất Trung Hoa, người sáng lập Nho giáo", IsVietnamese: false, Tags: pq.StringArray{"triết học", "Nho giáo", "giáo dục"}, IsActive: true},
		{Name: "Napoleon Bonaparte", BirthDate: datePtr(1769, 8, 15), BirthDay: intPtr(15), BirthMonth: intPtr(8), BirthYear: intPtr(1769), DeathDate: datePtr(1821, 5, 5), Nationality: "Pháp", Occupation: "Hoàng đế, Nhà quân sự", Category: models.FamousPersonCategoryPolitics, ShortBio: "Hoàng đế Pháp, nhà quân sự thiên tài, người thay đổi cục diện châu Âu", IsVietnamese: false, Tags: pq.StringArray{"quân sự", "lịch sử", "Pháp"}, IsActive: true},
		{Name: "Martin Luther King Jr.", BirthDate: datePtr(1929, 1, 15), BirthDay: intPtr(15), BirthMonth: intPtr(1), BirthYear: intPtr(1929), DeathDate: datePtr(1968, 4, 4), Nationality: "Mỹ", Occupation: "Mục sư, Nhà hoạt động nhân quyền", Category: models.FamousPersonCategoryPolitics, ShortBio: "Lãnh đạo phong trào dân quyền Mỹ, đoạt giải Nobel Hòa bình 1964", IsVietnamese: false, Tags: pq.StringArray{"dân quyền", "Nobel", "bất bạo động"}, IsActive: true},
	}

	created := 0
	for _, p := range people {
		var existing models.FamousPerson
		if db.Where("name = ? AND nationality = ?", p.Name, p.Nationality).First(&existing).Error == nil {
			continue
		}
		if err := db.Create(&p).Error; err != nil {
			fmt.Printf("   ❌ Failed to create '%s': %v\n", p.Name, err)
			continue
		}
		created++
	}
	fmt.Printf("   ✅ Created %d famous people (skipped %d existing)\n", created, len(people)-created)
}

// seedEvents creates historical events and national/international days.
func seedEvents(db *gorm.DB) {
	fmt.Println("\n📅 Seeding Events...")

	intPtr := func(i int) *int { return &i }

	events := []models.Event{
		// Vietnamese national holidays & events
		{Title: "Tết Dương lịch", Slug: utils.GenerateSlug("Tết Dương lịch"), EventDay: 1, EventMonth: 1, IsRecurring: true, EventType: models.EventTypeNationalDay, Country: "Việt Nam", CountryCode: "VN", FlagEmoji: "🇻🇳", ShortDescription: "Ngày đầu tiên của năm mới Dương lịch", Importance: models.EventImportanceHigh, Tags: pq.StringArray{"tết", "nghỉ lễ"}, IsActive: true},
		{Title: "Ngày thành lập Đảng Cộng sản Việt Nam", Slug: utils.GenerateSlug("Ngày thành lập Đảng Cộng sản Việt Nam"), EventDay: 3, EventMonth: 2, EventYear: intPtr(1930), IsRecurring: true, EventType: models.EventTypeNationalDay, Country: "Việt Nam", CountryCode: "VN", FlagEmoji: "🇻🇳", ShortDescription: "Kỷ niệm ngày thành lập Đảng Cộng sản Việt Nam 3/2/1930", Importance: models.EventImportanceHigh, Tags: pq.StringArray{"chính trị", "lịch sử"}, IsActive: true},
		{Title: "Ngày Thầy thuốc Việt Nam", Slug: utils.GenerateSlug("Ngày Thầy thuốc Việt Nam"), EventDay: 27, EventMonth: 2, IsRecurring: true, EventType: models.EventTypeNationalDay, Country: "Việt Nam", CountryCode: "VN", FlagEmoji: "🇻🇳", ShortDescription: "Vinh danh những người làm công tác y tế", Importance: models.EventImportanceMedium, Tags: pq.StringArray{"y tế", "vinh danh"}, IsActive: true},
		{Title: "Ngày Quốc tế Phụ nữ", Slug: utils.GenerateSlug("Ngày Quốc tế Phụ nữ"), EventDay: 8, EventMonth: 3, IsRecurring: true, EventType: models.EventTypeWorldDay, Country: "Quốc tế", ShortDescription: "Ngày tôn vinh phụ nữ trên toàn thế giới", Importance: models.EventImportanceHigh, Tags: pq.StringArray{"phụ nữ", "bình đẳng"}, IsActive: true},
		{Title: "Ngày Giỗ Tổ Hùng Vương", Slug: utils.GenerateSlug("Ngày Giỗ Tổ Hùng Vương"), EventDay: 10, EventMonth: 3, IsRecurring: true, EventType: models.EventTypeNationalDay, Country: "Việt Nam", CountryCode: "VN", FlagEmoji: "🇻🇳", ShortDescription: "Ngày giỗ tổ Hùng Vương - Mùng 10 tháng 3 Âm lịch", Importance: models.EventImportanceHigh, Tags: pq.StringArray{"lễ hội", "truyền thống", "nghỉ lễ"}, IsActive: true},
		{Title: "Ngày Giải phóng miền Nam, thống nhất đất nước", Slug: utils.GenerateSlug("Ngày Giải phóng miền Nam thống nhất đất nước"), EventDay: 30, EventMonth: 4, EventYear: intPtr(1975), IsRecurring: true, EventType: models.EventTypeNationalDay, Country: "Việt Nam", CountryCode: "VN", FlagEmoji: "🇻🇳", ShortDescription: "Kỷ niệm ngày 30/4/1975 thống nhất đất nước", Importance: models.EventImportanceHigh, Tags: pq.StringArray{"lịch sử", "thống nhất", "nghỉ lễ"}, IsActive: true},
		{Title: "Ngày Quốc tế Lao động", Slug: utils.GenerateSlug("Ngày Quốc tế Lao động"), EventDay: 1, EventMonth: 5, IsRecurring: true, EventType: models.EventTypeWorldDay, Country: "Quốc tế", ShortDescription: "Ngày Quốc tế Lao động 1/5", Importance: models.EventImportanceHigh, Tags: pq.StringArray{"lao động", "nghỉ lễ"}, IsActive: true},
		{Title: "Ngày sinh Chủ tịch Hồ Chí Minh", Slug: utils.GenerateSlug("Ngày sinh Chủ tịch Hồ Chí Minh"), EventDay: 19, EventMonth: 5, EventYear: intPtr(1890), IsRecurring: true, EventType: models.EventTypeNationalDay, Country: "Việt Nam", CountryCode: "VN", FlagEmoji: "🇻🇳", ShortDescription: "Kỷ niệm ngày sinh Chủ tịch Hồ Chí Minh 19/5/1890", Importance: models.EventImportanceHigh, Tags: pq.StringArray{"Hồ Chí Minh", "sinh nhật"}, IsActive: true},
		{Title: "Ngày Quốc tế Thiếu nhi", Slug: utils.GenerateSlug("Ngày Quốc tế Thiếu nhi"), EventDay: 1, EventMonth: 6, IsRecurring: true, EventType: models.EventTypeWorldDay, Country: "Quốc tế", ShortDescription: "Ngày Quốc tế Thiếu nhi 1/6", Importance: models.EventImportanceMedium, Tags: pq.StringArray{"thiếu nhi", "trẻ em"}, IsActive: true},
		{Title: "Ngày Báo chí Cách mạng Việt Nam", Slug: utils.GenerateSlug("Ngày Báo chí Cách mạng Việt Nam"), EventDay: 21, EventMonth: 6, EventYear: intPtr(1925), IsRecurring: true, EventType: models.EventTypeNationalDay, Country: "Việt Nam", CountryCode: "VN", FlagEmoji: "🇻🇳", ShortDescription: "Kỷ niệm ngày Báo chí Cách mạng Việt Nam", Importance: models.EventImportanceMedium, Tags: pq.StringArray{"báo chí", "truyền thông"}, IsActive: true},
		{Title: "Ngày Thương binh Liệt sĩ", Slug: utils.GenerateSlug("Ngày Thương binh Liệt sĩ"), EventDay: 27, EventMonth: 7, IsRecurring: true, EventType: models.EventTypeNationalDay, Country: "Việt Nam", CountryCode: "VN", FlagEmoji: "🇻🇳", ShortDescription: "Ngày Thương binh Liệt sĩ 27/7, tri ân các anh hùng liệt sĩ", Importance: models.EventImportanceHigh, Tags: pq.StringArray{"tri ân", "liệt sĩ"}, IsActive: true},
		{Title: "Cách mạng Tháng Tám", Slug: utils.GenerateSlug("Cách mạng Tháng Tám"), EventDay: 19, EventMonth: 8, EventYear: intPtr(1945), IsRecurring: true, EventType: models.EventTypeNationalDay, Country: "Việt Nam", CountryCode: "VN", FlagEmoji: "🇻🇳", ShortDescription: "Kỷ niệm Cách mạng Tháng Tám thành công 19/8/1945", Importance: models.EventImportanceHigh, Tags: pq.StringArray{"cách mạng", "lịch sử"}, IsActive: true},
		{Title: "Ngày Quốc khánh Việt Nam", Slug: utils.GenerateSlug("Ngày Quốc khánh Việt Nam"), EventDay: 2, EventMonth: 9, EventYear: intPtr(1945), IsRecurring: true, EventType: models.EventTypeNationalDay, Country: "Việt Nam", CountryCode: "VN", FlagEmoji: "🇻🇳", ShortDescription: "Ngày Quốc khánh nước Cộng hòa Xã hội Chủ nghĩa Việt Nam 2/9", Importance: models.EventImportanceHigh, Tags: pq.StringArray{"quốc khánh", "nghỉ lễ"}, IsActive: true},
		{Title: "Ngày Nhà giáo Việt Nam", Slug: utils.GenerateSlug("Ngày Nhà giáo Việt Nam"), EventDay: 20, EventMonth: 11, IsRecurring: true, EventType: models.EventTypeNationalDay, Country: "Việt Nam", CountryCode: "VN", FlagEmoji: "🇻🇳", ShortDescription: "Ngày Nhà giáo Việt Nam 20/11, tôn vinh các thầy cô giáo", Importance: models.EventImportanceHigh, Tags: pq.StringArray{"giáo dục", "nhà giáo"}, IsActive: true},
		{Title: "Ngày thành lập Quân đội Nhân dân Việt Nam", Slug: utils.GenerateSlug("Ngày thành lập Quân đội Nhân dân Việt Nam"), EventDay: 22, EventMonth: 12, EventYear: intPtr(1944), IsRecurring: true, EventType: models.EventTypeNationalDay, Country: "Việt Nam", CountryCode: "VN", FlagEmoji: "🇻🇳", ShortDescription: "Kỷ niệm ngày thành lập Quân đội Nhân dân Việt Nam 22/12", Importance: models.EventImportanceHigh, Tags: pq.StringArray{"quân đội", "lịch sử"}, IsActive: true},
		{Title: "Ngày Phụ nữ Việt Nam", Slug: utils.GenerateSlug("Ngày Phụ nữ Việt Nam"), EventDay: 20, EventMonth: 10, EventYear: intPtr(1930), IsRecurring: true, EventType: models.EventTypeNationalDay, Country: "Việt Nam", CountryCode: "VN", FlagEmoji: "🇻🇳", ShortDescription: "Ngày Phụ nữ Việt Nam 20/10", Importance: models.EventImportanceHigh, Tags: pq.StringArray{"phụ nữ", "vinh danh"}, IsActive: true},

		// World days (UN, UNESCO)
		{Title: "Ngày Quốc tế Giáo dục (UNESCO)", Slug: utils.GenerateSlug("Ngày Quốc tế Giáo dục UNESCO"), EventDay: 24, EventMonth: 1, IsRecurring: true, EventType: models.EventTypeWorldDay, Country: "Quốc tế", ShortDescription: "Ngày Quốc tế Giáo dục do UNESCO công nhận", Importance: models.EventImportanceMedium, Tags: pq.StringArray{"giáo dục", "UNESCO"}, IsActive: true},
		{Title: "Ngày Quốc tế Hạnh phúc", Slug: utils.GenerateSlug("Ngày Quốc tế Hạnh phúc"), EventDay: 20, EventMonth: 3, IsRecurring: true, EventType: models.EventTypeWorldDay, Country: "Quốc tế", ShortDescription: "Ngày Quốc tế Hạnh phúc do Liên Hợp Quốc công nhận", Importance: models.EventImportanceMedium, Tags: pq.StringArray{"hạnh phúc", "UN"}, IsActive: true},
		{Title: "Ngày Trái Đất", Slug: utils.GenerateSlug("Ngày Trái Đất"), EventDay: 22, EventMonth: 4, IsRecurring: true, EventType: models.EventTypeWorldDay, Country: "Quốc tế", ShortDescription: "Ngày Trái Đất - bảo vệ môi trường toàn cầu", Importance: models.EventImportanceHigh, Tags: pq.StringArray{"môi trường", "Trái Đất"}, IsActive: true},
		{Title: "Ngày Quốc tế Hòa bình", Slug: utils.GenerateSlug("Ngày Quốc tế Hòa bình"), EventDay: 21, EventMonth: 9, IsRecurring: true, EventType: models.EventTypeWorldDay, Country: "Quốc tế", ShortDescription: "Ngày Quốc tế Hòa bình do Liên Hợp Quốc thiết lập", Importance: models.EventImportanceMedium, Tags: pq.StringArray{"hòa bình", "UN"}, IsActive: true},
		{Title: "Ngày Nhân quyền Quốc tế", Slug: utils.GenerateSlug("Ngày Nhân quyền Quốc tế"), EventDay: 10, EventMonth: 12, IsRecurring: true, EventType: models.EventTypeWorldDay, Country: "Quốc tế", ShortDescription: "Kỷ niệm ngày ký kết Tuyên ngôn Nhân quyền Quốc tế", Importance: models.EventImportanceMedium, Tags: pq.StringArray{"nhân quyền", "UN"}, IsActive: true},
		{Title: "Ngày Quốc tế Nước", Slug: utils.GenerateSlug("Ngày Quốc tế Nước"), EventDay: 22, EventMonth: 3, IsRecurring: true, EventType: models.EventTypeWorldDay, Country: "Quốc tế", ShortDescription: "Ngày Nước Thế giới - nâng cao nhận thức về tài nguyên nước", Importance: models.EventImportanceMedium, Tags: pq.StringArray{"nước", "môi trường"}, IsActive: true},
		{Title: "Ngày Sách và Bản quyền Thế giới", Slug: utils.GenerateSlug("Ngày Sách và Bản quyền Thế giới"), EventDay: 23, EventMonth: 4, IsRecurring: true, EventType: models.EventTypeWorldDay, Country: "Quốc tế", ShortDescription: "Ngày Sách và Bản quyền Thế giới do UNESCO phát động", Importance: models.EventImportanceMedium, Tags: pq.StringArray{"sách", "UNESCO", "văn hóa đọc"}, IsActive: true},
		{Title: "Ngày Môi trường Thế giới", Slug: utils.GenerateSlug("Ngày Môi trường Thế giới"), EventDay: 5, EventMonth: 6, IsRecurring: true, EventType: models.EventTypeWorldDay, Country: "Quốc tế", ShortDescription: "Ngày Môi trường Thế giới do UNEP phát động", Importance: models.EventImportanceHigh, Tags: pq.StringArray{"môi trường", "UNEP"}, IsActive: true},

		// Historical events (one-time)
		{Title: "Chiến thắng Điện Biên Phủ", Slug: utils.GenerateSlug("Chiến thắng Điện Biên Phủ"), EventDay: 7, EventMonth: 5, EventYear: intPtr(1954), IsRecurring: true, EventType: models.EventTypeHistorical, Country: "Việt Nam", CountryCode: "VN", FlagEmoji: "🇻🇳", ShortDescription: "Chiến thắng Điện Biên Phủ 7/5/1954 - chấm dứt thực dân Pháp ở Đông Dương", Importance: models.EventImportanceHigh, Tags: pq.StringArray{"lịch sử", "quân sự", "Điện Biên Phủ"}, IsActive: true},
		{Title: "Khởi nghĩa Lam Sơn", Slug: utils.GenerateSlug("Khởi nghĩa Lam Sơn"), EventDay: 2, EventMonth: 1, EventYear: intPtr(1418), IsRecurring: false, EventType: models.EventTypeHistorical, Country: "Việt Nam", CountryCode: "VN", FlagEmoji: "🇻🇳", ShortDescription: "Lê Lợi phất cờ khởi nghĩa Lam Sơn chống giặc Minh 1418", Importance: models.EventImportanceHigh, Tags: pq.StringArray{"khởi nghĩa", "Lê Lợi"}, IsActive: true},
		{Title: "Vua Quang Trung đại phá quân Thanh", Slug: utils.GenerateSlug("Vua Quang Trung đại phá quân Thanh"), EventDay: 30, EventMonth: 1, EventYear: intPtr(1789), IsRecurring: true, EventType: models.EventTypeHistorical, Country: "Việt Nam", CountryCode: "VN", FlagEmoji: "🇻🇳", ShortDescription: "Mùng 5 Tết Kỷ Dậu 1789, vua Quang Trung đại phá 20 vạn quân Thanh", Importance: models.EventImportanceHigh, Tags: pq.StringArray{"Quang Trung", "Tây Sơn", "quân sự"}, IsActive: true},
	}

	created := 0
	for _, e := range events {
		var existing models.Event
		if db.Where("slug = ?", e.Slug).First(&existing).Error == nil {
			continue
		}
		if err := db.Create(&e).Error; err != nil {
			fmt.Printf("   ❌ Failed to create '%s': %v\n", e.Title, err)
			continue
		}
		created++
	}
	fmt.Printf("   ✅ Created %d events (skipped %d existing)\n", created, len(events)-created)
}

// seedFolkFestivals creates Vietnamese folk festivals.
func seedFolkFestivals(db *gorm.DB) {
	fmt.Println("\n🏮 Seeding Folk Festivals...")

	intPtr := func(i int) *int { return &i }

	festivals := []models.FolkFestival{
		{Name: "Tết Nguyên Đán", Slug: utils.GenerateSlug("Tết Nguyên Đán"), AlternateName: "Tết Cả, Tết Ta", CalendarType: models.CalendarTypeLunar, LunarDay: intPtr(1), LunarMonth: intPtr(1), DurationDays: 7, FestivalType: models.FestivalTypeFolk, Region: "Cả nước", Country: "Việt Nam", ShortDescription: "Tết Nguyên Đán là lễ hội lớn nhất và quan trọng nhất của người Việt, đánh dấu sự khởi đầu năm mới Âm lịch", Traditions: pq.StringArray{"Gói bánh chưng, bánh tét", "Chúc Tết, mừng tuổi", "Hái lộc đầu xuân", "Xông đất", "Thăm mộ tổ tiên"}, Importance: models.EventImportanceHigh, Tags: pq.StringArray{"tết", "truyền thống", "gia đình"}, IsActive: true},
		{Name: "Tết Nguyên Tiêu", Slug: utils.GenerateSlug("Tết Nguyên Tiêu"), AlternateName: "Rằm tháng Giêng", CalendarType: models.CalendarTypeLunar, LunarDay: intPtr(15), LunarMonth: intPtr(1), DurationDays: 1, FestivalType: models.FestivalTypeFolk, Region: "Cả nước", Country: "Việt Nam", ShortDescription: "Rằm tháng Giêng - lễ hội đèn lồng, cầu an đầu năm", Traditions: pq.StringArray{"Đi chùa cầu an", "Thả đèn hoa đăng", "Ăn chè trôi nước"}, Importance: models.EventImportanceMedium, Tags: pq.StringArray{"rằm", "cầu an"}, IsActive: true},
		{Name: "Tết Hàn Thực", Slug: utils.GenerateSlug("Tết Hàn Thực"), CalendarType: models.CalendarTypeLunar, LunarDay: intPtr(3), LunarMonth: intPtr(3), DurationDays: 1, FestivalType: models.FestivalTypeFolk, Region: "Miền Bắc", Country: "Việt Nam", ShortDescription: "Ngày mùng 3 tháng 3 Âm lịch - làm bánh trôi, bánh chay cúng tổ tiên", Traditions: pq.StringArray{"Làm bánh trôi, bánh chay", "Cúng tổ tiên"}, Importance: models.EventImportanceMedium, Tags: pq.StringArray{"bánh trôi", "tổ tiên"}, IsActive: true},
		{Name: "Tết Đoan Ngọ", Slug: utils.GenerateSlug("Tết Đoan Ngọ"), AlternateName: "Tết diệt sâu bọ, Tết giữa năm", CalendarType: models.CalendarTypeLunar, LunarDay: intPtr(5), LunarMonth: intPtr(5), DurationDays: 1, FestivalType: models.FestivalTypeFolk, Region: "Cả nước", Country: "Việt Nam", ShortDescription: "Mùng 5 tháng 5 Âm lịch - tục diệt sâu bọ, ăn hoa quả", Traditions: pq.StringArray{"Ăn rượu nếp", "Ăn hoa quả", "Tắm lá mùi", "Hái thuốc nam"}, Importance: models.EventImportanceMedium, Tags: pq.StringArray{"diệt sâu bọ", "mùa hè"}, IsActive: true},
		{Name: "Tết Trung Thu", Slug: utils.GenerateSlug("Tết Trung Thu"), AlternateName: "Tết Trung Nguyên, Tết thiếu nhi", CalendarType: models.CalendarTypeLunar, LunarDay: intPtr(15), LunarMonth: intPtr(8), DurationDays: 1, FestivalType: models.FestivalTypeFolk, Region: "Cả nước", Country: "Việt Nam", ShortDescription: "Rằm tháng 8 Âm lịch - lễ hội trăng rằm, phá cỗ, rước đèn cho thiếu nhi", Traditions: pq.StringArray{"Rước đèn lồng", "Múa lân", "Phá cỗ trông trăng", "Ăn bánh trung thu"}, Importance: models.EventImportanceHigh, Tags: pq.StringArray{"trung thu", "thiếu nhi", "trăng rằm"}, IsActive: true},
		{Name: "Tết Trung Nguyên", Slug: utils.GenerateSlug("Tết Trung Nguyên"), AlternateName: "Lễ Vu Lan, Rằm tháng 7", CalendarType: models.CalendarTypeLunar, LunarDay: intPtr(15), LunarMonth: intPtr(7), DurationDays: 1, FestivalType: models.FestivalTypeReligion, Region: "Cả nước", Country: "Việt Nam", ShortDescription: "Rằm tháng 7 - Lễ Vu Lan báo hiếu, xá tội vong nhân", Traditions: pq.StringArray{"Lễ Vu Lan báo hiếu", "Cúng cô hồn", "Đi chùa cầu siêu", "Phóng sinh"}, Importance: models.EventImportanceHigh, Tags: pq.StringArray{"vu lan", "báo hiếu", "tháng 7"}, IsActive: true},
		{Name: "Tết Ông Công Ông Táo", Slug: utils.GenerateSlug("Tết Ông Công Ông Táo"), CalendarType: models.CalendarTypeLunar, LunarDay: intPtr(23), LunarMonth: intPtr(12), DurationDays: 1, FestivalType: models.FestivalTypeFolk, Region: "Cả nước", Country: "Việt Nam", ShortDescription: "23 tháng Chạp - tiễn Ông Công Ông Táo về trời", Traditions: pq.StringArray{"Cúng ông Công ông Táo", "Thả cá chép", "Dọn dẹp nhà cửa"}, Importance: models.EventImportanceMedium, Tags: pq.StringArray{"ông táo", "cuối năm"}, IsActive: true},
		{Name: "Giỗ Tổ Hùng Vương", Slug: utils.GenerateSlug("Giỗ Tổ Hùng Vương"), CalendarType: models.CalendarTypeLunar, LunarDay: intPtr(10), LunarMonth: intPtr(3), DurationDays: 1, FestivalType: models.FestivalTypeNational, Region: "Cả nước, trung tâm tại Phú Thọ", Country: "Việt Nam", ShortDescription: "Mùng 10 tháng 3 Âm lịch - ngày Giỗ Tổ Hùng Vương, quốc lễ", Traditions: pq.StringArray{"Dâng hương Đền Hùng", "Hội trại", "Rước kiệu"}, Importance: models.EventImportanceHigh, Tags: pq.StringArray{"Hùng Vương", "quốc lễ", "Phú Thọ"}, IsActive: true},
		{Name: "Lễ hội Chùa Hương", Slug: utils.GenerateSlug("Lễ hội Chùa Hương"), CalendarType: models.CalendarTypeLunar, LunarDay: intPtr(6), LunarMonth: intPtr(1), DurationDays: 90, FestivalType: models.FestivalTypeReligion, Region: "Mỹ Đức, Hà Nội", Country: "Việt Nam", ShortDescription: "Lễ hội kéo dài từ mùng 6 tháng Giêng đến hết tháng 3 Âm lịch", Traditions: pq.StringArray{"Đi thuyền trên suối Yến", "Lễ Phật tại các động", "Leo núi hành hương"}, Importance: models.EventImportanceHigh, Tags: pq.StringArray{"chùa Hương", "hành hương", "Hà Nội"}, IsActive: true},
		{Name: "Hội Lim", Slug: utils.GenerateSlug("Hội Lim"), CalendarType: models.CalendarTypeLunar, LunarDay: intPtr(13), LunarMonth: intPtr(1), DurationDays: 3, FestivalType: models.FestivalTypeFolk, Region: "Tiên Du, Bắc Ninh", Country: "Việt Nam", ShortDescription: "Lễ hội Quan họ - di sản văn hóa phi vật thể UNESCO", Traditions: pq.StringArray{"Hát Quan họ", "Đấu vật", "Đánh đu", "Thi nấu cơm"}, Importance: models.EventImportanceHigh, Tags: pq.StringArray{"Quan họ", "UNESCO", "Bắc Ninh"}, IsActive: true},
		{Name: "Lễ hội Đền Trần", Slug: utils.GenerateSlug("Lễ hội Đền Trần"), CalendarType: models.CalendarTypeLunar, LunarDay: intPtr(14), LunarMonth: intPtr(1), DurationDays: 3, FestivalType: models.FestivalTypeFolk, Region: "Nam Định", Country: "Việt Nam", ShortDescription: "Lễ Khai ấn Đền Trần - cầu may mắn, tài lộc đầu năm", Traditions: pq.StringArray{"Khai ấn", "Lễ dâng hương", "Rước kiệu"}, Importance: models.EventImportanceHigh, Tags: pq.StringArray{"Đền Trần", "khai ấn", "Nam Định"}, IsActive: true},
		{Name: "Tết Thanh Minh", Slug: utils.GenerateSlug("Tết Thanh Minh"), CalendarType: models.CalendarTypeSolar, SolarDay: intPtr(5), SolarMonth: intPtr(4), DurationDays: 1, FestivalType: models.FestivalTypeFolk, Region: "Cả nước", Country: "Việt Nam", ShortDescription: "Tiết Thanh Minh - tảo mộ, tưởng nhớ tổ tiên", Traditions: pq.StringArray{"Tảo mộ", "Cúng tổ tiên", "Sửa sang mộ phần"}, Importance: models.EventImportanceMedium, Tags: pq.StringArray{"tảo mộ", "tổ tiên"}, IsActive: true},
		{Name: "Lễ hội Bà Chúa Xứ", Slug: utils.GenerateSlug("Lễ hội Bà Chúa Xứ"), CalendarType: models.CalendarTypeLunar, LunarDay: intPtr(23), LunarMonth: intPtr(4), DurationDays: 5, FestivalType: models.FestivalTypeReligion, Region: "Châu Đốc, An Giang", Country: "Việt Nam", ShortDescription: "Lễ hội Vía Bà Chúa Xứ Núi Sam - lễ hội lớn nhất miền Tây Nam Bộ", Traditions: pq.StringArray{"Lễ tắm Bà", "Rước kiệu", "Dâng hương"}, Importance: models.EventImportanceHigh, Tags: pq.StringArray{"Bà Chúa Xứ", "An Giang", "miền Tây"}, IsActive: true},
		{Name: "Tết Nguyên Đán - Giao thừa", Slug: utils.GenerateSlug("Tết Nguyên Đán Giao thừa"), CalendarType: models.CalendarTypeLunar, LunarDay: intPtr(30), LunarMonth: intPtr(12), DurationDays: 1, FestivalType: models.FestivalTypeFolk, Region: "Cả nước", Country: "Việt Nam", ShortDescription: "Đêm Giao thừa - khoảnh khắc chuyển giao giữa năm cũ và năm mới", Traditions: pq.StringArray{"Cúng Giao thừa", "Bắn pháo hoa", "Đón năm mới"}, Importance: models.EventImportanceHigh, Tags: pq.StringArray{"giao thừa", "tết"}, IsActive: true},
	}

	created := 0
	for _, f := range festivals {
		var existing models.FolkFestival
		if db.Where("slug = ?", f.Slug).First(&existing).Error == nil {
			continue
		}
		if err := db.Create(&f).Error; err != nil {
			fmt.Printf("   ❌ Failed to create '%s': %v\n", f.Name, err)
			continue
		}
		created++
	}
	fmt.Printf("   ✅ Created %d folk festivals (skipped %d existing)\n", created, len(festivals)-created)
}

// seedContentPermissions adds content management permissions and assigns them.
func seedContentPermissions(db *gorm.DB) {
	fmt.Println("\n🔐 Seeding Content Permissions...")

	permissions := []models.Permission{
		// Content Management
		{Name: "content.read", DisplayName: "Xem nội dung", Module: "content", Action: "read", Description: "Xem bài viết, danh ngôn, nhân vật, sự kiện, lễ hội"},
		{Name: "content.create", DisplayName: "Tạo nội dung", Module: "content", Action: "create", Description: "Tạo mới bài viết, danh ngôn, nhân vật, sự kiện, lễ hội"},
		{Name: "content.update", DisplayName: "Cập nhật nội dung", Module: "content", Action: "update", Description: "Chỉnh sửa bài viết, danh ngôn, nhân vật, sự kiện, lễ hội"},
		{Name: "content.delete", DisplayName: "Xóa nội dung", Module: "content", Action: "delete", Description: "Xóa bài viết, danh ngôn, nhân vật, sự kiện, lễ hội"},
		{Name: "content.publish", DisplayName: "Xuất bản nội dung", Module: "content", Action: "publish", Description: "Xuất bản hoặc gỡ bài viết"},
	}

	for _, perm := range permissions {
		var existing models.Permission
		if db.Where("name = ?", perm.Name).First(&existing).Error == nil {
			fmt.Printf("   ⏭️  Permission '%s' already exists, skipping\n", perm.Name)
			continue
		}
		if err := db.Create(&perm).Error; err != nil {
			fmt.Printf("   ❌ Failed to create permission '%s': %v\n", perm.Name, err)
			continue
		}
		fmt.Printf("   ✅ Created permission: %s\n", perm.Name)
	}

	// Assign content permissions to roles
	fmt.Println("\n🔗 Assigning content permissions to roles...")

	rolePermissions := map[string][]string{
		models.RoleSuperAdmin: {"content.read", "content.create", "content.update", "content.delete", "content.publish"},
		models.RoleAdmin:      {"content.read", "content.create", "content.update", "content.delete", "content.publish"},
		models.RoleEditor:     {"content.read", "content.create", "content.update", "content.publish"},
		models.RoleViewer:     {"content.read"},
	}

	for roleName, permNames := range rolePermissions {
		var role models.Role
		if err := db.Where("name = ?", roleName).First(&role).Error; err != nil {
			fmt.Printf("   ❌ Role '%s' not found: %v\n", roleName, err)
			continue
		}

		assignedCount := 0
		for _, permName := range permNames {
			var perm models.Permission
			if err := db.Where("name = ?", permName).First(&perm).Error; err != nil {
				continue
			}

			var existing models.RolePermission
			if db.Where("role_id = ? AND permission_id = ?", role.ID, perm.ID).First(&existing).Error == nil {
				continue
			}

			rp := models.RolePermission{
				RoleID:       role.ID,
				PermissionID: perm.ID,
			}
			if err := db.Create(&rp).Error; err != nil {
				continue
			}
			assignedCount++
		}

		if assignedCount > 0 {
			fmt.Printf("   ✅ Assigned %d content permissions to: %s\n", assignedCount, roleName)
		} else {
			fmt.Printf("   ⏭️  Role '%s' already has content permissions\n", roleName)
		}
	}
}
