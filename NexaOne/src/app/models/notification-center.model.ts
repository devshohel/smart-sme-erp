export type NotificationCenterType = 'INFO' | 'SUCCESS' | 'WARNING' | 'ERROR';
export type NotificationSeverity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export interface NotificationItem {
  id: number;
  title: string;
  message: string;
  type: NotificationCenterType;
  severity: NotificationSeverity;
  entityType?: string | null;
  entityId?: number | null;
  createdAt: string;
  createdBy?: string | null;
  read: boolean;
  readAt?: string | null;
  targetUserId?: number | null;
  targetRoleId?: number | null;
  actionUrl?: string | null;
}

export interface NotificationPage {
  content: NotificationItem[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface NotificationFilters {
  read?: boolean | '';
  type?: NotificationCenterType | '';
  severity?: NotificationSeverity | '';
  fromDate?: string;
  toDate?: string;
  page?: number;
  size?: number;
}
