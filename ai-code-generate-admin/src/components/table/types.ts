import type { ColumnType } from 'ant-design-vue/es/table';

export interface TableColumn extends ColumnType {
  dataIndex: string; 
  key?: string;
}

export interface PageOption {
  current: number;
  pageSize: number;
  total: number;
}