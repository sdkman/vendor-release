CREATE TABLE versions(
    id SERIAL PRIMARY KEY,
    candidate TEXT,
    version TEXT,
    url TEXT,
    platform TEXT,
    visible BOOLEAN
)