--CREATE INDEX IF NOT EXISTS idx_net_votes ON post((upvote_count - downvote_count) DESC);
SELECT 1; -- 빈 sql문 에러 방지용