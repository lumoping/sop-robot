create table if not exists sop
(
    id             serial primary key,
    doc_url        varchar(255),
    title          varchar(255),
    description    text,
    estimated_time int,
    enable         bool
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
    id             serial primary key,
    sop_id         int,
    description    text,
    sort_index     int,
    estimated_time int,
    foreign key (sop_id) references sop (id)
);

create table if not exists sop_event
(
    id          serial primary key,
    sop_id      int,
    create_by   varchar(50),
    create_time date,
    done        bool,
    done_time   date,
    full_done   bool,
    foreign key (id) references sop (id)
);

create table if not exists sop_event_operation
(
    id          serial primary key,
    sop_id      int,
    event_id    int,
    description text,
    sort_index  int,
    start_time  date,
    done        bool,
    done_time   date,
    foreign key (sop_id) references sop (id),
    foreign key (event_id) references sop_event (id)
);