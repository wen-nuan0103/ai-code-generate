import {
  DashboardOutlined,
  UserOutlined,
  RobotOutlined,
  SettingOutlined,
  PayCircleOutlined
} from '@ant-design/icons-vue';
import type { Component } from 'vue';

// 定义菜单项的接口
export interface MenuItem {
  key: string;
  title: string;
  icon?: Component;
  children?: MenuItem[];
}

// 菜单配置数据
export const menuList: MenuItem[] = [
  {
    key: '/dashboard',
    title: '仪表盘',
    icon: DashboardOutlined,
  },
  {
    key: '/user',
    title: '用户管理',
    icon: UserOutlined,
    children: [
      {
        key: '/user/list',
        title: '用户列表',
      },
      {
        key: '/user/points',
        title: '积分记录',
      },
    ],
  },
  {
    key: '/model',
    title: 'AI 模型引擎',
    icon: RobotOutlined,
    children: [
      {
        key: '/ai/models',
        title: '模型配置',
      },
      {
        key: '/ai/keys',
        title: '密钥池管理',
      },
      {
        key: '/ai/prompts',
        title: 'Prompt 调优',
      },
      {
        key: '/ai/logs',
        title: '生成日志',
      },
    ],
  },
  {
    key: '/finance',
    title: '财务管理',
    icon: PayCircleOutlined,
    children: [
      {
        key: '/finance/orders',
        title: '订单列表',
      },
      {
        key: '/finance/cdkeys',
        title: '兑换码管理',
      },
    ],
  },
  {
    key: '/system',
    title: '系统设置',
    icon: SettingOutlined,
    children: [
      {
        key: '/system/base',
        title: '基础设置',
      },
      // {
      //   key: '/system/admin',
      //   title: '管理员管理',
      // },
    ],
  },
];