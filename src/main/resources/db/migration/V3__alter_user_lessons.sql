-- 中間テーブル：ユーザが受講したレッスンテーブル（複合主キーから複合ユニークキーに変更し、主キーを追加した）
ALTER TABLE user_lessons DROP CONSTRAINT user_lessons_pkey;
ALTER TABLE user_lessons ADD CONSTRAINT unique_user_lesson UNIQUE(user_id, lesson_id);
ALTER TABLE user_lessons ADD id UUID PRIMARY KEY DEFAULT gen_random_uuid();
