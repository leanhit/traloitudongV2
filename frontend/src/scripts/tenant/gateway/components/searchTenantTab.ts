import { ref, watch } from 'vue'
import { Search } from '@element-plus/icons-vue'
import type { Tenant } from '@/types/tenant'

export default {
  name: 'SearchTenantTab',

  props: {
    results: {
      type: Array as () => Tenant[],
      required: true
    },
    loading: {
      type: Boolean,
      required: true
    }
  },

  emits: ['search', 'join'],

  setup(props, context) {
    /* =====================
     * STATE
     * ===================== */
    const keyword = ref('')

    /* =====================
     * METHODS
     * ===================== */
    const handleSearch = (value?: unknown) => {
      let searchValue = ''

      if (typeof value === 'string') {
        searchValue = value.trim()
      } else {
        searchValue = keyword.value.trim()
      }

      context.emit('search', searchValue)
    }

    const onJoinClick = (tenantId: string) => {
      console.log('[SearchTenantTab] join tenant:', tenantId)
      context.emit('join', tenantId)
    }

    // ✅ WATCH KEYWORD
    watch(
      keyword,
      (newVal) => {
        handleSearch(newVal);
      }
    );

    return {
      Search,
      keyword,
      handleSearch,
      onJoinClick
    }
  }
}
