-- ユーザー情報
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),   -- ユーザーID
    email_address VARCHAR(255) NOT NULL UNIQUE,      -- メールアドレス（ユニーク）
    password VARCHAR(255) NOT NULL,                  -- ハッシュ化されたパスワード
    real_name VARCHAR(50) NOT NULL,                  -- 本名
    user_name VARCHAR(50) NOT NULL UNIQUE,           -- ユーザー名
    thumbnail_url TEXT,                              -- サムネイル画像のURL
    user_role VARCHAR(10) NOT NULL,                       -- 権限
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 登録日時
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP  -- 更新日時
);

-- ユーザーのログイン履歴
CREATE TABLE user_login_histories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),       -- 履歴ID
    user_id UUID REFERENCES users(id) ON DELETE CASCADE, -- 関連するユーザーID
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- ログイン日時
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP  -- レコード更新日時
);

-- コース情報
CREATE TABLE courses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),   -- コースID
    course_order NUMERIC(10,4) NOT NULL UNIQUE,      -- コース内での並び順
    thumbnail_url TEXT,                              -- サムネイル画像のURL
    title VARCHAR(255) NOT NULL,                     -- コースタイトル
    description TEXT,                                -- コース説明
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 作成日時
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP  -- 更新日時
);

-- レッスングループ（複数レッスンをまとめる）
CREATE TABLE lesson_groups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),   -- レッスングループID
    course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE, -- コースID
    lesson_group_order NUMERIC(10,4) NOT NULL,       -- レッスングループ内での並び順
    title VARCHAR(255) NOT NULL,                      -- レッスングループタイトル
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 作成日時
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- 更新日時
    CONSTRAINT course_id_and_lesson_group_order_constraint UNIQUE (course_id, lesson_group_order)
);

-- レッスン情報
CREATE TABLE lessons (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),   -- レッスンID
    lesson_group_id UUID NOT NULL REFERENCES lesson_groups(id) ON DELETE CASCADE, -- レッスングループID
    course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE, -- コースID
    lesson_order NUMERIC(10,4) NOT NULL,             -- レッスン内での並び順
    title VARCHAR(255) NOT NULL,                     -- レッスンタイトル
    description TEXT,                                -- レッスンの説明
    video_url TEXT,                                  -- 動画のURL
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 作成日時
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- 更新日時
    CONSTRAINT lesson_group_id_and_lesson_order_constraint UNIQUE (lesson_group_id, lesson_order)
);

-- お知らせ
CREATE TABLE news (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),   -- お知らせID
    title VARCHAR(255) NOT NULL,                     -- タイトル
    content TEXT NOT NULL,                                    -- 本文
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 作成日時
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP  -- 更新日時
);

-- 中間テーブル：ユーザーが受講したレッスン
CREATE TABLE user_lessons (
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,   -- ユーザーID
    lesson_id UUID REFERENCES lessons(id) ON DELETE CASCADE, -- レッスンID
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 登録日時
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 更新日時
    PRIMARY KEY (user_id, lesson_id)                    -- 複合主キー
);

-- パスワード再設定トークン管理
CREATE TABLE password_reset_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), -- トークンID
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE, -- 対象ユーザーID
    token VARCHAR(255) NOT NULL UNIQUE,            -- トークン文字列（URLに付加）
    expires_at TIMESTAMP NOT NULL,                 -- 有効期限（例：30分）
    used_at TIMESTAMP                              -- 使用済み日時（nullなら未使用）
);
