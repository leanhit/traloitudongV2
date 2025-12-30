<template>
  <!-- Loading -->
  <el-skeleton
    v-if="loading"
    :rows="3"
    animated
  />

  <!-- Empty -->
  <el-empty
    v-else-if="tenants.length === 0"
    description="Bạn chưa gửi yêu cầu tham gia tenant nào"
  />

  <!-- List -->
  <el-space
    v-else
    direction="vertical"
    size="middle"
    style="width: 100%"
  >
    <el-card
      v-for="tenant in tenants"
      :key="tenant.id"
      shadow="never"
      class="pending-card"
    >
      <div class="item-content">
        <div class="info">
          <div class="name">
            {{ tenant.name }}
          </div>

          <div
            v-if="tenant.description"
            class="description"
          >
            {{ tenant.description }}
          </div>
        </div>

        <el-tag type="warning" effect="light">
          Đang chờ duyệt
        </el-tag>
      </div>
    </el-card>
  </el-space>
</template>

<script setup lang="ts">
import type { Tenant } from '@/types/tenant'

defineProps<{
  tenants: Tenant[]
  loading: boolean
}>()
</script>

<style scoped>
.pending-card {
  width: 100%;
}

.item-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.name {
  font-weight: 600;
  font-size: 14px;
}

.description {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>
