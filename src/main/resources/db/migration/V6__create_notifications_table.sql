create table if not exists notifications (
    id          bigserial    primary key,
    user_id     bigint       not null,
    title       varchar(255) not null,
    body        varchar(1000) not null,
    type        varchar(50)  not null,
    reference_id bigint,
    is_read     boolean      not null default false,
    created_at  timestamp    not null,
    constraint fk_notifications_user foreign key (user_id) references users(id) on delete cascade
);

create index idx_notifications_user_unread on notifications(user_id, is_read);
create index idx_notifications_user_created on notifications(user_id, created_at desc);
