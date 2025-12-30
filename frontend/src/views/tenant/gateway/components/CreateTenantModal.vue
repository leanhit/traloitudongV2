<template>
  <el-dialog
    :model-value="visible"
    @update:model-value="$emit('update:visible', $event)"
    title="Tạo Workspace mới"
    width="420px"
    destroy-on-close
  >
    <el-form label-position="top">
      <el-form-item label="Tên Workspace" required>
        <el-input v-model="form.name" placeholder="Nhập tên workspace..." />
      </el-form-item>

      <el-form-item label="Chế độ hiển thị" required>
        <el-radio-group v-model="form.visibility">
          <el-radio label="PUBLIC">Công khai</el-radio>
          <el-radio label="PRIVATE">Riêng tư</el-radio>
        </el-radio-group>
        <div class="tip">
          {{ form.visibility === 'PUBLIC' 
            ? 'Mọi người có thể tìm thấy workspace này.' 
            : 'Workspace này sẽ bị ẩn khỏi tìm kiếm.' 
          }}
        </div>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="$emit('update:visible', false)">Hủy</el-button>
      <el-button
        type="primary"
        :loading="loading"
        :disabled="!form.name"
        @click="handleSubmit"
      >
        Tạo ngay
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import type { TenantVisibility } from '@/types/tenant'

defineProps<{
  visible: boolean
  loading: boolean
}>()

const emit = defineEmits(['update:visible', 'submit'])

const form = reactive({
  name: '',
  visibility: 'PUBLIC' as TenantVisibility
})

const handleSubmit = () => {
  emit('submit', { ...form })
  // Reset form sau khi submit
  form.name = ''
  form.visibility = 'PUBLIC'
}
</script>

<style scoped>
.tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>