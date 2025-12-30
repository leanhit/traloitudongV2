import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { tenantMembershipApi } from '@/api/tenantMembershipApi';
import type { TenantUser, JoinRequest } from '@/types/tenant';

export const useTenantMembershipStore = defineStore('tenantMembership', () => {
  const pendingJoinRequests = ref<JoinRequest[]>([]);
  const tenantUsers = ref<TenantUser[]>([]);
  const loading = ref(false);

  const pendingTenants = computed(() =>
    pendingJoinRequests.value
      .filter(req => req.status === 'PENDING')
      .map(req => req.tenant)
  );

  const fetchPendingJoinRequests = async () => {
    loading.value = true;
    try {
      const { data } = await tenantMembershipApi.getMyPending();
      pendingJoinRequests.value = data;
    } finally {
      loading.value = false;
    }
  };

  const requestJoinTenant = async (tenantId: string) => {
    await tenantMembershipApi.requestJoinTenant(tenantId);
    ElMessage.success('Đã gửi yêu cầu tham gia');
    await fetchPendingJoinRequests();
  };

  const fetchTenantUsers = async (tenantId: string) => {
    loading.value = true;
    try {
      const { data } = await tenantMembershipApi.getTenantUsers(tenantId);
      tenantUsers.value = data;
    } finally {
      loading.value = false;
    }
  };

  const processJoinRequest = async (tenantId: string, requestId: string, approve: boolean) => {
    if (approve) {
      await tenantMembershipApi.approveJoinRequest(tenantId, requestId);
      ElMessage.success('Đã chấp nhận yêu cầu');
    } else {
      await tenantMembershipApi.rejectJoinRequest(tenantId, requestId);
      ElMessage.success('Đã từ chối yêu cầu');
    }
    await Promise.all([
      fetchPendingJoinRequests(),
      fetchTenantUsers(tenantId)
    ]);
  };

  const updateUserRole = async (tenantId: string, userId: number, role: string) => {
    await tenantMembershipApi.updateUserRole(tenantId, userId, role);
    await fetchTenantUsers(tenantId);
    ElMessage.success('Cập nhật vai trò thành công');
  };

  const removeUserFromTenant = async (tenantId: string, userId: number) => {
    await tenantMembershipApi.removeUserFromTenant(tenantId, userId);
    tenantUsers.value = tenantUsers.value.filter(u => u.id.userId !== userId);
    ElMessage.success('Đã xóa thành viên');
  };

  return {
    pendingJoinRequests,
    tenantUsers,
    pendingTenants,
    loading,
    fetchPendingJoinRequests,
    requestJoinTenant,
    fetchTenantUsers,
    processJoinRequest,
    updateUserRole,
    removeUserFromTenant
  };
});
