import axios from '@/plugins/axios';
import type { Tenant, CreateTenantRequest } from '@/types/tenant';

export const tenantApi = {
  // Tenant core
  async getTenant(tenantId: string): Promise<{ data: Tenant }> {
    return axios.get(`/tenants/${tenantId}`);
  },

  async switchTenant(tenantId: string): Promise<{ data: Tenant }> {
    return axios.post(`/tenants/${tenantId}/switch`);
  },

  async getUserTenants(): Promise<{ data: Tenant[] }> {
    return axios.get('/tenants/me');
  },

  async createTenant(data: CreateTenantRequest): Promise<{ data: Tenant }> {
    return axios.post('/tenants', data);
  },

  async deleteTenant(tenantId: string): Promise<void> {
    return axios.delete(`/tenants/${tenantId}`);
  },

  async searchTenant(keyword: string): Promise<{ data: Tenant[] }> {
    return axios.get('/tenants/search', { params: { keyword } });
  },
  
  async suspendTenant(id: number | string): Promise<void> {
    // Sử dụng POST và truyền đúng ID vào path
    return axios.post(`/tenants/${id}/suspend`);
  },

  async activateTenant(id: number | string): Promise<void> {
    // Sử dụng POST và truyền đúng ID vào path
    return axios.post(`/tenants/${id}/activate`);
  }
};
