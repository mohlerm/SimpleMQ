DROP FUNCTION IF EXISTS add_message(INTEGER, INTEGER, INTEGER, TIMESTAMP, TEXT);
DROP FUNCTION IF EXISTS add_queue();
DROP FUNCTION IF EXISTS delete_queue(INTEGER);
DROP FUNCTION IF EXISTS add_client(INTEGER);
DROP FUNCTION IF EXISTS delete_client(INTEGER);
DROP FUNCTION IF EXISTS pop_queue(INTEGER, INTEGER);
DROP FUNCTION IF EXISTS peek_queue(INTEGER, INTEGER);

DROP TABLE IF EXISTS messages CASCADE;
DROP TABLE IF EXISTS queues CASCADE;
DROP TABLE IF EXISTS clients CASCADE;

CREATE TABLE IF NOT EXISTS queues (
        id SERIAL PRIMARY KEY );
CREATE TABLE IF NOT EXISTS clients (
        id INTEGER PRIMARY KEY );
CREATE TABLE IF NOT EXISTS messages (
        id SERIAL PRIMARY KEY,
        sender_id INTEGER REFERENCES clients (id) ON DELETE CASCADE,
        receiver_id INTEGER REFERENCES clients (id) ON DELETE CASCADE,
        queue_id INTEGER NOT NULL REFERENCES queues (id) ON DELETE CASCADE,
        sendtime TIMESTAMP DEFAULT now(),
        message TEXT);

CREATE INDEX queue_index ON queues(id);
CREATE INDEX user_index ON clients(id);
CREATE INDEX message_index ON messages(id);
CREATE INDEX message_queue_index ON messages(queue_id);
CREATE INDEX message_sender_index ON messages(sender_id);
CREATE INDEX message_receiver_index ON messages(receiver_id);


CREATE OR REPLACE FUNCTION add_message(sender_id INTEGER, receiver_id INTEGER, queue_id INTEGER, sendtime TIMESTAMP, message TEXT)
RETURNS INTEGER AS $$
        DECLARE new_id INTEGER;
        BEGIN
                INSERT INTO messages VALUES (default, sender_id, receiver_id, queue_id, sendtime, message) RETURNING id into new_id;
                RETURN new_id;
        END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION add_queue()
RETURNS INTEGER AS $$
        DECLARE new_id INTEGER;
        BEGIN
                INSERT INTO queues VALUES (default) RETURNING id into new_id;
                RETURN new_id;
        END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_queue(queue_id INTEGER)
RETURNS void AS $$
        BEGIN
                DELETE FROM queues WHERE id=queue_id;
        END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION add_client(client_id INTEGER)
RETURNS INTEGER AS $$
        DECLARE new_id INTEGER;
        BEGIN
                INSERT INTO clients VALUES (client_id) RETURNING id into new_id;
                RETURN new_id;
        END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_client(client_id INTEGER)
RETURNS void AS $$
        BEGIN
                DELETE FROM clients WHERE id=client_id;
        END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION pop_queue(queue_id2 INTEGER, client_id2 INTEGER)
RETURNS messages AS $$
        DECLARE res_id INTEGER;
                res_messages messages%ROWTYPE;
        BEGIN
                SELECT INTO res_id id FROM messages
                WHERE queue_id = queue_id2
                AND (receiver_id = client_id2 OR receiver_id = 0)
                ORDER BY id ASC
                LIMIT 1;

                DELETE FROM messages WHERE id = res_id RETURNING * INTO res_messages;
                RETURN res_messages;
        END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION peek_queue(queue_id2 INTEGER, client_id2 INTEGER)
RETURNS messages AS $$
        DECLARE res_messages messages%ROWTYPE;
        BEGIN
                SELECT INTO res_messages * FROM messages
                WHERE queue_id = queue_id2
                AND (receiver_id = client_id2 OR receiver_id = 0)
                ORDER BY id ASC
                LIMIT 1;
                RETURN res_messages;
        END;
$$ LANGUAGE plpgsql;

