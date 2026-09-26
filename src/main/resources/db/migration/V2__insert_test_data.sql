-- ユーザー
INSERT INTO public.users
(email_address, "password", real_name, user_name, thumbnail_url, user_role, created_at, updated_at) 
VALUES
('kanri@test.com', '$2a$10$ScES8tI69yGJbmutyo0JZuir1ywUpPcLRLS.qci2c7bc5eIlfxlpS', '管理者太郎', 'kanri123', '/uploads/kanrisya.png', 'ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO public.users 
(email_address, "password", real_name, user_name, thumbnail_url, user_role, created_at, updated_at) 
VALUES
('ippan@test.com', '$2a$10$ScES8tI69yGJbmutyo0JZuir1ywUpPcLRLS.qci2c7bc5eIlfxlpS', '一般次郎', 'ippan123', '/uploads/ippan.png', 'GENERAL', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- お知らせ
INSERT INTO public.news (title, "content", created_at, updated_at) VALUES
('なんでもお悩み相談会開催のご案内', '7月15日にGoogleMeetにて「なんでもお悩み相談会」を開催します。学習やキャリアの悩みなど、気軽にご参加ください。', '2025-04-20 10:00:00', '2025-04-20 10:00:00');
INSERT INTO public.news (title, "content", created_at, updated_at) VALUES
('ポートフォリオ相談会のご案内', '就職活動に向けたポートフォリオの個別相談会を7月第2週に開催します。実際の作品に対して具体的なフィードバックを行います。', '2025-06-25 14:00:00', '2025-06-25 14:00:00');
INSERT INTO public.news (title, "content", created_at, updated_at) VALUES
('実務案件のご紹介', '現在、受講生には実務案件に参画していただくことが可能です。学んだスキルを実践の場で試し、成長できるチャンスです。希望者は私までご相談ください。', '2025-09-10 12:00:00', '2025-09-10 12:00:00');
INSERT INTO public.news (title, "content", created_at, updated_at) VALUES
('転職相談会のお知らせ', '11月1日〜3日に私が直接、転職相談会を実施します。履歴書・職務経歴書の添削や企業選びのアドバイスを行いますので、気軽にご参加ください。', '2025-10-25 13:00:00', '2025-10-25 13:00:00');
INSERT INTO public.news (title, "content", created_at, updated_at) VALUES
('年末年始の授業スケジュールについて', '12月29日〜1月3日は年末年始休講となります。年明けの授業再開は1月4日からを予定しています。振替授業については別途ご案内します。', '2025-12-01 11:45:00', '2025-12-01 11:45:00');

-- コース
INSERT INTO public.courses (course_order, thumbnail_url, title, description) VALUES(1024.0000, '/uploads/course_thumbnail_sample.png', '1章:Java', 'Javaの基本文法、条件分岐・繰り返し処理、オブジェクト指向、例外処理までを体系的に学び、開発の基礎を固めます。');
INSERT INTO public.courses (course_order, thumbnail_url, title, description) VALUES(2048.0000, '/uploads/course_thumbnail_sample.png', '2章:SQL', 'リレーショナルデータベースの基礎から、SELECT文・JOIN・サブクエリ・集計関数など実務で必要なSQLスキルを習得します。');
INSERT INTO public.courses (course_order, thumbnail_url, title, description) VALUES(3072.0000, '/uploads/course_thumbnail_sample.png', '3章:HTML&CSS', 'Webページの構造を作るHTMLと、見た目を整えるCSSの基本からレスポンシブ対応まで、Web制作の土台を学びます。');
INSERT INTO public.courses (course_order, thumbnail_url, title, description) VALUES(4096.0000, '/uploads/course_thumbnail_sample.png', '4章:JavaScript', '変数・関数・制御構文などの基本から、DOM操作やイベント処理、非同期通信まで動的Web開発の基礎を習得します。');
INSERT INTO public.courses (course_order, thumbnail_url, title, description) VALUES(5120.0000, '/uploads/course_thumbnail_sample.png', '5章:Git', 'Gitによるバージョン管理の基本操作から、ブランチ運用、マージ、GitHubでのチーム開発までを実践的に学びます。');
INSERT INTO public.courses (course_order, thumbnail_url, title, description) VALUES(6144.0000, '/uploads/course_thumbnail_sample.png', '6章:Spring', 'Spring Bootを使ったアプリ開発を通じて、MVCモデル、DI、REST APIなどのJavaバックエンド技術を体系的に学習します。');
INSERT INTO public.courses (course_order, thumbnail_url, title, description) VALUES(7168.0000, '/uploads/course_thumbnail_sample.png', '7章:Docker', 'Dockerの仕組みや基本コマンドから、開発環境のコンテナ化、本番環境へのデプロイ方法まで幅広く学びます。');
INSERT INTO public.courses (course_order, thumbnail_url, title, description) VALUES(8192.0000, '/uploads/course_thumbnail_sample.png', '8章:AWS', 'AWSの基本概念を理解し、EC2やS3、RDSなど主要サービスを使って、クラウド上にアプリを構築・運用する方法を学びます。');
INSERT INTO public.courses (course_order, thumbnail_url, title, description) VALUES(9216.0000, '/uploads/course_thumbnail_sample.png', '9章:CI/CD', 'GitHub Actionsを用いて、ソースコードの自動テスト、ビルド、デプロイまでを自動化するCI/CD環境の構築を学びます。');

-- レッスングループ
-- '1章:Java' の実IDを使って lesson_groups を挿入
INSERT INTO public.lesson_groups (course_id, lesson_group_order, title)
SELECT c.id, x.lesson_group_order, x.title
FROM (VALUES
  (1024.0000, 'Java導入'),
  (2048.0000, 'Java基礎')
) AS x(lesson_group_order, title)
JOIN public.courses c ON c.title = '1章:Java';

-- レッスン
-- lessons を “グループ名 + コース名” で正しく紐付けて一括投入
INSERT INTO public.lessons
  (lesson_group_id, course_id, lesson_order, title, description, video_url)
SELECT
  lg.id,                 -- 実在する lesson_groups.id
  lg.course_id,          -- グループが属する courses.id をそのまま採用（安全）
  v.lesson_order,
  v.title,
  v.description,
  v.video_url
FROM (
  VALUES
    ('Java導入',  1024.0000, '導入', 'この講座について…', 'https://drive.google.com/file/d/1NO83lbAkhlDD6h3xbX0CX8rUbdUcInuD/view'),
    ('Java導入',  2048.0000, 'Javaの環境構築（Windowsの方向け）', 'Windowsの方向け…', 'https://drive.google.com/file/d/1oB-bdDTvFxwRk5kRaRXBxC4myaHd_pc1/view'),
    ('Java導入',  3072.0000, 'Javaの環境構築（Macの方向け）', 'Macの方向け…', 'https://drive.google.com/file/d/1th6GZ_gIRMYMr0pjFogT10-B5KeZFjag/view'),
    ('Java導入',  4096.0000, 'Javaプログラムの基本構造', NULL, 'https://drive.google.com/file/d/17wGI6m0hMm7zertYhGIH0FIZWXfBHyOg/view'),
    ('Java導入',  5120.0000, 'IntelliJにCodeStyleを導入しよう', NULL, 'https://drive.google.com/file/d/1W5titzRPeqKc3dHFxgPZF5Y9MxiG1aeu/view'),
    ('Java基礎',  6144.0000, '変数と定数', NULL, 'https://drive.google.com/file/d/1Hx9WUAEJaKYuXcDjCAWx7nDOy8_Pi-qx/view'),
    ('Java基礎',  7168.0000, '演算子', NULL, 'https://drive.google.com/file/d/1imRO53zerY11A5LgHOYgrTavzKhT9G_I/view'),
    ('Java基礎',  8192.0000, '条件分岐（if文、switch文）', NULL, 'https://drive.google.com/file/d/1mPjuZLDlsHUckfJLXfJB7Cv2KxTJhE1p/view'),
    ('Java基礎',  9216.0000, '配列', NULL, 'https://drive.google.com/file/d/1L_IZr_LdtAKkH4_DVk_cItz_sGnOP-D6/view'),
    ('Java基礎', 10240.0000, '繰り返し処理（for文、while文、do while文、拡張for文、break、continue）', NULL, 'https://drive.google.com/file/d/13K2pJn1G1lAkstGVkaSo8Vp-NPEGsNO-/view')
) AS v(lesson_group_title, lesson_order, title, description, video_url)
JOIN public.lesson_groups lg
  ON lg.title = v.lesson_group_title
JOIN public.courses c
  ON c.id = lg.course_id
 AND c.title = '1章:Java';  -- 念のためコースも固定（他コースの同名グループと誤結合しない