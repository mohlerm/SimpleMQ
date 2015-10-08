DROP TABLE IF EXISTS messages CASCADE;
DROP TABLE IF EXISTS queues CASCADE;
DROP TABLE IF EXISTS clients CASCADE;

CREATE TABLE IF NOT EXISTS queues (
        id SERIAL PRIMARY KEY );
CREATE TABLE IF NOT EXISTS clients (
        id SERIAL PRIMARY KEY );
CREATE TABLE IF NOT EXISTS messages (
        id SERIAL PRIMARY KEY,
        sender_id INTEGER REFERENCES clients (id) ON DELETE CASCADE,
        receiver_id INTEGER REFERENCES clients (id) ON DELETE CASCADE,
        queue_id INTEGER REFERENCES queues (id) ON DELETE CASCADE,
        sendtime DATE,
        message TEXT);


CREATE OR REPLACE FUNCTION add_message(sender_id INTEGER, receiver_id INTEGER, queue_id INTEGER, sendtime DATE, message TEXT )
RETURNS void AS $$
        BEGIN
                INSERT INTO messages VALUES (default, sender_id, receiver_id, queue_id, sendtime, message);
        END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION add_queue()
RETURNS void AS $$
        BEGIN
                INSERT INTO queues VALUES (default);
        END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION add_client()
        RETURNS void AS $$
        BEGIN
                INSERT INTO clients VALUES (default);
        END;
$$ LANGUAGE plpgsql;