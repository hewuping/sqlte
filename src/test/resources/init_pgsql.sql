DROP TABLE IF EXISTS "public"."users";

CREATE SEQUENCE users_id_seq;
CREATE TABLE "public"."users" (
"id" int4 DEFAULT nextval('users_id_seq'::regclass) NOT NULL,
"username" varchar(50),
"email" varchar(255) DEFAULT '@xxx.com'::character varying,
"password" varchar(64),
"updated_time" timestamp(6) DEFAULT now(),
"gender" varchar(10),
"creation_time" date,
CONSTRAINT "users_pkey" PRIMARY KEY ("id"),
CONSTRAINT "idx_un" UNIQUE ("username")
);
ALTER SEQUENCE users_id_seq OWNED BY "public"."users"."id";