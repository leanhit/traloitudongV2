// ✅ Đúng — dùng instance đã setup baseURL và interceptor
import axios from '@/plugins/axios'; // Import instance Axios đã setup

import type {
  Tenant,
  CreateTenantRequest,
  AddUserToTenantRequest,
  UpdateUserRoleRequest,
  TenantUser
} from '@/types/tenant';

// Không cần định nghĩa API_URL vì đã dùng instance Axios
// const API_URL = import.meta.env.VITE_API_URL || '/api'; 

export const tenantApi = {
  // Create a new tenant
  createTenant(data: CreateTenantRequest): Promise<Tenant> {
    // Trả về promise trực tiếp từ axios.post
    return axios.post('/tenant/create', data);
  },

  // Get a tenant by ID
  getTenant(tenantId: string): Promise<Tenant> {
    // Trả về promise trực tiếp từ axios.get
    return axios.get(`/tenant/${tenantId}`);
  },

  // Get all tenants for the current user
  getUserTenants(): Promise<Tenant[]> {
    return axios.get('/tenant');
  },

  // Add a user to a tenant
  addUserToTenant(data: AddUserToTenantRequest): Promise<void> {
    return axios.post('/tenant-user', data);
  },

  // Get all users in a tenant
  getTenantUsers(tenantId: string): Promise<TenantUser[]> {
    return axios.get(`/tenant-user/tenant/${tenantId}`);
  },

  // Update user role in a tenant
  updateUserRole(data: UpdateUserRoleRequest): Promise<void> {
    return axios.put(
      `/tenant-user/${data.userId}/role`,
      { role: data.role },
      { params: { tenantId: data.tenantId } }
    );
  },

  // Remove a user from a tenant
  removeUserFromTenant(tenantId: string, userId: number): Promise<void> {
    return axios.delete(`/tenant-user/${userId}`, {
      params: { tenantId }
    });
  },
  
  // Delete a tenant
  deleteTenant(tenantId: string): Promise<void> {
    return axios.delete(`/tenant/${tenantId}`);
  }
};

// Lưu ý: Nếu muốn dùng cách export default, bạn có thể thay thế 
// 'export const tenantApi' bằng 'const tenantApi' và thêm 'export default tenantApi' ở cuối.