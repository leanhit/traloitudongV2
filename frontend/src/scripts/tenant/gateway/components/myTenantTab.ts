import { ref } from 'vue'
import type { Tenant } from '@/types/tenant'

export default {
    name: 'MyTenantTab',
    props: {
        tenants: {
            type: Array as () => Tenant[],
            required: true
        },
        loading: {
            type: Boolean,
            default: false
        }
    },
    emits: ['enter', 'suspend', 'activate'],
    setup(props: any, context: any) {
        // Nếu cần xử lý logic trung gian, có thể dùng ref/computed
        const tenantList = ref<Tenant[]>(props.tenants)

        // Emit sự kiện vào workspace
        const enterWorkspace = (tenant: Tenant) => {
            context.emit('enter', tenant)
        }

        // Debug
        console.log('Dữ liệu tenants nhận được:', props.tenants)

        return {
            tenantList,
            enterWorkspace
        }
    }
}
