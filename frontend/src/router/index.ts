// src/router/index.ts
import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { useTenantStore } from '@/stores/tenantStore';
import ZoterDefault from '@/layouts/ZoterDefault.vue';

const routes = [
    {
        path: '/login',
        name: 'login',
        component: () => import('@/views/Login.vue'),
    },
    {
        path: '/tenant-gateway',
        name: 'tenant-gateway',
        component: () => import('@/views/tenant/gateway/Index.vue'),
        meta: { requiresAuth: true },
    },
    {
        path: '/',
        component: ZoterDefault,
        name: 'LayoutZoter',
        redirect: '/home',
        meta: { requiresAuth: true }, // Bảo vệ tất cả route con
        children: [
            {
                path: 'home',
                name: 'home',
                component: () => import('@/views/Home.vue'),
            },
            {
                path: 'help',
                name: 'help',
                component: () => import('@/views/help/Index.vue'),
            },
            {
                path: 'generate-embed-code',
                name: 'generate-embed-code',
                component: () => import('@/views/generate-embed-code/Index.vue'),
            },
            {
                path: 'profile',
                name: 'profile',
                component: () => import('@/views/profile/Index.vue'),
            },
            {
                path: 'takeover',
                name: 'takeover',
                component: () => import('@/views/takeover/Index.vue'),
            },
            {
                path: 'tenant',
                name: 'tenant',
                component: () => import('@/views/tenant/Index.vue'),
            },
            // Image Manager
            {
                path: 'image-manager',
                name: 'image-manager',
                redirect: { name: 'images' },
                component: () => import('@/views/image-manager/ImageManagerLayout.vue'),
                children: [
                    {
                        path: 'images',
                        name: 'images',
                        component: () => import('@/views/image-manager/image/Index.vue'),
                        meta: { title: 'Image List' },
                    },
                    {
                        path: 'categories',
                        name: 'categories',
                        component: () => import('@/views/image-manager/category/Index.vue'),
                        meta: { title: 'Category List' },
                    },
                ],
            },
            // Bot Manager
            {
                path: 'bot-manager',
                name: 'bot-manager',
                redirect: { name: 'create-bot' }, // Sửa lại redirect đúng name
                component: () => import('@/views/bot-manager/BotManagerLayout.vue'),
                children: [
                    {
                        path: 'create-bot',
                        name: 'create-bot',
                        component: () => import('@/views/bot-manager/create-bot/Index.vue'),
                        meta: { title: 'Middleware Bot List' },
                    },
                    {
                        path: 'bot-botpress',
                        name: 'bot-botpress',
                        component: () => import('@/views/bot-manager/bot-botpress/Index.vue'),
                        meta: { title: 'Botpress Bot List' },
                    },
                ],
            },
            // Connection Manager
            {
                path: 'connection-manager',
                name: 'connection-manager',
                redirect: { name: 'auto-connect' },
                component: () => import('@/views/connection-manager/ConnectionManagerLayout.vue'),
                children: [
                    {
                        path: 'auto-connect',
                        name: 'auto-connect',
                        component: () => import('@/views/connection-manager/auto-connect/Index.vue'),
                        meta: { title: 'Auto Connect List' },
                    },
                    {
                        path: 'hand-connect',
                        name: 'hand-connect',
                        component: () => import('@/views/connection-manager/hand-connect/Index.vue'),
                        meta: { title: 'Hand Connect List' },
                    },
                ],
            },
            // Phone Review
            {
                path: 'phone-review',
                name: 'phone-review',
                redirect: { name: 'temp-user' },
                component: () => import('@/views/phone-review/PhoneReviewLayout.vue'),
                children: [
                    {
                        path: 'temp-user',
                        name: 'temp-user',
                        component: () => import('@/views/phone-review/temp-user/Index.vue'),
                        meta: { title: 'Temp User List' },
                    },
                    {
                        path: 'phone-captured',
                        name: 'phone-captured',
                        component: () => import('@/views/phone-review/phone-captured/Index.vue'),
                        meta: { title: 'Phone Captured List' },
                    },
                ],
            },
        ],
    },
];

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes,
});

// Navigation Guard
router.beforeEach(async (to, from, next) => {
    const authStore = useAuthStore();
    const tenantStore = useTenantStore();

    const token = authStore.token;
    // Kiểm tra tenant từ store hoặc localStorage
    const activeTenantId = tenantStore.currentTenant?.id;

    // 1. Nếu chưa đăng nhập
    if (!token) {
        if (to.meta.requiresAuth) {
            return next({ name: 'login', query: { redirect: to.fullPath } });
        }
        return next();
    }

    // 2. Nếu đã đăng nhập mà cố vào login
    if (to.name === 'login') {
        return activeTenantId ? next({ name: 'home' }) : next({ name: 'tenant-gateway' });
    }

    // 3. Nếu đã đăng nhập nhưng chưa chọn Tenant (và không phải đang ở trang chọn tenant)
    if (to.meta.requiresAuth && !activeTenantId && to.name !== 'tenant-gateway') {
        return next({ 
            name: 'tenant-gateway', 
            query: { redirect: to.fullPath } 
        });
    }

    next();
});

export default router;