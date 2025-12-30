import { defineStore } from 'pinia';
import { ref } from 'vue';
import { tenantApi } from '@/api/tenantApi';
import type { Tenant, CreateTenantRequest } from '@/types/tenant';

const ACTIVE_TENANT_ID = 'active_tenant_id';
const TENANT_DATA = 'tenant_data';

export const useTenantStore = defineStore('tenant', () => {
  const currentTenant = ref<Tenant | null>(null);
  const userTenants = ref<Tenant[]>([]);
  const searchResults = ref<Tenant[]>([]);
  const loading = ref(false);

  const loadTenant = async (tenantId: string) => {
    loading.value = true;
    try {
      const { data } = await tenantApi.getTenant(tenantId);
      currentTenant.value = data;
      localStorage.setItem(TENANT_DATA, JSON.stringify(data));
      localStorage.setItem(ACTIVE_TENANT_ID, tenantId);
      return data;
    } finally {
      loading.value = false;
    }
  };

  const switchTenant = async (tenantId: string) => {
    loading.value = true;
    try {
      const { data } = await tenantApi.switchTenant(tenantId);
      currentTenant.value = data;
      localStorage.setItem(TENANT_DATA, JSON.stringify(data));
      localStorage.setItem(ACTIVE_TENANT_ID, tenantId);
      return data;
    } finally {
      loading.value = false;
    }
  };

  const fetchUserTenants = async () => {
    loading.value = true;
    try {
      const { data } = await tenantApi.getUserTenants();
      userTenants.value = data;
    } finally {
      loading.value = false;
    }
  };

  const searchTenants = async (keyword: string) => {
    if (!keyword.trim()) {
      searchResults.value = [];
      return;
    }
    loading.value = true;
    try {
      const { data } = await tenantApi.searchTenant(keyword);
      searchResults.value = data;
    } finally {
      loading.value = false;
    }
  };

  const createTenant = async (payload: CreateTenantRequest) => {
    loading.value = true;
    try {
      const { data } = await tenantApi.createTenant(payload);
      userTenants.value.push(data);
      await switchTenant(data.id);
      return data;
    } finally {
      loading.value = false;
    }
  };

  const clearTenant = () => {
    currentTenant.value = null;
    localStorage.removeItem(ACTIVE_TENANT_ID);
    localStorage.removeItem(TENANT_DATA);
  };

  const suspendTenant = async (id: string) => {
      await tenantApi.suspendTenant(id);
      await fetchUserTenants(); // Refresh lại danh sách sau khi đổi status
    };

    const activateTenant = async (id: string) => {
      await tenantApi.activateTenant(id);
      await fetchUserTenants(); // Refresh lại danh sách sau khi đổi status
    };

  return {
    currentTenant,
    userTenants,
    searchResults,
    loading,
    loadTenant,
    switchTenant,
    fetchUserTenants,
    searchTenants,
    createTenant,
    clearTenant,
    suspendTenant,
    activateTenant
  };
});
