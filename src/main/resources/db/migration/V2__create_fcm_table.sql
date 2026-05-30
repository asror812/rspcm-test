create table if not exists fcm (
    id bigserial primary key,
    created_at timestamp not null,
    updated_at timestamp not null,
    user_id bigint,
    fcm_token varchar(512)
);

ALTER TABLE fcm
    ADD CONSTRAINT fk_fcm_user
        FOREIGN KEY (user_id) REFERENCES users(id);

create unique index if not exists uk_fcm_user_token
    on fcm (user_id, fcm_token);

create index if not exists idx_fcm_user_id
    on fcm (user_id);
