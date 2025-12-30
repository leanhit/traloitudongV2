// /src/plugins/axios.ts (hoặc .js)
import axios from 'axios';
import { useAuthStore } from '@/stores/auth';
import router from '@/router';

const instance = axios.create({
    baseURL: import.meta.env.VITE_API_URL,
    headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
    },
});

// Danh sách các API KHÔNG cần đính kèm Tenant ID (Global APIs)
const EXCLUDED_PATHS = [
    '/auth/login',
    '/auth/register',
    '/auth/forgot-password', // Loại trừ quên mật khẩu
    '/auth/reset-password',  // Loại trừ đặt lại mật khẩu
    '/users/change-password',// Loại trừ đổi mật khẩu khi đã login
    '/tenants/search',
    '/tenants/my-list'
];

instance.interceptors.request.use(
    (config) => {
        // 1. Xử lý JWT
        const token = localStorage.getItem('accessToken');
        //console.log('Request interceptor - Token:', token); // Thêm dòng này để debug
        //console.log('Request URL:', config.url); // Log URL được gọi
        
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }

        // 2. XỬ LÝ TENANT ID VỚI BỘ LỌC
        const activeTenantId = localStorage.getItem('ACTIVE_TENANT_ID');
        
        // Kiểm tra xem URL hiện tại có nằm trong danh sách loại trừ không
        const isExcluded = EXCLUDED_PATHS.some(path => config.url?.includes(path));

        if (activeTenantId && !isExcluded) {
            config.headers['X-Tenant-ID'] = activeTenantId;
            // console.log(`[Tenant Context] Applied ID: ${activeTenantId} for ${config.url}`);
        }

        return config;
    },
    (error) => Promise.reject(error)
);

instance.interceptors.response.use(
    (response) => {
        //console.log('Response:', response.config.url, response.status); // Log response thành công
        return response;
    },
    (error) => {
        console.error('Response error:', { // Log chi tiết lỗi
            url: error.config?.url,
            status: error.response?.status,
            data: error.response?.data
        });
        
        if (error.response?.status === 401) {
            const authStore = useAuthStore();
            authStore.logout();
            router.push({ name: 'login' });
        }
        return Promise.reject(error);
    }
);

export default instance;