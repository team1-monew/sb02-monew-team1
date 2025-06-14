CREATE TABLE IF NOT EXISTS users (
                       id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       nickname VARCHAR(20) NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS interests (
                           id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                           name VARCHAR(50) NOT NULL UNIQUE,
                           subscriber_count BIGINT DEFAULT 0,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS keywords (
                          id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                          interest_id BIGINT NOT NULL,
                          keyword VARCHAR(50) NOT NULL,
                          FOREIGN KEY (interest_id) REFERENCES interests(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS subscriptions (
                               id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                               user_id BIGINT NOT NULL,
                               interest_id BIGINT NOT NULL,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                               FOREIGN KEY (interest_id) REFERENCES interests(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS articles (
                          id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                          source VARCHAR(255) NOT NULL,
                          source_url TEXT,
                          title VARCHAR(500) NOT NULL,
                          publish_date TIMESTAMP NOT NULL,
                          summary TEXT,
                          view_count BIGINT DEFAULT 0,
                          is_deleted BOOLEAN DEFAULT FALSE,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS article_interests (
                                   id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                   interest_id BIGINT,
                                   article_id BIGINT,
                                   FOREIGN KEY (interest_id) REFERENCES interests(id),
                                   FOREIGN KEY (article_id) REFERENCES articles(id)
);

CREATE TABLE IF NOT EXISTS article_views (
                               id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                               article_id BIGINT NOT NULL,
                               viewed_by BIGINT NOT NULL,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               FOREIGN KEY (article_id) REFERENCES articles(id),
                               FOREIGN KEY (viewed_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS comments (
                          id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                          article_id BIGINT NOT NULL,
                          user_id BIGINT NOT NULL,
                          content VARCHAR(500) NOT NULL,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          like_count BIGINT DEFAULT 0,
                          is_deleted BOOLEAN DEFAULT FALSE,
                          updated_at TIMESTAMP,
                          FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE CASCADE,
                          FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS comment_likes (
                               id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                               comment_id BIGINT NOT NULL,
                               liked_by BIGINT NOT NULL,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE,
                               FOREIGN KEY (liked_by) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS user_activities (
                                 id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                 user_id BIGINT NOT NULL,
                                 action_type VARCHAR(50),
                                 description TEXT,
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS notifications (
                               id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                               user_id BIGINT NOT NULL,
                               content TEXT NOT NULL,
                               resource_type VARCHAR(50) NOT NULL,
                               resource_id BIGINT,
                               confirmed BOOLEAN DEFAULT FALSE,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS shedlock (
                          name VARCHAR(64) PRIMARY KEY,
                          lock_until TIMESTAMP(3) NULL,
                          locked_at TIMESTAMP(3) NULL,
                          locked_by VARCHAR(255) NOT NULL
);