DO $$
BEGIN
  IF EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_name = 'lessons'
      AND column_name = 'description'
  ) THEN
    ALTER TABLE lessons RENAME COLUMN description TO content;
  END IF;
END $$;
