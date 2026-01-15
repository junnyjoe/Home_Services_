/**
 * Home Services - API Client
 * Client JavaScript pour interagir avec l'API REST
 */

const API_BASE_URL = '/api';
const TOKEN_KEY = 'hs_access_token';
const USER_KEY = 'hs_user';

// ============================================
// Token Management
// ============================================

const TokenManager = {
    getToken() {
        return localStorage.getItem(TOKEN_KEY);
    },

    setToken(token) {
        localStorage.setItem(TOKEN_KEY, token);
    },

    removeToken() {
        localStorage.removeItem(TOKEN_KEY);
    },

    getUser() {
        const user = localStorage.getItem(USER_KEY);
        return user ? JSON.parse(user) : null;
    },

    setUser(user) {
        localStorage.setItem(USER_KEY, JSON.stringify(user));
    },

    removeUser() {
        localStorage.removeItem(USER_KEY);
    },

    isAuthenticated() {
        return !!this.getToken();
    },

    clear() {
        this.removeToken();
        this.removeUser();
    }
};

// ============================================
// API Client
// ============================================

const ApiClient = {
    async request(endpoint, options = {}) {
        const url = `${API_BASE_URL}${endpoint}`;
        const token = TokenManager.getToken();

        const config = {
            headers: {
                'Content-Type': 'application/json',
                ...(token && { 'Authorization': `Bearer ${token}` }),
                ...options.headers
            },
            ...options
        };

        if (options.body && typeof options.body === 'object' && !(options.body instanceof FormData)) {
            config.body = JSON.stringify(options.body);
        } else if (options.body instanceof FormData) {
            config.body = options.body;
            // Remove application/json if it was set by default
            if (config.headers['Content-Type'] === 'application/json') {
                delete config.headers['Content-Type'];
            }
        }

        try {
            const response = await fetch(url, config);
            const data = await response.json();

            if (!response.ok) {
                throw new ApiError(data.message || 'Une erreur est survenue', response.status, data);
            }

            return data;
        } catch (error) {
            if (error instanceof ApiError) {
                throw error;
            }
            throw new ApiError('Erreur de connexion au serveur', 0, null);
        }
    },

    get(endpoint) {
        return this.request(endpoint, { method: 'GET' });
    },

    post(endpoint, body) {
        return this.request(endpoint, { method: 'POST', body });
    },

    put(endpoint, body) {
        return this.request(endpoint, { method: 'PUT', body });
    },

    delete(endpoint) {
        return this.request(endpoint, { method: 'DELETE' });
    }
};

// Custom Error Class
class ApiError extends Error {
    constructor(message, status, data) {
        super(message);
        this.name = 'ApiError';
        this.status = status;
        this.data = data;
    }
}

// ============================================
// Auth Service
// ============================================

const AuthService = {
    async login(email, password) {
        const response = await ApiClient.post('/auth/login', { email, password });

        if (response.success && response.data) {
            TokenManager.setToken(response.data.accessToken);
            TokenManager.setUser({
                id: response.data.userId,
                nom: response.data.nom,
                email: response.data.email,
                role: response.data.role,
                verified: response.data.verified
            });
        }

        return response;
    },

    async register(userData) {
        const response = await ApiClient.post('/auth/register', userData);

        if (response.success && response.data) {
            TokenManager.setToken(response.data.accessToken);
            TokenManager.setUser({
                id: response.data.userId,
                nom: response.data.nom,
                email: response.data.email,
                role: response.data.role,
                verified: response.data.verified
            });
        }

        return response;
    },

    async getCurrentUser() {
        return await ApiClient.get('/auth/me');
    },

    logout() {
        TokenManager.clear();
        window.location.href = '/pages/auth/login.html';
    },

    isAuthenticated() {
        return TokenManager.isAuthenticated();
    },

    getUser() {
        return TokenManager.getUser();
    },

    redirectBasedOnRole() {
        const user = this.getUser();
        if (!user) {
            window.location.href = '/pages/auth/login.html';
            return;
        }

        switch (user.role) {
            case 'CLIENT':
                window.location.href = '/pages/client/dashboard.html';
                break;
            case 'PRESTATAIRE':
                window.location.href = '/pages/provider/dashboard.html';
                break;
            case 'ADMIN':
                window.location.href = '/pages/admin/dashboard.html';
                break;
            default:
                window.location.href = '/';
        }
    }
};

// ============================================
// UI Helpers
// ============================================

const UI = {
    showAlert(container, message, type = 'error') {
        const alertHtml = `
            <div class="alert alert-${type}" role="alert">
                <span>${type === 'error' ? '❌' : type === 'success' ? '✅' : '⚠️'}</span>
                <span>${message}</span>
            </div>
        `;

        const alertContainer = document.querySelector(container);
        if (alertContainer) {
            alertContainer.innerHTML = alertHtml;
            alertContainer.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
        }
    },

    clearAlerts(container) {
        const alertContainer = document.querySelector(container);
        if (alertContainer) {
            alertContainer.innerHTML = '';
        }
    },

    setLoading(button, loading) {
        if (loading) {
            button.classList.add('btn-loading');
            button.disabled = true;
        } else {
            button.classList.remove('btn-loading');
            button.disabled = false;
        }
    },

    showFieldError(input, message) {
        input.classList.add('error');
        const errorSpan = input.parentElement.querySelector('.form-error') ||
            input.closest('.form-group').querySelector('.form-error');
        if (errorSpan) {
            errorSpan.textContent = message;
        } else {
            const span = document.createElement('span');
            span.className = 'form-error';
            span.textContent = message;
            input.parentElement.appendChild(span);
        }
    },

    clearFieldErrors(form) {
        form.querySelectorAll('.form-input.error').forEach(input => {
            input.classList.remove('error');
        });
        form.querySelectorAll('.form-error').forEach(span => {
            span.textContent = '';
        });
    },

    togglePasswordVisibility(inputId, buttonElement) {
        const input = document.getElementById(inputId);
        if (input.type === 'password') {
            input.type = 'text';
            buttonElement.textContent = '🙈';
        } else {
            input.type = 'password';
            buttonElement.textContent = '👁️';
        }
    }
};

// ============================================
// Form Validators
// ============================================

const Validators = {
    email(value) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(value);
    },

    minLength(value, length) {
        return value.length >= length;
    },

    required(value) {
        return value.trim().length > 0;
    },

    phone(value) {
        // Format ivoirien: +225 XX XX XX XX XX
        const phoneRegex = /^(\+225)?[\s]?[0-9]{2}[\s]?[0-9]{2}[\s]?[0-9]{2}[\s]?[0-9]{2}[\s]?[0-9]{2}$/;
        return !value || phoneRegex.test(value.replace(/\s/g, ''));
    }
};

// ============================================
// Categories Service
// ============================================

const CategoryService = {
    async getAll() {
        return await ApiClient.get('/categories');
    }
};

// ============================================
// Guard Authentication
// ============================================

const AuthGuard = {
    requireAuth() {
        if (!AuthService.isAuthenticated()) {
            window.location.href = '/pages/auth/login.html';
            return false;
        }
        return true;
    },

    requireRole(...roles) {
        if (!this.requireAuth()) return false;

        const user = AuthService.getUser();
        if (!roles.includes(user.role)) {
            AuthService.redirectBasedOnRole();
            return false;
        }
        return true;
    },

    redirectIfAuthenticated() {
        if (AuthService.isAuthenticated()) {
            AuthService.redirectBasedOnRole();
            return true;
        }
        return false;
    }
};

// ============================================
// Service Requests Service
// ============================================

const ServiceRequestService = {
    async getAll(filters = {}) {
        const queryParams = new URLSearchParams(filters).toString();
        const endpoint = `/service-requests${queryParams ? `?${queryParams}` : ''}`;
        return await ApiClient.get(endpoint);
    },

    async getById(id) {
        return await ApiClient.get(`/service-requests/${id}`);
    },

    async getRecent(limit = 5) {
        return await ApiClient.get(`/service-requests/recent?limit=${limit}`);
    },

    async create(requestData) {
        return await ApiClient.post('/service-requests', requestData);
    }
};

// ============================================
// Provider Service
// ============================================

const ProviderService = {
    async getMyApplications() {
        return await ApiClient.get('/providers/me/applications');
    },

    async apply(requestId, applicationData) {
        return await ApiClient.post(`/service-requests/${requestId}/apply`, applicationData);
    },

    async updateProfile(profileData) {
        return await ApiClient.put('/providers/me/profile', profileData);
    },

    async uploadDocument(type, file) {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('type', type);

        return await ApiClient.request('/documents/upload', {
            method: 'POST',
            body: formData
        });
    }
};

// ============================================
// Stats Service
// ============================================

const StatsService = {
    async getProviderStats() {
        return await ApiClient.get('/stats/provider');
    },

    async getClientStats() {
        return await ApiClient.get('/stats/client');
    }
};

// ============================================
// Message Service
// ============================================

const MessageService = {
    async getUnreadCount() {
        return await ApiClient.get('/messages/unread/count');
    }
};

// Export for use in other files
window.TokenManager = TokenManager;
window.ApiClient = ApiClient;
window.AuthService = AuthService;
window.CategoryService = CategoryService;
window.ServiceRequestService = ServiceRequestService;
window.ProviderService = ProviderService;
window.StatsService = StatsService;
window.MessageService = MessageService;
window.UI = UI;
window.Validators = Validators;
window.AuthGuard = AuthGuard;
