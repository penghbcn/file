CREATE TABLE `file_upload_db`.`cron_t`  (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL DEFAULT '' COMMENT '表达式的名字',
  `cron` varchar(255) NOT NULL DEFAULT '' COMMENT '表达式的值',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '0-失效;1-有效',
  `created_by` varchar(255) DEFAULT '' NOT NULL,
  `created_time` timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
  `modified_by` varchar(255) DEFAULT '' NOT NULL,
  `modified_time` timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
  PRIMARY KEY (`id`)
);