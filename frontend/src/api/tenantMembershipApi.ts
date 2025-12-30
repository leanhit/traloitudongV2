import axios from '@/plugins/axios';
import type { TenantUser,JoinRequest } from '@/types/tenant';

export const tenantMembershipApi = {
  // Member self-service
  async getMyPending(): Promise<{ data: JoinRequest[] }> {
    return axios.get('/tenants/members/pending-tenants');
  },

  async requestJoinTenant(tenantId: string): Promise<void> {
    return axios.post(`/tenants/${tenantId}/members/join-requests`);
  },

  // Admin / Owner
  async getTenantUsers(tenantId: string): Promise<{ data: TenantUser[] }> {
    return axios.get(`/tenants/${tenantId}/members`);
  },

  async getTenantJoinRequests(tenantId: string): Promise<{ data: JoinRequest[] }> {
    return axios.get(`/tenants/${tenantId}/members/join-requests`);
  },

  async approveJoinRequest(tenantId: string, requestId: string): Promise<void> {
    return axios.patch(`/tenants/${tenantId}/members/join-requests/${requestId}`, {
      status: 'APPROVED'
    });
  },

  async rejectJoinRequest(tenantId: string, requestId: string): Promise<void> {
    return axios.patch(`/tenants/${tenantId}/members/join-requests/${requestId}`, {
      status: 'REJECTED'
    });
  },

  async updateUserRole(tenantId: string, userId: number, role: string): Promise<void> {
    return axios.put(`/tenants/${tenantId}/members/${userId}/role`, { role });
  },

  async removeUserFromTenant(tenantId: string, userId: number): Promise<void> {
    return axios.delete(`/tenants/${tenantId}/members/${userId}`);
  }
};
