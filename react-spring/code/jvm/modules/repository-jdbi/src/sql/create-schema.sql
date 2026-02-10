create schema dbo;

create table dbo.Users(
    id int generated always as identity primary key,
    username VARCHAR(64) unique not null,
    password_validation VARCHAR(256) not null
);

create table dbo.Tokens(
    token_validation VARCHAR(256) primary key,
    user_id int references dbo.Users(id),
    created_at bigint not null,
    last_used_at bigint not null
);

create table dbo.Games(
    id serial PRIMARY KEY,
    name VARCHAR(64) not null,
    description VARCHAR(256) not null,
    rounds int not null,
    status VARCHAR(64) not null,
    min_players int not null,
    max_players int not null,
    timeout bigint not null,
    created_at bigint not null,
    updated_at bigint not null,
    game_state_type varchar(100),
    gameState jsonb default '{}'
);

create table dbo.PlayerGame(
    game_id int,
    user_id int,
    is_host bool default false,
    has_left bool default false,
    FOREIGN KEY (game_id) REFERENCES dbo.Games(id),
    FOREIGN KEY (user_id) REFERENCES dbo.Users(id)
);

create table dbo.UserInvite(
    id serial PRIMARY KEY,
    created_by int NOT NULL,
    invite_code bigint NOT NULL,
    used_by int,
    created_at bigint not null,
    updated_at bigint,
    FOREIGN KEY (created_by) REFERENCES dbo.Users(id),
    FOREIGN KEY (used_by) REFERENCES dbo.Users(id)
);
