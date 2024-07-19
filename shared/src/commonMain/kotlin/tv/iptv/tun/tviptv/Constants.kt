package tv.iptv.tun.tviptv

object Constants {

    const val DATE_TIME_FORMAT_0700 = "yyyyMMddHHmmss +0700"
    const val DATE_TIME_FORMAT = "yyyyMMddHHmmss"

    const val EXTRA_KEY_SCTV_CONFIG: String = "sctv_config"
    const val USER_AGENT: String =
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/106.0.0.0 Safari/537.36"
    const val ACCEPT_LANGUAGE =
        "vi-VN,vi;q=0.9,fr-FR;q=0.8,fr;q=0.7,en-US;q=0.6,en;q=0.5,am;q=0.4,en-AU;q=0.3"

    val mapping by lazy {
        mapOf(
            "thvl1-hd" to "vinhlong1hd",
            "thvl2-hd" to "vinhlong2hd",
            "thvl3-hd" to "vinhlong3hd",
            "thvl4-hd" to "vinhlong4hd",
            "vtv-can-tho" to "vtv6",
            "vtc2-reidius-tv" to "vtc2",
            "vtc16-hd" to "vtc16",
            "vtc4-yeah1-family-hd" to "vtc4",
            "vtc7-todaytv-hd" to "vtc7|todaytv",
            "vtc10-1" to "vtc10",
            "vtc11-kids-tv" to "vtc11",
            "htvc-du-lich-cuoc-song" to "htvcdulichhd",
            "htvc-du-lich-cuoc-song" to "htvcdulichhd",
            "an-giang-1" to "angiang",
            "bac-giang-hd" to "bacgiang",
            "bac-kan-1" to "backan",
            "bac-ninh-1" to "bacninh",
            "ben-tre-1-2" to "bentre",
            "binh-dinh" to "binhdinh",
            "binh-duong-1" to "binhduong1",
            "binh-duong-2" to "binhduong2",
            "binh-duong-4" to "binhduong4",
            "binh-phuoc-1" to "binhphuoc1",
            "binh-phuoc-2" to "binhphuoc2",
            "binh-thuan-1" to "binh-thuan",
            "ca-mau-1" to "camau",
            "can-tho-1" to "cantho",
            "dien-bien-20" to "dienbien",
            "dong-thap-1" to "dongthap",
            "gia-lai-1" to "gialai",
            "ha-giang-1" to "hagiang",
            "ha-nam-1" to "hanam",
            "ha-noi-2-2021" to "hanoi2",
            "ha-tinh-1" to "hatinh",
            "hai-phong-1" to "haiphong",
            "hau-giang-1" to "haugiang",
            "hoa-binh-1" to "hoabinh",
            "khanh-hoa-1" to "khanhhoa",
            "kon-tum-1" to "kontum",
            "lang-son-1" to "langson",
            "long-an-1" to "longan",
            "nghe-an-1" to "nghean",
            "ninh-binh-11" to "ninhbinh",
            "ninh-thuan-1" to "ninhthuan",
            "quang-binh-1" to "quangbinh",
            "quang-nam-1" to "quangnam",
            "quang-ngai-1" to "quangngai",
            "quang-ninh-1" to "quangninh1",
            "quang-ninh-3" to "quangninh3",
            "quang-tri-1" to "quangtri",
            "soc-trang-1" to "soctrang",
            "tay-ninh-3" to "tayninh",
            "thai-binh-1" to "thaibinh",
            "thai-nguyen-1" to "thainguyen",
            "thanh-hoa-48" to "thanhhoa",
            "thua-thien-hue-1" to "hue",
            "tien-giang-1" to "tiengiang",
            "tra-vinh-1" to "travinh",
            "tuyen-quang-1" to "tuyenquang",
            "vinh-phuc-1" to "vinhphuc",
            "ba-ria-vung-tau-1" to "vungtau",
            "yen-bai-1" to "yenbai",
            "nhan-dan-hd" to "nhandan",
            "quoc-hoi-viet-nam-hd" to "quochoi",
            "vnews-hd" to "ttxvnhd",
            "quoc-phong-hd" to "qpvnhd",
            "an-ninh-hd" to "antvhd|antv"
        )
    }
}