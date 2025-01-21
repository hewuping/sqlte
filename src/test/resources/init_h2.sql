DROP TABLE IF EXISTS `users`;

CREATE TABLE `users` (
  `id`            int(11)   NOT NULL AUTO_INCREMENT,
  `email`         varchar(60),
  `username`      varchar(60),
  `password`      varchar(64),
  `gender`        ENUM('MALE', 'FEMALE', 'OTHER')        DEFAULT NULL,
  `age`           int(4)             DEFAULT NULL,
  `updated_time`  timestamp NOT NULL DEFAULT "CURRENT_TIMESTAMP"(30),
  PRIMARY KEY (`id`)
);

