CREATE TABLE version
(
    id        SERIAL PRIMARY KEY,
    candidate TEXT,
    version   TEXT,
    platform  TEXT,
    visible   BOOLEAN,
    url       TEXT,
    UNIQUE (candidate, version, platform)
)