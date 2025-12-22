// /src/plugins/axios.ts (hoặc .js)

import axios from 'axios';
import { useAuthStore } from '@/stores/auth';
import { useTenantStore } from '@/stores/tenantStore'; // <<< Cần import Tenant Store
import router from '@/router';

const instance = axios.create({
    baseURL: import.meta.env.VITE_API_URL,
    headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
    },
});

instance.interceptors.request.use(
    (config) => {
        // 1. Xử lý JWT (Authorization)
        const token = localStorage.getItem('accessToken');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }

        // 2. XỬ LÝ TENANT ID (Multi-Tenancy Context)
        const ACTIVE_TENANT_ID_KEY = 'ACTIVE_TENANT_ID'; // Đảm bảo khớp với Pinia store
        const activeTenantId = localStorage.getItem(ACTIVE_TENANT_ID_KEY);

        // Đính kèm Tenant ID nếu tồn tại
        if (activeTenantId) {
            config.headers['X-Tenant-ID'] = activeTenantId;

            // <<< LOG ĐỂ KIỂM TRA >>>
            console.log(`[AXIOS INTERCEPTOR] Tenant ID found. Setting X-Tenant-ID: ${activeTenantId}`);
            // <<< LOG ĐỂ KIỂM TRA >>>
        } else {
            // <<< LOG ĐỂ KIỂM TRA >>>
            console.log(`[AXIOS INTERCEPTOR] No active Tenant ID found in localStorage for request to ${config.url}`);
            // <<< LOG ĐỂ KIỂM TRA >>>
        }

        return config;
    },
    (error) => Promise.reject(error)
);

instance.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response && error.response.status === 401) {
            const authStore = useAuthStore();
            authStore.logout();
            router.push({ name: 'login' });
        }
        return Promise.reject(error);
    }
);

export default instance;