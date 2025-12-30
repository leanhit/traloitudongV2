import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { ref } from 'vue';
import { usersApi } from '@/api/usersApi';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '@/stores/auth';
import { useTenantStore } from '@/stores/tenantStore';

export default {
    props: ['viewSettings'],
    emits: ['onChangeView'],
    setup(props: any, context: any) {
        const { t } = useI18n();
        const router = useRouter();
        const authStore = useAuthStore();

        // Data bindings
        const emailLogin = ref('');
        const passwordLogin = ref('');
        const passwordConfirm = ref('');
        const isLoginView = ref(true);

        // UI state
        const emptyFields = ref(false);
        const showPassword = ref(false);
        const showPasswordConfirm = ref(false);

        // Functions
        function toggleView() {
            isLoginView.value = !isLoginView.value;
            emailLogin.value = '';
            passwordLogin.value = '';
            passwordConfirm.value = '';
            emptyFields.value = false;
        }

        function isValidEmail(email: string): boolean {
            const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            return regex.test(email);
        }

        async function doLogin() {
            if (!emailLogin.value || !passwordLogin.value) {
                ElMessage.error(t('Please fill in all fields.'));
                emptyFields.value = true;
                return;
            }

            if (!isValidEmail(emailLogin.value)) {
                ElMessage.error(t('Invalid email format.'));
                return;
            }

            try {
                const response = await usersApi.login({
                    email: emailLogin.value,
                    password: passwordLogin.value,
                });

                if (response.status === 200) {
                    const { token, user } = response.data;
                    await authStore.login(token, user);
                    
                    // Check if user has an active tenant
                    const tenantStore = useTenantStore();
                    //await tenantStore.initializeTenantContext();
                    
                    if (!tenantStore.currentTenant) {
                        // No active tenant, redirect to tenant gateway
                        await router.push({ name: 'tenant-gateway' });
                    } else {
                        // Has active tenant, go to home
                        await router.push('/');
                    }
                } else {
                    ElMessage.error(t(`Oops, ${response.data.message}`));
                }
            } catch (error: any) {
                console.error('Login error:', error);
                const errorMessage =
                    error.response?.data?.message ||
                    'Login failed. Please check your credentials.';
                ElMessage.error(t(errorMessage));
            }
        }

        async function doRegister() {
            if (
                !emailLogin.value ||
                !passwordLogin.value ||
                !passwordConfirm.value
            ) {
                ElMessage.error(t('Please fill in all fields.'));
                emptyFields.value = true;
                return;
            }

            if (!isValidEmail(emailLogin.value)) {
                ElMessage.error(t('Invalid email format.'));
                return;
            }

            if (passwordLogin.value !== passwordConfirm.value) {
                ElMessage.error(t('Passwords do not match.'));
                return;
            }

            const data = {
                email: emailLogin.value,
                password: passwordLogin.value,
            };

            try {
                const response = await usersApi.register(data);

                if (response.status === 200) {
                    ElMessage.success(
                        t('Registered successfully. Please log in.')
                    );
                    toggleView();
                } else {
                    ElMessage.error(t(`Oops, ${response.data.message}`));
                }
            } catch (err: any) {
                console.error('Registration error:', err);
                const status = err.response?.status;
                if (status === 409) {
                    ElMessage.error(
                        t('Email already exists. Please use a different email.')
                    );
                } else if (status === 400) {
                    ElMessage.error(
                        t('Invalid input. Please check your data.')
                    );
                } else {
                    ElMessage.error(
                        t('Registration failed. Please try again.')
                    );
                }
            }
        }

        function toggleShowPassword() {
            showPassword.value = !showPassword.value;
        }

        function toggleShowPasswordConfirm() {
            showPasswordConfirm.value = !showPasswordConfirm.value;
        }

        return {
            t,
            emailLogin,
            passwordLogin,
            passwordConfirm,
            emptyFields,
            isLoginView,
            showPassword,
            showPasswordConfirm,
            toggleView,
            doLogin,
            doRegister,
            toggleShowPassword,
            toggleShowPasswordConfirm,
        };
    },
};
