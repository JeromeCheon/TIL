INSERT INTO article (title, content, author, created_at, updated_at)
VALUES ('제목 1', '내용 1', 'user1', NOW(), NOW());
INSERT INTO article (title, content, author, created_at, updated_at)
VALUES ('제목 2', '내용 2', 'user2', NOW(), NOW());
INSERT INTO article (title, content, author, created_at, updated_at)
VALUES ('제목 3', '내용 3', 'user3', NOW(), NOW());

INSERT INTO comments (article_id, author, content, created_at)
VALUES (1, 'user4', '댓글 1', NOW());
INSERT INTO comments (article_id, author, content, created_at)
VALUES (1, 'user5', '댓글 2', NOW());