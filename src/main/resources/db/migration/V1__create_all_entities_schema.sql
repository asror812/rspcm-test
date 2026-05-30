create table if not exists roles (
    id bigserial primary key,
    name varchar(255) not null unique
);

create table if not exists users (
    id bigserial primary key,
    university_id varchar(255) unique,
    first_name varchar(255) not null,
    last_name varchar(255) not null,
    email varchar(255) unique,
    password varchar(255) not null,
    birth_date date,
    phone_number varchar(32),
    enabled boolean not null default true,
    deleted boolean not null default false
);

create table if not exists user_roles (
    user_id bigint not null,
    role_id bigint not null,
    primary key (user_id, role_id)
);

create table if not exists admin_profiles (
    id bigserial primary key,
    user_id bigint unique
);

create table if not exists subjects (
    id bigserial primary key,
    name varchar(255) not null unique,
    description varchar(1000)
);

create table if not exists study_groups (
    id bigserial primary key,
    name varchar(255) not null unique,
    description varchar(1000),
    language varchar(255) not null
);

create table if not exists group_subjects (
    group_id bigint not null,
    subject_id bigint not null,
    primary key (group_id, subject_id)
);

create table if not exists group_teachers (
    group_id bigint not null,
    teacher_id bigint not null,
    primary key (group_id, teacher_id)
);

create table if not exists group_students (
    group_id bigint not null,
    student_id bigint not null,
    primary key (group_id, student_id)
);

create table if not exists subject_teachers (
    subject_id bigint not null,
    teacher_id bigint not null,
    primary key (subject_id, teacher_id)
);

create table if not exists student_profiles (
    id bigserial primary key,
    user_id bigint unique,
    course integer,
    group_id bigint
);

create table if not exists teacher_profiles (
    id bigserial primary key,
    user_id bigint unique,
    academic_degree varchar(128)
);

create table if not exists teacher_profile_subjects (
    teacher_profile_id bigint not null,
    subject_id bigint not null,
    primary key (teacher_profile_id, subject_id)
);

create table if not exists otp_verifications (
    id bigserial primary key,
    email varchar(255) not null,
    code varchar(255) not null,
    expires_at timestamp not null,
    used boolean not null
);

create table if not exists questions (
    id bigserial primary key,
    text varchar(2000) not null,
    type varchar(255) not null,
    subject_id bigint not null,
    created_by bigint not null,
    deleted boolean not null default false
);

create table if not exists question_option (
    id bigserial primary key,
    question_id bigint not null,
    text varchar(1000) not null,
    correct boolean,
    order_index integer
);

create table if not exists practices (
    id bigserial primary key,
    name varchar(255) not null,
    subject_id bigint not null,
    description varchar(2000),
    resource_url varchar(255),
    requirements varchar(2000),
    work_mode varchar(255) not null,
    team_size integer,
    scheduling_required boolean not null default false,
    deleted boolean not null default false,
    created_by bigint not null
);

create table if not exists practice_submission_types (
    practice_id bigint not null,
    submission_type varchar(255) not null
);

create table if not exists exams (
    id bigserial primary key,
    title varchar(255) not null,
    description varchar(2000),
    start_at timestamp,
    end_at timestamp,
    max_score integer,
    task_limit integer,
    type varchar(255) not null,
    status varchar(255) not null,
    subject_id bigint,
    created_at timestamp not null,
    updated_at timestamp,
    created_by bigint not null
);

create table if not exists exam_groups (
    exam_id bigint not null,
    group_id bigint not null,
    primary key (exam_id, group_id)
);

create table if not exists exam_students (
    exam_id bigint not null,
    student_id bigint not null,
    primary key (exam_id, student_id)
);

create table if not exists exam_questions (
    id bigserial primary key,
    exam_id bigint not null,
    question_id bigint not null,
    score integer not null,
    order_index integer not null,
    created_by bigint not null,
    constraint uk_exam_question_exam_question unique (exam_id, question_id),
    constraint uk_exam_question_exam_order unique (exam_id, order_index)
);

create table if not exists exam_attempts (
    id bigserial primary key,
    exam_id bigint not null,
    student_id bigint not null,
    status varchar(255) not null,
    started_at timestamp not null,
    submitted_at timestamp,
    constraint uk_exam_attempt_exam_student unique (exam_id, student_id)
);

create table if not exists exam_practices (
    id bigserial primary key,
    exam_id bigint not null,
    practice_id bigint not null,
    constraint uk_exam_practice unique (exam_id, practice_id)
);

create table if not exists practice_participation (
    id bigserial primary key,
    exam_id bigint not null,
    exam_practice_id bigint,
    created_at timestamp not null,
    ready_at timestamp,
    chosen_at timestamp,
    status varchar(255) not null
);

create table if not exists practice_participation_members (
    id bigserial primary key,
    practice_participation_id bigint not null,
    user_id bigint not null,
    role varchar(255) not null,
    status varchar(255) not null,
    constraint uk_participation_user unique (practice_participation_id, user_id)
);

create table if not exists practice_assignments (
    id bigserial primary key,
    exam_id bigint not null,
    text_answer varchar(5000),
    file_url varchar(255),
    student_id bigint,
    submitted_at timestamp,
    status varchar(255) not null,
    teacher_comment varchar(2000),
    constraint uk_exam_student_assignment unique (exam_id, student_id)
);

create table if not exists practice_logbooks (
    id bigserial primary key,
    practice_id bigint not null,
    student_id bigint not null,
    file_path varchar(255),
    submitted_at timestamp,
    status varchar(255) not null,
    constraint uk_practice_logbook_practice_student unique (practice_id, student_id)
);

create table if not exists practice_logbook_entries (
    id bigserial primary key,
    logbook_id bigint not null,
    entry_date date not null,
    content varchar(5000) not null,
    status varchar(255) not null,
    submitted_at timestamp,
    constraint uk_practice_logbook_entry_logbook_date unique (logbook_id, entry_date)
);

create table if not exists student_answers (
    id bigserial primary key,
    exam_question_id bigint not null,
    student_id bigint not null,
    text_answer varchar(5000),
    score integer,
    correct boolean,
    answered_at timestamp
);

create table if not exists student_answer_options (
    id bigserial primary key,
    student_answer_id bigint not null,
    question_option bigint not null
);

create table if not exists chats (
    id bigserial primary key,
    title varchar(255) not null,
    group_id bigint not null,
    type varchar(255) not null
);

create table if not exists chat_members (
    id bigserial primary key,
    user_id bigint not null,
    chat_id bigint not null,
    role varchar(255) not null,
    constraint uk_chat_user unique (chat_id, user_id)
);

create table if not exists chat_message (
    id bigserial primary key,
    chat_id bigint not null,
    sender_id bigint not null,
    content varchar(300) not null
);

alter table user_roles
    add constraint fk_user_roles_user foreign key (user_id) references users(id);

alter table user_roles
    add constraint fk_user_roles_role foreign key (role_id) references roles(id);
alter table admin_profiles
    add constraint fk_admin_profiles_user foreign key (user_id) references users(id);
alter table group_subjects
    add constraint fk_group_subjects_group foreign key (group_id) references study_groups(id);
alter table group_subjects
    add constraint fk_group_subjects_subject foreign key (subject_id) references subjects(id);
alter table group_teachers
    add constraint fk_group_teachers_group foreign key (group_id) references study_groups(id);
alter table group_teachers
    add constraint fk_group_teachers_teacher foreign key (teacher_id) references users(id);
alter table group_students
    add constraint fk_group_students_group foreign key (group_id) references study_groups(id);
alter table group_students
    add constraint fk_group_students_student foreign key (student_id) references users(id);
alter table subject_teachers
    add constraint fk_subject_teachers_subject foreign key (subject_id) references subjects(id);
alter table subject_teachers
    add constraint fk_subject_teachers_teacher foreign key (teacher_id) references users(id);
alter table student_profiles
    add constraint fk_student_profiles_user foreign key (user_id) references users(id);
alter table student_profiles
    add constraint fk_student_profiles_group foreign key (group_id) references study_groups(id);
alter table teacher_profiles
    add constraint fk_teacher_profiles_user foreign key (user_id) references users(id);
alter table teacher_profile_subjects
    add constraint fk_teacher_profile_subjects_profile foreign key (teacher_profile_id) references teacher_profiles(id);
alter table teacher_profile_subjects
    add constraint fk_teacher_profile_subjects_subject foreign key (subject_id) references subjects(id);
alter table questions
    add constraint fk_questions_subject foreign key (subject_id) references subjects(id);
alter table questions
    add constraint fk_questions_created_by foreign key (created_by) references users(id);
alter table question_option
    add constraint fk_question_option_question foreign key (question_id) references questions(id);
alter table practices
    add constraint fk_practices_subject foreign key (subject_id) references subjects(id);
alter table practices
    add constraint fk_practices_created_by foreign key (created_by) references users(id);
alter table practice_submission_types
    add constraint fk_practice_submission_types_practice foreign key (practice_id) references practices(id);
alter table exams
    add constraint fk_exams_subject foreign key (subject_id) references subjects(id);
alter table exams
    add constraint fk_exams_created_by foreign key (created_by) references users(id);
alter table exam_groups
    add constraint fk_exam_groups_exam foreign key (exam_id) references exams(id);
alter table exam_groups
    add constraint fk_exam_groups_group foreign key (group_id) references study_groups(id);
alter table exam_students
    add constraint fk_exam_students_exam foreign key (exam_id) references exams(id);
alter table exam_students
    add constraint fk_exam_students_student foreign key (student_id) references users(id);
alter table exam_questions
    add constraint fk_exam_questions_exam foreign key (exam_id) references exams(id);
alter table exam_questions
    add constraint fk_exam_questions_question foreign key (question_id) references questions(id);
alter table exam_questions
    add constraint fk_exam_questions_created_by foreign key (created_by) references users(id);
alter table exam_attempts
    add constraint fk_exam_attempts_exam foreign key (exam_id) references exams(id);
alter table exam_attempts
    add constraint fk_exam_attempts_student foreign key (student_id) references users(id);
alter table exam_practices
    add constraint fk_exam_practices_exam foreign key (exam_id) references exams(id);
alter table exam_practices
    add constraint fk_exam_practices_practice foreign key (practice_id) references practices(id);
alter table practice_participation
    add constraint fk_practice_participation_exam foreign key (exam_id) references exams(id);
alter table practice_participation
    add constraint fk_practice_participation_exam_practice foreign key (exam_practice_id) references exam_practices(id);
alter table practice_participation_members
    add constraint fk_practice_participation_members_participation foreign key (practice_participation_id) references practice_participation(id);
alter table practice_participation_members
    add constraint fk_practice_participation_members_user foreign key (user_id) references users(id);
alter table practice_assignments
    add constraint fk_practice_assignments_exam foreign key (exam_id) references practice_participation(id);
alter table practice_assignments
    add constraint fk_practice_assignments_student foreign key (student_id) references users(id);
alter table practice_logbooks
    add constraint fk_practice_logbooks_practice foreign key (practice_id) references practices(id);
alter table practice_logbooks
    add constraint fk_practice_logbooks_student foreign key (student_id) references users(id);
alter table practice_logbook_entries
    add constraint fk_practice_logbook_entries_logbook foreign key (logbook_id) references practice_logbooks(id);
alter table student_answers
    add constraint fk_student_answers_exam_question foreign key (exam_question_id) references exam_questions(id);
alter table student_answers
    add constraint fk_student_answers_student foreign key (student_id) references users(id);
alter table student_answer_options
    add constraint fk_student_answer_options_answer foreign key (student_answer_id) references student_answers(id);
alter table student_answer_options
    add constraint fk_student_answer_options_option foreign key (question_option) references question_option(id);
alter table chats
    add constraint fk_chats_group foreign key (group_id) references study_groups(id);
alter table chat_members
    add constraint fk_chat_members_user foreign key (user_id) references users(id);
alter table chat_members
    add constraint fk_chat_members_chat foreign key (chat_id) references chats(id);
alter table chat_message
    add constraint fk_chat_message_chat foreign key (chat_id) references chats(id);
alter table chat_message
    add constraint fk_chat_message_sender foreign key (sender_id) references users(id);
