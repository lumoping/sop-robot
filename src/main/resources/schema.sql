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
    doc_token   varchar(255),
    title       varchar(255),
    description text
);

create table if not exists sop_labels
(
    id     serial primary key,
    sop_id int,
    name   varchar(50),
    foreign key (sop_id) references sop (id)
);

create table if not exists sop_operation
(
    id          serial primary key,
    sop_id      int,
    description text,
    sort_index  int,
    foreign key (sop_id) references sop (id)
);