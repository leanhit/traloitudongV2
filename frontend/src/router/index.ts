// src/router/index.ts
import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import ZoterDefault from '@/layouts/ZoterDefault.vue';

const routes = [
    {
        path: '/login',
        name: 'login',
        component: () => import('@/views/Login.vue'),
    },
    {
        path: '/',
        component: ZoterDefault,
        name: 'LayoutZoter',
        redirect: '/home',
        children: [
            {
                path: '',
                name: 'home',
                component: () => import('@/views/Home.vue'),
                meta: { requiresAuth: true },
            },
            {
                path: 'help',
                name: 'help',
                component: () => import('@/views/help/Index.vue'),
                meta: { requiresAuth: true },
            },
            {
                path: 'generate-embed-code',
                name: 'generate-embed-code',
                component: () =>
                    import('@/views/generate-embed-code/Index.vue'),
                meta: { requiresAuth: true },
            },
            {
                path: 'profile',
                name: 'profile',
                component: () => import('@/views/profile/Index.vue'),
                meta: { requiresAuth: true },
            },
            {
                path: 'takeover',
                name: 'takeover',
                component: () => import('@/views/takeover/Index.vue'),
                meta: { requiresAuth: true },
            },
            {
                path: 'tenant',
                name: 'tenant',
                component: () => import('@/views/tenant/Index.vue'),
                meta: { requiresAuth: true },
            },
            {
                // Route cha cho Image Manager
                path: 'image-manager',
                name: 'image-manager',
                redirect: { name: 'images' }, // Tự động chuyển hướng đến route con đầu tiên
                component: () => import('@/views/image-manager/ImageManagerLayout.vue'), // Tạo một layout cha
                meta: { requiresAuth: true },
                children: [
                    {
                        path: 'images',
                        name: 'images',
                        component: () => import('@/views/image-manager/image/Index.vue'),
                        meta: { requiresAuth: true, title: 'Image List' },
                    },
                    {
                        path: 'categories',
                        name: 'categories',
                        component: () => import('@/views/image-manager/category/Index.vue'),
                        meta: { requiresAuth: true, title: 'Category List' },
                    },
                ],
            },
            {
                // Route cha cho bot Manager
                path: 'bot-manager',
                name: 'bot-manager',
                redirect: { name: 'images' }, // Tự động chuyển hướng đến route con đầu tiên
                component: () => import('@/views/bot-manager/BotManagerLayout.vue'), // Tạo một layout cha
                meta: { requiresAuth: true },
                children: [
                    {
                        path: 'create-bot',
                        name: 'create-bot',
                        component: () => import('@/views/bot-manager/create-bot/Index.vue'),
                        meta: { requiresAuth: true, title: 'Midleware Bot List' },
                    },
                    {
                        path: 'bot-botpress',
                        name: 'bot-botpress',
                        component: () => import('@/views/bot-manager/bot-botpress/Index.vue'),
                        meta: { requiresAuth: true, title: 'Botpress Bot List' },
                    },
                ],
            },
            {
                // Route cha cho connection Manager
                path: 'connection-manager',
                name: 'connection-manager',
                redirect: { name: 'images' }, // Tự động chuyển hướng đến route con đầu tiên
                component: () => import('@/views/connection-manager/ConnectionManagerLayout.vue'), // Tạo một layout cha
                meta: { requiresAuth: true },
                children: [
                    {
                        path: 'auto-connect',
                        name: 'auto-connect',
                        component: () => import('@/views/connection-manager/auto-connect/Index.vue'),
                        meta: { requiresAuth: true, title: 'Midleware Bot List' },
                    },
                    {
                        path: 'hand-connect',
                        name: 'hand-connect',
                        component: () => import('@/views/connection-manager/hand-connect/Index.vue'),
                        meta: { requiresAuth: true, title: 'Botpress Bot List' },
                    },
                ],
            },
            {
                // Route cha cho connection Manager
                path: 'phone-review',
                name: 'phone-review',
                redirect: { name: 'images' }, // Tự động chuyển hướng đến route con đầu tiên
                component: () => import('@/views/phone-review/PhoneReviewLayout.vue'), // Tạo một layout cha
                meta: { requiresAuth: true },
                children: [
                    {
                        path: 'temp-user',
                        name: 'temp-user',
                        component: () => import('@/views/phone-review/temp-user/Index.vue'),
                        meta: { requiresAuth: true, title: 'Midleware Bot List' },
                    },
                    {
                        path: 'phone-captured',
                        name: 'phone-captured',
                        component: () => import('@/views/phone-review/phone-captured/Index.vue'),
                        meta: { requiresAuth: true, title: 'Botpress Bot List' },
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

router.beforeEach((to, from, next) => {
    const authStore = useAuthStore();

    // Nếu đã đăng nhập mà vào lại login → chuyển về home
    if (authStore.token && to.name === 'login') {
        return next({ name: 'home' });
    }

    // Nếu chưa đăng nhập mà vào trang yêu cầu auth
    if (!authStore.token && to.meta.requiresAuth) {
        return next({ name: 'login' });
    }

    next();
});

export default router;
