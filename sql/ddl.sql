-- 创建库
create database if not exists zwBI_db;

-- 切换库
use zwBI_db;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userName     varchar(256)                           null comment '用户昵称',
    userAccount  varchar(256)                           not null comment '账号',
    userAvatar   varchar(1024)                          null comment '用户头像',
    gender       tinyint                                null comment '性别',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user / admin',
    userPassword varchar(512)                           not null comment '密码',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    constraint uni_userAccount
        unique (userAccount)
) comment '用户';

-- 图标信息表
create table if not exists chart
(
    id             bigint auto_increment comment 'id' primary key,
    goal           text                               null comment '分析目标',
    chartData      text                               null comment '图表数据',
    chartType      varchar(128)                       null comment '图表类型',
    chartName      varchar(128)                       null comment '图标名称',
    userId         bigint                             null comment '创建用户Id',
    generateChart  text                               null comment '生成的图表数据',
    generateResult text                               null comment '生成的分析结果',
    createTime     datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime     datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete       tinyint  default 0                 not null comment '是否删除'
) comment '图表信息表';