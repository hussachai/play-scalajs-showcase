# Tasks schema

# --- !Ups

CREATE TABLE Tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    txt VARCHAR(255) NOT NULL,
    done BOOLEAN NOT NULL
);

INSERT INTO Tasks(txt, done) VALUES('Upgrade Scala JS', true);
INSERT INTO Tasks(txt, done) VALUES('Make it Rx', false);
INSERT INTO Tasks(txt, done) VALUES('Make this example useful', false);

# --- !Downs

DROP TABLE Tasks;