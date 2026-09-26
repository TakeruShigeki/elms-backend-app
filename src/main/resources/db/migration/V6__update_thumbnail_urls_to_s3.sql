-- ローカルパス（/uploads/）のサムネイルURLをS3 URLに更新する
-- すでにS3 URLの場合はWHERE条件に一致しないためスキップされる

UPDATE public.courses
SET thumbnail_url = REPLACE(thumbnail_url, '/uploads/', '${s3BaseUrl}/')
WHERE thumbnail_url LIKE '/uploads/%';

UPDATE public.users
SET thumbnail_url = REPLACE(thumbnail_url, '/uploads/', '${s3BaseUrl}/')
WHERE thumbnail_url LIKE '/uploads/%';
