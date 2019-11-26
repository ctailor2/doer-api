ALTER TABLE users ADD COLUMN default_list_id character varying;

UPDATE users
SET default_list_id = lists.uuid
FROM lists
WHERE lists.user_id = users.id
AND lists.name = 'default';

ALTER TABLE users ALTER COLUMN default_list_id SET NOT NULL;