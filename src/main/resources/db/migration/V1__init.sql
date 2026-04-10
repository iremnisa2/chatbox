CREATE TABLE users (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       firstname VARCHAR(100) NOT NULL,
                       lastname VARCHAR(100) NOT NULL,
                       number VARCHAR(50) NOT NULL,
                       created_at DATETIME NOT NULL,
                       CONSTRAINT uk_users_number UNIQUE (number)
);

CREATE TABLE conversations (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT,
                               title VARCHAR(255),
                               is_group BOOLEAN NOT NULL,
                               created_by BIGINT NOT NULL,
                               created_at DATETIME NOT NULL,
                               CONSTRAINT fk_conversations_created_by
                                   FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE conversation_members (
                                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                      conversation_id BIGINT NOT NULL,
                                      user_id BIGINT NOT NULL,
                                      role VARCHAR(20) NOT NULL,
                                      joined_at DATETIME NOT NULL,

                                      CONSTRAINT fk_conversation_members_conversation
                                          FOREIGN KEY (conversation_id) REFERENCES conversations(id)
                                              ON DELETE CASCADE,

                                      CONSTRAINT fk_conversation_members_user
                                          FOREIGN KEY (user_id) REFERENCES users(id)
                                              ON DELETE CASCADE,

                                      CONSTRAINT uk_conversation_members UNIQUE (conversation_id, user_id)
);

CREATE TABLE messages (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          conversation_id BIGINT NOT NULL,
                          sender_id BIGINT NOT NULL,
                          content TEXT NOT NULL,
                          created_at DATETIME NOT NULL,

                          CONSTRAINT fk_messages_conversation
                              FOREIGN KEY (conversation_id) REFERENCES conversations(id)
                                  ON DELETE CASCADE,

                          CONSTRAINT fk_messages_sender
                              FOREIGN KEY (sender_id) REFERENCES users(id)
);


CREATE INDEX idx_members_user_id ON conversation_members(user_id);
CREATE INDEX idx_members_conversation_id ON conversation_members(conversation_id);

CREATE INDEX idx_messages_conversation_created_at ON messages(conversation_id, created_at);
CREATE INDEX idx_messages_sender_id ON messages(sender_id);
