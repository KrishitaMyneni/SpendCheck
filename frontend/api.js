// SpendCheck API & Authentication Helper

const TOKEN_KEY = "spendcheck_token";
const USER_KEY = "spendcheck_user";

function getToken() {
    return localStorage.getItem(TOKEN_KEY);
}

function getUser() {
    try {
        const u = localStorage.getItem(USER_KEY);
        return u ? JSON.parse(u) : null;
    } catch (e) {
        return null;
    }
}

function setAuth(token, user) {
    if (token) {
        localStorage.setItem(TOKEN_KEY, token);
    }
    if (user) {
        localStorage.setItem(USER_KEY, JSON.stringify(user));
    }
}

function clearAuth() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
}

function isAuthenticated() {
    return !!getToken();
}

function requireAuth() {
    if (!isAuthenticated()) {
        const currentPath = encodeURIComponent(window.location.pathname + window.location.search);
        window.location.href = `login.html?redirect=${currentPath}`;
        return false;
    }
    return true;
}

async function authFetch(url, options = {}) {
    const fullUrl = url.startsWith("http://") || url.startsWith("https://")
        ? url
        : `${API_BASE_URL}${url.startsWith("/") ? "" : "/"}${url}`;

    const headers = options.headers ? { ...options.headers } : {};
    const token = getToken();

    if (token) {
        headers["Authorization"] = `Bearer ${token}`;
    }

    if (!headers["Content-Type"] && !(options.body instanceof FormData)) {
        headers["Content-Type"] = "application/json";
    }

    const config = {
        ...options,
        headers
    };

    let response;
    try {
        response = await fetch(fullUrl, config);
    } catch (netErr) {
        throw new Error("Unable to reach SpendCheck server. Please check your network or try again in a few moments.");
    }

    // 401 Unauthorized: token expired or missing -> redirect to login
    if (response.status === 401) {
        clearAuth();
        const currentPath = encodeURIComponent(window.location.pathname + window.location.search);
        window.location.href = `login.html?redirect=${currentPath}`;
        throw new Error("Session expired. Please log in again.");
    }

    let data = null;
    const contentType = response.headers.get("content-type");
    if (contentType && contentType.includes("application/json")) {
        try {
            data = await response.json();
        } catch (e) {
            data = null;
        }
    }

    if (!response.ok) {
        const message = (data && (data.message || data.error)) || `Request failed with status ${response.status}`;
        const error = new Error(message);
        error.status = response.status;
        error.data = data;
        throw error;
    }

    return data;
}

// API methods
const SpendCheckAPI = {
    async login(email, password) {
        const res = await authFetch("/api/v1/auth/login", {
            method: "POST",
            body: JSON.stringify({ email, password })
        });
        if (res && res.data) {
            setAuth(res.data.token, {
                id: res.data.userId,
                name: res.data.name,
                email: res.data.email
            });
        }
        return res;
    },

    async signup(name, email, password) {
        const res = await authFetch("/api/v1/auth/signup", {
            method: "POST",
            body: JSON.stringify({ name, email, password })
        });
        if (res && res.data) {
            setAuth(res.data.token, {
                id: res.data.userId,
                name: res.data.name,
                email: res.data.email
            });
        }
        return res;
    },

    logout() {
        clearAuth();
        window.location.href = "login.html";
    },

    async getExpenses(filters = {}) {
        const params = new URLSearchParams();
        if (filters.category) params.append("category", filters.category);
        if (filters.startDate) params.append("startDate", filters.startDate);
        if (filters.endDate) params.append("endDate", filters.endDate);
        const query = params.toString() ? `?${params.toString()}` : "";
        return authFetch(`/api/v1/expenses${query}`, { method: "GET" });
    },

    async createExpense(expenseData) {
        return authFetch("/api/v1/expenses", {
            method: "POST",
            body: JSON.stringify(expenseData)
        });
    },

    async updateExpense(id, expenseData) {
        return authFetch(`/api/v1/expenses/${id}`, {
            method: "PUT",
            body: JSON.stringify(expenseData)
        });
    },

    async deleteExpense(id) {
        return authFetch(`/api/v1/expenses/${id}`, {
            method: "DELETE"
        });
    },

    async getCategorySummary() {
        return authFetch("/api/v1/expenses/summary/categories", { method: "GET" });
    },

    async getMonthlySummary(year, month) {
        const params = new URLSearchParams();
        if (year) params.append("year", year);
        if (month) params.append("month", month);
        const query = params.toString() ? `?${params.toString()}` : "";
        return authFetch(`/api/v1/expenses/summary/monthly${query}`, { method: "GET" });
    },

    async convertCurrency(from, to, amount) {
        const params = new URLSearchParams({
            from: from.trim().toUpperCase(),
            to: to.trim().toUpperCase(),
            amount: amount
        });
        return authFetch(`/api/v1/currency/convert?${params.toString()}`, { method: "GET" });
    }
};

