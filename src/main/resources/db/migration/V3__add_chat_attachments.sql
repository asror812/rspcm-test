alter table chat_message
    alter column content drop not null;

alter table chat_message
    alter column content type varchar(2000);

create table if not exists chat_attachments (
    id bigserial primary key,
    message_id bigint not null,
    file_name varchar(255) not null,
    stored_name varchar(255) not null unique,
    content_type varchar(255) not null,
    size bigint not null,
    file_path varchar(1024) not null
);

alter table chat_attachments
    add constraint fk_chat_attachments_message
        foreign key (message_id) references chat_message(id) on delete cascade;
