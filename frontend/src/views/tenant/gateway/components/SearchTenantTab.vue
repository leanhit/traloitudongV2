<script
    lang="ts"
    src="@/scripts/tenant/gateway/components/searchTenantTab.ts">
</script>

<template>
  <div class="search-tenant-container">
    <!-- SEARCH BOX -->
    <div class="search-box">
      <el-input
        v-model="keyword"
        placeholder="Nhập tên Workspace để tìm kiếm..."
        class="search-input"
        clearable
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button
            :icon="Search"
            :loading="loading"
            @mousedown.prevent="handleSearch"
          />
        </template>
      </el-input>
    </div>

    <!-- LOADING -->
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="3" animated />
    </div>

    <!-- RESULT -->
    <template v-else>
      <el-empty
        v-if="!results || results.length === 0"
        :description="
          keyword
            ? 'Không tìm thấy Workspace nào'
            : 'Nhập tên Workspace để tìm kiếm'
        "
      />

      <div v-else class="tenant-list">
        <div
          v-for="tenant in results"
          :key="tenant.id"
          class="tenant-item"
        >
          <div class="tenant-info">
            <el-avatar :size="40" class="tenant-avatar">
              {{ tenant.name?.charAt(0).toUpperCase() || '?' }}
            </el-avatar>

            <div class="details">
              <div class="name">{{ tenant.name }}</div>
              <div class="id">ID: {{ tenant.id }}</div>
            </div>
          </div>

          <el-button
            v-if="tenant.membershipStatus === 'NONE'"
            type="primary"
            plain
            @click="onJoinClick(tenant.id)"
          >
            Xin gia nhập
          </el-button>

          <el-button
            v-else-if="tenant.membershipStatus === 'PENDING'"
            type="warning"
            disabled
            class="is-pending"
          >
            Đang chờ duyệt
          </el-button>

          <el-tag
            v-else-if="tenant.membershipStatus === 'APPROVED'"
            type="success"
            effect="light"
          >
            Đã tham gia
          </el-tag>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.search-tenant-container {
  padding: 10px 0;
}

.search-box {
  max-width: 500px;
  margin-bottom: 24px;
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

.loading-state {
  padding: 20px;
}
</style>
