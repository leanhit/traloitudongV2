<script
    lang="ts"
    src="@/scripts/tenant/gateway/components/list.ts">
</script>

<template>
  <div class="tenant-gateway">
    <el-card class="card">
      <div class="header-row">
        <div class="header-text">
          <h2>Tenant Gateway</h2>
          <p class="header-subtitle">
            Quản lý các Workspace của bạn hoặc tìm kiếm Workspace mới để tham gia.
          </p>
        </div>

        <el-button type="primary" icon="Plus" @click="showCreateModal = true">
          Tạo tenant
        </el-button>
      </div>

      <el-tabs v-model="activeTab" class="tenant-tabs">
        <!-- MY TENANTS -->
        <el-tab-pane label="Tenant của tôi" name="my">
          <MyTenantTab
            :tenants="userTenants"
            :loading="loading"
            @enter="enterTenant"
            @suspend="handleSuspendTenant"
            @activate="handleActivateTenant"
          />
        </el-tab-pane>

        <!-- SEARCH TENANT -->
        <el-tab-pane label="Tìm kiếm tenant" name="search">
          <SearchTenantTab
            :results="searchResults?.content || []"
            :loading="loading"
            @search="handleSearchTenant"
            @join="handleRequestJoin"
          />
        </el-tab-pane>

        <!-- PENDING -->
        <el-tab-pane label="Đang xin vào" name="pending">
          <PendingTenantTab
            :tenants="pendingTenants"
            :loading="loading"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <CreateTenantModal
      v-model:visible="showCreateModal"
      :loading="loading"
      @submit="handleCreateTenant"
    />
  </div>
</template>

<style scoped>
.tenant-gateway {
  display: flex;
  justify-content: center;
  padding-top: 60px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.card {
  width: 800px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-row h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.header-text {
  display: flex;
  flex-direction: column;
  gap: 4px; /* Khoảng cách giữa tiêu đề và dòng hướng dẫn */
}

.header-row h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: #303133;
}

.header-subtitle {
  margin: 0;
  font-size: 14px;
  color: #909399; /* Màu xám nhẹ nhàng */
  line-height: 1.5;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start; /* Căn lề trên để nút bấm không bị kéo dài theo dòng chữ */
  margin-bottom: 24px;
}

.tenant-tabs {
  margin-top: 10px;
}

:deep(.el-tabs__item) {
  font-weight: 600;
  font-size: 15px;
}
</style>
