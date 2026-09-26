-- コースサムネイル画像を差し替え（V7 適用済み環境向け）
UPDATE public.courses
SET thumbnail_url = '/uploads/course_thumbnail_java_basic.png',
    updated_at = CURRENT_TIMESTAMP
WHERE title = '1章:Java基礎';

UPDATE public.courses
SET thumbnail_url = '/uploads/course_thumbnail_git.png',
    updated_at = CURRENT_TIMESTAMP
WHERE title = '2章:Git';

UPDATE public.courses
SET thumbnail_url = '/uploads/course_thumbnail_java_advanced.png',
    updated_at = CURRENT_TIMESTAMP
WHERE title = '3章:Java応用';

UPDATE public.courses
SET thumbnail_url = '/uploads/course_thumbnail_sql.png',
    updated_at = CURRENT_TIMESTAMP
WHERE title = '4章:SQL';

UPDATE public.courses
SET thumbnail_url = '/uploads/course_thumbnail_spring.png',
    updated_at = CURRENT_TIMESTAMP
WHERE title = '5章:Spring';

UPDATE public.courses
SET thumbnail_url = '/uploads/course_thumbnail_docker.png',
    updated_at = CURRENT_TIMESTAMP
WHERE title = '6章:Docker';

UPDATE public.courses
SET thumbnail_url = REPLACE(thumbnail_url, '/uploads/', '${s3BaseUrl}/')
WHERE thumbnail_url LIKE '/uploads/%';
