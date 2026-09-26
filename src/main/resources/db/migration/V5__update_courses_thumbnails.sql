-- 不要なコースを削除（lesson_groups, lessons は ON DELETE CASCADE で連鎖削除）
DELETE FROM public.courses WHERE title IN ('3章:HTML&CSS', '4章:JavaScript', '8章:AWS', '9章:CI/CD');

-- Git を第3章に変更（course_order も詰める）
UPDATE public.courses SET
  title = '3章:Git',
  course_order = 3072.0000,
  thumbnail_url = '/uploads/course_thumbnail_git.png',
  description = 'ローカルでのコミット・チェックアウトといった基本操作から、GitHubを使ったブランチ・マージ・プルリクエストまで実践的に学べます。',
  updated_at = CURRENT_TIMESTAMP
WHERE title = '5章:Git';

-- Spring を第4章に変更
UPDATE public.courses SET
  title = '4章:Spring',
  course_order = 4096.0000,
  thumbnail_url = '/uploads/course_thumbnail_spring.png',
  description = 'DIの仕組みから始め、Spring MVC・バリデーション・Spring Data JDBC・REST APIまでWebバックエンド開発を体系的に学べます。',
  updated_at = CURRENT_TIMESTAMP
WHERE title = '6章:Spring';

-- Docker を第5章に変更
UPDATE public.courses SET
  title = '5章:Docker',
  course_order = 5120.0000,
  thumbnail_url = '/uploads/course_thumbnail_docker.png',
  description = 'コンテナの基礎とライフサイクルを理解した上で、Dockerfile・Docker Composeによる複数コンテナの管理まで実践的に学べます。',
  updated_at = CURRENT_TIMESTAMP
WHERE title = '7章:Docker';

-- Java のサムネイル・説明更新
UPDATE public.courses SET
  thumbnail_url = '/uploads/course_thumbnail_java.png',
  description = '変数・演算子・条件分岐などの基本文法から始まり、クラスやオブジェクト指向の概念、StreamAPI・Enumまで体系的に学べます。',
  updated_at = CURRENT_TIMESTAMP
WHERE title = '1章:Java';

-- SQL のサムネイル・説明更新
UPDATE public.courses SET
  thumbnail_url = '/uploads/course_thumbnail_sql.png',
  description = 'SELECT・INSERT・UPDATE・DELETEの基本から、JOIN・トランザクション・DDL・制約まで課題を通じて実践的に習得できます。',
  updated_at = CURRENT_TIMESTAMP
WHERE title = '2章:SQL';
