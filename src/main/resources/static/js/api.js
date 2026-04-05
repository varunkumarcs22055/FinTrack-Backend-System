const API_URL = '/api';

function getAuthHeaders() {
    const token = localStorage.getItem('token');
    return token ? { 
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json' 
    } : { 
        'Content-Type': 'application/json' 
    };
}

async function apiFetch(endpoint, options = {}) {
    const response = await fetch(`${API_URL}${endpoint}`, {
        ...options,
        headers: {
            ...getAuthHeaders(),
            ...options.headers
        }
    });
    
    if (response.status === 401 && endpoint !== '/auth/login') {
        localStorage.clear();
        window.location.href = '/index.html';
        throw new Error('Unauthorized - please log in again');
    }
    
    if (response.status === 403) {
        throw new Error('Access denied - you do not have permission for this action');
    }

    if (!response.ok) {
        const errData = await response.json().catch(() => ({}));
        throw new Error(errData.message || 'API request failed');
    }
    
    // 204 No Content has no body
    if (response.status === 204) return null;
    
    return response.json();
}

function getUser() {
    return {
        username: localStorage.getItem('username'),
        role: localStorage.getItem('role')
    };
}

function logout() {
    localStorage.clear();
    window.location.href = '/index.html';
}

function checkAuth(allowedRoles = []) {
    const user = getUser();
    if (!user.username) {
        window.location.href = '/index.html';
        return false;
    }
    
    if (allowedRoles.length > 0 && !allowedRoles.includes(user.role)) {
        alert('You do not have permission to view this page.');
        window.location.href = '/dashboard.html';
        return false;
    }
    
    return true;
}

// UI Helpers
function setupNav() {
    const user = getUser();
    if (!user.username) return;
    
    document.getElementById('nav-username').textContent = `${user.username} (${user.role})`;
    document.getElementById('logout-btn').addEventListener('click', (e) => {
        e.preventDefault();
        logout();
    });
    
    // Hide nav links based on role
    if (user.role === 'VIEWER') {
        const recordsLink = document.getElementById('nav-records');
        if (recordsLink) recordsLink.style.display = 'none';
        
        const usersLink = document.getElementById('nav-users');
        if (usersLink) usersLink.style.display = 'none';
    } else if (user.role === 'ANALYST') {
        const usersLink = document.getElementById('nav-users');
        if (usersLink) usersLink.style.display = 'none';
    }
}
