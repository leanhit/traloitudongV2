<template>
  <div class="tenant-list">
    <div class="header">
      <h2>My Tenants</h2>
      <el-button type="primary" @click="showCreateDialog = true">
        <i class="el-icon-plus"></i> Create Tenant
      </el-button>
    </div>

    <el-table :data="tenants" v-loading="loading" style="width: 100%">
      <el-table-column prop="name" label="Name" />
      <el-table-column prop="status" label="Status" width="120">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">
            {{ row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="Created At" width="180" />
      <el-table-column label="Thao tác" width="200">
        <template #default="{ row }">
          <el-button 
            type="primary" 
            size="small" 
            @click="selectTenant(row)"
            :loading="loading"
          >
            Chọn
          </el-button>
          <el-button 
            v-if="row.ownerId === userStore.user?.id"
            type="danger" 
            size="small" 
            @click="confirmDeleteTenant(row)"
            :loading="loading"
          >
            Xóa
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Create Tenant Dialog -->
    <el-dialog v-model="showCreateDialog" title="Create New Tenant" width="500px">
      <el-form :model="newTenant" :rules="rules" ref="tenantForm">
        <el-form-item label="Tenant Name" prop="name">
          <el-input v-model="newTenant.name" placeholder="Enter tenant name" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCreateDialog = false">Cancel</el-button>
          <el-button 
            type="primary" 
            @click="createTenant"
            :loading="loading"
          >
            Create
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useTenantStore } from '@/stores/tenantStore';
import { useAuthStore } from '@/stores/auth';
import type { Tenant } from '@/types/tenant';

const router = useRouter();
const tenantStore = useTenantStore();
const userStore = useAuthStore();

const tenants = ref<Tenant[]>([]);
const loading = ref(false);
const showCreateDialog = ref(false);
const newTenant = ref({
  name: ''
});

const rules = {
  name: [
    { required: true, message: 'Please input tenant name', trigger: 'blur' },
    { min: 3, max: 50, message: 'Length should be 3 to 50', trigger: 'blur' }
  ]
};

const fetchTenants = async () => {
  try {
    loading.value = true;
    await tenantStore.fetchUserTenants();
    tenants.value = tenantStore.userTenants;
  } catch (error) {
    ElMessage.error('Failed to load tenants');
  } finally {
    loading.value = false;
  }
};

const createTenant = async () => {
  try {
    loading.value = true;
    await tenantStore.createTenant({
      name: newTenant.value.name
    });
    
    ElMessage.success('Tenant created successfully');
    showCreateDialog.value = false;
    newTenant.value.name = '';
    await fetchTenants();
  } catch (error) {
    ElMessage.error('Failed to create tenant');
  } finally {
    loading.value = false;
  }
};

const selectTenant = (tenant: Tenant) => {
  tenantStore.setCurrentTenant(tenant);
  router.push({ name: 'tenant-details', params: { id: tenant.id } });
};

const confirmDeleteTenant = async (tenant: Tenant) => {
  try {
    await ElMessageBox.confirm(
      `Bạn có chắc chắn muốn xóa tenant "${tenant.name}"? Hành động này không thể hoàn tác.`,
      'Xác nhận xóa',
      {
        confirmButtonText: 'Xóa',
        cancelButtonText: 'Hủy',
        type: 'warning',
      }
    );
    
    // Nếu người dùng xác nhận, thực hiện xóa
    await tenantStore.deleteTenant(tenant.id);
    
    // Làm mới danh sách
    await fetchTenants();
    
    ElMessage.success('Xóa tenant thành công');
  } catch (error) {
    // Bỏ qua lỗi nếu người dùng hủy
    if (error !== 'cancel' && error !== 'close') {
      console.error('Lỗi khi xóa tenant:', error);
      ElMessage.error('Có lỗi xảy ra khi xóa tenant');
    }
  }
};

onMounted(() => {
  fetchTenants();
});
</script>

<style scoped>
.tenant-list {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
</style>
