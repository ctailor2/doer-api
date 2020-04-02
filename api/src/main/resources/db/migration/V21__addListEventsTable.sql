CREATE TABLE list_events (
    user_id varchar NOT NULL,
    list_id varchar NOT NULL,
    version integer NOT NULL,
    event_class varchar NOT NULL,
    data varchar NOT NULL
);
