DROP TABLE IF EXISTS users CASCADE ;
DROP TABLE IF EXISTS categories CASCADE ;
DROP TABLE IF EXISTS events CASCADE ;
DROP TABLE IF EXISTS requests CASCADE ;
DROP TABLE IF EXISTS compilations CASCADE ;
DROP TABLE IF EXISTS event_compilations CASCADE ;

CREATE TABLE IF NOT EXISTS users
(
    id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name  VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS categories
(
    id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS events
(
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    annotation         VARCHAR(1024)               NOT NULL,
    category_id        BIGINT                      NOT NULL REFERENCES categories (id),
    created_on         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    description        VARCHAR(1024)               NOT NULL,
    event_date         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    initiator_id       BIGINT                      NOT NULL REFERENCES users (id),
    location_lon       FLOAT                       NOT NULL,
    location_lat       FLOAT                       NOT NULL,
    is_paid            BOOLEAN                     NOT NULL,
    participant_limit  BIGINT,
    published_on       TIMESTAMP WITHOUT TIME ZONE,
    request_moderation BOOLEAN,
    state              VARCHAR(50),
    title              VARCHAR(512)                NOT NULL
);

CREATE TABLE IF NOT EXISTS requests
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    created      TIMESTAMP WITHOUT TIME ZONE,
    event_id     BIGINT NOT NULL REFERENCES events (id),
    requester_id BIGINT NOT NULL REFERENCES users (id),
    status       VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS compilations
(
    id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    is_pinned BOOLEAN      NOT NULL,
    title     VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS event_compilations
(
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_id       BIGINT NOT NULL REFERENCES events (id),
    compilation_id BIGINT NOT NULL REFERENCES compilations (id)
);