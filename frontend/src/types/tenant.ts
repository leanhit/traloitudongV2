// types/tenant.ts

/** ========================
 * TENANT
 * ======================== */

// Tenant represents a tenant/organization in the system
export interface Tenant {
  id: string;
  name: string;
  ownerId: number;
  description?: string;
  status: 'ACTIVE' | 'INACTIVE';
  membershipStatus: 'NONE' | 'PENDING' | 'APPROVED';
  createdAt: string;
  updatedAt: string;
}

/** ========================
 * TENANT USER / MEMBERSHIP
 * ======================== */

// TenantUser represents a user's membership in a tenant
export interface TenantUser {
  id: {
    userId: number;
    tenantId: string;
  };
  user: {
    id: number;
    email: string;
    firstName?: string;
    lastName?: string;
  };
  roleInTenant: 'OWNER' | 'ADMIN' | 'MEMBER';
  status: 'ACTIVE' | 'INVITED' | 'REMOVED';
  joinedAt?: string;
}

/** ========================
 * JOIN REQUEST / PENDING
 * ======================== */

// JoinRequest represents a user's request to join a tenant
export interface JoinRequest {
  id: number; // tương ứng với TenantMember.id ở backend
  tenant: Tenant;
  user: {
    id: number;
    email: string;
    firstName?: string;
    lastName?: string;
  };
  role: 'MEMBER'; // default role khi request
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  createdAt: string;
}

// PendingTenant represents a tenant the user has requested to join
export interface PendingTenant {
  id: string; // tenant ID
  name: string;
  description?: string;
  requestedAt: string; // thời điểm user gửi request
}

/** ========================
 * REQUEST TYPES (for API calls)
 * ======================== */

// Enum tương ứng với TenantVisibility ở Backend
export type TenantVisibility = 'PUBLIC' | 'PRIVATE';

// Request payload for creating a new tenant
export interface CreateTenantRequest {
  name: string;
  visibility: TenantVisibility; // Trường bắt buộc theo @NotNull ở Backend
  // Lưu ý: description đã được loại bỏ để khớp hoàn toàn với DTO Java của bạn
}

// Request payload for adding a user to a tenant (invite or admin add)
export interface AddUserToTenantRequest {
  tenantId: string;
  userId: number;
  roleInTenant: 'OWNER' | 'ADMIN' | 'MEMBER';
}

// Request payload for updating a user's role in a tenant
export interface UpdateUserRoleRequest {
  tenantId: string;
  userId: number;
  role: 'OWNER' | 'ADMIN' | 'MEMBER';
}

/** ========================
 * RESPONSE TYPES
 * ======================== */

// Response type for paginated list of tenants
export interface TenantListResponse {
  content: Tenant[];
  totalElements: number;
}

// Response type for paginated list of tenant users
export interface TenantUsersResponse {
  content: TenantUser[];
  totalElements: number;
}

// Response type for pending join requests
export interface JoinRequestsResponse {
  content: JoinRequest[];
  totalElements: number;
}

// Response type for pending tenants of current user
export interface PendingTenantsResponse {
  content: PendingTenant[];
  totalElements: number;
}
