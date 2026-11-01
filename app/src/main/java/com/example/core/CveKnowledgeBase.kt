package com.example.core

import com.example.data.CveVulnerability

object CveKnowledgeBase {

    val CURATED_CVES = listOf(
        // Telnet
        CveVulnerability(
            cveId = "CVE-2020-10188",
            cvssScore = 9.8f,
            affectedService = "Telnet",
            titleEn = "Telnet Daemon Buffer Overflow & Plaintext CLI",
            titleAr = "فيض ذاكرة مؤقتة ونقل غير مشفر في خدمة Telnet",
            descriptionEn = "Telnet transmits all credentials and terminal keystrokes in unencrypted plaintext, vulnerable to passive sniffing and buffer manipulation.",
            descriptionAr = "يرسل بروتوكول Telnet بيانات الاعتماد والأوامر بنص صريح غير مشفر مما يسمح باعتراضها أو التلاعب بذاكرة النظام.",
            remediationEn = "Disable telnetd immediately. Replace with SSH (port 22) using Ed25519 or RSA-4096 keys.",
            remediationAr = "عطّل خدمة telnetd فوراً واستبدلها بـ SSH (منفذ 22) مع مفاتيح تشفير قوية."
        ),
        CveVulnerability(
            cveId = "CVE-2022-39028",
            cvssScore = 9.8f,
            affectedService = "Telnet",
            titleEn = "Embedded Router Telnet Factory Credentials Exposure",
            titleAr = "كشف بيانات مصنعية افتراضية عبر منفذ Telnet في الموجهات",
            descriptionEn = "Many home routers and IoT cameras keep an open Telnet port with hardcoded factory credentials accessible to LAN clients.",
            descriptionAr = "العديد من الموجهات وأجهزة IoT تترك منفذ Telnet مفتوحاً بكلمات مرور مصنعية افتراضية يمكن للشبكة الوصول إليها.",
            remediationEn = "Disable Telnet in router administration settings or flash updated vendor firmware.",
            remediationAr = "عطّل وصول Telnet من لوحة تحكم الموجه أو قم بتحديث البرنامج الثابت (Firmware)."
        ),

        // FTP
        CveVulnerability(
            cveId = "CVE-2015-3306",
            cvssScore = 9.8f,
            affectedService = "FTP",
            titleEn = "ProFTPD mod_copy Arbitrary File Execution",
            titleAr = "ثغرة نسخ الملفات غير المصرح به في ProFTPD mod_copy",
            descriptionEn = "The mod_copy module in ProFTPD allows unauthenticated remote attackers to read and write arbitrary files via SITE CPFR/CPTO commands.",
            descriptionAr = "تتيح وحدة mod_copy في خادم ProFTPD قراءة وكتابة ملفات عشوائية دون مصادقة.",
            remediationEn = "Disable mod_copy or upgrade ProFTPD to 1.3.5b or later. Migrate to SFTP.",
            remediationAr = "عطّل إضافة mod_copy أو قم بترقية ProFTPD إلى 1.3.5b فأحدث، أو انتقل إلى SFTP."
        ),
        CveVulnerability(
            cveId = "CVE-2011-2523",
            cvssScore = 9.8f,
            affectedService = "FTP",
            titleEn = "vsftpd 2.3.4 Backdoor Command Execution",
            titleAr = "ثغرة الباب الخلفي في خادم vsftpd 2.3.4",
            descriptionEn = "Legacy vsftpd 2.3.4 archive contains an intentional backdoor triggered by a smiley face in the username that opens a root shell on port 6200.",
            descriptionAr = "يحتوي أرشيف vsftpd 2.3.4 القديم على باب خلفي يفتح سطر أوامر بصلاحيات الجذر على المنفذ 6200.",
            remediationEn = "Immediately replace with patched vsftpd or SFTP.",
            remediationAr = "استبدل الحزمة فوراً بنسخة حديثة ومحمية أو استخدم SFTP."
        ),

        // SMB
        CveVulnerability(
            cveId = "CVE-2017-0144",
            cvssScore = 10.0f,
            affectedService = "SMB",
            titleEn = "EternalBlue SMBv1 Remote Code Execution",
            titleAr = "ثغرة EternalBlue في بروتوكول مشاركة الملفات SMBv1",
            descriptionEn = "A critical vulnerability in Microsoft SMBv1 protocol allows unauthenticated remote attackers to execute arbitrary code via crafted packets.",
            descriptionAr = "ثغرة حرجة تسمح بتنفيذ تعليمات برمجية عن بُعد دون مصادقة عبر حزم SMBv1 مصممة خصيصاً.",
            remediationEn = "Disable SMBv1 completely on all hosts. Enforce SMBv2/SMBv3 with packet signing.",
            remediationAr = "عطّل بروتوكول SMBv1 تماماً وافرض استخدام SMBv2/SMBv3 مع التوقيع المشفر."
        ),
        CveVulnerability(
            cveId = "CVE-2020-0796",
            cvssScore = 10.0f,
            affectedService = "SMB",
            titleEn = "SMBGhost SMBv3 Compression Integer Overflow",
            titleAr = "ثغرة SMBGhost في ضغط بيانات بروتوكول SMBv3",
            descriptionEn = "An integer overflow vulnerability exists in the way Microsoft Server Message Block 3.1.1 handles specially crafted compressed requests.",
            descriptionAr = "ثغرة في معالجة طلبات الضغط لبروتوكول SMB 3.1.1 تسمح بالسيطرة على الخادم عن بُعد.",
            remediationEn = "Apply Microsoft KB4551762 security patch or disable SMBv3 compression.",
            remediationAr = "قم بتثبيت تحديثات الأمان الرسمية أو تعطيل خاصية ضغط SMBv3."
        ),
        CveVulnerability(
            cveId = "CVE-2021-44142",
            cvssScore = 9.9f,
            affectedService = "SMB",
            titleEn = "Samba vfs_fruit Heap Out-of-Bounds Write",
            titleAr = "فيض ذاكرة في إضافة Samba vfs_fruit لمشاركة الملفات",
            descriptionEn = "All versions of Samba prior to 4.13.17 are vulnerable to an out-of-bounds heap read/write when parsing Apple Double extended attributes.",
            descriptionAr = "ثغرة كتابة خارج حدود الذاكرة في خوادم Samba تتيح تنفيذ كود بصلاحيات root.",
            remediationEn = "Update Samba to 4.13.17, 4.14.12, or 4.15.5 or remove fruit:aapl VFS module.",
            remediationAr = "حدّث خادم Samba إلى الإصدارات الآمنة أو احذف إضافة fruit VFS."
        ),

        // SSH
        CveVulnerability(
            cveId = "CVE-2023-48795",
            cvssScore = 5.9f,
            affectedService = "SSH",
            titleEn = "Terrapin Attack: SSH Prefix Truncation",
            titleAr = "هجوم Terrapin لقص بادئة تشفير بروتوكول SSH",
            descriptionEn = "BBP integrity vulnerability allows a Man-in-the-Middle attacker to truncate handshake messages when ChaCha20-Poly1305 or CBC-EtM are used.",
            descriptionAr = "تتيح الثغرة لمهاجم في المنتصف تعديل تسلسل مصافحة SSH عند استخدام خوارزميات ChaCha20-Poly1305 أو CBC معينة.",
            remediationEn = "Enable strict key exchange extension (kex-strict) in OpenSSH 9.6+ or Dropbear 2024.84+.",
            remediationAr = "فعّل امتداد التبادل الصارم (kex-strict) في OpenSSH 9.6+ أو Dropbear الحديث."
        ),
        CveVulnerability(
            cveId = "CVE-2024-6387",
            cvssScore = 8.1f,
            affectedService = "SSH",
            titleEn = "regreSSHion: OpenSSH Signal Handler Race Condition",
            titleAr = "ثغرة regreSSHion في معالج الإشارات لخادم OpenSSH",
            descriptionEn = "A race condition in OpenSSH server (sshd) SIGALRM handler allows unauthenticated remote code execution as root on 32-bit/64-bit Linux.",
            descriptionAr = "سباق زمني في معالج SIGALRM لخادم OpenSSH يتيح تنفيذ تعليمات برمجية بصلاحيات root.",
            remediationEn = "Upgrade OpenSSH to 9.8p1 or set LoginGraceTime 0 in sshd_config as interim workaround.",
            remediationAr = "قم بترقية OpenSSH إلى 9.8p1 أو ضبط LoginGraceTime 0 في إعدادات sshd."
        ),

        // HTTP & Embedded Web
        CveVulnerability(
            cveId = "CVE-2014-9222",
            cvssScore = 9.8f,
            affectedService = "HTTP",
            titleEn = "Misfortune Cookie (RomPager Embedded Web Server)",
            titleAr = "ثغرة كعكة الشؤم (Misfortune Cookie) في خوادم الموجهات RomPager",
            descriptionEn = "Allegro RomPager prior to 4.34 allows remote attackers to corrupt memory and gain administrative access via a crafted 'C107135021' HTTP Cookie header.",
            descriptionAr = "تتيح ثغرة RomPager الشائعة في ملايين الموجهات المنزلية السيطرة الإدارية الكاملة عبر ترويسة Cookie ملغومة.",
            remediationEn = "Update modem/router firmware immediately or disable remote WAN/LAN web administration.",
            remediationAr = "حدّث البرنامج الثابت للموجه فوراً أو عطّل واجهة الإدارة عبر الويب."
        ),
        CveVulnerability(
            cveId = "CVE-2021-33558",
            cvssScore = 7.5f,
            affectedService = "HTTP",
            titleEn = "Boa Web Server Arbitrary File Disclosure",
            titleAr = "تسريب ملفات النظام في خادم الويب المدمج Boa",
            descriptionEn = "Boa web server 0.94.14 contains path traversal allowing unauthenticated reading of arbitrary files from the filesystem.",
            descriptionAr = "يحتوي خادم Boa المستخدم في كاميرات وموجهات قديمة على ثغرة مسار تسمح بقراءة ملفات النظام.",
            remediationEn = "Migrate from discontinued Boa web server to a maintained lightweight server like lighttpd or nginx.",
            remediationAr = "استبدل خادم Boa غير المدعوم بخوادم حديثة مثل lighttpd أو nginx."
        ),
        CveVulnerability(
            cveId = "CVE-2017-17562",
            cvssScore = 9.8f,
            affectedService = "HTTP",
            titleEn = "GoAhead Web Server CGI Remote Code Execution",
            titleAr = "تنفيذ تعليمات برمجية عن بُعد في خادم GoAhead CGI",
            descriptionEn = "GoAhead web server before 3.6.5 mishandles HTTP requests that initialize CGI environment variables, leading to LD_PRELOAD code injection.",
            descriptionAr = "ثغرة في تهيئة متغيرات CGI لخادم GoAhead تسمح بحقن مكتبات LD_PRELOAD وتشغيل كود خبيث.",
            remediationEn = "Upgrade to GoAhead 3.6.5 or later.",
            remediationAr = "قم بترقية خادم GoAhead إلى 3.6.5 فما فوق."
        ),

        // RTSP / Streaming Video
        CveVulnerability(
            cveId = "CVE-2018-9995",
            cvssScore = 9.8f,
            affectedService = "RTSP",
            titleEn = "TBK / DVR IP Camera Authentication Bypass",
            titleAr = "تجاوز المصادقة وكشف كلمات مرور كاميرات المراقبة DVR",
            descriptionEn = "DVR and IP cameras using TBK software allow remote unauthenticated attackers to extract valid administrator credentials via crafted HTTP cookie request.",
            descriptionAr = "تتيح لكاميرات المراقبة وأجهزة التسجيل DVR تسريب كلمات مرور المشرف بدون مصادقة.",
            remediationEn = "Isolate video cameras on dedicated IoT VLAN without internet exposure, update device firmware.",
            remediationAr = "اعزل أجهزة المراقبة في شبكة VLAN منفصلة دون كشفها للإنترنت وحدّث برمجياتها."
        ),

        // DNS
        CveVulnerability(
            cveId = "CVE-2020-1350",
            cvssScore = 10.0f,
            affectedService = "DNS",
            titleEn = "SIGRed: Windows DNS Server Heap Overflow",
            titleAr = "ثغرة SIGRed الحرجة في خوادم أسماء النطاقات DNS",
            descriptionEn = "A remote code execution vulnerability exists in Windows DNS Server when it fails to properly handle requests.",
            descriptionAr = "ثغرة فيض في معالجة استعلامات SIG لخدمة DNS تسمح بالسيطرة على خادم النطاق.",
            remediationEn = "Apply Microsoft Security Update KB4565511 or enforce MaxFieldLength TCP registry limit.",
            remediationAr = "تثبيت التحديثات الأمنية الرسمية أو ضبط حد حجم حزم TCP في سجلات النظام."
        )
    )

    fun findMatchesForPort(port: Int, banner: String?): List<CveVulnerability> {
        val matches = mutableListOf<CveVulnerability>()
        val cleanBanner = banner?.lowercase() ?: ""

        when (port) {
            23 -> {
                matches.addAll(CURATED_CVES.filter { it.affectedService == "Telnet" })
            }
            21 -> {
                if (cleanBanner.contains("vsftpd")) {
                    matches.addAll(CURATED_CVES.filter { it.cveId == "CVE-2011-2523" })
                } else if (cleanBanner.contains("proftpd")) {
                    matches.addAll(CURATED_CVES.filter { it.cveId == "CVE-2015-3306" })
                } else {
                    matches.addAll(CURATED_CVES.filter { it.affectedService == "FTP" })
                }
            }
            445, 139 -> {
                matches.addAll(CURATED_CVES.filter { it.affectedService == "SMB" })
            }
            22 -> {
                matches.addAll(CURATED_CVES.filter { it.affectedService == "SSH" })
            }
            80, 8080, 8443, 8000 -> {
                if (cleanBanner.contains("rompager")) {
                    matches.addAll(CURATED_CVES.filter { it.cveId == "CVE-2014-9222" })
                } else if (cleanBanner.contains("boa")) {
                    matches.addAll(CURATED_CVES.filter { it.cveId == "CVE-2021-33558" })
                } else if (cleanBanner.contains("goahead")) {
                    matches.addAll(CURATED_CVES.filter { it.cveId == "CVE-2017-17562" })
                } else {
                    matches.addAll(CURATED_CVES.filter { it.affectedService == "HTTP" })
                }
            }
            554 -> {
                matches.addAll(CURATED_CVES.filter { it.affectedService == "RTSP" })
            }
            53 -> {
                matches.addAll(CURATED_CVES.filter { it.affectedService == "DNS" })
            }
        }

        return matches
    }
}
