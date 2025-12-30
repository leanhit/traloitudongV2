<script
    lang="ts"
    src="@/scripts/tenant/gateway/components/myTenantTab.ts">
</script>

<template>
  <div class="my-tenant-tab">
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="3" animated />
    </div>

    <template v-else>
      <el-empty v-if="!tenants || tenants.length === 0" description="Bạn chưa thuộc tenant nào" />

      <div v-else class="tenant-list">
        <div v-for="tenant in tenants" :key="tenant.id" class="tenant-item">
          <div class="tenant-info">
            <el-avatar 
              :size="40" 
              :class="['tenant-avatar', { 'is-inactive': tenant.status === 'INACTIVE' }]"
            >
              {{ tenant.name.charAt(0).toUpperCase() }}
            </el-avatar>
            <div class="details">
              <div class="name">
                {{ tenant.name }}
                <el-tag v-if="tenant.status === 'INACTIVE'" type="info" size="small" class="status-tag">
                  Đã đóng
                </el-tag>
              </div>
              <div class="id">ID: {{ tenant.id }}</div>
            </div>
          </div>
          
          <div class="actions">
            <el-button 
              v-if="tenant.status === 'ACTIVE'" 
              type="danger" plain size="default" 
              @click="$emit('suspend', tenant.id)"
            >
              Tạm dừng
            </el-button>
            
            <el-button 
              v-else 
              type="success" plain size="default" 
              @click="$emit('activate', tenant.id)"
            >
              Kích hoạt
            </el-button>

            <el-button
              type="primary"
              :disabled="tenant.status === 'INACTIVE'"
              @click="$emit('enter', tenant)"
            >
              Vào Workspace
            </el-button>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.tenant-avatar.is-inactive { background-color: #909399; }
.name { display: flex; align-items: center; font-weight: 600; }
.status-tag { margin-left: 8px; }
.actions { display: flex; gap: 10px; }

.my-tenant-tab {
  padding: 10px 0;
}
.tenant-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.tenant-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  transition: all 0.2s ease;
}
.tenant-item:hover {
  background-color: #f0f7ff;
  border-color: #c6e2ff;
  transform: translateY(-2px);
}
.tenant-info {
  display: flex;
  align-items: center;
  gap: 15px;
}
.tenant-avatar {
  background-color: #409eff;
  color: white;
  font-weight: bold;
}
.details .name {
  font-weight: 600;
  font-size: 16px;
  color: #303133;
}
.details .id {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
.empty-tip {
  color: #909399;
  font-size: 14px;
}
.loading-state {
  padding: 20px;
}
.tenant-avatar.is-inactive { background-color: #909399; }
.name { display: flex; align-items: center; font-weight: 600; }
.status-tag { margin-left: 8px; }
.actions { display: flex; gap: 10px; }
</style>