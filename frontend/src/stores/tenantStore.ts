import { defineStore } from 'pinia';
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import { tenantApi } from '@/api/tenantApi';
import type { Tenant, CreateTenantRequest, AddUserToTenantRequest, UpdateUserRoleRequest } 
from '@/types/tenant';

// Khóa lưu trữ trong Local Storage
const ACTIVE_TENANT_ID_KEY = 'ACTIVE_TENANT_ID';

// Hàm hỗ trợ để đọc/ghi vào Local Storage
const saveTenantIdToLocalStorage = (tenantId: string) => {
    localStorage.setItem(ACTIVE_TENANT_ID_KEY, tenantId);
};

const getTenantIdFromLocalStorage = (): string | null => {
    return localStorage.getItem(ACTIVE_TENANT_ID_KEY);
};

const removeTenantIdFromLocalStorage = () => {
    localStorage.removeItem(ACTIVE_TENANT_ID_KEY);
};

export const useTenantStore = defineStore('tenantStore', () => {
  const currentTenant = ref<Tenant | null>(null);
  const userTenants = ref<Tenant[]>([]);
  const tenantUsers = ref<any[]>([]);
  const loading = ref(false);
  const error = ref<string | null>(null);

  // --- HÀM MỚI: Khởi tạo ngữ cảnh Tenant từ Local Storage ---
  const initializeTenantContext = async () => {
    const activeTenantId = getTenantIdFromLocalStorage();
    
    // Bước 1: Lấy danh sách tenants của người dùng (nếu chưa có)
    if (userTenants.value.length === 0) {
      await fetchUserTenants();
    }

    // Bước 2: Đặt currentTenant dựa trên Local Storage
    if (activeTenantId) {
      const tenant = userTenants.value.find(t => t.id === activeTenantId);
      if (tenant) {
        currentTenant.value = tenant;
      } else {
        // Nếu ID trong localStorage không còn tồn tại trong danh sách của người dùng
        console.warn(`Tenant ID ${activeTenantId} from local storage not found in user list.`);
        removeTenantIdFromLocalStorage();
        currentTenant.value = null;
      }
    }
  };

  // Cập nhật: Fetch all tenants for the current user
  const fetchUserTenants = async () => {
    try {
      loading.value = true;
      // Gọi API để lấy danh sách tenants mà người dùng này là owner/thành viên
const temp = await tenantApi.getUserTenants();
      userTenants.value = temp.data;
    } catch (err) {
      error.value = 'Failed to fetch user tenants';
      console.error(err);
      throw err; // Re-throw để hàm initialize có thể xử lý lỗi
    } finally {
      loading.value = false;
    }
  };

  // Cập nhật: Create a new tenant
  const createTenant = async (data: CreateTenantRequest) => {
    try {
      loading.value = true;
      // Server trả về TenantDTO chứa tenantId
      const newTenant = await tenantApi.createTenant(data);
      
      // Thêm tenant mới vào danh sách
      userTenants.value.push(newTenant);
      
      // Kích hoạt tenant vừa tạo ngay lập tức
      setCurrentTenant(newTenant.data);
      
      return newTenant;
    } catch (err) {
      error.value = 'Failed to create tenant';
      console.error(err);
      throw err;
    } finally {
      loading.value = false;
    }
  };

  // Xóa tenant
  const deleteTenant = async (tenantId: string) => {
    try {
      loading.value = true;
      await tenantApi.deleteTenant(tenantId);
      
      // Xóa khỏi danh sách
      const index = userTenants.value.findIndex(t => t.id === tenantId);
      if (index !== -1) {
        userTenants.value.splice(index, 1);
      }
      
      // Nếu đang xóa tenant hiện tại
      if (currentTenant.value?.id === tenantId) {
        currentTenant.value = null;
        removeTenantIdFromLocalStorage();
      }
      
      ElMessage.success('Đã xóa tenant thành công');
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || 'Không thể xóa tenant';
      error.value = errorMessage;
      ElMessage.error(errorMessage);
      console.error(err);
      throw err;
    } finally {
      loading.value = false;
    }
  };

  // Cập nhật: Set current tenant (Chức năng Switch Tenant)
  const setCurrentTenant = (tenant: Tenant | null) => {
    currentTenant.value = tenant;
    if (tenant) {
      // Lưu ID vào localStorage để duy trì ngữ cảnh
      saveTenantIdToLocalStorage(tenant.id);
    } else {
      // Xóa ID khỏi localStorage khi người dùng đăng xuất hoặc context bị xóa
      removeTenantIdFromLocalStorage();
    }
  };

  // --- Các hàm quản lý người dùng trong Tenant (Giữ nguyên) ---
  const addUserToTenant = async (data: AddUserToTenantRequest) => { /* ... */ };
  const updateUserRole = async (data: UpdateUserRoleRequest) => { /* ... */ };
  const removeUserFromTenant = async (tenantId: string, userId: number) => { /* ... */ };
  const fetchTenantUsers = async (tenantId: string) => { /* ... */ };
  
  return {
    currentTenant,
    userTenants,
    tenantUsers,
    loading,
    error,
    fetchUserTenants,
    createTenant,
    addUserToTenant,
    updateUserRole,
    removeUserFromTenant,
    fetchTenantUsers,
    setCurrentTenant,
    initializeTenantContext, // Rất quan trọng cho việc khởi động ứng dụng
    getTenantIdFromLocalStorage, // Hữu ích cho Interceptor
    deleteTenant
  };
});