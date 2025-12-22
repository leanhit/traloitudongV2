// Tenant represents a tenant/organization in the system
export interface Tenant {
  id: string;
  name: string;
  ownerId: number;
  status: 'ACTIVE' | 'INACTIVE';
  createdAt: string;
  updatedAt: string;
}

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
}

// Request types
export interface CreateTenantRequest {
  name: string;
}

export interface AddUserToTenantRequest {
  tenantId: string;
  userId: number;
  roleInTenant: 'OWNER' | 'ADMIN' | 'MEMBER';
}

export interface UpdateUserRoleRequest {
  tenantId: string;
  userId: number;
  role: 'OWNER' | 'ADMIN' | 'MEMBER';
}

// Response types
export interface TenantListResponse {
  tenants: Tenant[];
  total: number;
}

export interface TenantUsersResponse {
  users: TenantUser[];
  total: number;
}
