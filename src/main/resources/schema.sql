create table if not exists chat_info
(
    id           serial primary key,
    chat_id      text,
    folder_token text,
    folder_url   text
);

create table if not exists sop
(
    id          serial primary key,
    chat_id     text,
    doc_token   varchar(255),
    doc_url     varchar(255),
    title       varchar(255),
    description text
);

create table if not exists sop_todo
(
    id          serial primary key,
    doc_token   varchar(255),
    description text
);