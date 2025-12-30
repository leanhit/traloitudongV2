import { defineComponent, ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import { useTenantStore } from '@/stores/tenantStore';
import { useTenantMembershipStore } from '@/stores/tenantMembershipStore';
import { ElMessage } from 'element-plus';

import MyTenantTab from '@/views/tenant/gateway/components/MyTenantTab.vue';
import SearchTenantTab from '@/views/tenant/gateway/components/SearchTenantTab.vue';
import PendingTenantTab from '@/views/tenant/gateway/components/PendingTenantTab.vue';
import CreateTenantModal from '@/views/tenant/gateway/components/CreateTenantModal.vue';

export default defineComponent({
  name: 'TenantGateway',
  components: { MyTenantTab, SearchTenantTab, PendingTenantTab, CreateTenantModal },
  setup() {
    const router = useRouter();
    const tenantStore = useTenantStore();
    const membershipStore = useTenantMembershipStore();

    const { userTenants, searchResults, currentTenant, loading } = storeToRefs(tenantStore);
    const { pendingTenants } = storeToRefs(membershipStore);

    const activeTab = ref('my');
    const showCreateModal = ref(false);

    onMounted(async () => {
      try {
        await Promise.all([
          tenantStore.fetchUserTenants(),
          membershipStore.fetchPendingJoinRequests()
        ]);
      } catch (err) {
        console.error(err);
        ElMessage.error('Không thể tải dữ liệu tenant');
      }
    });

    const enterTenant = async (tenant: any) => {
      try {
        await tenantStore.switchTenant(tenant.id);
        ElMessage.success(`Đã vào workspace: ${tenant.name}`);
        // Chuyển hướng về trang chủ sau khi chuyển tenant thành công
        await router.push({ name: 'home' });
      } catch {
        ElMessage.error('Không thể chuyển workspace');
      }
    };

    const handleSearchTenant = async (keyword: string) => {
      await tenantStore.searchTenants(keyword);
    };

    const handleRequestJoin = async (tenantId: string) => {
      await membershipStore.requestJoinTenant(tenantId);
      activeTab.value = 'pending';
    };

    const handleCreateTenant = async (payload: { name: string, visibility: string }) => {
      try {
        // Gọi store truyền object { name, visibility }
        await tenantStore.createTenant(payload);

        showCreateModal.value = false;
        ElMessage.success('Tạo workspace thành công');

        // Refresh lại danh sách để thấy tenant mới
        await tenantStore.fetchUserTenants();
      } catch (err) {
        ElMessage.error('Không thể tạo workspace');
      }
    };

    const handleSuspendTenant = async (id: string) => {
      try {
        await tenantStore.suspendTenant(id); // Giả sử store đã có hàm này
        ElMessage.success('Đã tạm dừng workspace');
        await tenantStore.fetchUserTenants(); // Load lại danh sách
      } catch (err) {
        ElMessage.error('Lỗi khi tạm dừng');
      }
    };

    const handleActivateTenant = async (id: string) => {
      try {
        await tenantStore.activateTenant(id);
        ElMessage.success('Đã kích hoạt workspace');
        await tenantStore.fetchUserTenants();
      } catch (err) {
        ElMessage.error('Lỗi khi kích hoạt');
      }
    };

    return {
      userTenants,
      pendingTenants,
      searchResults,
      currentTenant,
      loading,
      activeTab,
      showCreateModal,
      enterTenant,
      handleSearchTenant,
      handleRequestJoin,
      handleCreateTenant,
      handleSuspendTenant,
      handleActivateTenant
    };
  }
});
