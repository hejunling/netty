/*
Navicat MySQL Data Transfer

Source Server         : MySQL
Source Server Version : 50541
Source Host           : localhost:3306
Source Database       : demo

Target Server Type    : MYSQL
Target Server Version : 50541
File Encoding         : 65001

Date: 2015-04-27 15:36:28
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for task
-- ----------------------------
DROP TABLE IF EXISTS `task`;
CREATE TABLE `task` (
  `task_id` varchar(255) NOT NULL COMMENT 'PK，唯一标识，默认是UUID',
  `req_url` varchar(255) DEFAULT NULL COMMENT '请求方url',
  `rev_url` varchar(255) DEFAULT NULL COMMENT '接收方url',
  `gmt_create` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  `gmt_handle` timestamp NULL DEFAULT NULL COMMENT '任务待执行日期',
  `task_handler` int(11) NOT NULL COMMENT '任务执行类型 0:上传到OSS，1:发送回调请求',
  `task_status` int(11) NOT NULL DEFAULT '0' COMMENT '任务处理状态；0:未处理 1:处理中',
  `compute_num` int(11) NOT NULL DEFAULT '0' COMMENT '运行任务的机器代号',
  `task_params` varchar(4000) DEFAULT NULL COMMENT '待执行任务需要的参数',
  `retry_count` int(11) NOT NULL DEFAULT '0' COMMENT '重试次数，每次加1',
  `retry_reason` varchar(4000) DEFAULT NULL COMMENT '重试原因，即上次失败原因，便于排错',
  PRIMARY KEY (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*
Navicat MySQL Data Transfer

Source Server         : MySQL
Source Server Version : 50541
Source Host           : localhost:3306
Source Database       : demo

Target Server Type    : MYSQL
Target Server Version : 50541
File Encoding         : 65001

Date: 2015-04-27 15:36:28
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for task
-- ----------------------------
DROP TABLE IF EXISTS `task`;
CREATE TABLE `task` (
  `task_id` varchar(255) NOT NULL COMMENT 'PK，唯一标识，默认是UUID',
  `req_url` varchar(255) DEFAULT NULL COMMENT '请求方url',
  `rev_url` varchar(255) DEFAULT NULL COMMENT '接收方url',
  `gmt_create` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  `gmt_handle` timestamp NULL DEFAULT NULL COMMENT '任务待执行日期',
  `task_handler` int(11) NOT NULL COMMENT '任务执行类型 0:上传到OSS，1:发送回调请求',
  `task_status` int(11) NOT NULL DEFAULT '0' COMMENT '任务处理状态；0:未处理 1:处理中',
  `compute_num` int(11) NOT NULL DEFAULT '0' COMMENT '运行任务的机器代号',
  `task_params` varchar(4000) DEFAULT NULL COMMENT '待执行任务需要的参数',
  `retry_count` int(11) NOT NULL DEFAULT '0' COMMENT '重试次数，每次加1',
  `retry_reason` varchar(4000) DEFAULT NULL COMMENT '重试原因，即上次失败原因，便于排错',
  PRIMARY KEY (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
