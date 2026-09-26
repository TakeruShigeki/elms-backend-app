-- コース・レッスングループ・レッスンの初期データを差し替える
-- user_lessons は lessons 削除時に CASCADE で消える
DELETE FROM public.courses;

INSERT INTO public.courses (course_order, thumbnail_url, title, description) VALUES (1024.0000, '/uploads/course_thumbnail_java_basic.png', '1章:Java基礎', '変数・演算子・条件分岐からオブジェクト指向の基礎まで、Javaプログラミングの土台を体系的に学びます。');
INSERT INTO public.courses (course_order, thumbnail_url, title, description) VALUES (2048.0000, '/uploads/course_thumbnail_git.png', '2章:Git', 'バージョン管理の基本からブランチ運用、Pull Request、コンフリクト解消まで、チーム開発に必要なGit/GitHubスキルを習得します。');
INSERT INTO public.courses (course_order, thumbnail_url, title, description) VALUES (3072.0000, '/uploads/course_thumbnail_java_advanced.png', '3章:Java応用', '継承・ポリモーフィズム・コレクションからStream API・Optionalまで、実務で頻出するJavaの応用テーマを学びます。');
INSERT INTO public.courses (course_order, thumbnail_url, title, description) VALUES (4096.0000, '/uploads/course_thumbnail_sql.png', '4章:SQL', 'SELECTからJOIN・集計・トランザクション・DDLまで、データベース操作の基礎を実践課題で身につけます。');
INSERT INTO public.courses (course_order, thumbnail_url, title, description) VALUES (5120.0000, '/uploads/course_thumbnail_spring.png', '5章:Spring', 'Spring Bootを使ったWeb API開発。DI、Spring Data JDBC、バリデーション、トランザクションまで体系的に学びます。');
INSERT INTO public.courses (course_order, thumbnail_url, title, description) VALUES (6144.0000, '/uploads/course_thumbnail_docker.png', '6章:Docker', 'コンテナの基礎からDockerfile、Docker Compose、ネットワークまで。総合課題でタスク管理APIの構築にも挑戦します。');

INSERT INTO public.lesson_groups (course_id, lesson_group_order, title)
SELECT c.id, 1024.0000, '導入'
FROM public.courses c WHERE c.title = '1章:Java基礎';
INSERT INTO public.lesson_groups (course_id, lesson_group_order, title)
SELECT c.id, 2048.0000, '基礎文法'
FROM public.courses c WHERE c.title = '1章:Java基礎';
INSERT INTO public.lesson_groups (course_id, lesson_group_order, title)
SELECT c.id, 3072.0000, 'オブジェクト指向'
FROM public.courses c WHERE c.title = '1章:Java基礎';

INSERT INTO public.lesson_groups (course_id, lesson_group_order, title)
SELECT c.id, 4096.0000, '概要と環境構築'
FROM public.courses c WHERE c.title = '2章:Git';
INSERT INTO public.lesson_groups (course_id, lesson_group_order, title)
SELECT c.id, 5120.0000, 'ローカルリポジトリの操作'
FROM public.courses c WHERE c.title = '2章:Git';
INSERT INTO public.lesson_groups (course_id, lesson_group_order, title)
SELECT c.id, 6144.0000, 'チーム開発'
FROM public.courses c WHERE c.title = '2章:Git';
INSERT INTO public.lesson_groups (course_id, lesson_group_order, title)
SELECT c.id, 7168.0000, 'リポジトリを1から作成（個人開発用）'
FROM public.courses c WHERE c.title = '2章:Git';

INSERT INTO public.lesson_groups (course_id, lesson_group_order, title)
SELECT c.id, 8192.0000, '応用'
FROM public.courses c WHERE c.title = '3章:Java応用';
INSERT INTO public.lesson_groups (course_id, lesson_group_order, title)
SELECT c.id, 9216.0000, '実務頻出'
FROM public.courses c WHERE c.title = '3章:Java応用';
INSERT INTO public.lesson_groups (course_id, lesson_group_order, title)
SELECT c.id, 10240.0000, '実践課題'
FROM public.courses c WHERE c.title = '3章:Java応用';

INSERT INTO public.lesson_groups (course_id, lesson_group_order, title)
SELECT c.id, 11264.0000, '基礎理解'
FROM public.courses c WHERE c.title = '4章:SQL';
INSERT INTO public.lesson_groups (course_id, lesson_group_order, title)
SELECT c.id, 12288.0000, 'データ取得'
FROM public.courses c WHERE c.title = '4章:SQL';
INSERT INTO public.lesson_groups (course_id, lesson_group_order, title)
SELECT c.id, 13312.0000, 'データ操作'
FROM public.courses c WHERE c.title = '4章:SQL';
INSERT INTO public.lesson_groups (course_id, lesson_group_order, title)
SELECT c.id, 14336.0000, '集計'
FROM public.courses c WHERE c.title = '4章:SQL';
INSERT INTO public.lesson_groups (course_id, lesson_group_order, title)
SELECT c.id, 15360.0000, 'テーブル設計'
FROM public.courses c WHERE c.title = '4章:SQL';
INSERT INTO public.lesson_groups (course_id, lesson_group_order, title)
SELECT c.id, 16384.0000, '複数テーブル'
FROM public.courses c WHERE c.title = '4章:SQL';
INSERT INTO public.lesson_groups (course_id, lesson_group_order, title)
SELECT c.id, 17408.0000, '安全性'
FROM public.courses c WHERE c.title = '4章:SQL';

INSERT INTO public.lesson_groups (course_id, lesson_group_order, title)
SELECT c.id, 18432.0000, '第1章｜Spring Bootで最初のAPIを動かしてみよう'
FROM public.courses c WHERE c.title = '5章:Spring';
INSERT INTO public.lesson_groups (course_id, lesson_group_order, title)
SELECT c.id, 19456.0000, '第2章｜Springの最重要機能「DI」について学ぼう'
FROM public.courses c WHERE c.title = '5章:Spring';
INSERT INTO public.lesson_groups (course_id, lesson_group_order, title)
SELECT c.id, 20480.0000, '第3章｜DBとつないでユーザー一覧APIを作成しよう'
FROM public.courses c WHERE c.title = '5章:Spring';
INSERT INTO public.lesson_groups (course_id, lesson_group_order, title)
SELECT c.id, 21504.0000, '第4章｜ユーザー管理CRUD APIを完成させよう'
FROM public.courses c WHERE c.title = '5章:Spring';
INSERT INTO public.lesson_groups (course_id, lesson_group_order, title)
SELECT c.id, 22528.0000, '第5章｜実践的なバリデーションとエラーハンドリングを学ぼう'
FROM public.courses c WHERE c.title = '5章:Spring';
INSERT INTO public.lesson_groups (course_id, lesson_group_order, title)
SELECT c.id, 23552.0000, '第6章｜トランザクションとロックを理解しよう'
FROM public.courses c WHERE c.title = '5章:Spring';
INSERT INTO public.lesson_groups (course_id, lesson_group_order, title)
SELECT c.id, 24576.0000, '第7章｜一対多を実装しよう'
FROM public.courses c WHERE c.title = '5章:Spring';

INSERT INTO public.lesson_groups (course_id, lesson_group_order, title)
SELECT c.id, 25600.0000, '導入と準備'
FROM public.courses c WHERE c.title = '6章:Docker';
INSERT INTO public.lesson_groups (course_id, lesson_group_order, title)
SELECT c.id, 26624.0000, 'dockerコマンド'
FROM public.courses c WHERE c.title = '6章:Docker';
INSERT INTO public.lesson_groups (course_id, lesson_group_order, title)
SELECT c.id, 27648.0000, 'コンテナを触ってみる'
FROM public.courses c WHERE c.title = '6章:Docker';
INSERT INTO public.lesson_groups (course_id, lesson_group_order, title)
SELECT c.id, 28672.0000, 'データの永続化'
FROM public.courses c WHERE c.title = '6章:Docker';
INSERT INTO public.lesson_groups (course_id, lesson_group_order, title)
SELECT c.id, 29696.0000, 'イメージ'
FROM public.courses c WHERE c.title = '6章:Docker';
INSERT INTO public.lesson_groups (course_id, lesson_group_order, title)
SELECT c.id, 30720.0000, 'ネットワーク'
FROM public.courses c WHERE c.title = '6章:Docker';
INSERT INTO public.lesson_groups (course_id, lesson_group_order, title)
SELECT c.id, 31744.0000, 'Docker Compose'
FROM public.courses c WHERE c.title = '6章:Docker';
INSERT INTO public.lesson_groups (course_id, lesson_group_order, title)
SELECT c.id, 32768.0000, '総合課題'
FROM public.courses c WHERE c.title = '6章:Docker';

-- lessons
INSERT INTO public.lessons (lesson_group_id, course_id, lesson_order, title, content, video_url)
SELECT lg.id, lg.course_id, v.lesson_order, v.title, v.content, v.video_url
FROM (
  VALUES
    ('1章:Java基礎', '導入', 1024.0000, '導入', '[レッスン資料を開く](https://drive.google.com/file/d/1BADzhkPYL8pCY2KY-xCNgtMsGxpmabJ8/view?usp=drive_link)', 'https://drive.google.com/file/d/1NO83lbAkhlDD6h3xbX0CX8rUbdUcInuD/view?usp=drive_link'),
    ('1章:Java基礎', '導入', 2048.0000, 'Javaの環境構築（Windowsの方向け）', '[レッスン資料を開く](https://drive.google.com/file/d/1ulh6xqqtgiCo-6ZqIKWomb62q6BLlDyO/view?usp=drive_link)', 'https://drive.google.com/file/d/1oB-bdDTvFxwRk5kRaRXBxC4myaHd_pc1/view?usp=drive_link'),
    ('1章:Java基礎', '導入', 3072.0000, 'Javaの環境構築（Macの方向け）', '[レッスン資料を開く](https://drive.google.com/file/d/1ulh6xqqtgiCo-6ZqIKWomb62q6BLlDyO/view?usp=drive_link)', 'https://drive.google.com/file/d/1th6GZ_gIRMYMr0pjFogT10-B5KeZFjag/view?usp=drive_link'),
    ('1章:Java基礎', '導入', 4096.0000, 'Javaプログラムの基本構造', '[レッスン資料を開く](https://drive.google.com/file/d/128424tj7IwvycvFa1EFbLRd_m1yjK5UR/view?usp=drive_link)', 'https://drive.google.com/file/d/17wGI6m0hMm7zertYhGIH0FIZWXfBHyOg/view?usp=drive_link'),
    ('1章:Java基礎', '導入', 5120.0000, 'IntelliJにCodeStyleを導入しよう', '[レッスン資料を開く](https://drive.google.com/file/d/14xCoPNCLx4Y6p4bibi5QNDH4zIwC6nbI/view?usp=drive_link)', 'https://drive.google.com/file/d/1W5titzRPeqKc3dHFxgPZF5Y9MxiG1aeu/view?usp=drive_link'),
    ('1章:Java基礎', '基礎文法', 1024.0000, '変数と定数', '[レッスン資料を開く](https://drive.google.com/file/d/1o0aFKL1l_ZBUwXHdzNc6b2DC_Gwavhi_/view?usp=drive_link)', 'https://drive.google.com/file/d/1Hx9WUAEJaKYuXcDjCAWx7nDOy8_Pi-qx/view?usp=drive_link'),
    ('1章:Java基礎', '基礎文法', 2048.0000, '演算子', '[レッスン資料を開く](https://drive.google.com/file/d/1xe_yjxyvFVFwmIRWLXWIkmyoME_pVsKf/view?usp=drive_link)', 'https://drive.google.com/file/d/1imRO53zerY11A5LgHOYgrTavzKhT9G_I/view?usp=drive_link'),
    ('1章:Java基礎', '基礎文法', 3072.0000, '条件分岐', '[レッスン資料を開く](https://drive.google.com/file/d/1JcrGyZZS5-Ow-go5C_no_9hmrqFI1_W6/view?usp=drive_link)', 'https://drive.google.com/file/d/1mPjuZLDlsHUckfJLXfJB7Cv2KxTJhE1p/view?usp=drive_link'),
    ('1章:Java基礎', '基礎文法', 4096.0000, '配列', '[レッスン資料を開く](https://drive.google.com/file/d/1eZp38Y_XlU_GNKaliKSQFqYVHuQc0DD4/view?usp=drive_link)', 'https://drive.google.com/file/d/1L_IZr_LdtAKkH4_DVk_cItz_sGnOP-D6/view?usp=drive_link'),
    ('1章:Java基礎', '基礎文法', 5120.0000, '繰り返し処理', '[レッスン資料を開く](https://drive.google.com/file/d/1LZp2GqxImJfm145On_LdcoJlFLqkfZ55/view?usp=drive_link)', 'https://drive.google.com/file/d/13K2pJn1G1lAkstGVkaSo8Vp-NPEGsNO-/view?usp=drive_link'),
    ('1章:Java基礎', 'オブジェクト指向', 1024.0000, 'クラス（概念）', '[レッスン資料を開く](https://drive.google.com/file/d/1PRQTwSmCNAGvE4Hstxdx0pPTVk0XGvBC/view?usp=drive_link)', 'https://drive.google.com/file/d/1jwcEwbMTYuJ_Xt7miB95dAyTurNfOsqG/view?usp=drive_link'),
    ('1章:Java基礎', 'オブジェクト指向', 2048.0000, 'クラス（実践）', '[レッスン資料を開く](https://drive.google.com/file/d/1l1IcHYRmDN31_POjXSsyyWfUdHb5EBCv/view?usp=drive_link)', 'https://drive.google.com/file/d/1a0pC1aX6FGGVTRj1QBSU_V_j5XWSVB8C/view?usp=drive_link'),
    ('1章:Java基礎', 'オブジェクト指向', 3072.0000, 'カプセル化', '[レッスン資料を開く](https://drive.google.com/file/d/1AGGAMfRz4Asl_rBefXoEQSweVEhgorQo/view?usp=drive_link)', 'https://drive.google.com/file/d/1N4nJlTDMd3dF3RRY_H3q5tM-fT4dn2SJ/view?usp=drive_link'),
    ('1章:Java基礎', 'オブジェクト指向', 4096.0000, 'コンストラクタ', '[レッスン資料を開く](https://drive.google.com/file/d/1f2HJByjbOa2BL_HUz3s17waCIupICbuV/view?usp=drive_link)', 'https://drive.google.com/file/d/1lYC_mlcQcuhLANpNcyDvVQqhsdX6fw7q/view?usp=drive_link'),
    ('1章:Java基礎', 'オブジェクト指向', 5120.0000, 'デバッグ', '[レッスン資料を開く](https://drive.google.com/file/d/1CDqmDvxT0dLxVgGVgp4c3E_X_sQfEfaX/view?usp=drive_link)', 'https://drive.google.com/file/d/1AIgEUJqHyZzy2PrG6EoeQe7fNvrWFmz5/view?usp=drive_link'),
    ('1章:Java基礎', 'オブジェクト指向', 6144.0000, 'オーバーロード（メソッド、コンストラクタ）', '[レッスン資料を開く](https://drive.google.com/file/d/1yNJL-ZZGcFjydJ7HLfyDu29VfCxgpDKB/view?usp=drive_link)', 'https://drive.google.com/file/d/1SvrgPcfwFgR7W34SqkHNokylO7Iji_Q-/view?usp=drive_link'),
    ('1章:Java基礎', 'オブジェクト指向', 7168.0000, 'パッケージ、インポート', '[レッスン資料を開く](https://drive.google.com/file/d/1PU37YzrJIErFvsVe-wpwhgdFk23tB6y-/view?usp=drive_link)', 'https://drive.google.com/file/d/10-Tz6DQQSjmW3UT0jBmjktht_pmWB26U/view?usp=drive_link'),
    ('1章:Java基礎', 'オブジェクト指向', 8192.0000, 'クラスフィールド、クラスメソッド', '[レッスン資料を開く](https://drive.google.com/file/d/1WDrD3gCrMfihhWch6s8zHj0ZLO9iaLo4/view?usp=drive_link)', 'https://drive.google.com/file/d/1aXbHLupKpWEsqqKXRTcoLp8X1ZbWkxXp/view?usp=drive_link'),
    ('2章:Git', '概要と環境構築', 1024.0000, 'GitとGitHubとは何か', '[レッスン資料を開く](https://app.notion.com/p/Git-01-Git-GitHub-37d5dee69c3781389968df002e890b09)', NULL),
    ('2章:Git', '概要と環境構築', 2048.0000, 'Gitの環境構築とSSH設定', '[レッスン資料を開く](https://app.notion.com/p/Git-02-Git-SSH-37d5dee69c37813a88edf8ae3e7b990e)', NULL),
    ('2章:Git', 'ローカルリポジトリの操作', 1024.0000, 'リポジトリをcloneしよう', '[レッスン資料を開く](https://app.notion.com/p/Git-03-clone-37d5dee69c3781789e1ae5fcf1f35b78)', NULL),
    ('2章:Git', 'ローカルリポジトリの操作', 2048.0000, '3つのエリアを理解しよう', '[レッスン資料を開く](https://app.notion.com/p/Git-04-3-37d5dee69c37810b8dfbdb0971616fd8)', NULL),
    ('2章:Git', 'ローカルリポジトリの操作', 3072.0000, 'ファイルを修正してステージング・コミットしよう', '[レッスン資料を開く](https://app.notion.com/p/Git-05-37d5dee69c3781b383eef763b05a2445)', NULL),
    ('2章:Git', 'ローカルリポジトリの操作', 4096.0000, 'ブランチとHEADを理解しよう', '[レッスン資料を開く](https://app.notion.com/p/Git-06-HEAD-37d5dee69c37817f805ee6aa3030a0c7)', NULL),
    ('2章:Git', 'ローカルリポジトリの操作', 5120.0000, '作業用ブランチを作ってコードを修正しよう', '[レッスン資料を開く](https://app.notion.com/p/Git-07-37d5dee69c3781ce9301fb6551344a70)', NULL),
    ('2章:Git', 'ローカルリポジトリの操作', 6144.0000, '変更をステージングしてコミットしよう', '[レッスン資料を開く](https://app.notion.com/p/Git-08-37d5dee69c37812eb84ecb9fff4608b6)', NULL),
    ('2章:Git', 'チーム開発', 1024.0000, 'GitHubにpushしてPull Requestを作ろう', '[レッスン資料を開く](https://app.notion.com/p/Git-09-GitHub-push-Pull-Request-37d5dee69c378153a154d047b8f31a79)', NULL),
    ('2章:Git', 'チーム開発', 2048.0000, 'git fetchとgit pullでチームの変更を取り込もう', '[レッスン資料を開く](https://app.notion.com/p/Git-10-git-fetch-git-pull-37f5dee69c3781d19c22d8c44965432b)', NULL),
    ('2章:Git', 'チーム開発', 3072.0000, 'コンフリクトを解消しよう', '[レッスン資料を開く](https://app.notion.com/p/Git-11-37f5dee69c3781228276cd4caaf8996b)', NULL),
    ('2章:Git', 'リポジトリを1から作成（個人開発用）', 1024.0000, 'git initで自分のリポジトリを作ろう', '[レッスン資料を開く](https://app.notion.com/p/Git-12-git-init-3845dee69c37811cbb02f2beb8cd0341)', NULL),
    ('3章:Java応用', '応用', 1024.0000, '標準ライブラリ', '[レッスン資料を開く](https://drive.google.com/file/d/1LSk8oW84VK8m25fx9eX8_a2ulx0Cf1GR/view?usp=drive_link)', 'https://drive.google.com/file/d/1J0cHkgNIAtWmM4-u0YtHlxKwmdEJMZ_I/view?usp=drive_link'),
    ('3章:Java応用', '応用', 2048.0000, 'ラッパークラス', '[レッスン資料を開く](https://drive.google.com/file/d/1EGWbfmKvivAWgSzqC2jMi--9FtzHnlho/view?usp=drive_link)', 'https://drive.google.com/file/d/1q6eMJp9QpsBydSz6KtakBMiUzlKW7qWO/view?usp=drive_link'),
    ('3章:Java応用', '応用', 3072.0000, '日付ライブラリ', '[レッスン資料を開く](https://drive.google.com/file/d/1up1bbRtqJBAzPjRd4LU4tRygjIj64V5_/view?usp=drive_link)', 'https://drive.google.com/file/d/18tFQBTVBzUdPeHJTeexE-zMAzx3rpqvl/view?usp=drive_link'),
    ('3章:Java応用', '応用', 4096.0000, '継承', '[レッスン資料を開く](https://drive.google.com/file/d/1dfoASuCB5SZjn2NJOdQLJ-E0Qhd2vBXf/view?usp=drive_link)', 'https://drive.google.com/file/d/17zJBbrjuvsSs8nihwlPBmAtegin0QgTD/view?usp=drive_link'),
    ('3章:Java応用', '応用', 5120.0000, 'オーバーライド', '[レッスン資料を開く](https://drive.google.com/file/d/1ytiJZxyNHcLxXgV_yLjgBHbjVJ32PWnz/view?usp=drive_link)', 'https://drive.google.com/file/d/1axq7UyGB-WXVBe2yBO_za2U5wyuPS-zl/view?usp=drive_link'),
    ('3章:Java応用', '応用', 6144.0000, '抽象クラス、抽象メソッド', '[レッスン資料を開く](https://drive.google.com/file/d/14qlutm-uJyZQ7qLX9zukWfrqw-9rCGaA/view?usp=drive_link)', 'https://drive.google.com/file/d/17dmdeYLwJiBsbpSqdoE6ic_EZEvoNorc/view?usp=drive_link'),
    ('3章:Java応用', '応用', 7168.0000, 'インターフェースと実装', '[レッスン資料を開く](https://drive.google.com/file/d/1YGyz5k1sRYGDIpKqK-xRpdDIgodDhTgS/view?usp=drive_link)', 'https://drive.google.com/file/d/1DgWjvtySO7rUe3J7ybgK-A2VkRuBx8XC/view?usp=drive_link'),
    ('3章:Java応用', '応用', 8192.0000, 'ポリモーフィズム', '[レッスン資料を開く](https://drive.google.com/file/d/1CXONJsh59sg3mItO6EY-mWgu43njGIRW/view?usp=drive_link)', 'https://drive.google.com/file/d/1HNXMIt-j5ocp8D67lhf-XvMfQVuXw8JL/view?usp=drive_link'),
    ('3章:Java応用', '応用', 9216.0000, 'コレクションフレームワーク（List, Map, Set）', '[レッスン資料を開く](https://drive.google.com/file/d/1-cg5CLvph2aBr__BdaMOZZOFdGyAh1nZ/view?usp=drive_link)', 'https://drive.google.com/file/d/1382CmvY7ewEx3g4uz7HL4y8gKGXkaXmK/view?usp=drive_link'),
    ('3章:Java応用', '応用', 10240.0000, '例外処理', '[レッスン資料を開く](https://drive.google.com/file/d/1R80wIVkkfUewc1hzLSO7p9IsgDpgf_c9/view?usp=sharing)', 'https://drive.google.com/file/d/1Gpob_RWF-lf2LiBwJII7Fuy8dObxrp1t/view?usp=drive_link'),
    ('3章:Java応用', '応用', 11264.0000, 'Objectクラス', '[レッスン資料を開く](https://drive.google.com/file/d/1P_97IxPsYN58jfepZn_mNNbyGnQ1-8k9/view?usp=drive_link)', 'https://drive.google.com/file/d/1GsjaiOzSjux2a-ETyds2KrhXjBSzKtLS/view?usp=drive_link'),
    ('3章:Java応用', '実務頻出', 1024.0000, '関数型インターフェース、ラムダ式、メソッド参照', '[レッスン資料を開く](https://drive.google.com/file/d/1i5Y2ntwMNTFjmQKdcuYViHlMHGj5Lm0e/view?usp=drive_link)', 'https://drive.google.com/file/d/1RN-oRLU7p7G7e671PDX9zmAC-5q0401F/view?usp=drive_link'),
    ('3章:Java応用', '実務頻出', 2048.0000, 'StreamAPI', '[レッスン資料を開く](https://drive.google.com/file/d/1f_x4AbcaZ-iplR7JneA-eCVGp9wemkkV/view?usp=drive_link)', 'https://drive.google.com/file/d/1f-9bZizAT_e6uOLYRvlBb6PsAYppL4ux/view?usp=drive_link'),
    ('3章:Java応用', '実務頻出', 3072.0000, 'Optional', '[レッスン資料を開く](https://drive.google.com/file/d/1dTEv86fUTE8DDOG-zDfwp9iTW8bWPDNy/view?usp=drive_link)', 'https://drive.google.com/file/d/1fYp9plAoCfltsOWUpd1_iSFhKnUQ9sNR/view?usp=drive_link'),
    ('3章:Java応用', '実務頻出', 4096.0000, 'Enum', '[レッスン資料を開く](https://drive.google.com/file/d/1GSbsMO1oNIYtPQ5Wr6p8xweVkq2xa6kW/view?usp=drive_link)', 'https://drive.google.com/file/d/1-uyhDM3MIetOKHfXcd3-AAek1RisHArc/view?usp=drive_link'),
    ('3章:Java応用', '実践課題', 1024.0000, 'Java実践課題：じゃんけんゲームを作ろう', '[レッスン資料を開く](https://app.notion.com/p/Java-3865dee69c3780b98eb3d709492c5aa4)', NULL),
    ('4章:SQL', '基礎理解', 1024.0000, 'データベースとSQLとは？', '[レッスン資料を開く](https://www.notion.so/SQL-1-SQL-3525dee69c3780a0b619ee38c3bf4b20)', NULL),
    ('4章:SQL', 'データ取得', 1024.0000, 'データを取り出してみよう（SELECT）', '[レッスン資料を開く](https://www.notion.so/SQL-2-SELECT-3525dee69c378011890bd2cad0ce38da)', NULL),
    ('4章:SQL', 'データ操作', 1024.0000, 'データを追加・更新・削除しよう（INSERT・UPDATE・DELETE）', '[レッスン資料を開く](https://www.notion.so/SQL-3-INSERT-UPDATE-DELETE-3535dee69c3780ab84d8d218c46a2f08)', NULL),
    ('4章:SQL', 'データ取得', 2048.0000, '条件でデータを絞り込もう（WHERE）', '[レッスン資料を開く](https://www.notion.so/SQL-4-WHERE-3535dee69c37817d9581ea8defc78fd1)', NULL),
    ('4章:SQL', 'データ取得', 3072.0000, '結果を整理しよう（ORDER BY・DISTINCT・LIMIT・UNION）', '[レッスン資料を開く](https://www.notion.so/SQL-5-ORDER-BY-DISTINCT-LIMIT-UNION-3535dee69c378140bee3dd7c9c6d5046)', NULL),
    ('4章:SQL', '集計', 1024.0000, 'データを集計しよう（GROUP BY・HAVING）', '[レッスン資料を開く](https://www.notion.so/SQL-6-GROUP-BY-HAVING-3535dee69c37819fa26dd1af8e45b65f)', NULL),
    ('4章:SQL', 'テーブル設計', 1024.0000, 'テーブルを設計しよう（DDL・制約）', '[レッスン資料を開く](https://www.notion.so/SQL-7-DDL-3535dee69c3781b98c11cf640975dd7e)', NULL),
    ('4章:SQL', '複数テーブル', 1024.0000, '複数テーブルを結合しよう（JOIN）', '[レッスン資料を開く](https://www.notion.so/SQL-8-JOIN-3535dee69c37811e81eae4e741a6f5c8)', NULL),
    ('4章:SQL', '複数テーブル', 2048.0000, 'クエリの中にクエリを書こう（サブクエリ）', '[レッスン資料を開く](https://www.notion.so/SQL-9-3535dee69c37814592bdc07d0dce4720)', NULL),
    ('4章:SQL', '安全性', 1024.0000, 'データを安全に更新しよう（トランザクション）', '[レッスン資料を開く](https://www.notion.so/SQL-10-3535dee69c3781ea81a0c3e9b90c16cc)', NULL),
    ('4章:SQL', '安全性', 2048.0000, '実務で使える便利機能（インデックス・ビュー）', '[レッスン資料を開く](https://www.notion.so/SQL-11-3535dee69c3781609bbcc7135e45c6d6)', NULL),
    ('5章:Spring', '第1章｜Spring Bootで最初のAPIを動かしてみよう', 1024.0000, 'Springの概要', '[レッスン資料を開く](https://www.notion.so/1-1-Spring-35b5dee69c37811291dee6bde9c4a9f2)', NULL),
    ('5章:Spring', '第1章｜Spring Bootで最初のAPIを動かしてみよう', 2048.0000, 'Spring Bootのプロジェクトを作成しよう', '[レッスン資料を開く](https://www.notion.so/1-2-Spring-Boot-35b5dee69c37819c83fde93ddcc1eaf3)', NULL),
    ('5章:Spring', '第1章｜Spring Bootで最初のAPIを動かしてみよう', 3072.0000, 'Spring Bootのプロジェクト構造を理解しよう', '[レッスン資料を開く](https://www.notion.so/1-3-Spring-Boot-35b5dee69c3781828bb3c4bd3f418e50)', NULL),
    ('5章:Spring', '第1章｜Spring Bootで最初のAPIを動かしてみよう', 4096.0000, 'REST APIとJSONを理解しよう', '[レッスン資料を開く](https://www.notion.so/1-4-REST-API-JSON-35b5dee69c3781f6ab55fd77b2f05a8b)', NULL),
    ('5章:Spring', '第1章｜Spring Bootで最初のAPIを動かしてみよう', 5120.0000, 'Postmanで最初のAPIを動かしてみよう', '[レッスン資料を開く](https://www.notion.so/1-5-Postman-API-35b5dee69c378134873ec0d13d5800b0)', NULL),
    ('5章:Spring', '第2章｜Springの最重要機能「DI」について学ぼう', 1024.0000, 'DIについて理解しよう（Beanの生成）', '[レッスン資料を開く](https://www.notion.so/2-1-DI-Bean-35b5dee69c3781fa8af3eb0a97d70f31?pvs=18)', NULL),
    ('5章:Spring', '第2章｜Springの最重要機能「DI」について学ぼう', 2048.0000, 'DIについて理解しよう（Beanの注入）', '[レッスン資料を開く](https://www.notion.so/2-2-DI-Bean-35b5dee69c3781cab7dbcff46e07c4c7)', NULL),
    ('5章:Spring', '第3章｜DBとつないでユーザー一覧APIを作成しよう', 1024.0000, 'FlywayでDB（PostgreSQL）に初期テーブル・初期データを作成しよう', '[レッスン資料を開く](https://www.notion.so/3-1-Flyway-DB-PostgreSQL-35b5dee69c37814e923af07b20abcd06)', NULL),
    ('5章:Spring', '第3章｜DBとつないでユーザー一覧APIを作成しよう', 2048.0000, 'ユーザー一覧取得APIを実装しよう', '[レッスン資料を開く](https://www.notion.so/3-2-API-35b5dee69c37811eb5b7ef69924dda61)', NULL),
    ('5章:Spring', '第3章｜DBとつないでユーザー一覧APIを作成しよう', 3072.0000, '派生クエリメソッドでユーザー一覧取得APIを改善しよう', '[レッスン資料を開く](https://www.notion.so/3-3-API-35b5dee69c3781e18965dd7088dadf41)', NULL),
    ('5章:Spring', '第3章｜DBとつないでユーザー一覧APIを作成しよう', 4096.0000, '@Queryでユーザー検索APIを作ろう', '[レッスン資料を開く](https://www.notion.so/3-4-Query-API-35b5dee69c3781d0a34df80c22a5e3f4)', NULL),
    ('5章:Spring', '第4章｜ユーザー管理CRUD APIを完成させよう', 1024.0000, 'ユーザー一覧取得APIを設計・実装しよう', '[レッスン資料を開く](https://app.notion.com/p/4-1-API-35b5dee69c3781a58e08e1d6bd2d7c45)', NULL),
    ('5章:Spring', '第4章｜ユーザー管理CRUD APIを完成させよう', 2048.0000, 'ユーザー詳細取得APIを設計・実装しよう', '[レッスン資料を開く](https://app.notion.com/p/4-2-API-3775dee69c3781eab2b8e00e32761690)', NULL),
    ('5章:Spring', '第4章｜ユーザー管理CRUD APIを完成させよう', 3072.0000, 'ユーザー登録APIを設計・実装しよう', '[レッスン資料を開く](https://app.notion.com/p/4-3-API-3775dee69c37817b9a9edcc17f6dae87)', NULL),
    ('5章:Spring', '第4章｜ユーザー管理CRUD APIを完成させよう', 4096.0000, 'ユーザー更新APIを設計・実装しよう', '[レッスン資料を開く](https://app.notion.com/p/4-4-API-3775dee69c3781029649ff99c4b2f810)', NULL),
    ('5章:Spring', '第4章｜ユーザー管理CRUD APIを完成させよう', 5120.0000, 'ユーザー削除APIを設計・実装しよう', '[レッスン資料を開く](https://app.notion.com/p/4-5-API-3775dee69c3781339cd0f9e7003ab306)', NULL),
    ('5章:Spring', '第5章｜実践的なバリデーションとエラーハンドリングを学ぼう', 1024.0000, 'リクエストを検証し、400を返そう', '[レッスン資料を開く](https://app.notion.com/p/5-1-400-3785dee69c3781ccb990fcc1d3adc24d)', NULL),
    ('5章:Spring', '第5章｜実践的なバリデーションとエラーハンドリングを学ぼう', 2048.0000, '全てのエラーを共通のフォーマットで返そう', '[レッスン資料を開く](https://app.notion.com/p/5-2-37b5dee69c3781acb307e88c9ef0f368)', NULL),
    ('5章:Spring', '第6章｜トランザクションとロックを理解しよう', 1024.0000, '@Transactionalについて理解しよう', '[レッスン資料を開く](https://app.notion.com/p/6-1-Transactional-37b5dee69c3781ecbd89df76beaa6c9e)', NULL),
    ('5章:Spring', '第6章｜トランザクションとロックを理解しよう', 2048.0000, '同時リクエストされても大丈夫なように、ロックを実装しよう', '[レッスン資料を開く](https://app.notion.com/p/6-2-37b5dee69c37817db7efe89215a1e408)', NULL),
    ('5章:Spring', '第7章｜一対多を実装しよう', 1024.0000, 'ユーザーとその趣味という1対多を実装しよう', '[レッスン資料を開く](https://app.notion.com/p/7-1-1-37b5dee69c3781e1b15fe5effbb441e5)', NULL),
    ('6章:Docker', '導入と準備', 1024.0000, 'Dockerとは？', '[レッスン資料を開く](https://www.notion.so/Docker-_Docker-30b5dee69c378065b9b7d779f139cf9c)', NULL),
    ('6章:Docker', '導入と準備', 2048.0000, 'Dockerの環境構築とHelloWorld', '[レッスン資料を開く](https://www.notion.so/Docker-_Docker-HelloWorld-30f5dee69c37807db1f7f9ce76ca493d)', NULL),
    ('6章:Docker', 'dockerコマンド', 1024.0000, 'dockerコマンドの基本と、コンテナのライフサイクル', '[レッスン資料を開く](https://www.notion.so/Docker-_docker-30f5dee69c37806fac37e78e9174c85b)', NULL),
    ('6章:Docker', 'コンテナを触ってみる', 1024.0000, 'Webサーバを立ち上げてみよう【ポートフォワーディング】', '[レッスン資料を開く](https://www.notion.so/Docker-_Web-30f5dee69c3780ffb1b4d12a5bad7478)', NULL),
    ('6章:Docker', 'コンテナを触ってみる', 2048.0000, 'コンテナの中に入ってみよう【docker exec】', '[レッスン資料を開く](https://www.notion.so/Docker-_-docker-exec-3105dee69c3780469e11d3ac7b4cce98)', NULL),
    ('6章:Docker', 'コンテナを触ってみる', 3072.0000, 'コンテナ内のファイルを書き換えてみよう【docker cp】', '[レッスン資料を開く](https://www.notion.so/Docker-_-docker-cp-3125dee69c3780d0b698c5746d930722)', NULL),
    ('6章:Docker', 'データの永続化', 1024.0000, 'コンテナを消してもデータを守る方法1【バインドマウント】', '[レッスン資料を開く](https://www.notion.so/Docker-_-1-3105dee69c37800f962cd2e1772119b9)', NULL),
    ('6章:Docker', 'データの永続化', 2048.0000, 'コンテナを消してもデータを守る方法2【ボリュームマウント】', '[レッスン資料を開く](https://www.notion.so/Docker-_-2-3245dee69c378071bfa3d274d644ad27)', NULL),
    ('6章:Docker', 'イメージ', 1024.0000, 'Docker Hubとイメージ', '[レッスン資料を開く](https://www.notion.so/Docker-_Docker-Hub-3245dee69c3780f8bfddf6dfe5316ced)', NULL),
    ('6章:Docker', 'イメージ', 2048.0000, '手動でイメージを作る方法【docker commit】', '[レッスン資料を開く](https://www.notion.so/Docker-_-docker-commit-3155dee69c3780ea93e5ecea1d76b668)', NULL),
    ('6章:Docker', 'イメージ', 3072.0000, '設定ファイルからイメージを作る方法【Dockerfile】', '[レッスン資料を開く](https://www.notion.so/Docker-_-Dockerfile-3155dee69c37800a9adec55ab6995a99)', NULL),
    ('6章:Docker', 'ネットワーク', 1024.0000, 'コンテナ間で通信できるようにする【ネットワーク】', '[レッスン資料を開く](https://www.notion.so/Docker-_-3155dee69c37805ca640f243f6079da6)', NULL),
    ('6章:Docker', 'Docker Compose', 1024.0000, '複数コンテナを一発起動する【Docker Compose】', '[レッスン資料を開く](https://app.notion.com/p/Docker-_-Docker-Compose-ee85dee69c3782cd921c81c18e933b4d)', NULL),
    ('6章:Docker', '総合課題', 1024.0000, '総合課題：タスク管理APIを作成しよう', '[レッスン資料を開く](https://app.notion.com/p/Spring-API-3865dee69c37808a8905e272d6d0545b)', NULL)
) AS v(course_title, lesson_group_title, lesson_order, title, content, video_url)
JOIN public.courses c ON c.title = v.course_title
JOIN public.lesson_groups lg ON lg.course_id = c.id AND lg.title = v.lesson_group_title;

-- 本番など S3 利用時: Flyway placeholder でサムネイル URL を更新（dev では /uploads のまま）
UPDATE public.courses
SET thumbnail_url = REPLACE(thumbnail_url, '/uploads/', '${s3BaseUrl}/')
WHERE thumbnail_url LIKE '/uploads/%';
